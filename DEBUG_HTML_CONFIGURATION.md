# Debug HTML Output Configuration / 调试HTML输出配置

## Overview / 概述

The PDF render library now supports saving intermediate HTML files for debugging purposes. This feature helps you inspect the rendered HTML template before it's converted to PDF, making it easier to debug styling and layout issues.

PDF渲染库现在支持保存中间态HTML文件用于调试。此功能帮助您在HTML转换为PDF之前检查渲染后的模板，使调试样式和布局问题变得更容易。

## Configuration / 配置

### Method 1: Programmatic Configuration / 方法1：编程方式配置

```java
import com.finos.matcher.report.ReportService;
import com.finos.matcher.report.HtmlReportRenderer;

// Create report service
ReportService service = new ReportService();
service.setUseHtmlPipeline(true);

HtmlReportRenderer renderer = service.getHtmlRenderer();

// Enable debug HTML output
renderer.setDebugHtmlEnabled(true);

// Set output directory (default: "debug-html")
renderer.setDebugHtmlOutputDirectory("target/debug-html");

// Include timestamp in filename (default: false)
renderer.setDebugHtmlIncludeTimestamp(false);

// Generate PDF (HTML will be saved automatically)
byte[] pdf = service.generatePdf(reportData, "matcher-report-1.0");
```

### Method 2: Spring Boot Configuration / 方法2：Spring Boot配置

Add to your `application.yml`:

```yaml
pdf-render:
  debug:
    enabled: true
    output-directory: debug-html
    include-timestamp: false
```

## Configuration Options / 配置选项

| Option / 选项 | Type / 类型 | Default / 默认值 | Description / 说明 |
|--------------|------------|-----------------|-------------------|
| `enabled` | boolean | false | Enable/disable debug HTML output<br/>启用/禁用调试HTML输出 |
| `output-directory` | String | "debug-html" | Directory where HTML files will be saved<br/>HTML文件保存目录 |
| `include-timestamp` | boolean | false | Include timestamp in filename<br/>文件名中包含时间戳 |

## File Naming / 文件命名

### Without Timestamp / 不包含时间戳
```
debug-html/
  ├── matcher-report-1.0.html
  ├── flexible.html
  └── invoice.html
```

Each template overwrites the previous HTML file with the same name.

每个模板会覆盖之前同名的HTML文件。

### With Timestamp / 包含时间戳
```
debug-html/
  ├── matcher-report-1.0-20260127-132610.html
  ├── matcher-report-1.0-20260127-132645.html
  ├── flexible-20260127-133012.html
  └── invoice-20260127-133145.html
```

Each PDF generation creates a new HTML file with unique timestamp.

每次生成PDF都会创建带有唯一时间戳的新HTML文件。

## Use Cases / 使用场景

### 1. Development / 开发调试

Enable debug HTML during development to quickly inspect rendered templates:

在开发期间启用调试HTML以快速检查渲染的模板：

```yaml
# application-dev.yml
pdf-render:
  debug:
    enabled: true
    output-directory: target/debug-html
    include-timestamp: true
```

### 2. Template Debugging / 模板调试

When you're working on HTML templates and CSS styles:

当您处理HTML模板和CSS样式时：

1. Enable debug HTML output
2. Generate PDF
3. Open the HTML file in a browser
4. Inspect elements and styles using browser DevTools
5. Make changes to the template
6. Regenerate and verify

### 3. Border Issue Investigation / 边框问题调查

For the reported border issue:

针对报告的边框问题：

```java
// Enable debug output
renderer.setDebugHtmlEnabled(true);
renderer.setDebugHtmlOutputDirectory("debug-html");

// Generate PDF
byte[] pdf = service.generatePdf(reportData, "matcher-report-1.0");

// Open debug-html/matcher-report-1.0.html in browser
// Inspect CSS to verify border styles
```

### 4. Continuous Investigation / 持续调查

Use timestamps to compare different versions:

使用时间戳比较不同版本：

```java
renderer.setDebugHtmlIncludeTimestamp(true);

// Generate multiple times with different data
byte[] pdf1 = service.generatePdf(reportData1, "matcher-report-1.0");
byte[] pdf2 = service.generatePdf(reportData2, "matcher-report-1.0");

// Compare the HTML files side by side
```

## Inspecting Debug HTML / 检查调试HTML

### Opening in Browser / 在浏览器中打开

Simply open the HTML file in any web browser:

