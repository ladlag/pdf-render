# pdf-render

A Java-based PDF report generation library using clean HTML/CSS architecture with Flying Saucer and OpenPDF.

---

## 🚨 Chinese Display Issues?

**If Chinese characters show as boxes (□) in your PDF:**

📖 **[Chinese Quick Fix Guide](CHINESE_QUICKSTART.md)** (中文显示快速解决指南)

Or run the validation script:
```bash
./validate-chinese-fonts.sh
```

---

## Overview

This project generates multi-section PDF reports with **unlimited dynamic sections**:
- Cover page with title and metadata
- Flexible sections with any combination of:
  - Titles and subtitles
  - Paragraphs and text content
  - Tables with automatic pagination
  - Charts (bar, pie, line, etc.)
  - Custom HTML content

## Key Features

### Clean HTML/CSS Architecture
The library uses **Flying Saucer + OpenPDF** for converting HTML/CSS to PDF, providing:
- ✅ **Unlimited sections** - add as many sections as needed
- ✅ **Stable table pagination** - no lost rows at page breaks
- ✅ **Repeated table headers** - headers automatically repeat on new pages using `<thead>`
- ✅ **Page break control** - CSS `page-break-inside: avoid` prevents row splitting
- ✅ **Easy customization** - modify templates and styles without touching Java code
- ✅ **Professional styling** - CSS-based styling with @page rules and custom fonts
- ✅ **Clean codebase** - no legacy code or deprecated implementations

### Architecture

```
ReportData → HtmlReportRenderer → Thymeleaf Template → HTML → Flying Saucer → PDF
```

1. **Data Preparation**: `ReportData` model with all report content
2. **Template Rendering**: Thymeleaf processes `report.html` template
3. **Chart Embedding**: Charts rendered as images and embedded as base64 data URIs
4. **PDF Generation**: Flying Saucer converts HTML/CSS to PDF with OpenPDF

## Getting Started

### ⚠️ Chinese/CJK Font Configuration (Important!)

**If your PDF needs to display Chinese/Japanese/Korean characters, you MUST configure FontConfig, otherwise CJK text will show as boxes (□)**

```java
import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.config.FontConfig;

ReportService service = new ReportService();
service.

setUseHtmlPipeline(true);

// ✅ REQUIRED for Chinese/CJK character display
// Option 1: Using HarmonyOS Sans SC (recommended for Chinese)
FontConfig fontConfig = new FontConfig();
fontConfig.

setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.

setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, sans-serif");

// Option 2: Using Noto Sans CJK SC (alternative)
// fontConfig.setRegularFontPath("classpath:/fonts/NotoSansCJKsc-Regular.otf");
// fontConfig.setDefaultFontFamily("Noto Sans CJK SC, DejaVu Sans, sans-serif");

service.

getHtmlRenderer().

setFontConfig(fontConfig);

// Now you can generate PDFs with Chinese text
byte[] pdf = service.generatePdf(reportData);
```

**Quick Demo:** Run the demonstration to see the difference
```bash
java -cp "target/classes:..." com.mercury.pdf.render.ChineseFontConfigurationDemo
```

**Documentation:**
- 📖 [Complete Font Configuration Guide](FONT_CONFIGURATION.md)
- 📖 [How to Verify Font Configuration](HOW_TO_VERIFY_CHINESE_FONTS.md)
- 📖 [Technical Analysis](TECHNICAL_ANALYSIS_CHINESE_FONTS.md)

### Prerequisites
- Java 8 or higher (JDK 1.8+)
- Maven 3.6+

### Build
```bash
mvn clean install
```

This will generate:
- `pdf-render-1.0.0-SNAPSHOT.jar` - Main library JAR
- `pdf-render-1.0.0-SNAPSHOT-sources.jar` - Sources JAR
- `pdf-render-1.0.0-SNAPSHOT-javadoc.jar` - Javadoc JAR

### Run Tests
```bash
mvn test
```

Test PDFs will be generated in the `test-output/` directory for inspection.

## Integration with Spring Boot

**📘 完整集成指南:** 
- [Spring Boot 集成指南（中文）](SPRING_BOOT_INTEGRATION_GUIDE.md) - 详细的配置说明和完整代码示例
- [Spring Boot Examples](SPRING_BOOT_EXAMPLES.md) - 快速开始示例和项目结构

