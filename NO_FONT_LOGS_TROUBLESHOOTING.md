# 问题诊断：没有看到字体注册日志 / Troubleshooting: Font Registration Logs Not Appearing

## 问题描述 / Problem Description

用户报告生成PDF时，没有看到以下日志：
```
✓ Font registered with Flying Saucer: /tmp/pdf-render-font-xxx.ttf
  Encoding: Identity-H | Embedded: true
  Font family name (for CSS): HarmonyOS Sans SC
✓ Total fonts registered for PDF: 1
```

**这说明字体配置没有生效！** 这就是中文显示为方框的根本原因。

---

## 原因分析 / Root Cause Analysis

字体注册日志不出现的原因只有一个：**FontConfig没有被正确设置**

让我们检查每一个可能的原因：

---

## 检查清单 / Checklist

### ✓ 检查点1：确认是否调用了 setFontConfig()

**问题：** 忘记调用 `setFontConfig()` 方法

**检查方法：**

```java
// ❌ 错误 - 创建了FontConfig但忘记设置
ReportService service = new ReportService();
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
// 忘记了这一行！
// service.getHtmlRenderer().setFontConfig(fontConfig);

byte[] pdf = service.generatePdf(data); // 不会有字体注册日志
```

**正确做法：**

```java
// ✅ 正确 - 必须调用 setFontConfig()
ReportService service = new ReportService();
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, sans-serif");

// 必须调用这一行！
service.getHtmlRenderer().setFontConfig(fontConfig);

byte[] pdf = service.generatePdf(data); // 现在会看到日志
```

---

### ✓ 检查点2：Spring Boot - @PostConstruct问题

**问题：** 在Spring Boot中使用了 `@PostConstruct`，但配置在对象创建后被覆盖

**错误示例：**

```java
@Service
public class PdfService {
    
    @Autowired
    private ReportService reportService;
    
    @PostConstruct
    public void init() {
        // ❌ 这个配置会覆盖YAML配置
        FontConfig config = new FontConfig();
        config.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        config.setDefaultFontFamily("HarmonyOS Sans SC, sans-serif");
        reportService.getHtmlRenderer().setFontConfig(config);
    }
    
    public byte[] generatePdf(ReportData data) throws Exception {
        // 这里调用时，@PostConstruct中的配置可能已经失效
        return reportService.generatePdf(data);
    }
}
```

**为什么会失效？**

Spring Boot的自动配置在 `@PostConstruct` **之后**运行，会创建一个新的 `ReportService` 实例，覆盖你的配置。

**正确做法 - 使用构造函数注入：**

```java
@Service
public class PdfService {
    
    private final ReportService reportService;
    
    // ✅ 使用构造函数注入
    public PdfService(ReportService reportService) {
        this.reportService = reportService;
        // 配置已经从application.yml加载了
    }
    
    public byte[] generatePdf(ReportData data) throws Exception {
        return reportService.generatePdf(data);
    }
}
```

**application.yml 配置：**

```yaml
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    default-family: HarmonyOS Sans SC, sans-serif
```

---

### ✓ 检查点3：路径配置错误

**问题：** FontConfig设置了，但路径是 null 或空字符串

**检查方法：**

```java
FontConfig fontConfig = new FontConfig();
// ❌ 如果这些是null，不会注册字体
fontConfig.setRegularFontPath(null);  // 不会注册
fontConfig.setRegularFontPath("");    // 不会注册
fontConfig.setRegularFontPath("classpath:/fonts/NonExistent.ttf"); // 会报错
```

**验证配置：**

```java
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, sans-serif");

// 添加日志验证
System.out.println("Font path configured: " + fontConfig.getRegularFontPath());
System.out.println("Font family configured: " + fontConfig.getDefaultFontFamily());

service.getHtmlRenderer().setFontConfig(fontConfig);
```

---

### ✓ 检查点4：检查代码执行顺序

**问题：** 配置在生成PDF之后才设置

```java
// ❌ 错误顺序
ReportService service = new ReportService();
byte[] pdf = service.generatePdf(data); // 这时还没有配置字体！

FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
service.getHtmlRenderer().setFontConfig(fontConfig); // 太晚了！
```

**正确顺序：**

