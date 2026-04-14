# 中文显示快速指南 / Chinese Character Quick Start Guide

## 问题：PDF中文显示为方框（□）

如果您生成的PDF中，中文字符显示为方框（□），这是因为**没有正确配置中文字体**。

## ✅ 解决方案（3步）

### 第1步：确认字体文件存在

检查 `src/main/resources/fonts/` 目录下是否有中文字体文件：

```
src/main/resources/fonts/
├── HarmonyOS_Sans_SC_Regular.ttf  ✓ (项目已包含)
└── NotoSansCJKsc-Regular.otf      ✓ (项目已包含)
```

**✓ 本项目已内置这两个字体，可直接使用！**

### 第2步：配置字体

#### 方式1：纯Java代码配置（推荐用于非Spring Boot项目）

```java
import com.mercury.pdf.render.PdfRenderService;
import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.model.ReportData;

// 创建服务
PdfRenderService service = new PdfRenderService();

// 配置中文字体 - 关键步骤！
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, sans-serif");

// 应用字体配置
service.getHtmlRenderer().setFontConfig(fontConfig);

// 生成PDF
ReportData data = createYourReportData(); // 包含中文的数据
byte[] pdf = service.generatePdf(data);
```

#### 方式2：Spring Boot YAML配置（推荐用于Spring Boot项目）

**在 `application.yml` 中添加：**

```yaml
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    default-family: HarmonyOS Sans SC, DejaVu Sans, sans-serif
```

**在Service中注入使用：**

```java
import com.mercury.pdf.render.PdfRenderService;
import org.springframework.stereotype.Service;

@Service
public class PdfService {
    private final PdfRenderService pdfRenderService;
    
    // ✅ 直接注入 - 自动配置已生效
    public PdfService(PdfRenderService pdfRenderService) {
        this.pdfRenderService = pdfRenderService;
    }
    
    public byte[] generatePdf(ReportData data) throws Exception {
        return pdfRenderService.generatePdf(data);
    }
}
```

**⚠️ 注意：不要使用 @PostConstruct！** 这会覆盖YAML配置。

### 第3步：验证

运行以下测试验证配置是否正确：

```bash
mvn test -Dtest=ChineseFontTest
```

测试通过后，打开生成的PDF文件：
- `test-output/chinese_text_custom_fonts.pdf` - 应该能正确显示中文

## 🔍 常见错误

### 错误1：忘记配置FontConfig

```java
// ❌ 错误 - 没有配置字体
PdfRenderService service = new PdfRenderService();
byte[] pdf = service.generatePdf(data); // 中文会显示为 □
```

```java
// ✅ 正确 - 必须配置字体
PdfRenderService service = new PdfRenderService();
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, sans-serif");
service.getHtmlRenderer().setFontConfig(fontConfig);
byte[] pdf = service.generatePdf(data); // 中文正常显示
```

### 错误2：字体路径缺少 `/`

```java
// ❌ 错误 - 路径必须以 / 开头
fontConfig.setRegularFontPath("classpath:fonts/xxx.ttf");
```

```java
// ✅ 正确
fontConfig.setRegularFontPath("classpath:/fonts/xxx.ttf");
```

### 错误3：Spring Boot使用@PostConstruct

```java
// ❌ 错误 - @PostConstruct会覆盖YAML配置
@Service
public class PdfService {
    @Autowired
    private PdfRenderService pdfRenderService;
    
    @PostConstruct
    public void init() {
        FontConfig config = new FontConfig();
        config.setRegularFontPath("...");
        pdfRenderService.getHtmlRenderer().setFontConfig(config);
    }
}
```

```java
// ✅ 正确 - 直接注入，依赖YAML配置
@Service
public class PdfService {
    private final PdfRenderService pdfRenderService;
    
    public PdfService(PdfRenderService pdfRenderService) {
        this.pdfRenderService = pdfRenderService; // 自动配置已生效
    }
}
```

### 错误4：font-family名称不匹配

