# 字体嵌入改进和诊断 / Font Embedding Improvements and Diagnostics

## 问题背景 / Background

用户报告在实际运行中看到了日志：
```
Font extracted for PDF rendering: HarmonyOS_Sans_SC_Regular.ttf
```

这说明字体提取机制正在工作，但可能仍有中文字符显示为方框的问题。

## 本次改进 / Improvements Made

### 1. 使用 BaseFont 常量（更明确可靠）

**之前 (Before):**
```java
renderer.getFontResolver().addFont(fontPath, "Identity-H", true);
```

**现在 (Now):**
```java
import com.lowagie.text.pdf.BaseFont;
...
renderer.getFontResolver().addFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
```

**为什么这样更好 / Why This is Better:**
- 明确使用 OpenPDF 的官方常量
- `BaseFont.IDENTITY_H` = "Identity-H" (Unicode 编码)
- `BaseFont.EMBEDDED` = true (嵌入字体到 PDF)
- 更符合 OpenPDF/Flying Saucer 的最佳实践
- 避免字符串拼写错误

### 2. 增强日志输出（更好的诊断信息）

现在会看到完整的字体注册流程日志：

```
✓ Font extracted for PDF rendering: HarmonyOS_Sans_SC_Regular.ttf
✓ Font registered with Flying Saucer: /tmp/pdf-render-font-xxx.ttf
  Encoding: Identity-H | Embedded: true
✓ Total fonts registered for PDF: 1
  CSS font-family should match: HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif
```

**日志说明 / Log Explanation:**

| 日志 | 含义 | 验证点 |
|-----|------|--------|
| Font extracted | 字体已从 JAR 提取到临时文件 | ✓ 字体文件存在且可读 |
| Font registered | 字体已注册到 Flying Saucer | ✓ 注册成功，可用于 PDF |
| Encoding: Identity-H | 使用 Unicode 编码 | ✓ 支持 CJK 字符 |
| Embedded: true | 字体嵌入 PDF | ✓ 跨平台兼容 |
| Total fonts registered | 已注册字体总数 | ✓ 确认配置生效 |
| CSS font-family | 应匹配的 CSS 字体族 | ⚠️ 需要与字体内部名称一致 |

### 3. 诊断工具 (FontEmbeddingDiagnostic)

提供了一个独立的诊断工具，帮助用户验证字体嵌入：

```bash
java -cp "target/classes:..." com.mercury.pdf.render.FontEmbeddingDiagnostic
```

工具会：
1. 显示详细的步骤日志
2. 生成测试 PDF
3. 检查 PDF 文件大小（判断是否嵌入字体）
4. 提供验证步骤指导

## 如何验证字体是否真的嵌入 / How to Verify Font Embedding

### 方法 1: 检查日志输出

**正常情况 (Normal):**
```
✓ Font extracted for PDF rendering: HarmonyOS_Sans_SC_Regular.ttf
✓ Font registered with Flying Saucer: /tmp/pdf-render-font-xxx.ttf
✓ Total fonts registered for PDF: 1
```

**异常情况 (Error):**
```
✗ Warning: Failed to register custom fonts: Font not found in classpath: /fonts/xxx.ttf
```

### 方法 2: 检查 PDF 文件大小

| 场景 | 文件大小 | 说明 |
|-----|---------|------|
| 无字体 | < 10 KB | 字体未嵌入，中文显示方框 |
| 有字体 | > 40 KB | 字体已嵌入，中文正常显示 |

### 方法 3: 使用 PDF 阅读器查看属性

1. 打开 PDF 文件
2. 查看 **文件 → 属性 → 字体** (File → Properties → Fonts)
3. 应该看到：

**正确 (Correct):**
```
HarmonyOS Sans SC (Embedded Subset)
或
Noto Sans CJK SC (Embedded Subset)
```

**错误 (Wrong):**
```
Helvetica
Times-Roman
DejaVuSans
```

### 方法 4: 直接打开 PDF 查看

最简单直接的方法：
- ✓ 看到清晰的中文字符 → 成功
- ✗ 看到方框（□□□） → 失败

## 常见问题排查 / Troubleshooting

### 问题 1: 看到 "Font extracted" 但中文还是方框