```java
// ✅ 正确顺序：先配置，再生成
ReportService service = new ReportService();

FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, sans-serif");
service.getHtmlRenderer().setFontConfig(fontConfig); // 必须在generatePdf之前

byte[] pdf = service.generatePdf(data); // 现在配置生效了
```

---

### ✓ 检查点5：Spring Boot YAML配置问题

**问题：** YAML配置格式错误或位置错误

**错误示例：**

```yaml
# ❌ 错误 - 缩进不正确
pdf-render:
fonts:
  regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
  
# ❌ 错误 - 键名错误（应该是 regular-path 不是 regularPath）
pdf-render:
  fonts:
    regularPath: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    
# ❌ 错误 - 放在错误的配置文件中（应该在 application.yml）
```

**正确示例：**

```yaml
# ✅ 正确 - application.yml 中
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    default-family: HarmonyOS Sans SC, sans-serif
```

---

## 调试步骤 / Debug Steps

### 步骤1：添加调试日志

在你的代码中添加日志来追踪配置：

```java
import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.config.FontConfig;

public class DebugFontConfig {
    public static void main(String[] args) {
        System.out.println("========== 开始调试 Debug Start ==========");
        
        // 创建服务
        ReportService service = new ReportService();
        System.out.println("✓ ReportService created");
        
        // 配置字体
        FontConfig fontConfig = new FontConfig();
        System.out.println("✓ FontConfig created");
        
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        System.out.println("✓ Font path set: " + fontConfig.getRegularFontPath());
        
        fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, sans-serif");
        System.out.println("✓ Font family set: " + fontConfig.getDefaultFontFamily());
        
        service.getHtmlRenderer().setFontConfig(fontConfig);
        System.out.println("✓ FontConfig applied to renderer");
        
        // 验证配置
        FontConfig retrievedConfig = service.getHtmlRenderer().getFontConfig();
        if (retrievedConfig != null) {
            System.out.println("✓ FontConfig is set in renderer");
            System.out.println("  Path: " + retrievedConfig.getRegularFontPath());
            System.out.println("  Family: " + retrievedConfig.getDefaultFontFamily());
        } else {
            System.err.println("✗ ERROR: FontConfig is NULL in renderer!");
        }
        
        System.out.println("========== 现在生成PDF Now generating PDF ==========");
        
        try {
            // 创建测试数据
            ReportData data = new ReportData();
            data.setTitle("字体测试 Font Test");
            Section section = new Section("测试章节");
            section.addParagraph("这是中文测试 This is Chinese test");
            data.getSections().add(section);
            
            // 生成PDF - 应该看到字体注册日志
            byte[] pdf = service.generatePdf(data, "flexible");
            
            System.out.println("========== PDF生成完成 PDF Generated ==========");
            System.out.println("PDF size: " + (pdf.length / 1024) + " KB");
            
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

**预期输出：**

```
========== 开始调试 Debug Start ==========
✓ ReportService created
✓ FontConfig created
✓ Font path set: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
✓ Font family set: HarmonyOS Sans SC, sans-serif
✓ FontConfig applied to renderer
✓ FontConfig is set in renderer
  Path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
  Family: HarmonyOS Sans SC, sans-serif
========== 现在生成PDF Now generating PDF ==========
✓ Font registered with Flying Saucer: /tmp/pdf-render-font-xxx.ttf
  Encoding: Identity-H | Embedded: true
  Font family name (for CSS): HarmonyOS Sans SC
✓ Total fonts registered for PDF: 1
========== PDF生成完成 PDF Generated ==========
PDF size: 110 KB
```

如果没有看到 "Font registered with Flying Saucer"，检查上面的哪一步失败了。

---

### 步骤2：运行诊断工具

```bash
mvn compile exec:java -Dexec.mainClass="com.mercury.pdf.render.FontConfigDiagnostic"
```

这个工具会自动检查所有配置并生成测试PDF。

---

### 步骤3：检查Spring Boot配置加载

如果使用Spring Boot，添加日志检查配置是否加载：

```java
@Service
public class PdfService {
    
    private final ReportService reportService;
    private final PdfRenderProperties properties;
    
