# API 变更说明 API Changes and Migration Guide

## ⚠️ 重要变更 Important Changes

本文档说明了 pdf-render 库的 API 变更，帮助用户从旧版本迁移到新版本。

This document explains API changes in the pdf-render library to help users migrate from older versions.

---

## 已移除的方法 Removed Methods

### 1. `ReportService.setUseHtmlPipeline()` - 已移除

**原因 Reason**: 
- 库现在只使用 HTML/CSS 管道（Flying Saucer + OpenPDF）
- 不再支持旧的 PDFBox 直接渲染方式
- The library now exclusively uses HTML/CSS pipeline
- Old PDFBox direct rendering is no longer supported

**旧代码 Old Code**:
```java
ReportService service = new ReportService();
service.setUseHtmlPipeline(true);  // ❌ 此方法不存在 This method doesn't exist
```

**新代码 New Code**:
```java
ReportService service = new ReportService();
// ✅ 无需设置，默认使用 HTML/CSS 管道
// No need to set, HTML/CSS pipeline is used by default
```

---

## YAML 配置变更 YAML Configuration Changes

### ✅ 字体配置现在生效 Font Configuration Now Works

**问题 Problem**: 在之前的版本中，YAML 中配置的字体没有被应用到 ReportService。
In previous versions, fonts configured in YAML were not applied to ReportService.

**修复 Fixed**: 现在 `PdfRenderAutoConfiguration` 会正确读取并应用 YAML 中的字体配置。
Now `PdfRenderAutoConfiguration` correctly reads and applies font configuration from YAML.

**YAML 配置示例 YAML Configuration Example**:
```yaml
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    bold-path: classpath:/fonts/HarmonyOS_Sans_SC_Bold.ttf
    default-family: HarmonyOS Sans SC, DejaVu Sans, sans-serif
```

**等价的代码配置 Equivalent Programmatic Configuration**:
```java
ReportService service = new ReportService();

FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.setBoldFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Bold.ttf");
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, sans-serif");

service.getHtmlRenderer().setFontConfig(fontConfig);
```

---

## 迁移检查清单 Migration Checklist

### 从旧版本迁移 Migrating from Old Versions

- [ ] **移除 `setUseHtmlPipeline()` 调用**
  - 搜索代码中的 `setUseHtmlPipeline` 并删除
  - Search for `setUseHtmlPipeline` in your code and remove it

- [ ] **验证字体配置**
  - 如果使用 YAML 配置字体，验证配置已生效
  - If using YAML for fonts, verify the configuration is applied
  - 运行测试 Run test: `mvn test -Dtest=PdfRenderAutoConfigurationTest`

- [ ] **更新文档**
  - 更新项目文档中对 API 的引用
  - Update references to the API in your documentation

---

## 正确的使用方式 Correct Usage

### 1. 基本使用 Basic Usage

```java
import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.model.ReportData;
import com.mercury.pdf.render.model.ReportDataBuilder;

// 创建服务 Create service
ReportService service = new ReportService();

// 创建报告数据 Create report data
ReportData data = ReportDataBuilder.create()
    .title("My Report")
    .addSection(section)
    .build();

// 生成 PDF Generate PDF
byte[] pdf = service.generatePdf(data);
```

### 2. 配置中文字体 Configure Chinese Fonts

**方式 A: 编程方式 Programmatic**
```java
import com.mercury.pdf.render.config.FontConfig;

ReportService service = new ReportService();

// 配置字体 Configure fonts
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, sans-serif");

service.getHtmlRenderer().setFontConfig(fontConfig);

// 生成 PDF Generate PDF
byte[] pdf = service.generatePdf(data);
```

**方式 B: Spring Boot YAML**
```yaml
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    default-family: HarmonyOS Sans SC, DejaVu Sans, sans-serif
```

```java
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class MyService {
    
    @Autowired
    private ReportService reportService;  // ✅ 自动配置了字体 Fonts auto-configured
    
    public byte[] generateReport(ReportData data) throws IOException {
        return reportService.generatePdf(data);
    }
}
```