**可能原因:**
1. CSS font-family 与字体内部名称不匹配
2. 模板中没有正确使用 font-family
3. 字体注册失败（检查是否有错误日志）

**解决方案:**
```yaml
# 确保配置正确
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    default-family: HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif
    #                ^^^^^^^^^^^^^^^^ 必须与字体内部名称匹配！
```

运行诊断工具查看警告：
```bash
java -cp ... com.mercury.pdf.render.FontEmbeddingDiagnostic
```

### 问题 2: 看到 "Font registered" 但 PDF 很小 (< 10KB)

**可能原因:**
字体注册了但 PDF 生成时没有使用（CSS 问题）

**解决方案:**
检查模板是否使用了正确的 font-family：
```html
<style>
body {
    font-family: [(${fontFamily})]; /* 应该是 HarmonyOS Sans SC, ... */
}
</style>
```

### 问题 3: 没有看到任何字体相关日志

**可能原因:**
1. 没有配置字体属性
2. 使用了手动创建的 ReportService 而不是 Spring 注入的

**解决方案:**
```java
// ❌ 错误：手动创建，跳过配置
ReportService service = new ReportService();

// ✅ 正确：使用 Spring 注入
@Service
public class PdfService {
    private final ReportService reportService;
    
    public PdfService(ReportService reportService) {
        this.reportService = reportService; // 已配置好字体
    }
}
```

## 测试验证 / Test Verification

运行字体嵌入测试：

```bash
mvn test -Dtest=FontEmbeddingVerificationTest
```

应该看到：
```
✓ Font registered with Flying Saucer
✓ Total fonts registered for PDF: 1
✓ PDF size verification passed: 43000+ bytes
```

## 技术细节 / Technical Details

### Flying Saucer + OpenPDF 字体嵌入流程

1. **提取字体** (Extract Font)
   ```
   classpath:/fonts/xxx.ttf → /tmp/pdf-render-font-xxx.ttf
   ```

2. **注册字体** (Register Font)
   ```java
   renderer.getFontResolver().addFont(
       tempFilePath,           // 临时文件路径
       BaseFont.IDENTITY_H,    // Unicode 编码
       BaseFont.EMBEDDED       // 嵌入到 PDF
   );
   ```

3. **CSS 匹配** (CSS Matching)
   ```html
   <style>
   body { font-family: "HarmonyOS Sans SC", sans-serif; }
   </style>
   ```

4. **PDF 渲染** (PDF Rendering)
   - Flying Saucer 解析 HTML/CSS
   - 匹配 font-family 到已注册的字体
   - 使用 OpenPDF 将字体嵌入 PDF

### 为什么必须用临时文件

Flying Saucer/OpenPDF 需要真实文件路径，不能直接使用：
- ❌ `jar:file:/app.jar!/fonts/xxx.ttf` (JAR URL 不支持)
- ❌ `InputStream` (需要文件路径)
- ✓ `/tmp/pdf-render-font-xxx.ttf` (临时文件，支持)

### 字体缓存机制

```java
private final Map<String, String> fontPathCache = new ConcurrentHashMap<>();
```

- 同一字体只提取一次
- 避免重复 I/O 操作
- 提高性能

## 总结 / Summary

### 改进前 (Before)
```java
renderer.getFontResolver().addFont(fontPath, "Identity-H", true);
// 没有详细日志，难以诊断问题
```

### 改进后 (After)
```java
renderer.getFontResolver().addFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
// ✓ 使用官方常量
// ✓ 详细日志输出
// ✓ 诊断工具支持
```

### 用户体验提升

1. **更清晰的日志**: 可以准确知道字体是否成功注册
2. **更好的诊断**: 提供诊断工具快速定位问题  
3. **更可靠的实现**: 使用 OpenPDF 官方推荐方式

### 验证清单 / Verification Checklist

- [x] 看到 "Font extracted" 日志
- [x] 看到 "Font registered" 日志
- [x] 看到 "Total fonts registered: 1" 日志
- [x] PDF 文件 > 40KB
- [x] 打开 PDF 看到中文字符（不是方框）
- [x] PDF 属性显示嵌入字体

如果以上全部 ✓，说明字体嵌入成功！
