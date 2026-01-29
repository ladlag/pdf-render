# 如何验证中文字体是否正确配置 How to Verify Chinese Font Configuration

## 快速检查清单 Quick Checklist

### ✅ 步骤1：确认字体文件存在 Step 1: Confirm Font File Exists
```bash
ls -lh src/main/resources/fonts/
```
**期望结果 Expected**:
```
-rw-r--r-- 1 user user 16M Jan 27 10:36 NotoSansCJKsc-Regular.otf
```

### ✅ 步骤2：确认FontConfig已配置 Step 2: Confirm FontConfig Is Set
```java
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/NotoSansCJKsc-Regular.otf");
fontConfig.setDefaultFontFamily("Noto Sans CJK SC, DejaVu Sans, sans-serif");
service.getHtmlRenderer().setFontConfig(fontConfig);
```

**重要 Important**: 
- ✅ 使用 `classpath:` 前缀
- ✅ `defaultFontFamily` 必须是 `"Noto Sans CJK SC"` (字体内部名称)
- ❌ 不要使用文件名 `"NotoSansCJKsc-Regular"`

### ✅ 步骤3：运行验证测试 Step 3: Run Verification Test
```bash
mvn test -Dtest=ChineseFontTest#testChineseTextWithCustomFonts
```

**期望输出 Expected Output**:
```
✓ Chinese text PDF (custom fonts) generated: .../chinese_text_custom_fonts.pdf
  Chinese characters should now render correctly!
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

### ✅ 步骤4：检查PDF文件大小 Step 4: Check PDF File Size
```bash
ls -lh test-output/chinese_text_custom_fonts.pdf
```

**期望结果 Expected**:
```
-rw-r--r-- 1 user user 250K Jan 27 10:37 chinese_text_custom_fonts.pdf
```

**文件大小说明 File Size Explanation**:
- 📄 **< 10KB**: ⚠️ 字体未嵌入，可能配置错误 Font not embedded, configuration error
- 📄 **200-500KB**: ✅ 字体子集化正常工作 Font subsetting working correctly  
- 📄 **> 16MB**: ⚠️ 完整字体嵌入，子集化未生效 Full font embedded, subsetting not working

### ✅ 步骤5：检查控制台输出 Step 5: Check Console Output
```java
service.generatePdf(reportData);
```

**如果有字体加载错误，会看到 If font loading fails, you'll see**:
```
Warning: Failed to register custom fonts: Font not found in classpath: /fonts/xxx.otf
```

## 完整验证代码 Complete Verification Code

```java
import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.model.ReportData;
import com.mercury.pdf.render.model.ReportDataBuilder;
import com.mercury.pdf.render.model.Section;

import java.nio.file.Files;
import java.nio.file.Paths;

public class VerifyChineseFont {
    public static void main(String[] args) throws Exception {
        System.out.println("=== 中文字体验证 Chinese Font Verification ===\n");

        // 1. 创建服务 Create service
        ReportService service = new ReportService();
        service.setUseHtmlPipeline(true);

        // 2. 配置字体 Configure fonts
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/NotoSansCJKsc-Regular.otf");
        fontConfig.setDefaultFontFamily("Noto Sans CJK SC, DejaVu Sans, sans-serif");
        service.getHtmlRenderer().setFontConfig(fontConfig);

        // 3. 创建测试数据 Create test data
        ReportData data = ReportDataBuilder.create()
                .title("中文测试报告 Chinese Test")
                .subtitle("字体验证 Font Verification")
                .reportDate("2024-12-31")
                .addSection(new Section("测试章节 Test Section")
                        .addParagraph("这是中文段落，包含常用汉字。")
                        .addParagraph("This is a Chinese paragraph with common characters."))
                .build();

        // 4. 生成PDF Generate PDF
        byte[] pdf = service.generatePdf(data, "flexible");
        Files.write(Paths.get("verify_chinese.pdf"), pdf);

        // 5. 验证结果 Verify results
        System.out.println("✓ PDF生成成功 PDF generated successfully");
        System.out.println("  文件 File: verify_chinese.pdf");
        System.out.println("  大小 Size: " + (pdf.length / 1024) + " KB");

        if (pdf.length > 100000) {
            System.out.println("\n✅ 成功 SUCCESS: 字体已嵌入，中文应该正常显示");
            System.out.println("   Font embedded, Chinese should display correctly");
        } else {
            System.out.println("\n⚠️  警告 WARNING: 文件太小，字体可能未嵌入");
            System.out.println("   File too small, font may not be embedded");
            System.out.println("   检查 FontConfig 配置 Check FontConfig configuration");
        }
    }
}
```

## 运行验证 Run Verification

### 方式1：使用Maven测试 Option 1: Use Maven Test
```bash
mvn test -Dtest=ChineseFontTest
```

### 方式2：运行独立程序 Option 2: Run Standalone Program
```bash
# 编译 Compile
javac -cp "target/classes:..." VerifyChineseFont.java

