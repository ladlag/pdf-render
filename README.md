# pdf-render

A Java PDF report generation library using clean HTML/CSS architecture with Flying Saucer and OpenPDF.

**Based on JDK 8** - Fully compatible with Java 8 and higher.

---

## 🔴 Chinese Characters Display as Boxes (□)?

**If Chinese characters appear as boxes in your PDF, check this immediately:**

👉 **[Complete Solution - CHINESE_DISPLAY_COMPLETE_SOLUTION.md](CHINESE_DISPLAY_COMPLETE_SOLUTION.md)** 👈

**Or see quick guide:**
- **[3-Step Quick Start - CHINESE_QUICKSTART.md](CHINESE_QUICKSTART.md)**

**Environment-Specific Guides:**
- **[Windows Compatibility - WINDOWS_COMPATIBILITY.md](WINDOWS_COMPATIBILITY.md)** - v1.0.1+ auto-handles path compatibility
- **[Container Deployment - CONTAINER_DEPLOYMENT.md](CONTAINER_DEPLOYMENT.md)** - Docker/Kubernetes deployment guide 🐳

**Run diagnostic tools to find the issue:**
```bash
# Verify font files and get correct configuration
mvn compile exec:java -Dexec.mainClass="com.mercury.pdf.render.FontFileVerifier"

# Test if font configuration is correct
mvn compile exec:java -Dexec.mainClass="com.mercury.pdf.render.MinimalFontTest"
```

**Common Issue Diagnostic Docs:**
- [No Font Registration Logs](NO_FONT_LOGS_TROUBLESHOOTING.md)
- [Logs Show But Still Boxes](FONT_REGISTERED_BUT_BOXES.md)
- [Font File Size Issue](FONT_SIZE_ISSUE.md)

**TL;DR: You MUST configure FontConfig to display Chinese!** See quick guide for details.

---

## Overview

This library generates professional multi-section PDF reports with:
- Cover pages with title, metadata, and custom notices
- **Unlimited dynamic sections** with flexible content composition
- Tables with automatic pagination and repeated headers
- Charts (bar, pie, line, area, stacked bar)
- Custom HTML content
- **Full Chinese/CJK font support**

### Key Features

✅ **Clean HTML/CSS Architecture** - Flying Saucer + OpenPDF pipeline  
✅ **Unlimited Sections** - No artificial limits on report size  
✅ **Stable Table Pagination** - No lost rows at page breaks  
✅ **Spring Boot Auto-Configuration** - Zero-config integration  
✅ **Template System** - 5 built-in professional templates  
✅ **JDK 8 Compatible** - Works with Java 8+  

---

## Quick Start

### Prerequisites
- **JDK 1.8+** (Java 8 or higher)
- **Maven 3.6+**

### Build and Install

```bash
git clone <repository-url>
cd pdf-render
mvn clean install
```

This generates:
- `pdf-render-1.0.1.jar` - Main library
- `pdf-render-1.0.1-sources.jar` - Source code
- `pdf-render-1.0.1-javadoc.jar` - API documentation

### Run Tests

```bash
mvn test
```

Test PDFs will be generated in `test-output/` directory.

---

## Integration Options

### 🚀 Spring Boot (Recommended)

**Simplest integration with auto-configuration:**

1. **Add dependency to your `pom.xml`:**
```xml
<dependency>
    <groupId>com.mercury</groupId>
    <artifactId>pdf-render</artifactId>
    <version>1.0.1</version>
</dependency>
```

2. **Configure `application.yml`:**
```yaml
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    default-family: HarmonyOS Sans SC, sans-serif
  template:
    default-name: report
    cache-enabled: true
```

3. **Inject and use:**
```java
@Service
public class PdfService {
    private final ReportService reportService;
    
    // ✅ Auto-configured - just inject!
    public PdfService(ReportService reportService) {
        this.reportService = reportService;
    }
    
    public byte[] generatePdf(ReportData data) throws IOException {
        return reportService.generatePdf(data);
    }
}
```

**📖 Complete guide:** [SPRING_BOOT_INTEGRATION_GUIDE.md](SPRING_BOOT_INTEGRATION_GUIDE.md)

---

### 📦 Standalone JAR (Non-Spring Boot)

**Direct integration in plain Java projects:**

```java
import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.model.*;

public class PdfDemo {
    public static void main(String[] args) throws IOException {
        ReportService service = new ReportService();
        
        // Configure Chinese fonts (if needed)
        PdfRenderProperties.FontProperties fonts = new PdfRenderProperties.FontProperties();
        fonts.setRegularPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fonts.setDefaultFamily("HarmonyOS Sans SC, sans-serif");
        service.getHtmlRenderer().setFontProperties(fonts);
        
        // Build report data
        ReportData reportData = ReportDataBuilder.create()
            .title("Sample Report")
            .subtitle("Generated with pdf-render")
            .date("2026-01-30")
            .addSection(new Section("Introduction")
                .addParagraph("This is a sample PDF report."))
            .build();
        
        // Generate PDF
        byte[] pdfBytes = service.generatePdf(reportData);
        
        // Save to file
        try (FileOutputStream fos = new FileOutputStream("output.pdf")) {
            fos.write(pdfBytes);
        }
        
        System.out.println("PDF generated: output.pdf");
    }
}
```

**📖 Complete guide:** [JAR_INTEGRATION.md](JAR_INTEGRATION.md)

---