### Maven Dependency

To use this library in your Spring Boot project, add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.mercury</groupId>
    <artifactId>pdf-render</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

For local development, first build and install the library to your local Maven repository:

```bash
cd pdf-render
mvn clean install
```

### Configuration with application.yml

The library supports configuration via Spring Boot's `application.yml` file. This allows you to customize templates, output directories, fonts, and other settings without changing code.

Create or update `src/main/resources/application.yml` in your Spring Boot project:

```yaml
pdf-render:
  # Template configuration
  template:
    # Location of template files (default: classpath:/templates/)
    location: classpath:/templates/
    # Default template name without .html extension (default: report)
    default-name: report
    # Whether to cache templates for better performance (default: true)
    # Set to false in development for hot reload
    cache-enabled: true
  
  # Output configuration (optional)
  output:
    # Directory for generated PDF files
    directory: /var/pdfs
    # Whether to automatically save PDFs to the directory (default: false)
    save-to-directory: false
  
  # Font configuration (optional)
  fonts:
    # Path to custom regular font file
    regular-path: classpath:/fonts/custom-regular.ttf
    # Path to custom bold font file
    bold-path: classpath:/fonts/custom-bold.ttf
    # Path to CJK (Chinese/Japanese/Korean) font file for Chinese support
    cjk-path: classpath:/fonts/NotoSansCJK-Regular.otf
    # Default font family CSS
    default-family: DejaVu Sans, Arial, sans-serif
    # CJK font family CSS
    cjk-family: Noto Sans CJK, SimSun, sans-serif
```

**Note**: An example configuration file is included at `src/main/resources/application.yml.example`.

### Using Configuration in Your Service

If you want to use the configuration properties in your service, you can inject them:

```java
package com.example.myapp.service;

import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.config.PdfRenderProperties;
import com.mercury.pdf.render.model.ReportData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.FileOutputStream;
import java.nio.file.Paths;

@Service
public class PdfReportService {

    private final ReportService reportService;
    private final PdfRenderProperties properties;

    @Autowired
    public PdfReportService(PdfRenderProperties properties) {
        this.properties = properties;
        this.reportService = new ReportService();

        // Configure the service with properties from application.yml
        reportService.getHtmlRenderer().setDefaultTemplateName(
                properties.getTemplate().getDefaultName()
        );
        reportService.getHtmlRenderer().setCacheTemplates(
                properties.getTemplate().isCacheEnabled()
        );
    }

    public byte[] generateReport(ReportData reportData) throws IOException {
        byte[] pdfBytes = reportService.generatePdf(reportData);

        // Optionally save to configured directory
        if (properties.getOutput().isSaveToDirectory()
                && properties.getOutput().getDirectory() != null) {
            String outputPath = Paths.get(
                    properties.getOutput().getDirectory(),
                    "report-" + System.currentTimeMillis() + ".pdf"
            ).toString();

            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                fos.write(pdfBytes);
            }
        }

        return pdfBytes;
    }

    public byte[] generateInvoice(ReportData reportData) throws IOException {
        return reportService.generatePdf(reportData, "invoice");
    }
}
```

### Spring Boot Service Example (Simple)

Create a service in your Spring Boot application:

```java
package com.example.myapp.service;

import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.model.ReportData;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class PdfReportService {

    private final ReportService reportService = new ReportService();

    public byte[] generateReport(ReportData reportData) throws IOException {
        return reportService.generatePdf(reportData);
    }

    public byte[] generateInvoice(ReportData reportData) throws IOException {
        return reportService.generatePdf(reportData, "invoice");
    }
}
```

### Spring Boot Controller Example

Create a REST controller to expose PDF generation:

```java
package com.example.myapp.controller;

import com.example.myapp.service.PdfReportService;
import com.mercury.pdf.render.model.ReportData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private PdfReportService pdfReportService;

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generateReport(@RequestBody ReportData reportData) throws IOException {
        byte[] pdfBytes = pdfReportService.generateReport(reportData);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "report.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
```

### Compatibility

- **Java**: Compatible with JDK 8 and higher
- **Spring Boot**: Compatible with Spring Boot 2.x and 3.x (when using JDK 17+)
- **Dependencies**: All transitive dependencies are compatible with JDK 8

## Usage

### Basic Example

