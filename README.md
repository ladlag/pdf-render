# pdf-render

A Java-based PDF report generation library that uses an HTML/CSS template approach with Flying Saucer and OpenPDF for reliable, stable table pagination.

## Overview

This project generates multi-section PDF reports with:
- Cover page with title and metadata
- Section 1: Detailed analysis tables with grouped blocks
- Section 2: Analysis paragraphs
- Section 3: Summary tables and charts
- Section 4: Notice and metadata

## Key Features

### HTML/CSS → PDF Pipeline (Default)
The project now uses **Flying Saucer + OpenPDF** for converting HTML/CSS to PDF, providing:
- ✅ **Stable table pagination** - no more lost rows at page breaks
- ✅ **Repeated table headers** - headers automatically repeat on new pages using `<thead>`
- ✅ **Page break control** - CSS `page-break-inside: avoid` prevents row splitting
- ✅ **Easy customization** - modify templates and styles without touching Java code
- ✅ **Professional styling** - CSS-based styling with @page rules and custom fonts

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
import com.finos.matcher.report.ReportService;
import com.finos.matcher.report.config.FontConfig;

ReportService service = new ReportService();
service.setUseHtmlPipeline(true);

// ✅ REQUIRED for Chinese/CJK character display
// Option 1: Using HarmonyOS Sans SC (recommended for Chinese)
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, sans-serif");

// Option 2: Using Noto Sans CJK SC (alternative)
// fontConfig.setRegularFontPath("classpath:/fonts/NotoSansCJKsc-Regular.otf");
// fontConfig.setDefaultFontFamily("Noto Sans CJK SC, DejaVu Sans, sans-serif");

service.getHtmlRenderer().setFontConfig(fontConfig);

// Now you can generate PDFs with Chinese text
byte[] pdf = service.generatePdf(reportData);
```

**Quick Demo:** Run the demonstration to see the difference
```bash
java -cp "target/classes:..." com.finos.matcher.report.ChineseFontConfigurationDemo
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
    <groupId>com.finos.matcher</groupId>
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

import com.finos.matcher.report.ReportService;
import com.finos.matcher.report.config.PdfRenderProperties;
import com.finos.matcher.report.model.ReportData;
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

import com.finos.matcher.report.ReportService;
import com.finos.matcher.report.model.ReportData;
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
import com.finos.matcher.report.model.ReportData;
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
import com.finos.matcher.report.ReportService;
import com.finos.matcher.report.model.*;

// Create report data
ReportData reportData = new ReportData();
reportData.setTitle("Annual Financial Report");
reportData.setReportDate("2024-01-27");

// Add table blocks, charts, etc.
// ...

// Generate PDF
ReportService service = new ReportService();
byte[] pdfBytes = service.generatePdf(reportData);

// Save to file
Files.write(Paths.get("report.pdf"), pdfBytes);
```

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

### Switching Between Implementations

The library now defaults to the HTML/CSS pipeline. If you need to use the legacy PDFBox implementation (not recommended due to pagination issues):

```java
ReportService service = new ReportService();
service.setUseHtmlPipeline(false); // Use legacy PDFBox (deprecated)
byte[] pdfBytes = service.generatePdf(reportData);
```

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
import com.finos.matcher.report.config.FontConfig;

// Configure fonts
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_SansSC_Regular.ttf");
fontConfig.setBoldFontPath("classpath:/fonts/HarmonyOS_SansSC_Bold.ttf");

// Apply to renderer
service.getHtmlRenderer().setFontConfig(fontConfig);
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

Charts are generated using JFreeChart and embedded as base64-encoded PNG images:

```java
ChartData chart = new ChartData("Sales by Quarter", "bar", data);
reportData.setCharts(Arrays.asList(chart));
```

Supported chart types:
- `bar` - Bar chart
- `pie` - Pie chart

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

