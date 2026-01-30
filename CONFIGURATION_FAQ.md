# 配置常见问题 / Configuration FAQ

本文档回答关于pdf-render库配置的常见问题，特别是关于Spring Boot集成时YAML配置与代码配置的关系。

## 目录

- [Spring Boot配置问题](#spring-boot配置问题)
- [配置优先级](#配置优先级)
- [最佳实践](#最佳实践)
- [故障排查](#故障排查)

---

## Spring Boot配置问题

### Q1: JAR_INTEGRATION.md中的示例使用代码设置字体，那么application.yaml的配置是否还生效？

**答案：不生效。代码配置会覆盖YAML配置。**

**详细说明：**

JAR_INTEGRATION.md中展示的这种配置方式：

```java
PdfRenderProperties properties = new PdfRenderProperties();
PdfRenderProperties.FontProperties fonts = new PdfRenderProperties.FontProperties();
fonts.setRegularPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fonts.setDefaultFamily("HarmonyOS Sans SC, DejaVu Sans, sans-serif");
properties.setFonts(fonts);

service.getHtmlRenderer().setFontProperties(fonts);
```

这是**程序化配置**方式，主要用于**普通Java项目（非Spring Boot）**。

当你调用 `service.getHtmlRenderer().setFontProperties(fonts)` 时：
- ✅ 代码中设置的字体配置**会生效**
- ❌ application.yaml中的字体配置**不再生效**（被覆盖）

**配置覆盖原理：**

```
1. Spring Boot启动
   ↓
2. PdfRenderAutoConfiguration读取application.yaml
   ↓
3. 自动配置ReportService，应用YAML配置
   ↓
4. 你的代码调用 setFontProperties(fonts)
   ↓
5. 字体配置被覆盖为代码中的设置
   ↓
6. 生成PDF时使用代码配置的字体
```

### Q2: 是否还需要在application.yaml中配置字体？

**答案：取决于你的配置方式。**

#### 情况A：仅使用YAML配置（推荐）

**需要配置application.yaml，不需要代码配置**

```yaml
# application.yml
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    default-family: HarmonyOS Sans SC, sans-serif
```

```java
@Service
public class PdfService {
    private final ReportService reportService;
    
    // 自动注入，字体已配置
    public PdfService(ReportService reportService) {
        this.reportService = reportService;
    }
    
    public byte[] generatePdf(ReportData data) throws IOException {
        // 直接使用，无需再设置字体
        return reportService.generatePdf(data);
    }
}
```

**优点：**
- ✅ 配置集中管理
- ✅ 支持多环境配置（dev/test/prod）
- ✅ 代码简洁
- ✅ 符合Spring Boot最佳实践

#### 情况B：使用代码配置

**不需要配置application.yaml**

```java
@Service
public class PdfService {
    private final ReportService reportService;
    
    public PdfService(ReportService reportService) {
        this.reportService = reportService;
        
        // 程序化配置
        PdfRenderProperties.FontProperties fonts = new PdfRenderProperties.FontProperties();
        fonts.setRegularPath("classpath:/fonts/Custom_Font.ttf");
        fonts.setDefaultFamily("Custom Font, sans-serif");
        reportService.getHtmlRenderer().setFontProperties(fonts);
    }
    
    public byte[] generatePdf(ReportData data) throws IOException {
        return reportService.generatePdf(data);
    }
}
```

**适用场景：**
- 需要动态切换字体
- 不同的Service使用不同的字体
- 运行时决定字体配置

**缺点：**
- ❌ 配置分散在代码中
- ❌ 修改配置需要重新编译
- ❌ 不支持多环境配置

### Q3: 是否还需要在Application类上添加@EnableConfigurationProperties注解？

**答案：取决于你的配置方式。**

#### 情况A：使用YAML配置

**必须添加** `@EnableConfigurationProperties(PdfRenderProperties.class)`

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.mercury.pdf.render.config.PdfRenderProperties;

@SpringBootApplication
@EnableConfigurationProperties(PdfRenderProperties.class)  // ← 必须添加
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

**为什么需要：**
- Spring Boot需要这个注解来识别`PdfRenderProperties`配置类
- 没有这个注解，`pdf-render`配置节点不会被解析
- 自动配置类`PdfRenderAutoConfiguration`需要读取这些属性
- 没有这个注解，YAML配置完全不生效

#### 情况B：仅使用代码配置

**不需要**添加`@EnableConfigurationProperties`

```java
@SpringBootApplication
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

**说明：**
- 如果你完全不使用YAML配置
- 所有配置都通过代码完成
- 可以省略这个注解

**但是**，即使使用代码配置，建议还是添加这个注解，因为：
- ✅ 保持配置的一致性
- ✅ 方便将来改用YAML配置
- ✅ 不会造成任何副作用

---

## 配置优先级

### 配置方式对比表

| 配置方式 | @EnableConfigurationProperties | application.yaml | 代码配置 | 最终生效 | 推荐度 |
|---------|-------------------------------|------------------|---------|---------|--------|
| 仅YAML | ✅ 必需 | ✅ 配置 | ❌ 不配置 | YAML | ⭐⭐⭐⭐⭐ |
| 仅代码 | ❌ 可选 | ❌ 不配置 | ✅ 配置 | 代码 | ⭐⭐⭐ |
| YAML+代码 | ✅ 必需 | ✅ 配置 | ✅ 配置 | 代码（覆盖YAML） | ⭐⭐ |

### 优先级规则

```
代码配置 > YAML配置 > 默认配置
```

**示例场景：**

```yaml
# application.yml
pdf-render:
  fonts:
    regular-path: classpath:/fonts/Font_A.ttf
    default-family: Font A, sans-serif
```

```java
@Service
public class PdfService {
    public PdfService(ReportService reportService) {
        // 这会覆盖YAML配置
        PdfRenderProperties.FontProperties fonts = new PdfRenderProperties.FontProperties();
        fonts.setRegularPath("classpath:/fonts/Font_B.ttf");
        fonts.setDefaultFamily("Font B, sans-serif");
        reportService.getHtmlRenderer().setFontProperties(fonts);
    }
}
```

**结果：**
- 实际使用的字体是 **Font B**（代码配置）
- Font A（YAML配置）被忽略

### 如何验证当前生效的配置？

```java
@Service
public class PdfService {
    private final ReportService reportService;
    
    @PostConstruct
    public void init() {
        PdfRenderProperties.FontProperties fonts = 
            reportService.getHtmlRenderer().getFontProperties();
        
        if (fonts != null) {
            System.out.println("当前字体配置:");
            System.out.println("  Regular Path: " + fonts.getRegularPath());
            System.out.println("  Default Family: " + fonts.getDefaultFamily());
        } else {
            System.out.println("未配置字体（将使用默认字体）");
        }
    }
}
```

---

## 最佳实践

### 推荐：Spring Boot项目使用YAML配置

```yaml
# application.yml
pdf-render:
  template:
    default-name: report
    cache-enabled: true
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    bold-path: classpath:/fonts/HarmonyOS_Sans_SC_Bold.ttf
    default-family: HarmonyOS Sans SC, DejaVu Sans, sans-serif
  debug:
    enabled: false
    output-directory: debug-html
```

```java
@SpringBootApplication
@EnableConfigurationProperties(PdfRenderProperties.class)
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

```java
@Service
public class PdfService {
    private final ReportService reportService;
    
    public PdfService(ReportService reportService) {
        this.reportService = reportService;
        // 不需要任何额外配置
    }
    
    public byte[] generatePdf(ReportData data) throws IOException {
        return reportService.generatePdf(data);
    }
}
```

**优点：**
- ✅ 配置集中在application.yml
- ✅ 支持多环境配置（application-dev.yml, application-prod.yml）
- ✅ 可以使用Spring的配置加密
- ✅ 代码简洁，易于维护
- ✅ 符合Spring Boot最佳实践

### 场景：需要动态字体配置

如果需要根据不同情况使用不同字体，可以使用策略模式：

```java
@Configuration
public class PdfConfiguration {
    
    @Bean
    public FontSelector fontSelector() {
        return new FontSelector();
    }
}

public class FontSelector {
    
    public PdfRenderProperties.FontProperties selectFont(String language) {
        PdfRenderProperties.FontProperties fonts = new PdfRenderProperties.FontProperties();
        
        switch (language) {
            case "zh":
                fonts.setRegularPath("classpath:/fonts/Chinese_Font.ttf");
                fonts.setDefaultFamily("Chinese Font, sans-serif");
                break;
            case "ja":
                fonts.setRegularPath("classpath:/fonts/Japanese_Font.ttf");
                fonts.setDefaultFamily("Japanese Font, sans-serif");
                break;
            case "ko":
                fonts.setRegularPath("classpath:/fonts/Korean_Font.ttf");
                fonts.setDefaultFamily("Korean Font, sans-serif");
                break;
            default:
                fonts.setRegularPath("classpath:/fonts/Default_Font.ttf");
                fonts.setDefaultFamily("Default Font, sans-serif");
        }
        
        return fonts;
    }
}

@Service
public class MultiLanguagePdfService {
    private final FontSelector fontSelector;
    
    public MultiLanguagePdfService(FontSelector fontSelector) {
        this.fontSelector = fontSelector;
    }
    
    public byte[] generatePdf(ReportData data, String language) throws IOException {
        // 创建新的ReportService实例
        ReportService service = new ReportService();
        
        // 根据语言选择字体
        PdfRenderProperties.FontProperties fonts = fontSelector.selectFont(language);
        service.getHtmlRenderer().setFontProperties(fonts);
        
        return service.generatePdf(data);
    }
}
```

### 场景：多租户不同配置

```java
@Service
public class TenantPdfService {
    
    @Autowired
    private TenantConfigRepository tenantConfigRepository;
    
    public byte[] generatePdf(ReportData data, String tenantId) throws IOException {
        // 创建新的ReportService实例
        ReportService service = new ReportService();
        
        // 从数据库加载租户配置
        TenantConfig config = tenantConfigRepository.findByTenantId(tenantId);
        
        // 应用租户特定的字体配置
        PdfRenderProperties.FontProperties fonts = new PdfRenderProperties.FontProperties();
        fonts.setRegularPath(config.getFontPath());
        fonts.setDefaultFamily(config.getFontFamily());
        service.getHtmlRenderer().setFontProperties(fonts);
        
        return service.generatePdf(data);
    }
}
```

---

## 故障排查

### 问题1：中文显示为方框（□）

**可能原因：**
1. 字体配置未生效
2. 代码配置覆盖了YAML配置
3. @EnableConfigurationProperties未添加

**排查步骤：**

```java
@PostConstruct
public void checkConfiguration() {
    System.out.println("=== PDF Render Configuration Check ===");
    
    PdfRenderProperties.FontProperties fonts = 
        reportService.getHtmlRenderer().getFontProperties();
    
    if (fonts == null) {
        System.err.println("❌ 错误：字体配置为空！");
        System.err.println("可能原因：");
        System.err.println("  1. application.yml中没有配置字体");
        System.err.println("  2. 缺少@EnableConfigurationProperties注解");
        return;
    }
    
    System.out.println("✓ 字体配置已加载：");
    System.out.println("  Regular Path: " + fonts.getRegularPath());
    System.out.println("  Default Family: " + fonts.getDefaultFamily());
    
    // 检查字体文件是否存在
    String fontPath = fonts.getRegularPath();
    if (fontPath != null && fontPath.startsWith("classpath:")) {
        String resourcePath = fontPath.substring("classpath:".length());
        InputStream is = getClass().getResourceAsStream(resourcePath);
        if (is == null) {
            System.err.println("❌ 错误：字体文件不存在：" + resourcePath);
        } else {
            System.out.println("✓ 字体文件存在");
            try { is.close(); } catch (IOException e) {}
        }
    }
}
```

### 问题2：YAML配置不生效

**检查清单：**

1. ✅ 确认已添加`@EnableConfigurationProperties`注解
```java
@SpringBootApplication
@EnableConfigurationProperties(PdfRenderProperties.class)
public class MyApplication { }
```

2. ✅ 确认application.yml配置格式正确
```yaml
pdf-render:  # ← 注意缩进
  fonts:     # ← 注意缩进
    regular-path: classpath:/fonts/font.ttf
```

3. ✅ 确认没有在代码中调用`setFontProperties()`
```java
// 错误：这会覆盖YAML配置
service.getHtmlRenderer().setFontProperties(fonts);  // ← 删除这行
```

4. ✅ 确认使用的是Spring注入的ReportService
```java
@Service
public class PdfService {
    private final ReportService reportService;
    
    // 正确：使用构造器注入
    public PdfService(ReportService reportService) {
        this.reportService = reportService;
    }
}
```

5. ✅ 确认没有手动创建ReportService实例
```java
// 错误：手动创建的实例不会应用YAML配置
ReportService service = new ReportService();  // ← 不要这样做

// 正确：使用Spring注入的实例
@Autowired
private ReportService reportService;
```

### 问题3：配置了YAML也配置了代码，不知道哪个生效

**解决方案：启用调试日志**

```yaml
logging:
  level:
    com.mercury.pdf.render: DEBUG
```

启动应用后，查看日志：

```
PDF Render: Font configuration applied from properties
  - Regular font: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
```

如果看到这个日志，说明YAML配置已应用。

如果之后又看到代码中的字体配置，说明代码配置覆盖了YAML配置。

---

## 总结

### 快速决策树

```
你在使用Spring Boot吗？
├─ 是
│  ├─ 需要动态切换字体吗？
│  │  ├─ 是 → 使用代码配置
│  │  └─ 否 → 使用YAML配置（推荐）
│  └─ 使用YAML配置需要：
│     ├─ 添加@EnableConfigurationProperties注解
│     ├─ 配置application.yml
│     └─ 不要在代码中调用setFontProperties()
└─ 否（普通Java项目）
   └─ 使用代码配置
      └─ 参考JAR_INTEGRATION.md
```

### 推荐配置方式

| 项目类型 | 推荐方式 | 配置文件 | 代码配置 | 注解 |
|---------|---------|---------|---------|------|
| Spring Boot（单一配置） | YAML | ✅ | ❌ | ✅ |
| Spring Boot（多语言/多租户） | 代码 | ❌ | ✅ | ❌ |
| 普通Java项目 | 代码 | ❌ | ✅ | ❌ |

### 相关文档

- [JAR_INTEGRATION.md](JAR_INTEGRATION.md) - JAR集成和普通Java项目配置
- [SPRING_BOOT_INTEGRATION_GUIDE.md](SPRING_BOOT_INTEGRATION_GUIDE.md) - Spring Boot详细集成指南
- [FONT_CONFIGURATION.md](FONT_CONFIGURATION.md) - 字体配置详解
- [TROUBLESHOOTING.md](TROUBLESHOOTING.md) - 故障排查指南

---

**最后更新：** 2026-01-30