只需在任何Web浏览器中打开HTML文件：

```bash
# Open with default browser
open debug-html/matcher-report-1.0.html

# Or on Linux
xdg-open debug-html/matcher-report-1.0.html

# Or on Windows
start debug-html/matcher-report-1.0.html
```

### Using Browser DevTools / 使用浏览器开发工具

1. Right-click on any element → "Inspect"
2. Check the "Styles" panel to see applied CSS
3. Look for `border` properties in the CSS
4. Verify which selectors are applying borders

右键点击任何元素 → "检查"
查看"样式"面板以查看应用的CSS
查找CSS中的`border`属性
验证哪些选择器在应用边框

### Verifying Border Styles / 验证边框样式

Search for border styles in the HTML:

在HTML中搜索边框样式：

```bash
# Search for border styles in the HTML
grep -i "border:" debug-html/matcher-report-1.0.html

# Count border occurrences
grep -c "border:" debug-html/matcher-report-1.0.html
```

## Examples / 示例

### Example 1: Simple Debugging / 示例1：简单调试

```java
@Test
public void debugBorderIssue() throws IOException {
    ReportService service = new ReportService();
    service.setUseHtmlPipeline(true);
    
    // Enable debug HTML
    service.getHtmlRenderer().setDebugHtmlEnabled(true);
    service.getHtmlRenderer().setDebugHtmlOutputDirectory("test-output/debug");
    
    // Generate PDF
    byte[] pdf = service.generatePdf(reportData, "matcher-report-1.0");
    
    // HTML is automatically saved to:
    // test-output/debug/matcher-report-1.0.html
}
```

### Example 2: Timestamp Comparison / 示例2：时间戳对比

```java
@Test
public void compareMultipleVersions() throws IOException {
    ReportService service = new ReportService();
    service.setUseHtmlPipeline(true);
    
    // Enable with timestamp
    service.getHtmlRenderer().setDebugHtmlEnabled(true);
    service.getHtmlRenderer().setDebugHtmlIncludeTimestamp(true);
    
    // Generate before changes
    byte[] pdfBefore = service.generatePdf(reportData, "matcher-report-1.0");
    
    // Make changes to data or template
    // ...
    
    // Generate after changes
    byte[] pdfAfter = service.generatePdf(reportData, "matcher-report-1.0");
    
    // Two HTML files with timestamps created for comparison
}
```

## Disabling in Production / 在生产环境中禁用

**Important:** Always disable debug HTML output in production to avoid performance overhead and disk space usage.

**重要：**始终在生产环境中禁用调试HTML输出，以避免性能开销和磁盘空间使用。

```yaml
# application-prod.yml
pdf-render:
  debug:
    enabled: false
```

## File Location / 文件位置

Debug HTML files are saved relative to the working directory:

调试HTML文件相对于工作目录保存：

- `debug-html/` - Default location / 默认位置
- `target/debug-html/` - Common for Maven projects / Maven项目常用
- Absolute path: `/var/debug/html/` - Custom location / 自定义位置

## Troubleshooting / 故障排除

### HTML files not created / HTML文件未创建

1. Check if debug is enabled:
   ```java
   renderer.isDebugHtmlEnabled(); // should return true
   ```

2. Check output directory permissions
3. Check console for warning messages

### HTML looks different from PDF / HTML与PDF看起来不同

This is expected. The HTML shows the template after Thymeleaf processing but before Flying Saucer's PDF rendering. PDF rendering may apply additional transformations.

这是正常的。HTML显示Thymeleaf处理后但Flying Saucer PDF渲染前的模板。PDF渲染可能会应用额外的转换。

## Performance Considerations / 性能考虑

- Debug HTML output adds minimal overhead (~5-10ms)
- File I/O is non-blocking - PDF generation continues if save fails
- Use timestamps only when needed to avoid excessive disk usage

调试HTML输出增加的开销很小（约5-10毫秒）
文件I/O是非阻塞的 - 如果保存失败，PDF生成会继续
仅在需要时使用时间戳以避免过度使用磁盘空间

## See Also / 相关文档

- [BORDER_REMOVAL_SUMMARY.md](BORDER_REMOVAL_SUMMARY.md) - Border removal documentation
- [FONT_CONFIGURATION.md](FONT_CONFIGURATION.md) - Font configuration guide
- [TEMPLATE_GUIDE.md](TEMPLATE_GUIDE.md) - Template development guide
