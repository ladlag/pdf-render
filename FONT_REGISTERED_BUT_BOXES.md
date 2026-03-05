# 问题诊断：看到字体注册日志但中文仍显示为方框 / Font Registered But Chinese Still Shows as Boxes

## 问题描述 / Problem

用户报告：
- ✅ **看到了字体注册日志**：
  ```
  Font registered with Flying Saucer : C:\Users\xxx\TEMP\PDF-RENDER-FONT-XXX.TTF
  ```
- ❌ **但PDF中文仍然显示为方框（□）**

## 根本原因 / Root Cause

字体已经注册，但**CSS中的font-family名称与字体内部名称不匹配**！

Flying Saucer在渲染时，会尝试找到CSS指定的字体。如果CSS中的font-family与注册的字体名称不匹配，就会回退到默认字体（不包含中文字符），导致中文显示为方框。

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

**结果：** 字体会被注册，但CSS使用的是默认字体名，无法匹配。

**解决方法：**

```java
// ✅ 正确 - 必须同时设置路径和字体族
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, sans-serif"); // 必须设置！

service.getHtmlRenderer().setFontConfig(fontConfig);
```

---

### 步骤2：检查字体族名称是否匹配字体内部名称

**问题：** defaultFontFamily设置的名称与字体文件的内部名称不一致

字体文件有内部名称（Font Family Name），CSS必须使用这个**确切的名称**才能匹配。

**错误示例：**

```java
// ❌ 错误 - 名称不完整或不正确
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");

// 这些都是错误的：
fontConfig.setDefaultFontFamily("HarmonyOS Sans");        // 缺少 "SC"
fontConfig.setDefaultFontFamily("HarmonyOS_Sans_SC");     // 用了下划线
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC Regular"); // 多了 "Regular"
fontConfig.setDefaultFontFamily("harmonyos sans sc");     // 大小写错误
```

**正确的内部名称：**

| 字体文件 | 正确的内部名称 |
|---------|--------------|
| HarmonyOS_Sans_SC_Regular.ttf | `HarmonyOS Sans SC` |
| NotoSansCJKsc-Regular.otf | `Noto Sans CJK SC` |
| SourceHanSansSC-Regular.otf | `Source Han Sans SC` |

**正确示例：**

```java
// ✅ 正确 - 使用精确的内部名称
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, sans-serif");
```

---

### 步骤3：如何获取字体的内部名称

运行这个命令获取字体的真实内部名称：

```bash
mvn compile exec:java -Dexec.mainClass="com.mercury.pdf.render.FontConfigDiagnostic"
```

输出会显示：

```
✓ 字体内部名称 Font internal name: HarmonyOS Sans SC

⚠️  重要 IMPORTANT:
   在配置时必须使用这个内部名称！
   You MUST use this internal name in configuration!

   正确配置示例 Correct configuration example:
   fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, sans-serif");
```

**使用这个输出的名称进行配置！**

---

### 步骤4：Spring Boot配置检查

如果使用Spring Boot，检查 application.yml：

**错误配置：**

```yaml
# ❌ 错误 - 只配置了路径，没有配置字体族
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

import com.mercury.pdf.render.PdfRenderService;
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
            
            PdfRenderService service = new PdfRenderService();
            
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
PdfRenderService service = new PdfRenderService();

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