```java
import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.model.ReportData;

// Create report data
ReportData reportData = new ReportData();
reportData.

        setTitle("Annual Financial Report");
reportData.

        setReportDate("2024-01-27");

// Add table blocks, charts, etc.
// ...

        // Generate PDF
        ReportService service = new ReportService();
        byte[] pdfBytes = service.generatePdf(reportData);

// Save to file
Files.

        write(Paths.get("report.pdf"),pdfBytes);
```

### Using Unlimited Dynamic Sections

The library supports creating reports with **unlimited sections** using the flexible `Section` model:

```java
import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.model.*;
import java.util.Arrays;

// Assume tables and charts are defined (see FlexibleReportDemo.java for details)
// TableData financialTable = ...
// ChartData revenueChart = ...

// Create report with unlimited dynamic sections using the builder
ReportData report = ReportDataBuilder.create()
    .title("Comprehensive Business Report")
    .subtitle("Multi-Section Analysis")
    .reportDate("2024-12-31")
    
    // Add as many sections as needed!
    .addSection(new Section("Executive Summary")
        .withSubtitle("Key Highlights")
        .addParagraph("Overview of key findings and results..."))
    
    .addSection(new Section("Financial Performance")
        .addTable(financialTable)
        .addChart(revenueChart))
    
    .addSection(new Section("Market Analysis")
        .addParagraph("Market trends and competitive analysis...")
        .addChart(marketShareChart))
    
    .addSection(new Section("Operations")
        .addParagraph("Operational efficiency metrics...")
        .addTable(operationsTable))
    
    .addSection(new Section("Risk Assessment")
        .addParagraph("Risk factors and mitigation strategies..."))
    
    .addSection(new Section("Recommendations")
        .withCustomContent("<ul><li>Strategy 1</li><li>Strategy 2</li></ul>"))
    
    // Add even more sections as needed!
    
    .build();

// Generate PDF with unlimited sections
ReportService service = new ReportService();
byte[] pdfBytes = service.generatePdf(report);
```

**Key Features:**
- ✅ **Unlimited sections** - Add as many sections as your report needs
- ✅ **Flexible content** - Each section can contain paragraphs, tables, charts, or custom HTML
- ✅ **Fluent API** - Easy-to-use builder pattern for constructing reports
- ✅ **Clean architecture** - single implementation, no legacy code

**📖 See [FlexibleReportDemo.java](src/main/java/com/mercury/pdf/render/FlexibleReportDemo.java) for complete working examples with table and chart creation**

### Using Custom Templates

You can use different templates for different report types:

```java
// Use default template
byte[] pdf = service.generatePdf(reportData);

// Use invoice template
byte[] invoicePdf = service.generatePdf(reportData, "invoice");

// Use your custom template
byte[] customPdf = service.generatePdf(reportData, "my-template");
```

**📖 For detailed template customization guide, see [TEMPLATE_GUIDE.md](TEMPLATE_GUIDE.md)**

## Customizing Templates and Styles

### Template Location
HTML template: `src/main/resources/templates/report.html`

### Modifying Styles
Edit the `<style>` section in `report.html` to customize:

```css
/* Table styling */
table {
    width: 100%;
    border-collapse: collapse;
}

thead {
    display: table-header-group; /* Repeat on each page */
}

tr {
    page-break-inside: avoid; /* Don't split rows across pages */
}
```

### Adding Custom Fonts

**For Chinese/Japanese/Korean (CJK) Support, see [FONT_CONFIGURATION.md](FONT_CONFIGURATION.md)**

The project supports custom font configuration for proper CJK character rendering:

```java
import com.mercury.pdf.render.config.FontConfig;

// Configure fonts
FontConfig fontConfig = new FontConfig();
fontConfig.

        setRegularFontPath("classpath:/fonts/HarmonyOS_SansSC_Regular.ttf");
fontConfig.

        setBoldFontPath("classpath:/fonts/HarmonyOS_SansSC_Bold.ttf");

// Apply to renderer
service.

        getHtmlRenderer().

        setFontConfig(fontConfig);
```

Or via Spring Boot `application.yml`:

```yaml
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_SansSC_Regular.ttf
    bold-path: classpath:/fonts/HarmonyOS_SansSC_Bold.ttf
    default-family: HarmonyOS Sans SC, DejaVu Sans, sans-serif
```

For manual template-level font configuration:

