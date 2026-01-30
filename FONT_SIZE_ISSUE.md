# 问题诊断：字体文件大小异常 / Font File Size Issue

## 问题描述 / Problem

用户报告：
- ✅ 看到字体注册日志
- ✅ 字体文件提取到临时目录：`C:\Users\xxx\TEMP\PDF-RENDER-FONT-XXX.TTF`
- ⚠️ **临时文件大小是 7.8M**
- ⚠️ HarmonyOS字体通常只有几百KB到2MB
- ❓ **是否加载了错误的字体？**

## 字体文件大小对比 / Font File Size Comparison

典型的中文字体文件大小：

| 字体 | 典型大小 | 说明 |
|------|---------|------|
| **HarmonyOS Sans SC Regular** | 2-4 MB | 华为鸿蒙字体（简体中文） |
| **HarmonyOS Sans SC (完整版)** | 7-10 MB | 包含多个字重和扩展字符集 |
| **Noto Sans CJK SC Regular** | 15-17 MB | Google思源黑体（完整版） |
| **Noto Sans SC (子集)** | 5-8 MB | 只包含简体中文的版本 |
| **Source Han Sans SC** | 15-20 MB | Adobe思源黑体 |

## 诊断：确认加载的是哪个字体 / Verify Which Font is Loaded

### 方法1：查看日志中的字体内部名称

当看到字体注册日志时，会显示字体的内部名称：

```
✓ Font registered with Flying Saucer: C:\Users\xxx\TEMP\PDF-RENDER-FONT-XXX.TTF
  Encoding: Identity-H | Embedded: true
  Font family name (for CSS): HarmonyOS Sans SC    <-- 这个是关键！
```

**如果这里显示的是 "HarmonyOS Sans SC"，那确实是HarmonyOS字体**

**如果显示的是 "Noto Sans CJK SC" 或其他，则加载了错误的字体**

### 方法2：运行字体验证工具

创建这个测试程序来验证：

```java
package com.mercury.pdf.render;

import com.mercury.pdf.render.util.FontNameExtractor;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * 字体文件验证工具 - 检查加载的是哪个字体
 */
public class FontFileVerifier {
    
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║  字体文件验证工具 Font File Verifier                        ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");
        
        checkFont("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf", "HarmonyOS");
        System.out.println();
        checkFont("classpath:/fonts/NotoSansCJKsc-Regular.otf", "Noto Sans CJK");
    }
    
    private static void checkFont(String fontPath, String fontLabel) {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("检查字体 Checking: " + fontLabel);
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        try {
            String resourcePath = fontPath.substring("classpath:".length());
            
            // 1. 检查字体是否存在
            InputStream stream = FontFileVerifier.class.getResourceAsStream(resourcePath);
            if (stream == null) {
                System.out.println("✗ 字体文件不存在 Font file not found: " + fontPath);
                return;
            }
            
            // 2. 检查文件大小
            int available = stream.available();
            double sizeMB = available / (1024.0 * 1024.0);
            System.out.println("✓ 字体文件存在 Font file exists");
            System.out.printf("  文件大小 File size: %.2f MB\n", sizeMB);
            
            // 3. 提取字体到临时文件
            File tempFile = File.createTempFile("verify-font-", ".ttf");
            tempFile.deleteOnExit();
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            try (java.io.FileOutputStream out = new java.io.FileOutputStream(tempFile)) {
                while ((bytesRead = stream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            stream.close();
            
            System.out.println("  临时文件 Temp file: " + tempFile.getAbsolutePath());
            long actualSize = tempFile.length();
            double actualSizeMB = actualSize / (1024.0 * 1024.0);
            System.out.printf("  实际大小 Actual size: %.2f MB\n", actualSizeMB);
            
            // 4. 提取字体内部名称
            String internalName = FontNameExtractor.extractFontFamilyName(tempFile.getAbsolutePath());
            System.out.println("  字体内部名称 Internal name: " + internalName);
            
            // 5. 分析
            System.out.println("\n分析 Analysis:");
            if (actualSizeMB > 10) {
                System.out.println("  ⚠️  这是一个大字体文件（>10MB）");
                System.out.println("  ⚠️  This is a large font file (>10MB)");
                if (internalName.contains("Noto") || internalName.contains("CJK")) {
                    System.out.println("  ℹ️  这是Noto Sans CJK完整版字体");
                    System.out.println("  ℹ️  This is Noto Sans CJK full version");
                }
            } else if (actualSizeMB > 5) {
                System.out.println("  ℹ️  中等大小字体（5-10MB）");
                System.out.println("  ℹ️  Medium size font (5-10MB)");
                if (internalName.contains("HarmonyOS")) {
                    System.out.println("  ℹ️  这可能是HarmonyOS完整版或多字重版本");
                    System.out.println("  ℹ️  This may be HarmonyOS full version with multiple weights");
                }
            } else if (actualSizeMB > 2) {
                System.out.println("  ✓ 标准大小的简体中文字体（2-5MB）");
                System.out.println("  ✓ Standard size Simplified Chinese font (2-5MB)");
            } else {
                System.out.println("  ⚠️  字体文件较小（<2MB），可能字符集不完整");
                System.out.println("  ⚠️  Small font file (<2MB), may have incomplete character set");
            }
            
        } catch (Exception e) {
            System.err.println("✗ 错误 Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

运行这个工具：

```bash
mvn compile exec:java -Dexec.mainClass="com.mercury.pdf.render.FontFileVerifier"
```

### 方法3：检查你的配置

**检查你的代码中配置的路径：**

```java
// 你配置的是哪个路径？
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("???");  // 检查这里配置的是什么