# 运行 Run
java -cp ".:target/classes:..." VerifyChineseFont
```

### 方式3：在Spring Boot中验证 Option 3: Verify in Spring Boot
```java
@RestController
public class TestController {
    
    @Autowired
    private PdfReportService pdfService; // 已配置FontConfig
    
    @GetMapping("/test-chinese")
    public ResponseEntity<byte[]> testChinese() throws IOException {
        ReportData data = ReportDataBuilder.create()
            .title("中文测试")
            .addSection(new Section("测试").addParagraph("这是中文"))
            .build();
        
        byte[] pdf = pdfService.generateReport(data);
        
        // 检查文件大小
        if (pdf.length < 50000) {
            throw new RuntimeException("PDF太小，字体可能未配置！");
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "test-chinese.pdf");
        
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
```

## 常见问题诊断 Common Issues Diagnosis

### 问题：中文显示为方框 □ Issue: Chinese Shows as Boxes □

**可能原因 Possible Causes**:

#### 1. FontConfig未配置
```java
// ❌ 错误 Wrong
ReportService service = new ReportService();
byte[] pdf = service.generatePdf(data); // 没有配置字体！

// ✅ 正确 Correct
ReportService service = new ReportService();
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/NotoSansCJKsc-Regular.otf");
fontConfig.setDefaultFontFamily("Noto Sans CJK SC, DejaVu Sans, sans-serif");
service.getHtmlRenderer().setFontConfig(fontConfig);
byte[] pdf = service.generatePdf(data);
```

#### 2. 字体名称错误
```java
// ❌ 错误 Wrong - 使用文件名
fontConfig.setDefaultFontFamily("NotoSansCJKsc-Regular");

// ✅ 正确 Correct - 使用内部字体名
fontConfig.setDefaultFontFamily("Noto Sans CJK SC");
```

#### 3. 字体路径错误
```java
// ❌ 错误 Wrong
fontConfig.setRegularFontPath("/fonts/NotoSansCJKsc-Regular.otf");

// ✅ 正确 Correct
fontConfig.setRegularFontPath("classpath:/fonts/NotoSansCJKsc-Regular.otf");
```

#### 4. 字体文件缺失
```bash
# 检查 Check
ls src/main/resources/fonts/NotoSansCJKsc-Regular.otf

# 如果文件不存在，下载 If missing, download:
# https://github.com/googlefonts/noto-cjk/releases
```

### 问题：PDF文件太小（< 50KB）Issue: PDF File Too Small

**原因 Cause**: 字体未嵌入 Font not embedded

**解决方案 Solution**:
1. 检查 FontConfig 是否正确配置
2. 检查字体文件路径是否正确
3. 查看控制台是否有字体加载错误
4. 确认 `setFontConfig()` 在 `generatePdf()` 之前调用

### 问题：编译错误 Issue: Compilation Error

**错误 Error**:
```
cannot find symbol: class FontConfig
```

**解决方案 Solution**:

```java

```

## 推荐的字体 Recommended Fonts

| 字体 Font | 大小 Size | 许可 License | 下载 Download |
|----------|-----------|-------------|--------------|
| Noto Sans CJK SC | ~16MB | SIL OFL | [GitHub](https://github.com/googlefonts/noto-cjk) |
| HarmonyOS Sans SC | ~4MB | 免费商用 Free | [HarmonyOS](https://developer.harmonyos.com) |
| Source Han Sans CN | ~15MB | SIL OFL | [Adobe](https://github.com/adobe-fonts/source-han-sans) |

## 技术支持 Technical Support

如果以上步骤都无法解决问题，请提供：
If the above steps don't solve the issue, please provide:

1. FontConfig配置代码 FontConfig configuration code
2. 生成的PDF文件大小 Generated PDF file size  
3. 控制台完整输出 Complete console output
4. `mvn --version` 和 `java -version` 输出

---

**最后更新 Last Updated**: 2024-01-27
