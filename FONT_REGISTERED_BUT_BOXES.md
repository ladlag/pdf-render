# 问题诊断：看到字体注册日志但中文仍显示为方框 / Font Registered But Chinese Still Shows as Boxes

## 问题描述 / Problem

用户报告：
- ✅ **看到了字体注册日志**：
  ```
  Font registered with Flying Saucer : C:\Users\xxx\TEMP\PDF-RENDER-FONT-XXX.TTF
  ```
- ❌ **但PDF中文仍然显示为方框（□）**

## 根本原因 / Root Cause

字体已经注册，但**CSS中的font-family名称与注册时使用的名称不匹配**！

Flying Saucer在渲染时，会尝试找到CSS指定的字体。如果CSS中的font-family与注册字体时使用的名称不匹配，就会回退到默认字体（不包含中文字符），导致中文显示为方框。

---

## 诊断步骤 / Diagnostic Steps

### 步骤1：检查是否设置了 defaultFontFamily

**问题：** 只设置了 regularFontPath，但忘记设置 defaultFontFamily

```java
// ❌ 错误 - 只设置了路径，没有设置字体族
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
// 忘记设置这一行！
// fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, sans-serif");

service.getHtmlRenderer().setFontConfig(fontConfig);
```

**结果：** 字体会被注册为默认别名 "PDFFont"，但你可能期望使用字体的实际名称。

**解决方法：**

```java
// ✅ 正确 - 设置字体族以使用字体的实际名称
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, sans-serif"); // 推荐设置！

service.getHtmlRenderer().setFontConfig(fontConfig);
```

**或者使用默认别名（也能正常工作）：**

```java
// ✅ 也正确 - 不设置 defaultFontFamily 会使用默认别名 "PDFFont"
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
// 不设置 defaultFontFamily - 将使用 "PDFFont" 别名

service.getHtmlRenderer().setFontConfig(fontConfig);
// 字体会被注册为 "PDFFont"，CSS也会自动使用 "PDFFont"
```

---

### 步骤2：理解字体名称的工作原理

**重要概念：** 系统使用你配置的 defaultFontFamily 的**第一个字体名称**来注册所有字体。

**工作原理：**

1. 如果你设置了 `defaultFontFamily = "HarmonyOS Sans SC, sans-serif"`
   - 系统提取第一个名称："HarmonyOS Sans SC"
   - 所有字体（regular, bold, CJK）都注册为 "HarmonyOS Sans SC"
   - CSS使用你完整的 defaultFontFamily: "HarmonyOS Sans SC, sans-serif"

2. 如果你**没有**设置 `defaultFontFamily`
   - 系统使用默认别名："PDFFont"
   - 所有字体注册为 "PDFFont"
   - CSS使用 "PDFFont, sans-serif"

**关键点：** 字体注册名称**不需要**匹配字体文件的内部名称！系统会**覆盖**内部名称。

---

### 步骤3：推荐的字体配置

**选项A：使用字体的内部名称（推荐用于单一字体）**

```java
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, sans-serif");
service.getHtmlRenderer().setFontConfig(fontConfig);
```

**好处：**
- PDF属性中显示实际字体名称
- 更容易理解和调试
- 符合大多数用户的期望

**选项B：使用默认别名（向后兼容）**

```java
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
// 不设置 defaultFontFamily
service.getHtmlRenderer().setFontConfig(fontConfig);
```

**好处：**
- 简单配置
- 避免字体名称中的空格问题
- 与旧版本完全兼容

---

### 步骤4：Spring Boot配置

**推荐配置（使用字体实际名称）：**

```yaml
# ✅ 推荐 - 使用字体的实际名称
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    default-family: HarmonyOS Sans SC, sans-serif
```

**简化配置（使用默认别名）：**

```yaml
# ✅ 也可以 - 使用默认别名 "PDFFont"
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    # 不配置 default-family，将使用 "PDFFont"
```

---

## 常见字体的内部名称参考 / Common Font Internal Names Reference

虽然系统会覆盖字体内部名称，但如果你想使用字体的实际名称，这里是常用字体的内部名称：

| 字体文件 | 内部名称 |
|---------|---------|
| HarmonyOS_Sans_SC_Regular.ttf | `HarmonyOS Sans SC` |
| NotoSansCJKsc-Regular.otf | `Noto Sans CJK SC` |
| SourceHanSansSC-Regular.otf | `Source Han Sans SC` |
| SimSun.ttf | `SimSun` |
| Microsoft YaHei.ttf | `Microsoft YaHei` |

---

## 验证配置 / Verify Configuration

运行最小化测试验证配置：

```bash
mvn compile exec:java -Dexec.mainClass="com.mercury.pdf.render.MinimalFontTest"
```

应该看到：
- ✅ PDF文件大小 > 50KB（字体已嵌入）
- ✅ 日志显示字体注册成功
- ✅ 打开PDF可以看到中文字符

---

