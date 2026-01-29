# 中文渲染问题排查 Chinese Rendering Troubleshooting

## 问题描述 Problem Description

**症状 Symptoms**: 生成的PDF中，中文字符显示为方框（□）或乱码

**Symptom**: Chinese characters appear as boxes (□) or garbled text in generated PDF

---

## 快速解决方案 Quick Solution

### 步骤 1: 确认字体文件存在 Step 1: Verify Font Files Exist

```bash
ls -lh src/main/resources/fonts/
```

**应该看到 Should see**:
```
HarmonyOS_Sans_SC_Regular.ttf  (约 4MB)
NotoSansCJKsc-Regular.otf      (约 16MB)
```

**如果文件不存在 If files missing**: [下载字体](#下载字体)

---

### 步骤 2: 配置 FontConfig Step 2: Configure FontConfig

**方法 A: 编程方式 Programmatic (推荐 Recommended)**

```java
import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.config.FontConfig;

// 创建服务 Create service
ReportService service = new ReportService();

// ✅ 关键步骤：配置字体 CRITICAL: Configure fonts
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, sans-serif");
service.getHtmlRenderer().setFontConfig(fontConfig);

// 现在生成PDF Now generate PDF
byte[] pdf = service.generatePdf(reportData, "flexible");
```

**方法 B: Spring Boot 配置 Spring Boot Configuration**

在 `application.yml` 中添加 Add to `application.yml`:

```yaml
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    default-family: HarmonyOS Sans SC, DejaVu Sans, sans-serif
```

---

### 步骤 3: 运行验证测试 Step 3: Run Verification Test

```bash
# 方式 1: 运行测试 Run test
mvn test -Dtest=ChineseFontTest#testChineseTextWithCustomFonts

# 方式 2: 运行演示 Run demo
mvn compile exec:java -Dexec.mainClass="com.mercury.pdf.render.ChineseFontConfigurationDemo"
```

**成功标志 Success indicators**:
- ✅ 测试通过 Test passes
- ✅ PDF 文件大于 200KB (字体已嵌入)
- ✅ 打开 PDF 可以看到中文字符

---

## 常见错误 Common Mistakes

### ❌ 错误 1: 没有配置 FontConfig Error 1: No FontConfig

```java
// ❌ 错误 WRONG
ReportService service = new ReportService();
byte[] pdf = service.generatePdf(data);  // 中文显示为方框！
```

```java
// ✅ 正确 CORRECT
ReportService service = new ReportService();
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, sans-serif");
service.getHtmlRenderer().setFontConfig(fontConfig);
byte[] pdf = service.generatePdf(data);  // 中文正常显示！
```

---

### ❌ 错误 2: 使用文件名而不是字体名 Error 2: Using Filename Instead of Font Name

```java
// ❌ 错误 WRONG - 使用文件名
fontConfig.setDefaultFontFamily("HarmonyOS_Sans_SC_Regular");

// ✅ 正确 CORRECT - 使用字体内部名称
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC");
```

**如何查找字体内部名称 How to find font internal name**:
```bash
# 使用 FontNameExtractor 工具
java -cp target/classes com.mercury.pdf.render.util.FontNameExtractor \
  src/main/resources/fonts/HarmonyOS_Sans_SC_Regular.ttf
```

---

### ❌ 错误 3: 路径错误 Error 3: Wrong Path

```java
// ❌ 错误 WRONG - 缺少 classpath: 前缀
fontConfig.setRegularFontPath("/fonts/HarmonyOS_Sans_SC_Regular.ttf");

// ✅ 正确 CORRECT - 使用 classpath: 前缀
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
```

---

### ❌ 错误 4: 字体文件不存在 Error 4: Font File Missing

**检查 Check**:
```bash
ls src/main/resources/fonts/*.ttf
```

**如果为空 If empty**: [下载字体](#下载字体)

---

## 下载字体 Download Fonts

### 选项 1: 鸿蒙字体 HarmonyOS Sans SC (推荐 Recommended)

- **大小 Size**: ~4MB
- **授权 License**: 免费商用 Free for commercial use
- **下载 Download**: https://developer.harmonyos.com/cn/design/font

```bash
# 下载后放置在 After download, place in:
src/main/resources/fonts/HarmonyOS_Sans_SC_Regular.ttf
```

### 选项 2: 思源黑体 Noto Sans CJK SC

- **大小 Size**: ~16MB
- **授权 License**: SIL Open Font License
- **下载 Download**: https://github.com/googlefonts/noto-cjk/releases

```bash
# 下载后放置在 After download, place in:
src/main/resources/fonts/NotoSansCJKsc-Regular.otf
```

---

## 检查清单 Checklist

运行前确认 Verify before running:

- [ ] 字体文件存在于 `src/main/resources/fonts/`
- [ ] FontConfig 已创建并配置
- [ ] `setRegularFontPath()` 已调用
- [ ] `setDefaultFontFamily()` 已设置为正确的字体名称
- [ ] `service.getHtmlRenderer().setFontConfig()` 已调用
- [ ] PDF 文件大小 > 200KB (表示字体已嵌入)

---

## 验证成功 Verify Success

### 测试代码 Test Code

```java
public class QuickTest {
    public static void main(String[] args) throws Exception {
        ReportService service = new ReportService();
        
        // 配置字体 Configure font
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, sans-serif");
        service.getHtmlRenderer().setFontConfig(fontConfig);
        
        // 创建测试数据 Create test data
        ReportData data = ReportDataBuilder.create()
            .title("测试报告")
            .addSection(new Section("测试章节")
                .addParagraph("这是中文测试文本"))
            .build();
        
        // 生成 PDF Generate PDF
        byte[] pdf = service.generatePdf(data, "flexible");
        Files.write(Paths.get("test.pdf"), pdf);
        
        // 检查 Check
        if (pdf.length > 200000) {
            System.out.println("✅ 成功！字体已嵌入，中文应该正常显示");
        } else {
            System.out.println("❌ 失败！PDF太小，请检查 FontConfig 配置");
        }
    }
}
```

---

## 仍然不工作？Still Not Working?

### 1. 检查控制台输出 Check Console Output

看是否有字体加载错误 Look for font loading errors:
```
Warning: Failed to register custom fonts: ...
```

### 2. 运行完整的演示 Run Full Demo

```bash
mvn compile exec:java -Dexec.mainClass="com.mercury.pdf.render.ChineseFontConfigurationDemo"
```

这会生成两个PDF进行对比 This generates two PDFs for comparison:
- `demo_WITHOUT_font_config.pdf` - 错误示例 (方框)
- `demo_WITH_font_config.pdf` - 正确示例 (中文)

### 3. 查看详细文档 See Detailed Documentation

- 📖 [完整配置指南 Full Configuration Guide](FONT_CONFIGURATION.md)
- 📖 [验证方法 Verification Methods](HOW_TO_VERIFY_CHINESE_FONTS.md)

---

## 联系支持 Contact Support

如果以上方法都无法解决问题，请提供：

If the above doesn't solve the issue, please provide:

1. FontConfig 配置代码 FontConfig configuration code
2. 生成的 PDF 文件大小 Generated PDF file size
3. 控制台完整输出 Complete console output
4. `mvn --version` 输出
5. 字体文件列表 Font file list: `ls -lh src/main/resources/fonts/`

---

**最后更新 Last Updated**: 2026-01-29