### 3. 配置模板 Configure Templates

**YAML 配置 YAML Configuration**:
```yaml
pdf-render:
  template:
    default-name: report          # 默认模板名称
    cache-enabled: true           # 启用模板缓存
    location: classpath:/templates/  # 模板位置
```

**编程配置 Programmatic Configuration**:
```java
ReportService service = new ReportService();

HtmlReportRenderer renderer = service.getHtmlRenderer();
renderer.setDefaultTemplateName("custom-template");
renderer.setCacheTemplates(true);

byte[] pdf = service.generatePdf(data);
```

---

## 常见错误 Common Errors

### 错误 1: 找不到 setUseHtmlPipeline 方法
**Error 1: Cannot resolve method setUseHtmlPipeline**

```
Error: Cannot resolve method 'setUseHtmlPipeline(boolean)' in 'ReportService'
```

**解决方案 Solution**:
移除这个调用，不再需要。Remove this call, it's no longer needed.

```java
// ❌ 错误 Wrong
service.setUseHtmlPipeline(true);

// ✅ 正确 Correct
// 不需要任何代码 No code needed
```

---

### 错误 2: YAML 配置的字体没有生效
**Error 2: Fonts configured in YAML not working**

**症状 Symptoms**: 
- 在 application.yml 中配置了字体
- PDF 中中文仍显示为方框
- Configured fonts in application.yml
- Chinese still shows as boxes in PDF

**原因 Cause**:
可能使用的是旧版本的库，字体配置没有被应用。
May be using an old version where font config wasn't applied.

**解决方案 Solution**:
更新到最新版本或使用编程方式配置：
Update to latest version or use programmatic configuration:

```java
@Bean
public ReportService reportService(PdfRenderProperties properties) {
    ReportService service = new ReportService();
    
    // 手动应用字体配置 Manually apply font config
    if (properties.getFonts().getRegularPath() != null) {
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath(properties.getFonts().getRegularPath());
        fontConfig.setDefaultFontFamily(properties.getFonts().getDefaultFamily());
        service.getHtmlRenderer().setFontConfig(fontConfig);
    }
    
    return service;
}
```

---

### 错误 3: HtmlReportRenderer 方法不可访问
**Error 3: HtmlReportRenderer methods not accessible**

**问题 Problem**:
某些方法（如 `isCacheTemplates()`）是 package-private 的。
Some methods (like `isCacheTemplates()`) are package-private.

**解决方案 Solution**:
使用公共的 setter 方法，不要尝试读取内部状态：
Use public setter methods, don't try to read internal state:

```java
// ✅ 可以设置 Can set
renderer.setCacheTemplates(true);

// ❌ 不能读取 Cannot read (package-private)
// boolean cached = renderer.isCacheTemplates();
```

---

## 验证迁移 Verify Migration

### 运行测试 Run Tests

```bash
# 运行所有测试 Run all tests
mvn test

# 运行配置测试 Run configuration tests
mvn test -Dtest=PdfRenderAutoConfigurationTest

# 运行中文字体测试 Run Chinese font tests
mvn test -Dtest=ChineseFontTest
```

### 验证字体配置 Verify Font Configuration

```bash
# 运行验证脚本 Run validation script
bash validate-chinese-fonts.sh
```

---

## 获取帮助 Getting Help

如果遇到问题，请查看：
If you encounter issues, please see:

1. **[中文快速指南 Chinese Quick Start](CHINESE_QUICKSTART.md)**
2. **[故障排除指南 Troubleshooting Guide](CHINESE_TROUBLESHOOTING.md)**
3. **[字体配置指南 Font Configuration Guide](FONT_CONFIGURATION.md)**
4. **[Spring Boot 集成指南 Spring Boot Integration Guide](SPRING_BOOT_INTEGRATION_GUIDE.md)**

---

**最后更新 Last Updated**: 2026-01-29
