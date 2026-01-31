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