1. Place font files in `src/main/resources/fonts/`
2. Add `@font-face` rules in the template (note: fonts configured via `FontConfig` are automatically embedded):

```css
@font-face {
    font-family: 'CustomFont';
    src: url('classpath:/fonts/custom-font.ttf');
}

body {
    font-family: 'CustomFont', Arial, sans-serif;
}
```

### Page Configuration

Control page size, margins, and headers/footers:

```css
@page {
    size: A4;
    margin: 1.5cm;
    
    @bottom-center {
        content: counter(page);
    }
}
```

## Table Pagination

### The Problem (Old PDFBox Implementation)
The previous PDFBox-based implementation used manual table pagination logic that could lose rows when page breaks occurred mid-table. This was due to:
- Naive page height calculations
- Manual content stream management
- Race conditions during page creation

### The Solution (HTML/CSS Pipeline)
The new HTML/CSS approach uses standard web layout algorithms:

```html
<table>
    <thead>  <!-- Automatically repeats on each page -->
        <tr><th>Header 1</th><th>Header 2</th></tr>
    </thead>
    <tbody>
        <tr>...</tr>  <!-- page-break-inside: avoid -->
    </tbody>
</table>
```

CSS rules ensure stability:
```css
table {
    page-break-inside: auto; /* Allow table to span pages */
}

thead {
    display: table-header-group; /* Repeat header */
}

tr {
    page-break-inside: avoid; /* Keep rows together */
}
```

## Chart Generation

Charts are generated using JFreeChart and embedded as base64-encoded PNG images. The library now supports multiple chart types with extensive customization options.

### Basic Chart Usage

```java
Map<String, Double> data = new LinkedHashMap<>();
data.put("Q1", 200000.0);
data.put("Q2", 230000.0);
data.put("Q3", 260000.0);
data.put("Q4", 290000.0);

ChartData chart = new ChartData("Sales by Quarter", "bar", data);
reportData.setCharts(Arrays.asList(chart));
```

### Supported Chart Types

- `bar` - Bar chart
- `pie` - Pie chart (supports 3D with configuration)
- `line` - Line chart
- `area` - Area chart
- `stackedbar` - Stacked bar chart

### Chart Customization

Use `ChartConfig` to customize chart appearance:

```java
import com.mercury.pdf.render.model.ChartConfig;

import java.awt.Color;

// Create chart configuration
ChartConfig config = new ChartConfig();

// Set custom dimensions
config.

        setWidth(800);
config.

        setHeight(400);

// Set custom colors
config.

        setColors(Arrays.asList(
                new Color(52, 152,219),   // Blue
    new

        Color(46,204,113),   // Green
    new

        Color(155,89,182),   // Purple
    new

        Color(241,196,15)    // Yellow
));

// Configure axis labels
        config.

        setXAxisLabel("Quarter");
config.

        setYAxisLabel("Revenue ($)");

// Configure display options
config.

        setShowLegend(true);
config.

        setShowGridLines(true);
config.

        setShow3D(false);  // 3D effect (for pie charts)
config.

        setBackgroundColorHex("#f8f9fa");

        // Apply configuration to chart
        ChartData chart = new ChartData("Quarterly Revenue", "bar", data);
chart.

        setConfig(config);
```

### Chart Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `width` | Integer | 500 | Chart width in pixels |
| `height` | Integer | 300 | Chart height in pixels |
| `colors` | List<Color> | null | Custom color palette for data series |
| `showLegend` | Boolean | true | Show/hide chart legend |
| `show3D` | Boolean | false | Enable 3D effect (for pie charts) |
| `backgroundColorHex` | String | null | Chart background color (hex format) |
| `showGridLines` | Boolean | true | Show/hide grid lines (category charts) |
| `xAxisLabel` | String | "Category" | X-axis label |
| `yAxisLabel` | String | "Value" | Y-axis label |

### Examples

#### Line Chart with Custom Styling

```java
Map<String, Double> growthData = new LinkedHashMap<>();
growthData.put("Q1", 15.0);
growthData.put("Q2", 18.5);
growthData.put("Q3", 22.3);
growthData.put("Q4", 28.7);

ChartConfig lineConfig = new ChartConfig();
lineConfig.setColors(Arrays.asList(new Color(231, 76, 60)));
lineConfig.setXAxisLabel("Quarter");
lineConfig.setYAxisLabel("Growth Rate (%)");
lineConfig.setShowGridLines(true);

ChartData lineChart = new ChartData("Growth Trend", "line", growthData);
lineChart.setConfig(lineConfig);
```

