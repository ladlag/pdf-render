# 中文显示问题快速解决 Quick Fix for Chinese Display Issues

## 🚨 问题 Problem

**PDF 中中文显示为方框 (□)?** 
**Chinese characters showing as boxes (□) in PDF?**

---

## ✅ 快速解决 Quick Solution

### 1️⃣ 运行验证脚本 Run Validation Script

```bash
bash validate-chinese-fonts.sh
# 或 or: ./validate-chinese-fonts.sh (需要先 chmod +x)
```

这个脚本会检查 This script checks:
- ✓ 字体文件是否存在 Font files exist
- ✓ 代码能否编译 Code compiles
- ✓ 测试是否通过 Tests pass

---

### 2️⃣ 配置字体 Configure Fonts

**必须在代码中配置 FontConfig:**
**You MUST configure FontConfig in your code:**

```java
import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.config.FontConfig;

ReportService service = new ReportService();

// ✅ 关键步骤：配置字体 CRITICAL STEP: Configure fonts
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, sans-serif");
service.getHtmlRenderer().setFontConfig(fontConfig);

// 现在可以生成中文 PDF Now you can generate Chinese PDFs
byte[] pdf = service.generatePdf(reportData, "flexible");
```

---

### 3️⃣ 运行演示查看效果 Run Demo to See Effect

```bash
mvn compile exec:java -Dexec.mainClass="com.mercury.pdf.render.ChineseFontConfigurationDemo"
```

这会生成两个 PDF 文件进行对比：
This generates two PDFs for comparison:

1. `demo_WITHOUT_font_config.pdf` - ❌ 错误示例 (中文显示为方框)
2. `demo_WITH_font_config.pdf` - ✅ 正确示例 (中文正常显示)

---

## 📖 详细文档 Detailed Documentation

### 遇到问题？Having Issues?

**查看完整故障排除指南 See full troubleshooting guide:**

```bash
cat CHINESE_TROUBLESHOOTING.md
```

或在浏览器中打开 Or open in browser:
- [中文故障排除指南 Chinese Troubleshooting Guide](CHINESE_TROUBLESHOOTING.md)

---

### 完整配置指南 Full Configuration Guide

- [字体配置完整指南 Font Configuration Guide](FONT_CONFIGURATION.md)
- [如何验证字体 How to Verify Fonts](HOW_TO_VERIFY_CHINESE_FONTS.md)
- [中文版 README Chinese README](README_CN.md)

---

## 🎯 核心要点 Key Points

1. **必须配置 FontConfig Must configure FontConfig**
   - 不配置 = 中文显示为方框 No config = boxes (□)
   - 配置正确 = 中文正常显示 Correct config = Chinese displays

2. **字体文件必须存在 Font files must exist**
   - 路径 Path: `src/main/resources/fonts/`
   - 文件 Files: `HarmonyOS_Sans_SC_Regular.ttf` 或 `NotoSansCJKsc-Regular.otf`

3. **使用 classpath: 前缀 Use classpath: prefix**
   - ✅ 正确 Correct: `"classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf"`
   - ❌ 错误 Wrong: `"/fonts/HarmonyOS_Sans_SC_Regular.ttf"`

4. **使用字体内部名称 Use font internal name**
   - ✅ 正确 Correct: `"HarmonyOS Sans SC"`
   - ❌ 错误 Wrong: `"HarmonyOS_Sans_SC_Regular"`

---

## 🔍 验证成功 Verify Success

### 测试文件大小 Check File Size

```bash
ls -lh test-output/chinese_text_custom_fonts.pdf
```

**成功标志 Success indicators:**
- 文件大小 > 200 KB File size > 200 KB
- 说明字体已嵌入 Means font is embedded
- 中文应该正常显示 Chinese should display correctly

---

## 💡 示例代码 Example Code

### 最小示例 Minimal Example

```java
import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.model.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MinimalChineseExample {
    public static void main(String[] args) throws Exception {
        // 1. 创建服务 Create service
        ReportService service = new ReportService();
        
        // 2. 配置中文字体 Configure Chinese font
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, sans-serif");
        service.getHtmlRenderer().setFontConfig(fontConfig);
        
        // 3. 创建包含中文的报告 Create report with Chinese
        ReportData data = ReportDataBuilder.create()
            .title("中文测试报告")
            .subtitle("Chinese Test Report")
            .addSection(new Section("测试章节")
                .addParagraph("这是中文段落。This is a Chinese paragraph."))
            .build();
        
        // 4. 生成 PDF Generate PDF
        byte[] pdf = service.generatePdf(data, "flexible");
        Files.write(Paths.get("chinese_test.pdf"), pdf);
        
        // 5. 验证 Verify
        if (pdf.length > 200000) {
            System.out.println("✅ 成功！中文应该正常显示");
            System.out.println("✅ Success! Chinese should display correctly");
        } else {
            System.out.println("❌ 失败！请检查 FontConfig");
            System.out.println("❌ Failed! Please check FontConfig");
        }
    }
}
```

---

## ⚠️ 常见错误 Common Mistakes

### 错误 1: 忘记配置 FontConfig
```java
// ❌ 错误 WRONG
ReportService service = new ReportService();
byte[] pdf = service.generatePdf(data);  // 中文显示为方框！
```

### 错误 2: 配置了但没有调用 setFontConfig
```java
// ❌ 错误 WRONG
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
// 忘记调用 setFontConfig！ Forgot to call setFontConfig!
byte[] pdf = service.generatePdf(data);
```

### 错误 3: 路径错误
```java
// ❌ 错误 WRONG - 缺少 classpath: 前缀
fontConfig.setRegularFontPath("/fonts/HarmonyOS_Sans_SC_Regular.ttf");

// ✅ 正确 CORRECT
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
```

---

## 🆘 仍然不工作？Still Not Working?

1. **运行验证脚本 Run validation script:**
   ```bash
   bash validate-chinese-fonts.sh
   ```

2. **查看详细故障排除 See detailed troubleshooting:**
   ```bash
   cat CHINESE_TROUBLESHOOTING.md
   ```

3. **运行完整演示 Run full demo:**
   ```bash
   mvn compile exec:java -Dexec.mainClass="com.mercury.pdf.render.ChineseFontConfigurationDemo"
   ```

4. **查看测试结果 Check test results:**
   ```bash
   mvn test -Dtest=ChineseFontTest
   open test-output/chinese_text_custom_fonts.pdf
   ```

---

**最后更新 Last Updated**: 2026-01-29