## 完整示例代码 / Complete Example Code

```java
package com.example;

import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.model.ReportData;
import com.mercury.pdf.render.model.Section;

import java.nio.file.Files;
import java.nio.file.Paths;

public class ChineseFontExample {
    public static void main(String[] args) throws Exception {
        // 创建服务
        ReportService service = new ReportService();
        
        // 配置字体
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, sans-serif");
        
        service.getHtmlRenderer().setFontConfig(fontConfig);
        
        // 创建测试数据
        ReportData data = new ReportData();
        data.setTitle("中文测试报告");
        
        Section section = new Section("测试章节");
        section.addParagraph("这是中文测试文本。如果能看到这些汉字，说明配置成功！");
        data.getSections().add(section);
        
        // 生成PDF
        byte[] pdf = service.generatePdf(data, "flexible");
        
        // 保存文件
        Files.createDirectories(Paths.get("output"));
        Files.write(Paths.get("output/chinese_test.pdf"), pdf);
        
        System.out.println("PDF生成成功！文件大小: " + (pdf.length / 1024) + " KB");
        System.out.println("如果文件 > 50KB，说明字体已正确嵌入。");
    }
}
```

---

## 故障排除 / Troubleshooting

如果中文仍然显示为方框：

1. **检查字体文件是否存在**
   - 确认 `src/main/resources/fonts/` 目录下有字体文件

2. **检查PDF文件大小**
   - 如果 < 10KB，字体未嵌入
   - 如果 > 50KB，字体已嵌入，可能是其他问题

3. **检查日志**
   - 应该看到 "Font registered with Flying Saucer" 日志
   - 应该看到 "Font family name for CSS" 日志

4. **尝试不设置 defaultFontFamily**
   - 这会使用默认的 "PDFFont" 别名
   - 如果这样能工作，说明是字体名称配置问题

5. **查看其他诊断文档**
   - `NO_FONT_LOGS_TROUBLESHOOTING.md` - 如果看不到字体注册日志
   - `CHINESE_QUICKSTART.md` - 快速开始指南

---

## 总结 / Summary

**关键要点：**

1. ✅ 设置 `defaultFontFamily` 是**推荐的**，但不是必须的
2. ✅ 系统会使用你配置的第一个字体名称来注册所有字体
3. ✅ 不设置 `defaultFontFamily` 会使用默认别名 "PDFFont"（也能正常工作）
4. ✅ 字体注册名称**不需要**匹配字体文件的内部名称
5. ✅ 所有方法都能正确显示中文，选择你喜欢的即可

**最简单的配置：**

```java
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, sans-serif");
service.getHtmlRenderer().setFontConfig(fontConfig);
```

这样配置后，中文应该能正常显示！
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    # 缺少 default-family!
```

**正确配置：**

```yaml
# ✅ 正确 - 必须同时配置路径和字体族
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    default-family: HarmonyOS Sans SC, DejaVu Sans, sans-serif  # 必须配置！
```

---

## 完整的测试代码 / Complete Test Code

创建这个测试文件来验证配置：

```java
package com.example;

import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.model.ReportData;
import com.mercury.pdf.render.model.Section;

import java.nio.file.Files;
import java.nio.file.Paths;