## Chinese/CJK Font Support

**⚠️ Important:** To display Chinese, Japanese, or Korean characters, you **MUST** configure fonts. Otherwise, CJK text will appear as boxes (□).

### Quick Font Configuration

```java
// Using PdfRenderProperties (recommended)
PdfRenderProperties.FontProperties fonts = new PdfRenderProperties.FontProperties();
fonts.setRegularPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fonts.setDefaultFamily("HarmonyOS Sans SC, sans-serif");
service.getHtmlRenderer().setFontProperties(fonts);
```

### Supported Fonts
- **HarmonyOS Sans SC** (recommended for Chinese)
- **Noto Sans CJK SC** (alternative)
- Any TrueType (.ttf) or OpenType (.otf) font

**📖 Detailed guide:** [FONT_CONFIGURATION.md](FONT_CONFIGURATION.md)

---

## Built-in Templates

The library includes 5 professional templates:

| Template | Description | Use Case |
|----------|-------------|----------|
| `report` | Standard multi-section report | General purpose (default) |
| `flexible` | Flexible layout | Custom layouts |
| `invoice` | Invoice format | Billing documents |
| `certificate` | Certificate design | Certificates, awards |
| `executive-summary` | Executive summary | Business summaries |

### Using Templates

```java
// Use default template
byte[] pdf = service.generatePdf(reportData);

// Use specific template
byte[] pdf = service.generatePdf(reportData, "invoice");
```

**📖 Template customization:** [TEMPLATE_GUIDE.md](TEMPLATE_GUIDE.md)

---

## Advanced Features

### Tables with Automatic Pagination

```java
TableData table = new TableData();
table.setHeaders(Arrays.asList("Name", "Value", "Status"));
table.addRow(Arrays.asList("Item 1", "100", "Active"));
table.addRow(Arrays.asList("Item 2", "200", "Pending"));

Section section = new Section("Data Table").addTable(table);
```

### Charts (JFreeChart Integration)

```java
ChartData chart = new ChartData();
chart.setTitle("Sales Trend");
chart.setType("bar"); // bar, pie, line, area, stacked-bar
chart.setLabels(Arrays.asList("Q1", "Q2", "Q3", "Q4"));
chart.setData(Arrays.asList(100.0, 150.0, 200.0, 180.0));

Section section = new Section("Sales Chart").addChart(chart);
```

### Debug HTML Output

Enable debug mode to view the intermediate HTML:

```java
service.getHtmlRenderer().setDebugHtmlEnabled(true);
service.getHtmlRenderer().setDebugHtmlOutputDirectory("debug-html");
service.getHtmlRenderer().setDebugHtmlIncludeTimestamp(true);
```

**📖 More details:** [DEBUG_HTML_CONFIGURATION.md](DEBUG_HTML_CONFIGURATION.md)

---

## Architecture

```
┌─────────────┐
│ ReportData  │ (Model: sections, tables, charts)
└──────┬──────┘
       │
       ▼
┌──────────────────┐
│ HtmlReport       │ (Thymeleaf template rendering)
│ Renderer         │ (Font management, chart embedding)
└──────┬───────────┘
       │
       ▼
┌──────────────────┐
│ HTML + CSS       │ (Base64 embedded charts/images)
└──────┬───────────┘
       │
       ▼
┌──────────────────┐
│ Flying Saucer    │ (HTML → PDF conversion)
│ + OpenPDF        │
└──────┬───────────┘
       │
       ▼
┌──────────────────┐
│ PDF Output       │ (Final PDF bytes)
└──────────────────┘
```

---

## Documentation

### Essential Guides
- [JAR Integration Guide](JAR_INTEGRATION.md) - Using as a JAR library
- [Spring Boot Integration Guide](SPRING_BOOT_INTEGRATION_GUIDE.md) - Spring Boot setup (Chinese)
- [Font Configuration](FONT_CONFIGURATION.md) - Chinese/CJK font setup
- [Template Guide](TEMPLATE_GUIDE.md) - Template usage and customization
- [Troubleshooting](TROUBLESHOOTING.md) - Common issues and solutions

### Examples
- [Example Application](EXAMPLE_APPLICATION.md) - Complete working examples
- [Spring Boot Examples](SPRING_BOOT_EXAMPLES.md) - Spring Boot code samples

### API Reference
- [Javadoc](target/apidocs/) - Generate with `mvn javadoc:javadoc`

---

## Dependencies

Core dependencies (JDK 8 compatible):
- **Flying Saucer + OpenPDF** - HTML/CSS to PDF conversion
- **Thymeleaf 3.1.1** - Template engine
- **JFreeChart 1.5.4** - Chart generation
- **Gson 2.10.1** - JSON processing
- **Spring Boot 2.7.18** (optional) - Auto-configuration support

All dependencies are JDK 8 compatible.

---

## Testing

The project includes comprehensive test coverage:

```bash
mvn test
```

- **62 tests** covering all major features
- Tests for Chinese fonts, tables, charts, templates
- Spring Boot auto-configuration tests
- JAR resource loading tests

---

## License

This project is licensed under the Apache License 2.0. See [LICENSE](LICENSE) file for details.

---

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## Support

- **Issues:** Report bugs or request features via GitHub Issues
- **Documentation:** See the `docs/` directory for additional guides
- **Examples:** Check `src/test/java/` for working code examples

---

**中文文档:** [README_CN.md](README_CN.md)
