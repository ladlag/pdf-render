# 字体配置指南 / Font Configuration Guide

[English](#english) | [中文](#chinese)

---

## <a name="chinese"></a>中文指南

### 问题描述

当PDF包含中文文本时，如果没有正确配置字体，中文字符可能会显示为方框（□），这是因为：
1. 默认字体（DejaVu Sans、Arial）不包含中文字形
2. 服务器环境可能没有安装中文系统字体
3. Flying Saucer PDF渲染引擎需要显式注册字体才能正确嵌入
4. **JFreeChart 图表引擎也需要中文字体才能正确渲染图表标签**

### 解决方案

本项目支持通过 `FontConfig` 类配置自定义字体，确保中文正确显示。
**v1.0.1+ 版本自动同步字体配置到图表引擎，一次配置，全局生效。**

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
import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.model.ReportData;

// 创建报表服务
ReportService service = new ReportService();

        // 配置中文字体
        FontConfig fontConfig = new FontConfig();
fontConfig.

        setRegularFontPath("classpath:/fonts/NotoSansCJKsc-Regular.otf");
// 重要：font family 必须匹配字体文件的内部名称
fontConfig.

        setDefaultFontFamily("Noto Sans CJK SC, DejaVu Sans, sans-serif");

// 应用字体配置
service.

        getHtmlRenderer().

        setFontConfig(fontConfig);

        // 生成PDF
        ReportData reportData = createYourReportData();  // 包含中文内容
        byte[] pdfBytes = service.generatePdf(reportData);
```

**重要提示：**
- `defaultFontFamily` 必须设置为字体文件的内部字体名称（不是文件名）
- Noto Sans CJK SC 的内部名称是 "Noto Sans CJK SC"
- HarmonyOS Sans SC 的内部名称是 "HarmonyOS Sans SC"
- 思源黑体的内部名称是 "Source Han Sans SC"

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
import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.config.PdfRenderProperties;
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
5. **检查控制台是否显示 "✓ Chart font loaded successfully"（v1.0.1+）**

**如果图表中的中文显示为方框：**
- 确保调用了 `setFontConfig()` 方法
- v1.0.1+ 版本会自动配置图表字体
- 控制台应显示："✓ Chart font loaded successfully: classpath:/fonts/xxx.ttf"

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

1. **Flying Saucer 字体注册**：使用 `ITextRenderer.getFontResolver().addFont(fontPath, "Identity-H", true)` 注册字体
   - `fontPath`: 字体文件路径（支持 classpath: 和文件系统路径）
   - `"Identity-H"`: Unicode 编码方式，对 CJK 字符支持至关重要
   - `true`: 将字体嵌入到 PDF 文件中

2. **CSS 字体引用**：模板中使用 `font-family` 引用字体的内部名称
   - 不需要 `@font-face` CSS 声明（Flying Saucer 不支持 classpath: URLs）
   - 字体通过 addFont() 注册后自动可用
   
3. **PDF 嵌入**：OpenPDF 将完整字体文件嵌入到生成的 PDF 中
   - 确保 PDF 在任何设备上都能正确显示
   - 文件大小会增加（中文字体通常 10-20MB）

#### Identity-H 编码的重要性

**Identity-H** 是 Adobe 定义的 CMap（Character Map）编码：
- 允许 Unicode 字符直接映射到字体字形
- 是 CJK 字体的标准编码方式
- **没有 Identity-H 编码，中文字符会被过滤掉，无法显示**

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
import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.model.ReportData;

// Create report service
ReportService service = new ReportService();

        // Configure Chinese fonts
        FontConfig fontConfig = new FontConfig();
fontConfig.

        setRegularFontPath("classpath:/fonts/NotoSansCJKsc-Regular.otf");
// IMPORTANT: defaultFontFamily must match the font's internal name
fontConfig.

        setDefaultFontFamily("Noto Sans CJK SC, DejaVu Sans, sans-serif");

// Apply font configuration
service.

        getHtmlRenderer().

        setFontConfig(fontConfig);

        // Generate PDF
        ReportData reportData = createYourReportData();  // With Chinese content
        byte[] pdfBytes = service.generatePdf(reportData);
```

**Important Notes:**
- `defaultFontFamily` must be set to the font file's internal font name (not the filename)
- Noto Sans CJK SC internal name is "Noto Sans CJK SC"
- HarmonyOS Sans SC internal name is "HarmonyOS Sans SC"  
- Source Han Sans internal name is "Source Han Sans SC"

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
import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.config.PdfRenderProperties;
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

#### Q1: Chinese characters still showing as boxes or missing?

**Checklist:**
1. Verify font files exist in `src/main/resources/fonts/`
2. Verify path is correct (use `classpath:/fonts/font-file.otf`)
3. Verify `defaultFontFamily` matches the font's internal name (e.g., "Noto Sans CJK SC")
4. Ensure the font file supports Chinese character set
5. Check console for font loading errors

**Common issue:** If `defaultFontFamily` doesn't match the font's internal name, Chinese characters will be filtered out during PDF generation.

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

1. **Flying Saucer Font Registration**: Uses `ITextRenderer.getFontResolver().addFont(fontPath, "Identity-H", true)` to register fonts
   - `fontPath`: Font file path (supports classpath: and file system paths)
   - `"Identity-H"`: Unicode encoding, crucial for CJK character support
   - `true`: Embed the font in the PDF file

2. **CSS Font Reference**: Templates reference fonts by their internal name using `font-family`
   - No `@font-face` CSS declarations needed (Flying Saucer doesn't support classpath: URLs)
   - Fonts registered via addFont() are automatically available

3. **PDF Embedding**: OpenPDF embeds the complete font file in the generated PDF
   - Ensures PDFs display correctly on any device
   - File size increases (Chinese fonts typically 10-20MB)

#### Importance of Identity-H Encoding

**Identity-H** is a CMap (Character Map) encoding defined by Adobe:
- Allows Unicode characters to map directly to font glyphs
- Standard encoding for CJK fonts
- **Without Identity-H encoding, Chinese characters are filtered out and won't display**

#### Supported Font Formats

- TrueType (.ttf)
- OpenType (.otf)
- TrueType Collection (.ttc) - partial support