public class FontFamilyTest {
    public static void main(String[] args) {
        try {
            System.out.println("========================================");
            System.out.println("字体族名称验证测试");
            System.out.println("Font Family Name Verification Test");
            System.out.println("========================================\n");
            
            ReportService service = new ReportService();
            
            FontConfig fontConfig = new FontConfig();
            
            // 设置字体路径
            String fontPath = "classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf";
            fontConfig.setRegularFontPath(fontPath);
            System.out.println("✓ 字体路径 Font path: " + fontPath);
            
            // 设置字体族 - 必须与字体内部名称完全一致！
            String fontFamily = "HarmonyOS Sans SC, DejaVu Sans, sans-serif";
            fontConfig.setDefaultFontFamily(fontFamily);
            System.out.println("✓ 字体族 Font family: " + fontFamily);
            
            service.getHtmlRenderer().setFontConfig(fontConfig);
            
            // 验证配置
            FontConfig retrieved = service.getHtmlRenderer().getFontConfig();
            System.out.println("\n配置验证 Configuration Verification:");
            System.out.println("  regularFontPath: " + retrieved.getRegularFontPath());
            System.out.println("  defaultFontFamily: " + retrieved.getDefaultFontFamily());
            
            if (retrieved.getDefaultFontFamily() == null || 
                retrieved.getDefaultFontFamily().isEmpty()) {
                System.err.println("\n✗ 错误：defaultFontFamily 未设置！");
                System.err.println("✗ ERROR: defaultFontFamily not set!");
                System.err.println("这就是中文显示为方框的原因！");
                System.err.println("This is why Chinese shows as boxes!");
                return;
            }
            
            System.out.println("\n========================================");
            System.out.println("生成PDF...");
            System.out.println("Generating PDF...");
            System.out.println("========================================\n");
            
            // 创建测试数据
            ReportData data = new ReportData();
            data.setTitle("字体族测试 Font Family Test");
            data.setSubtitle("验证font-family配置 Verify font-family configuration");
            
            Section section = new Section("测试章节");
            section.addParagraph("如果能看到这段中文，说明字体族配置正确。");
            section.addParagraph("If you can see this Chinese text, font-family is configured correctly.");
            section.addParagraph("测试常用汉字：的一是在不了有和人这中大为上个国我以要他时来用们生到作地");
            
            data.getSections().add(section);
            
            // 生成PDF
            byte[] pdf = service.generatePdf(data, "flexible");
            
            System.out.println("\n========================================");
            System.out.println("PDF生成完成！");
            System.out.println("PDF Generated!");
            System.out.println("========================================\n");
            
            // 保存文件
            Files.createDirectories(Paths.get("test-output"));
            String outputPath = "test-output/font_family_test.pdf";
            Files.write(Paths.get(outputPath), pdf);
            
            System.out.println("文件信息 File Info:");
            System.out.println("  路径 Path: " + outputPath);
            System.out.println("  大小 Size: " + (pdf.length / 1024) + " KB");
            
            if (pdf.length > 50000) {
                System.out.println("\n✓✓✓ 成功！字体已嵌入，中文应该能正常显示");
                System.out.println("✓✓✓ SUCCESS! Font embedded, Chinese should display correctly");
            } else {
                System.err.println("\n✗✗✗ 警告！文件过小，字体可能未嵌入");
                System.err.println("✗✗✗ WARNING! File too small, font may not be embedded");
            }
            
            System.out.println("\n请打开PDF文件验证：");
            System.out.println("Please open PDF to verify:");
            System.out.println("  " + outputPath);
            
        } catch (Exception e) {
            System.err.println("\n错误 Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

---

## 诊断工具输出分析 / Diagnostic Tool Output Analysis

### 正常输出（配置正确）

```
✓ Font registered with Flying Saucer: C:\Users\xxx\TEMP\PDF-RENDER-FONT-XXX.TTF
  Encoding: Identity-H | Embedded: true
  Font family name (for CSS): HarmonyOS Sans SC
✓ Total fonts registered for PDF: 1
```

**关键信息：**
- `Font family name (for CSS): HarmonyOS Sans SC` - 这是字体的内部名称
- 你的 `defaultFontFamily` **必须包含** 这个名称

### 配置对比

**错误配置：**
```java
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
// defaultFontFamily 未设置或不匹配
fontConfig.setDefaultFontFamily("Arial, sans-serif");  // 不包含 "HarmonyOS Sans SC"
```

**正确配置：**
```java
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, sans-serif");
// 包含了日志中显示的 "HarmonyOS Sans SC"
```

---

## 快速修复 / Quick Fix

### 纯Java项目

```java
ReportService service = new ReportService();

FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");

// ⚠️ 关键：这一行必须设置，且名称必须正确
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, sans-serif");

service.getHtmlRenderer().setFontConfig(fontConfig);

// 现在生成PDF，中文应该正常显示
byte[] pdf = service.generatePdf(data);
```

### Spring Boot项目

**application.yml:**
```yaml
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    # ⚠️ 关键：这一行必须配置，且名称必须正确
    default-family: HarmonyOS Sans SC, DejaVu Sans, sans-serif
```

---

## 验证修复是否成功 / Verify the Fix

修复后，PDF文件应该有以下特征：

1. **文件大小增加：**
   - 修复前：2-5 KB（没有字体数据）
   - 修复后：50-200 KB（包含字体数据）

2. **中文正常显示：**
   - 打开PDF，所有中文字符清晰显示
   - 不再是方框□

3. **PDF属性中能看到嵌入的字体：**
   - 文件 → 属性 → 字体
   - 应该看到 "HarmonyOS Sans SC (Embedded Subset)"

---

## 运行验证工具 / Run Verification Tool

运行这个工具获取你的字体的正确内部名称：

```bash
mvn compile exec:java -Dexec.mainClass="com.mercury.pdf.render.FontConfigDiagnostic"
```

或运行最小化测试：

```bash
mvn compile exec:java -Dexec.mainClass="com.mercury.pdf.render.MinimalFontTest"
```

---

## 总结 / Summary

**问题：** 看到字体注册日志，但中文仍显示为方框

**原因：** `defaultFontFamily` 未设置或名称不匹配

**解决方法：**

1. ✅ 运行诊断工具获取字体内部名称
2. ✅ 设置 `defaultFontFamily` 为该内部名称
3. ✅ 重新生成PDF验证

**关键代码：**
```java
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, sans-serif"); // 必须设置！
```

**Spring Boot配置：**
```yaml
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    default-family: HarmonyOS Sans SC, sans-serif  # 必须配置！
```