System.out.println("配置的字体路径: " + fontConfig.getRegularFontPath());
```

**或者检查 application.yml：**

```yaml
pdf-render:
  fonts:
    regular-path: ???  # 检查这里配置的是什么
```

## 可能的情况 / Possible Scenarios

### 情况1：HarmonyOS字体本身就是7.8M

如果项目中的 `HarmonyOS_Sans_SC_Regular.ttf` 确实是7.8M，可能是：
- 完整版HarmonyOS字体（包含多个字重）
- 包含繁体中文、日文、韩文等扩展字符集
- **这是正常的！** 只是文件比较大

**验证方法：** 检查字体内部名称是否显示 "HarmonyOS Sans SC"

### 情况2：配置错误，加载了Noto字体

如果你配置的是：
```java
fontConfig.setRegularFontPath("classpath:/fonts/NotoSansCJKsc-Regular.otf");
```

那么加载Noto字体（15-17MB）是正常的。

**解决方法：** 
- 如果想用HarmonyOS，改为：`classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf`
- 如果Noto字体工作正常，可以继续使用

### 情况3：路径配置错误，系统自动选择了备用字体

如果配置的路径不存在，系统可能自动使用了另一个字体。

**检查方法：** 看日志中的字体路径和内部名称是否匹配你的期望

## 解决方案 / Solutions

### 如果你想使用HarmonyOS字体

```java
FontConfig fontConfig = new FontConfig();
// 明确指定HarmonyOS字体
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, sans-serif");

service.getHtmlRenderer().setFontConfig(fontConfig);
```

**验证：** 生成PDF后，字体注册日志应该显示：
```
Font family name (for CSS): HarmonyOS Sans SC
```

### 如果你想使用Noto字体（更完整的字符支持）

```java
FontConfig fontConfig = new FontConfig();
// 使用Noto字体
fontConfig.setRegularFontPath("classpath:/fonts/NotoSansCJKsc-Regular.otf");
fontConfig.setDefaultFontFamily("Noto Sans CJK SC, sans-serif");

service.getHtmlRenderer().setFontConfig(fontConfig);
```

**验证：** 生成PDF后，字体注册日志应该显示：
```
Font family name (for CSS): Noto Sans CJK SC
```

### Spring Boot配置

**HarmonyOS：**
```yaml
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    default-family: HarmonyOS Sans SC, sans-serif
```

**Noto Sans CJK：**
```yaml
pdf-render:
  fonts:
    regular-path: classpath:/fonts/NotoSansCJKsc-Regular.otf
    default-family: Noto Sans CJK SC, sans-serif
```

## 确认当前使用的字体 / Confirm Current Font

添加这段调试代码到你的程序：

```java
FontConfig config = service.getHtmlRenderer().getFontConfig();
if (config != null) {
    System.out.println("======================================");
    System.out.println("当前字体配置 Current Font Configuration:");
    System.out.println("  路径 Path: " + config.getRegularFontPath());
    System.out.println("  字体族 Family: " + config.getDefaultFontFamily());
    System.out.println("======================================");
}
```

然后在生成PDF时，查看日志：

```
✓ Font registered with Flying Saucer: C:\Users\xxx\TEMP\PDF-RENDER-FONT-XXX.TTF
  Encoding: Identity-H | Embedded: true
  Font family name (for CSS): [这里会显示实际的字体名称]
```

**如果显示 "HarmonyOS Sans SC"** - 你正在使用HarmonyOS字体（即使文件是7.8M）

**如果显示 "Noto Sans CJK SC"** - 你正在使用Noto字体

## 两种字体的对比 / Font Comparison

| 特性 | HarmonyOS Sans SC | Noto Sans CJK SC |
|------|-------------------|------------------|
| 文件大小 | 2-8 MB | 15-17 MB |
| 字符支持 | 简体中文 + 常用字符 | CJK全字符集（中日韩） |
| 显示效果 | 现代、清晰 | 全面、兼容性好 |
| 推荐场景 | 简体中文报告 | 需要多语言支持 |

**两种字体都可以正常显示简体中文！** 选择哪个主要看你的需求和文件大小偏好。

## 总结 / Summary

1. **7.8M的文件大小可能是正常的**，特别是如果是HarmonyOS完整版或Noto字体
2. **关键是看日志中的 "Font family name"**，这会告诉你实际加载的是哪个字体
3. **只要字体内部名称与 defaultFontFamily 匹配，中文就应该能正常显示**
4. **如果中文仍显示为方框，检查 defaultFontFamily 是否正确设置**

运行验证工具获取详细信息：
```bash
mvn compile exec:java -Dexec.mainClass="com.mercury.pdf.render.FontFileVerifier"
```
