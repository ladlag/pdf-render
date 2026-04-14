# Windows兼容性说明 / Windows Compatibility Guide

## 问题描述 / Problem Description

**中文：** 使用Spring Boot注解配置在Mac/Linux上正常工作，但在Windows上可能出现字体加载问题。

**English:** Spring Boot annotation-based configuration works on Mac/Linux but may have font loading issues on Windows.

## 根本原因 / Root Cause

Windows和Unix系统使用不同的路径分隔符：
- **Windows**: 使用反斜杠 `\` (例如: `C:\Users\...\temp\font.ttf`)
- **Mac/Linux**: 使用正斜杠 `/` (例如: `/tmp/font.ttf`)

PDF渲染库（Flying Saucer/OpenPDF）期望使用正斜杠路径或正确转义的Windows路径。

Windows and Unix systems use different path separators:
- **Windows**: Uses backslashes `\` (e.g., `C:\Users\...\temp\font.ttf`)
- **Mac/Linux**: Uses forward slashes `/` (e.g., `/tmp/font.ttf`)

PDF rendering libraries (Flying Saucer/OpenPDF) expect forward-slash paths or properly escaped Windows paths.

## 解决方案 / Solution

**版本 1.0.1+** 已自动处理路径标准化：

Version 1.0.1+ automatically handles path normalization:

```java
// 在 HtmlReportRenderer.resolveFontPath() 中
// In HtmlReportRenderer.resolveFontPath()

// 获取绝对路径并标准化为跨平台兼容
// Get absolute path and normalize for cross-platform compatibility
String resolvedPath = tempFile.getAbsolutePath().replace('\\', '/');
```

这确保了无论在什么操作系统上提取字体，路径都会被转换为正斜杠格式，与PDF库兼容。

This ensures that regardless of which OS the font is extracted on, the path is converted to forward-slash format compatible with PDF libraries.

## 配置方式 / Configuration

### Spring Boot (application.yml)

```yaml
# 这种配置在所有平台上都能正常工作
# This configuration works on all platforms

pdf-render:
  fonts:
    # 使用 classpath: 前缀 - 在所有平台上都能工作
    # Use classpath: prefix - works on all platforms
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    default-family: HarmonyOS Sans SC, sans-serif
    
  debug:
    enabled: true
    # 调试目录路径 - 自动处理路径分隔符
    # Debug directory path - path separators handled automatically
    output-directory: test-output/debug-html
```

**重要提示 Important Notes:**

1. ✅ **使用 `classpath:` 前缀** - 这在所有平台上都能正常工作
   Use `classpath:` prefix - works on all platforms

2. ✅ **路径中使用正斜杠 `/`** - 即使在Windows上也使用正斜杠
   Use forward slashes `/` in paths - even on Windows

3. ❌ **不要使用 Windows 风格的路径** 如 `C:\fonts\font.ttf`
   Don't use Windows-style paths like `C:\fonts\font.ttf`

### 纯Java配置 / Pure Java Configuration

```java
// 正确的配置方式 - 在所有平台上都能工作
// Correct configuration - works on all platforms

PdfRenderService service = new PdfRenderService();

FontConfig fontConfig = new FontConfig();
// ✅ 使用 classpath: 和正斜杠
// ✅ Use classpath: and forward slashes
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, sans-serif");

service.getHtmlRenderer().setFontConfig(fontConfig);
```

## 验证 / Verification

### Windows用户验证步骤 / Verification Steps for Windows Users

1. **运行测试 Run Tests:**
   ```cmd
   mvn test -Dtest=WindowsPathCompatibilityTest
   ```

2. **检查日志输出 Check Log Output:**
   ```
   ✓ Font registered with Flying Saucer: C:/Users/.../TEMP/pdf-render-font-xxx.ttf
   ```
   注意路径使用正斜杠，即使在Windows上。
   Note the path uses forward slashes, even on Windows.

3. **生成测试PDF Generate Test PDF:**
   ```java
   // 在Windows上测试
   // Test on Windows
   PdfRenderService service = new PdfRenderService();
   // ... 配置字体 configure fonts ...
   byte[] pdf = service.generatePdf(data);
   
   // PDF应该包含正确的中文字符，不是方框
   // PDF should contain proper Chinese characters, not boxes
   ```

## 常见问题 / Common Issues

### 问题1：路径中的反斜杠
**症状 Symptom:** 字体加载失败，中文显示为方框
**原因 Cause:** 直接使用Windows文件路径
**解决 Solution:** 使用 `classpath:` 前缀

```java
// ❌ 错误 Wrong
fontConfig.setRegularFontPath("C:\\fonts\\font.ttf");

// ✅ 正确 Correct  
fontConfig.setRegularFontPath("classpath:/fonts/font.ttf");
```

### 问题2：调试HTML目录路径
**症状 Symptom:** 调试HTML无法保存
**原因 Cause:** Windows路径分隔符问题
**解决 Solution:** 使用相对路径或正斜杠

```yaml
# ✅ 正确 Correct - 使用正斜杠或相对路径
# Use forward slashes or relative paths
pdf-render:
  debug:
    output-directory: test-output/debug-html
    # 或 or: debug-html
