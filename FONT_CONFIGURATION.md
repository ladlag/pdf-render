# 字体配置指南 / Font Configuration Guide

[English](#english) | [中文](#chinese)

---

## <a name="chinese"></a>中文指南

### 问题描述

当PDF包含中文文本时，如果没有正确配置字体，中文字符可能会显示为方框（□），这是因为：
1. 默认字体（DejaVu Sans、Arial）不包含中文字形
2. 服务器环境可能没有安装中文系统字体
3. Flying Saucer PDF渲染引擎需要显式注册字体才能正确嵌入

### 解决方案

本项目支持通过 `FontConfig` 类配置自定义字体，确保中文正确显示。

### 快速开始

#### 1. 准备字体文件

将中文字体文件（.ttf 或 .otf）放入项目的 `src/main/resources/fonts/` 目录：

```
src/main/resources/fonts/
├── HarmonyOS_SansSC_Regular.ttf  # 常规字体
├── HarmonyOS_SansSC_Bold.ttf     # 粗体
└── NotoSansCJK-Regular.otf       # 或其他CJK字体
```

**推荐字体：**
- HarmonyOS Sans SC（华为鸿蒙字体）
- Noto Sans CJK SC（思源黑体）
- Source Han Sans CN（思源黑体国内版）
- Microsoft YaHei（微软雅黑，需商业授权）

#### 2. 编程方式配置

```java
import com.finos.matcher.report.ReportService;
import com.finos.matcher.report.config.FontConfig;
import com.finos.matcher.report.model.ReportData;

// 创建报表服务
ReportService service = new ReportService();

// 配置中文字体
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_SansSC_Regular.ttf");
fontConfig.setBoldFontPath("classpath:/fonts/HarmonyOS_SansSC_Bold.ttf");
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, sans-serif");

// 应用字体配置
service.getHtmlRenderer().setFontConfig(fontConfig);

// 生成PDF
ReportData reportData = createYourReportData();  // 包含中文内容
byte[] pdfBytes = service.generatePdf(reportData);
```

#### 3. Spring Boot 配置方式

在 `application.yml` 中配置：

```yaml
pdf-render:
  fonts:
    # 常规字体路径
    regular-path: classpath:/fonts/HarmonyOS_SansSC_Regular.ttf
    # 粗体路径
    bold-path: classpath:/fonts/HarmonyOS_SansSC_Bold.ttf
    # 默认字体族（CSS font-family）
    default-family: HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif
```

在 Service 中使用：

```java
import com.finos.matcher.report.ReportService;
import com.finos.matcher.report.config.FontConfig;
import com.finos.matcher.report.config.PdfRenderProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PdfReportService {
    
    private final ReportService reportService;
    private final PdfRenderProperties properties;
    
    @Autowired
    public PdfReportService(PdfRenderProperties properties) {
        this.properties = properties;
        this.reportService = new ReportService();
        
        // 从配置创建 FontConfig
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath(properties.getFonts().getRegularPath());
        fontConfig.setBoldFontPath(properties.getFonts().getBoldPath());
        fontConfig.setDefaultFontFamily(properties.getFonts().getDefaultFamily());
        
        // 应用字体配置
        reportService.getHtmlRenderer().setFontConfig(fontConfig);
    }
    
    public byte[] generateReport(ReportData reportData) throws IOException {
        return reportService.generatePdf(reportData);
    }
}
```

### 常见问题

#### Q1: 中文仍然显示为方框怎么办？

**检查清单：**
1. 确认字体文件存在于 `src/main/resources/fonts/` 目录
2. 确认路径正确（使用 `classpath:/fonts/字体文件名.ttf`）
3. 确认字体文件支持中文字符集
4. 查看控制台是否有字体加载错误

#### Q2: 哪里可以下载免费的中文字体？

**开源字体推荐：**
- **思源黑体 (Noto Sans CJK)**
  - 下载：https://github.com/googlefonts/noto-cjk
  - 许可：SIL Open Font License

- **鸿蒙字体 (HarmonyOS Sans)**
  - 下载：https://developer.harmonyos.com/cn/design/resource
  - 许可：免费商用

- **文泉驿字体**
  - 下载：http://wenq.org/wqy2/index.cgi
  - 许可：GPL with exception

#### Q3: 如何减小PDF文件大小？

如果嵌入完整中文字体导致PDF文件过大：
1. 使用字体子集（目前不支持，计划中）
2. 选择较小的字体文件
3. 对于纯文字报告，使用OTF而不是TTF

#### Q4: 支持繁体中文吗？

支持。使用支持繁体的字体即可，如：
- Noto Sans CJK TC（思源黑体繁体）
- Microsoft JhengHei（微软正黑体）

### 技术细节

#### 字体嵌入原理

1. **Thymeleaf 模板处理**：`FontConfig` 的配置会被注入到模板变量中
2. **CSS 生成**：`@font-face` 规则动态生成，定义字体族
3. **Flying Saucer 注册**：使用 `ITextRenderer.getFontResolver().addFont()` 注册字体
4. **PDF 嵌入**：OpenPDF 将字体文件嵌入到生成的 PDF 中

#### 支持的字体格式

- TrueType (.ttf)
- OpenType (.otf)
- TrueType Collection (.ttc) - 部分支持

---

## <a name="english"></a>English Guide

### Problem Description

When PDFs contain Chinese text, characters may appear as boxes (□) without proper font configuration because:
1. Default fonts (DejaVu Sans, Arial) don't include Chinese glyphs
2. Server environments may not have Chinese system fonts installed
3. Flying Saucer PDF rendering engine requires explicit font registration for embedding

### Solution

This project supports custom font configuration via the `FontConfig` class to ensure proper Chinese character rendering.

### Quick Start

#### 1. Prepare Font Files

Place Chinese font files (.ttf or .otf) in `src/main/resources/fonts/`:

```
src/main/resources/fonts/
├── HarmonyOS_SansSC_Regular.ttf  # Regular font
├── HarmonyOS_SansSC_Bold.ttf     # Bold font
└── NotoSansCJK-Regular.otf       # Or other CJK fonts
```

**Recommended Fonts:**
- HarmonyOS Sans SC (Huawei HarmonyOS Font)
- Noto Sans CJK SC (Source Han Sans)
- Source Han Sans CN
- Microsoft YaHei (requires commercial license)

#### 2. Programmatic Configuration

```java
import com.finos.matcher.report.ReportService;
import com.finos.matcher.report.config.FontConfig;
import com.finos.matcher.report.model.ReportData;

// Create report service
ReportService service = new ReportService();

// Configure Chinese fonts
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_SansSC_Regular.ttf");
fontConfig.setBoldFontPath("classpath:/fonts/HarmonyOS_SansSC_Bold.ttf");
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, sans-serif");

// Apply font configuration
service.getHtmlRenderer().setFontConfig(fontConfig);

// Generate PDF
ReportData reportData = createYourReportData();  // With Chinese content
byte[] pdfBytes = service.generatePdf(reportData);
```

#### 3. Spring Boot Configuration

Configure in `application.yml`:

```yaml
pdf-render:
  fonts:
    # Regular font path
    regular-path: classpath:/fonts/HarmonyOS_SansSC_Regular.ttf
    # Bold font path
    bold-path: classpath:/fonts/HarmonyOS_SansSC_Bold.ttf
    # Default font family (CSS font-family)
    default-family: HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif
```

Use in Service:

```java
import com.finos.matcher.report.ReportService;
import com.finos.matcher.report.config.FontConfig;
import com.finos.matcher.report.config.PdfRenderProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PdfReportService {
    
    private final ReportService reportService;
    private final PdfRenderProperties properties;
    
    @Autowired
    public PdfReportService(PdfRenderProperties properties) {
        this.properties = properties;
        this.reportService = new ReportService();
        
        // Create FontConfig from properties
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath(properties.getFonts().getRegularPath());
        fontConfig.setBoldFontPath(properties.getFonts().getBoldPath());
        fontConfig.setDefaultFontFamily(properties.getFonts().getDefaultFamily());
        
        // Apply font configuration
        reportService.getHtmlRenderer().setFontConfig(fontConfig);
    }
    
    public byte[] generateReport(ReportData reportData) throws IOException {
        return reportService.generatePdf(reportData);
    }
}
```

### FAQ

#### Q1: Chinese characters still showing as boxes?

**Checklist:**
1. Verify font files exist in `src/main/resources/fonts/`
2. Verify path is correct (use `classpath:/fonts/font-file.ttf`)
3. Verify font file supports Chinese character set
4. Check console for font loading errors

#### Q2: Where to download free Chinese fonts?

**Open Source Fonts:**
- **Noto Sans CJK**
  - Download: https://github.com/googlefonts/noto-cjk
  - License: SIL Open Font License

- **HarmonyOS Sans**
  - Download: https://developer.harmonyos.com/cn/design/resource
  - License: Free for commercial use

- **WenQuanYi Fonts**
  - Download: http://wenq.org/wqy2/index.cgi
  - License: GPL with exception

#### Q3: How to reduce PDF file size?

If embedding full Chinese fonts makes PDFs too large:
1. Use font subsetting (not yet supported, planned)
2. Choose smaller font files
3. For text-only reports, use OTF instead of TTF

#### Q4: Does it support Traditional Chinese?

Yes. Use fonts that support Traditional Chinese, such as:
- Noto Sans CJK TC
- Microsoft JhengHei

### Technical Details

#### Font Embedding Process

1. **Thymeleaf Processing**: `FontConfig` is injected as template variables
2. **CSS Generation**: `@font-face` rules are dynamically generated
3. **Flying Saucer Registration**: Fonts registered using `ITextRenderer.getFontResolver().addFont()`
4. **PDF Embedding**: OpenPDF embeds font files in generated PDF

#### Supported Font Formats

- TrueType (.ttf)
- OpenType (.otf)
- TrueType Collection (.ttc) - partial support