    public PdfService(ReportService reportService, PdfRenderProperties properties) {
        this.reportService = reportService;
        this.properties = properties;
        
        // 添加日志检查配置
        System.out.println("========== Spring Boot Configuration ==========");
        if (properties != null && properties.getFonts() != null) {
            System.out.println("✓ PdfRenderProperties loaded");
            System.out.println("  Regular path: " + properties.getFonts().getRegularPath());
            System.out.println("  Default family: " + properties.getFonts().getDefaultFamily());
        } else {
            System.err.println("✗ PdfRenderProperties is NULL or fonts not configured!");
        }
        
        // 验证ReportService配置
        FontConfig config = reportService.getHtmlRenderer().getFontConfig();
        if (config != null) {
            System.out.println("✓ FontConfig is set in ReportService");
            System.out.println("  Path: " + config.getRegularFontPath());
        } else {
            System.err.println("✗ FontConfig is NULL in ReportService!");
        }
        System.out.println("========================================");
    }
}
```

---

## 常见场景解决方案 / Common Scenario Solutions

### 场景1：纯Java项目（不使用Spring）

```java
// 完整的、能看到日志的代码
import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.model.*;

public class Main {
    public static void main(String[] args) throws Exception {
        // 1. 创建服务
        ReportService service = new ReportService();
        
        // 2. 配置字体 - 这是关键！
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, sans-serif");
        
        // 3. 应用配置 - 必须调用！
        service.getHtmlRenderer().setFontConfig(fontConfig);
        
        // 4. 创建数据
        ReportData data = new ReportData();
        data.setTitle("中文测试");
        Section section = new Section("测试");
        section.addParagraph("中文内容");
        data.getSections().add(section);
        
        // 5. 生成PDF - 会看到字体注册日志
        byte[] pdf = service.generatePdf(data);
        
        // 保存
        java.nio.file.Files.write(
            java.nio.file.Paths.get("test.pdf"), 
            pdf
        );
    }
}
```

### 场景2：Spring Boot项目

**application.yml:**

```yaml
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    default-family: HarmonyOS Sans SC, sans-serif

# 可选：启用调试日志
logging:
  level:
    com.mercury.pdf.render: DEBUG
```

**Service类:**

```java
@Service
public class PdfService {
    
    private final ReportService reportService;
    
    // 构造函数注入 - 配置自动加载
    public PdfService(ReportService reportService) {
        this.reportService = reportService;
    }
    
    public byte[] generatePdf(ReportData data) throws Exception {
        // 直接调用 - 配置已经生效
        return reportService.generatePdf(data);
    }
}
```

**不要使用 @PostConstruct！**

---

## 验证成功的标志 / Success Indicators

当配置正确时，你应该看到：

1. **控制台输出：**
   ```
   ✓ Font registered with Flying Saucer: /tmp/pdf-render-font-xxx.ttf
     Encoding: Identity-H | Embedded: true
     Font family name (for CSS): HarmonyOS Sans SC
   ✓ Total fonts registered for PDF: 1
   ```

2. **PDF文件大小：**
   - 无字体配置：2-5 KB
   - 有字体配置：50-200 KB（大得多！）

3. **PDF内容：**
   - 打开PDF，中文字符正常显示，不是方框□

---

## 仍然无法解决？ / Still Not Working?

如果按照以上步骤仍然看不到日志，请：

1. 运行诊断工具：
   ```bash
   mvn compile exec:java -Dexec.mainClass="com.mercury.pdf.render.FontConfigDiagnostic"
   ```

2. 提供以下信息：
   - 使用的是纯Java还是Spring Boot？
   - 完整的配置代码
   - 控制台完整输出
   - 生成的PDF文件大小

3. 查看相关文档：
   - [中文快速指南](CHINESE_QUICKSTART.md)
   - [完整示例](CHINESE_PDF_EXAMPLES.md)
   - [字体配置文档](FONT_CONFIGURATION.md)

---

## 总结 / Summary

**没有看到 "Font registered with Flying Saucer" 日志 = 字体配置没有生效**

最常见的原因：
1. ❌ 忘记调用 `setFontConfig()`
2. ❌ Spring Boot 使用了 `@PostConstruct`
3. ❌ 字体路径是 null 或空
4. ❌ 配置在生成PDF之后才设置
5. ❌ Spring Boot YAML配置格式错误

**解决方法：**
- ✅ 纯Java：确保调用 `service.getHtmlRenderer().setFontConfig(fontConfig)`
- ✅ Spring Boot：使用构造函数注入 + application.yml配置
- ✅ 运行诊断工具验证配置