#### 3D Pie Chart

```java
Map<String, Double> expenseData = new LinkedHashMap<>();
expenseData.put("Salaries", 40.0);
expenseData.put("Operations", 30.0);
expenseData.put("Marketing", 20.0);
expenseData.put("Other", 10.0);

ChartConfig pieConfig = new ChartConfig();
pieConfig.setShow3D(true);
pieConfig.setWidth(500);
pieConfig.setHeight(400);

ChartData pieChart = new ChartData("Expense Distribution", "pie", expenseData);
pieChart.setConfig(pieConfig);
```

#### Area Chart with Background Color

```java
ChartConfig areaConfig = new ChartConfig();
areaConfig.setColors(Arrays.asList(new Color(46, 204, 113)));
areaConfig.setBackgroundColorHex("#f8f9fa");
areaConfig.setXAxisLabel("Month");
areaConfig.setYAxisLabel("Sales");

ChartData areaChart = new ChartData("Monthly Sales", "area", salesData);
areaChart.setConfig(areaConfig);
```

Charts are automatically converted to base64 images and embedded in the HTML:
```html
<img src="data:image/png;base64,iVBORw0KGgoAAAANS..." alt="Chart"/>
```

## Dependencies

- **Flying Saucer (9.1.22)** - HTML/CSS rendering engine
- **OpenPDF** - PDF generation (via Flying Saucer)
- **Thymeleaf (3.1.1)** - Template engine
- **JFreeChart (1.5.4)** - Chart generation
- **PDFBox (2.0.29)** - Legacy implementation (deprecated)

## Migration from PDFBox

If you were using the old PDFBox implementation:

1. **No code changes needed** - The API remains the same (`generatePdf` method)
2. **Default behavior changed** - Now uses HTML pipeline by default
3. **Better results** - Tables no longer lose rows at page breaks
4. **Template-based** - Easier to customize without code changes

## Testing

Run the comprehensive test suite:
```bash
mvn test
```

Tests include:
- PDFBox implementation (legacy, showing pagination issue)
- HTML pipeline implementation (stable pagination)
- Default behavior verification
- Chart generation
- Multi-section reports

## Project Structure

```
pdf-render/
├── pom.xml
├── README.md
├── src/
│   ├── main/
│   │   ├── java/com/finos/matcher/report/
│   │   │   ├── model/
│   │   │   │   ├── ReportData.java
│   │   │   │   ├── TableData.java
│   │   │   │   ├── TableBlock.java
│   │   │   │   └── ChartData.java
│   │   │   ├── ReportService.java           # Main API
│   │   │   ├── HtmlReportRenderer.java      # HTML/CSS pipeline
│   │   │   ├── ChartRenderer.java           # Chart generation
│   │   │   └── TableRenderer.java           # Legacy PDFBox (deprecated)
│   │   └── resources/
│   │       ├── templates/
│   │       │   └── report.html              # Thymeleaf template
│   │       └── fonts/                        # Custom fonts (optional)
│   └── test/
│       └── java/com/finos/matcher/report/
│           └── ReportServiceTest.java
```

## Performance

- **HTML Pipeline**: ~90KB for 200-row report
- **PDFBox**: ~85KB (but with potential data loss)
- Rendering time: < 5 seconds for typical reports

## Troubleshooting

### Missing Fonts
If you see font-related warnings, either:
1. Add custom fonts to `src/main/resources/fonts/` and configure via `@font-face`
2. Use system fonts: `font-family: 'DejaVu Sans', Arial, sans-serif;`

### Page Breaks in Wrong Places
Adjust CSS `page-break-*` properties:
```css
.table-block {
    page-break-inside: avoid; /* Keep entire block together */
}

h2 {
    page-break-after: avoid; /* Keep heading with content */
}
```

### Charts Not Displaying
Ensure:
1. Chart data is not null
2. JFreeChart is properly configured
3. Base64 encoding is working (check generated HTML)

## License

This project is open source and available under standard licensing terms.

## Contributing

Contributions are welcome! Please ensure:
1. All tests pass (`mvn test`)
2. Code follows existing style
3. New features include tests
4. Documentation is updated