```java
// ❌ 错误 - font-family必须匹配字体内部名称
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.setDefaultFontFamily("HarmonyOS Sans"); // 名称不完整
```

```java
// ✅ 正确 - 使用完整的内部名称
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, sans-serif"); // 完整名称
```

## 📝 完整示例

查看项目中的示例代码：

- **演示代码**: `src/main/java/com/mercury/pdf/render/ChineseFontConfigurationDemo.java`
- **测试代码**: `src/test/java/com/mercury/pdf/render/ChineseFontTest.java`

运行演示：

```bash
# 生成对比PDF（带字体 vs 不带字体）
mvn exec:java -Dexec.mainClass="com.mercury.pdf.render.ChineseFontConfigurationDemo"
```

## 🆘 仍然无法显示中文？

如果按照以上步骤配置后仍然无法显示中文，请检查：

1. **字体文件是否存在**：确认 `src/main/resources/fonts/` 目录中有字体文件
2. **路径是否正确**：必须使用 `classpath:/fonts/xxx.ttf`（注意 `/`）
3. **是否真的调用了setFontConfig**：添加日志确认配置代码被执行
4. **Spring Boot项目**：检查是否有 `@PostConstruct` 覆盖了配置
5. **查看日志**：应该看到 "Font registered with Flying Saucer" 的日志

**启用调试日志查看详细信息：**

```yaml
logging:
  level:
    com.mercury.pdf.render: DEBUG
```

## 📚 更多文档

- [完整字体配置指南](FONT_CONFIGURATION.md)
- [Spring Boot集成指南](SPRING_BOOT_INTEGRATION_GUIDE.md)
- [常见问题解答](TROUBLESHOOTING.md)

---

## English Version

## Problem: Chinese Characters Display as Boxes (□) in PDF

If Chinese characters appear as boxes (□) in your generated PDF, it's because **Chinese fonts are not properly configured**.

## ✅ Solution (3 Steps)

### Step 1: Verify Font Files Exist

Check if Chinese font files exist in `src/main/resources/fonts/`:

```
src/main/resources/fonts/
├── HarmonyOS_Sans_SC_Regular.ttf  ✓ (included in project)
└── NotoSansCJKsc-Regular.otf      ✓ (included in project)
```

**✓ These fonts are already included in the project!**

### Step 2: Configure Fonts

#### Option 1: Java Code Configuration (for non-Spring Boot)

```java
import com.mercury.pdf.render.PdfRenderService;
import com.mercury.pdf.render.config.FontConfig;

PdfRenderService service = new PdfRenderService();

// Configure Chinese font - Critical step!
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, sans-serif");

// Apply font configuration
service.getHtmlRenderer().setFontConfig(fontConfig);

// Generate PDF
byte[] pdf = service.generatePdf(data);
```

#### Option 2: Spring Boot YAML Configuration (recommended)

**In `application.yml`:**

```yaml
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    default-family: HarmonyOS Sans SC, DejaVu Sans, sans-serif
```

**In your Service:**

```java
@Service
public class PdfService {
    private final PdfRenderService pdfRenderService;
    
    // ✅ Direct injection - auto-configuration active
    public PdfService(PdfRenderService pdfRenderService) {
        this.pdfRenderService = pdfRenderService;
    }
    
    public byte[] generatePdf(ReportData data) throws Exception {
        return pdfRenderService.generatePdf(data);
    }
}
```

**⚠️ Warning: Don't use @PostConstruct!** This will override YAML config.

### Step 3: Verify

Run test to verify configuration:

```bash
mvn test -Dtest=ChineseFontTest
```

After test passes, open the generated PDF:
- `test-output/chinese_text_custom_fonts.pdf` - Chinese should display correctly

## 🔍 Common Mistakes

See Chinese section above for detailed examples of common mistakes and their fixes.

## 📚 More Documentation

- [Complete Font Configuration Guide](FONT_CONFIGURATION.md)
- [Spring Boot Integration Guide](SPRING_BOOT_INTEGRATION_GUIDE.md)
- [Troubleshooting](TROUBLESHOOTING.md)