```

### 问题3：临时文件提取
**症状 Symptom:** 字体注册但仍显示方框
**原因 Cause:** 旧版本未标准化路径
**解决 Solution:** 升级到 v1.0.1+

```
版本 1.0.0: ❌ C:\Users\...\temp\font.ttf (可能失败 may fail)
版本 1.0.1+: ✅ C:/Users/.../temp/font.ttf (自动标准化 auto-normalized)
```

## 平台测试状态 / Platform Test Status

| 平台 Platform | 状态 Status | 注释 Notes |
|--------------|------------|-----------|
| **Windows 10/11** | ✅ 测试通过 Tested | 路径自动标准化 Paths auto-normalized |
| **macOS** | ✅ 测试通过 Tested | 原生支持正斜杠 Native forward slashes |
| **Linux** | ✅ 测试通过 Tested | 原生支持正斜杠 Native forward slashes |

## 技术细节 / Technical Details

### 路径标准化实现 / Path Normalization Implementation

```java
// 在 HtmlReportRenderer.resolveFontPath() 方法中
// In HtmlReportRenderer.resolveFontPath() method

// 提取字体到临时文件
// Extract font to temporary file
File tempFile = File.createTempFile("pdf-render-font-", extension);

// 标准化路径 - 关键步骤！
// Normalize path - critical step!
String resolvedPath = tempFile.getAbsolutePath().replace('\\', '/');

// Windows: C:/Users/username/AppData/Local/Temp/pdf-render-font-xxx.ttf
// Mac/Linux: /tmp/pdf-render-font-xxx.ttf
// 两者都使用正斜杠！Both use forward slashes!
```

### 为什么这样有效 / Why This Works

1. **Java File API**: `File.getAbsolutePath()` 返回平台特定路径
   Returns platform-specific paths

2. **字符串替换**: `.replace('\\', '/')` 将Windows反斜杠转换为正斜杠
   Converts Windows backslashes to forward slashes

3. **PDF库兼容**: Flying Saucer和OpenPDF接受正斜杠路径（即使在Windows上）
   Flying Saucer and OpenPDF accept forward-slash paths (even on Windows)

4. **跨平台**: Mac/Linux路径不受影响（没有反斜杠可替换）
   Mac/Linux paths unaffected (no backslashes to replace)

## 最佳实践 / Best Practices

### 开发建议 / Development Tips

1. **始终使用 `classpath:` 前缀**
   Always use `classpath:` prefix
   ```yaml
   regular-path: classpath:/fonts/font.ttf
   ```

2. **路径中使用正斜杠**
   Use forward slashes in paths
   ```yaml
   output-directory: target/debug-html  # ✅
   output-directory: target\debug-html  # ❌ (Windows only)
   ```

3. **测试多个平台**
   Test on multiple platforms
   - 在开发时在Mac/Linux上测试
     Test on Mac/Linux during development
   - 在部署前在Windows上测试
     Test on Windows before deployment

4. **使用相对路径**
   Use relative paths
   ```yaml
   # ✅ 推荐 Recommended
   output-directory: debug-html
   
   # ❌ 避免 Avoid
   output-directory: C:/absolute/path/debug-html
   ```

## 故障排除 / Troubleshooting

如果在Windows上仍然遇到问题：
If you still encounter issues on Windows:

1. **检查日志输出 Check Log Output:**
   ```
   ✓ Font registered with Flying Saucer: [查看路径 see path]
   ```
   路径应该使用正斜杠。
   Path should use forward slashes.

2. **运行诊断工具 Run Diagnostic Tool:**
   ```cmd
   mvn compile exec:java -Dexec.mainClass="com.mercury.pdf.render.FontFileVerifier"
   ```

3. **检查版本 Check Version:**
   确保使用 v1.0.1 或更高版本
   Ensure you're using v1.0.1 or higher

4. **查看完整文档 See Full Documentation:**
   - [CHINESE_QUICKSTART.md](CHINESE_QUICKSTART.md)
   - [CHINESE_DISPLAY_COMPLETE_SOLUTION.md](CHINESE_DISPLAY_COMPLETE_SOLUTION.md)

## 相关问题 / Related Issues

- **Issue #1**: 中文字符显示为方框 Chinese shows as boxes
  - Solution: 配置字体 Configure fonts
  
- **Issue #2**: 没有看到字体注册日志 No font registration logs
  - Solution: 调用 `setFontConfig()` Call `setFontConfig()`
  
- **Issue #3**: Windows路径兼容性 Windows path compatibility
  - Solution: **已在 v1.0.1+ 修复** Fixed in v1.0.1+

## 总结 / Summary

✅ **版本 1.0.1+ 自动处理Windows路径兼容性**
Version 1.0.1+ automatically handles Windows path compatibility

✅ **无需特殊配置 - 在所有平台上使用相同的配置**
No special configuration needed - use same config on all platforms

✅ **使用 `classpath:` 前缀和正斜杠确保最佳兼容性**
Use `classpath:` prefix and forward slashes for best compatibility
