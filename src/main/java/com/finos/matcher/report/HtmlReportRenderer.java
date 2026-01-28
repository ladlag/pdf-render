package com.finos.matcher.report;

import com.finos.matcher.report.config.FontConfig;
import com.finos.matcher.report.model.ChartData;
import com.finos.matcher.report.model.ReportData;
import com.finos.matcher.report.model.Section;
import com.finos.matcher.report.util.FontNameExtractor;
import com.lowagie.text.DocumentException;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for rendering PDF reports using HTML/CSS templates and Flying Saucer.
 * This approach provides stable table pagination without row loss.
 */
public class HtmlReportRenderer {
    
    private final TemplateEngine templateEngine;
    private final ChartRenderer chartRenderer;
    private boolean cacheTemplates = true; // Enable caching by default for production
    private String defaultTemplateName = "report"; // Default template name
    private FontConfig fontConfig; // Optional font configuration
    
    // Debug HTML configuration
    private boolean debugHtmlEnabled = false; // Whether to save intermediate HTML
    private String debugHtmlOutputDirectory = "debug-html"; // Directory for debug HTML files
    private boolean debugHtmlIncludeTimestamp = false; // Whether to include timestamp in filename
    
    public HtmlReportRenderer() {
        this.chartRenderer = new ChartRenderer();
        this.templateEngine = createTemplateEngine();
        this.fontConfig = null; // No custom fonts by default
    }
    
    /**
     * Sets the default template name to use when no template is specified.
     * Template files should be placed in src/main/resources/templates/ with .html extension.
     * 
     * @param templateName Template name without the .html extension (e.g., "report", "invoice")
     */
    public void setDefaultTemplateName(String templateName) {
        this.defaultTemplateName = templateName;
    }
    
    /**
     * Gets the current default template name
     */
    public String getDefaultTemplateName() {
        return defaultTemplateName;
    }
    
    /**
     * Sets whether to cache templates. Disable for development, enable for production.
     */
    public void setCacheTemplates(boolean cacheTemplates) {
        this.cacheTemplates = cacheTemplates;
        // Recreate template engine with new cache setting
        this.templateEngine.getTemplateResolvers().forEach(resolver -> {
            if (resolver instanceof ClassLoaderTemplateResolver) {
                ((ClassLoaderTemplateResolver) resolver).setCacheable(cacheTemplates);
            }
        });
    }
    
    /**
     * Sets the font configuration for custom fonts.
     * This allows specification of custom fonts for regular text, bold text, and CJK text.
     * 
     * @param fontConfig Font configuration object
     */
    public void setFontConfig(FontConfig fontConfig) {
        this.fontConfig = fontConfig;
    }
    
    /**
     * Gets the current font configuration
     * 
     * @return Current font configuration, or null if not set
     */
    public FontConfig getFontConfig() {
        return fontConfig;
    }
    
    /**
     * Enables or disables debug HTML output.
     * When enabled, intermediate HTML will be saved before PDF conversion.
     * 
     * @param enabled true to enable debug HTML output, false to disable
     */
    public void setDebugHtmlEnabled(boolean enabled) {
        this.debugHtmlEnabled = enabled;
    }
    
    /**
     * Checks if debug HTML output is enabled
     * 
     * @return true if enabled, false otherwise
     */
    public boolean isDebugHtmlEnabled() {
        return debugHtmlEnabled;
    }
    
    /**
     * Sets the directory where debug HTML files will be saved.
     * 
     * @param directory Directory path (e.g., "debug-html", "target/debug")
     */
    public void setDebugHtmlOutputDirectory(String directory) {
        this.debugHtmlOutputDirectory = directory;
    }
    
    /**
     * Gets the current debug HTML output directory
     * 
     * @return Directory path
     */
    public String getDebugHtmlOutputDirectory() {
        return debugHtmlOutputDirectory;
    }
    
    /**
     * Sets whether to include timestamp in debug HTML filenames.
     * When enabled, files will be named like "report-20240127-123045.html"
     * 
     * @param includeTimestamp true to include timestamp, false for simple names
     */
    public void setDebugHtmlIncludeTimestamp(boolean includeTimestamp) {
        this.debugHtmlIncludeTimestamp = includeTimestamp;
    }
    
    /**
     * Checks if timestamp should be included in debug HTML filenames
     * 
     * @return true if timestamp is included, false otherwise
     */
    public boolean isDebugHtmlIncludeTimestamp() {
        return debugHtmlIncludeTimestamp;
    }
    
    /**
     * Sets the path where intermediate HTML should be saved for debugging.
     * If set, the rendered HTML will be saved to this path before PDF conversion.
     * This method provides backward compatibility. Use setDebugHtmlEnabled() for new code.
     * 
     * @param path Path to save HTML file (e.g., "debug-output/report.html"), or null to disable
     * @deprecated Use setDebugHtmlEnabled() and setDebugHtmlOutputDirectory() instead
     */
    @Deprecated
    public void setDebugHtmlOutputPath(String path) {
        if (path != null && !path.isEmpty()) {
            this.debugHtmlEnabled = true;
            // Extract directory from path if it contains a filename
            java.nio.file.Path p = java.nio.file.Paths.get(path);
            if (p.getParent() != null) {
                this.debugHtmlOutputDirectory = p.getParent().toString();
            }
        } else {
            this.debugHtmlEnabled = false;
        }
    }
    
    /**
     * Gets the current debug HTML output path
     * This method provides backward compatibility.
     * 
     * @return Path where HTML is saved, or null if disabled
     * @deprecated Use isDebugHtmlEnabled() and getDebugHtmlOutputDirectory() instead
     */
    @Deprecated
    public String getDebugHtmlOutputPath() {
        if (debugHtmlEnabled) {
            return debugHtmlOutputDirectory;
        }
        return null;
    }
    
    /**
     * Generates a PDF from ReportData using the HTML/CSS pipeline with the default template
     */
    public byte[] generatePdf(ReportData reportData) throws IOException, DocumentException {
        return generatePdf(reportData, defaultTemplateName);
    }
    
    /**
     * Generates a PDF from ReportData using the HTML/CSS pipeline with a specified template
     * 
     * @param reportData Report data to render
     * @param templateName Template name without .html extension (e.g., "report", "invoice")
     * @return PDF content as byte array
     */
    public byte[] generatePdf(ReportData reportData, String templateName) throws IOException, DocumentException {
        // Step 1: Prepare data for template (including chart images)
        Map<String, Object> templateData = prepareTemplateData(reportData);
        
        // Step 2: Render HTML from template
        String html = renderHtml(templateData, templateName);
        
        // Step 2.5: Save intermediate HTML for debugging if enabled
        if (debugHtmlEnabled) {
            saveDebugHtml(html, templateName);
        }
        
        // Step 3: Convert HTML to PDF using Flying Saucer
        return convertHtmlToPdf(html);
    }
    
    /**
     * Prepares data for the Thymeleaf template, including converting charts to base64 images
     */
    private Map<String, Object> prepareTemplateData(ReportData reportData) throws IOException {
        Map<String, Object> data = new HashMap<>();
        
        // Cover page data
        data.put("title", reportData.getTitle() != null ? reportData.getTitle() : "");
        data.put("subtitle", reportData.getSubtitle());
        data.put("reportDate", reportData.getReportDate());
        data.put("reportNumber", reportData.getReportNumber());
        
        // Check if using new flexible sections or legacy structure
        if (reportData.hasSections()) {
            // Process flexible sections
            List<Section> sections = reportData.getSections();
            for (Section section : sections) {
                // Convert charts in sections to base64
                if (section.getCharts() != null && !section.getCharts().isEmpty()) {
                    for (ChartData chart : section.getCharts()) {
                        if (chart.getBase64Image() == null) {
                            String base64Image = chartRenderer.generateChartAsBase64(chart);
                            chart.setBase64Image(base64Image);
                        }
                    }
                }
            }
            data.put("sections", sections);
        } else {
            // Legacy structure
            data.put("sections", null);
            
            // Section 1: Table blocks
            data.put("tableBlocks", reportData.getTableBlocks());
            
            // Section 2: Analysis paragraphs
            data.put("analysisParagraphs", reportData.getAnalysisParagraphs());
            
            // Section 3: Summary table
            data.put("summaryTable", reportData.getSummaryTable());
            
            // Section 3: Charts (convert to base64 images)
            if (reportData.getCharts() != null && !reportData.getCharts().isEmpty()) {
                for (ChartData chart : reportData.getCharts()) {
                    if (chart.getBase64Image() == null) {
                        String base64Image = chartRenderer.generateChartAsBase64(chart);
                        chart.setBase64Image(base64Image);
                    }
                }
                data.put("charts", reportData.getCharts());
            }
            
            // Section 4: Notice and metadata
            data.put("reportNotice", reportData.getReportNotice());
            data.put("metadata", reportData.getMetadata());
        }
        
        // Add font configuration if available
        if (fontConfig != null) {
            data.put("fontFaceDeclaration", fontConfig.getCssFontFaceDeclaration());
            data.put("fontFamily", fontConfig.getFontFamilyCss());
            data.put("cjkFontFamily", fontConfig.getCjkFontFamily());
        } else {
            // Provide empty strings as defaults
            data.put("fontFaceDeclaration", "");
            data.put("fontFamily", "DejaVu Sans, Arial, sans-serif");
            data.put("cjkFontFamily", "");
        }
        
        return data;
    }
    
    /**
     * Renders HTML from the Thymeleaf template
     */
    private String renderHtml(Map<String, Object> data, String templateName) {
        Context context = new Context();
        context.setVariables(data);
        
        return templateEngine.process(templateName, context);
    }
    
    /**
     * Converts HTML to PDF using Flying Saucer + OpenPDF
     */
    private byte[] convertHtmlToPdf(String html) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        ITextRenderer renderer = new ITextRenderer();
        
        // Register custom fonts if configured
        if (fontConfig != null) {
            registerFontsWithRenderer(renderer);
        }
        
        // Set base URL to templates directory to allow Flying Saucer to resolve resources
        // This fixes the issue where CSS styles (including borders) fail to apply properly
        // when external resources cannot be resolved
        String baseUrl = getTemplatesBaseUrl();
        renderer.setDocumentFromString(html, baseUrl);
        renderer.layout();
        renderer.createPDF(baos);
        
        return baos.toByteArray();
    }
    
    /**
     * Gets the base URL for the templates directory.
     * This allows Flying Saucer to resolve relative resource paths (fonts, images, etc.)
     * 
     * @return Base URL string pointing to the templates directory
     */
    private String getTemplatesBaseUrl() {
        try {
            // Try to get the templates directory from classpath
            java.net.URL resource = getClass().getResource("/templates/");
            if (resource != null) {
                return resource.toString();
            } else {
                // Log warning if templates directory cannot be found
                System.err.println("Warning: Templates directory not found on classpath");
            }
        } catch (Exception e) {
            // If we can't get the resource URL, log the error
            System.err.println("Warning: Could not resolve templates base URL: " + e.getMessage());
        }
        
        // Fallback: use empty string to let Flying Saucer process HTML without base URL
        // CSS is inline in templates, so rendering will still work, just without external resource resolution
        return "";
    }
    
    /**
     * Registers custom fonts with the Flying Saucer renderer for PDF embedding.
     * 
     * Uses Identity-H encoding for proper Unicode/CJK character support.
     * Also validates that CSS font-family matches the registered font's internal name.
     */
    private void registerFontsWithRenderer(ITextRenderer renderer) {
        try {
            // Register regular font with Identity-H encoding for Unicode support
            if (fontConfig.getRegularFontPath() != null) {
                String fontPath = resolveFontPath(fontConfig.getRegularFontPath());
                
                // Validate font configuration
                validateFontConfiguration(fontConfig.getRegularFontPath(), fontConfig.getDefaultFontFamily(), "regular");
                
                renderer.getFontResolver().addFont(fontPath, "Identity-H", true);
            }
            
            // Register bold font with Identity-H encoding
            if (fontConfig.getBoldFontPath() != null) {
                String fontPath = resolveFontPath(fontConfig.getBoldFontPath());
                renderer.getFontResolver().addFont(fontPath, "Identity-H", true);
            }
            
            // Register CJK font with Identity-H encoding (essential for CJK characters)
            if (fontConfig.getCjkFontPath() != null) {
                String fontPath = resolveFontPath(fontConfig.getCjkFontPath());
                
                // Validate CJK font configuration
                validateFontConfiguration(fontConfig.getCjkFontPath(), fontConfig.getCjkFontFamily(), "CJK");
                
                renderer.getFontResolver().addFont(fontPath, "Identity-H", true);
            }
        } catch (Exception e) {
            // Log the error but don't fail - fall back to default fonts
            System.err.println("Warning: Failed to register custom fonts: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Validates that the CSS font-family contains the font file's internal name.
     * Prints warnings if mismatch is detected, which would cause CJK characters to display as boxes.
     * 
     * @param fontPath Path to the font file
     * @param cssFontFamily CSS font-family string
     * @param fontType Type of font (e.g., "regular", "CJK") for logging
     */
    private void validateFontConfiguration(String fontPath, String cssFontFamily, String fontType) {
        try {
            if (cssFontFamily == null || cssFontFamily.isEmpty()) {
                return; // No CSS font family configured, skip validation
            }
            
            String internalName = FontNameExtractor.extractFontFamilyName(fontPath);
            
            if (!cssFontFamily.contains(internalName)) {
                System.err.println("╔════════════════════════════════════════════════════════════════╗");
                System.err.println("║  ⚠️  WARNING: Font Configuration Mismatch Detected!          ║");
                System.err.println("╚════════════════════════════════════════════════════════════════╝");
                System.err.println();
                System.err.println("Font type: " + fontType);
                System.err.println("Font file: " + fontPath);
                System.err.println();
                System.err.println("  CSS font-family:        " + cssFontFamily);
                System.err.println("  Font's internal name:   " + internalName);
                System.err.println();
                System.err.println("⚠️  CSS references a font name that doesn't match the file!");
                System.err.println("⚠️  This will cause Chinese/CJK characters to show as boxes (□)");
                System.err.println();
                System.err.println("To fix, update your FontConfig:");
                System.err.println("  fontConfig.setDefaultFontFamily(\"" + internalName + ", DejaVu Sans, sans-serif\");");
                System.err.println();
                System.err.println("════════════════════════════════════════════════════════════════");
            }
        } catch (Exception e) {
            // If validation fails, just log it - don't block PDF generation
            System.err.println("Warning: Could not validate " + fontType + " font configuration: " + e.getMessage());
        }
    }
    
    /**
     * Resolves a font path, handling both classpath and file system paths
     */
    private String resolveFontPath(String path) throws IOException {
        if (path.startsWith("classpath:")) {
            // Load from classpath
            String resourcePath = path.substring("classpath:".length());
            // Return the classpath URL - Flying Saucer can handle it
            java.net.URL resource = getClass().getResource(resourcePath);
            if (resource == null) {
                throw new IOException("Font not found in classpath: " + resourcePath);
            }
            return resource.toString();
        } else {
            // Direct file path
            return path;
        }
    }
    
    /**
     * Saves the intermediate HTML to a file for debugging purposes.
     * Uses the configured output directory and generates filename based on template name.
     * Creates parent directories if they don't exist.
     * 
     * @param html HTML content to save
     * @param templateName Template name used to generate the HTML filename
     */
    private void saveDebugHtml(String html, String templateName) {
        try {
            // Generate filename
            String filename;
            if (debugHtmlIncludeTimestamp) {
                String timestamp = new java.text.SimpleDateFormat("yyyyMMdd-HHmmss").format(new java.util.Date());
                filename = templateName + "-" + timestamp + ".html";
            } else {
                filename = templateName + ".html";
            }
            
            // Build full path
            java.nio.file.Path directory = java.nio.file.Paths.get(debugHtmlOutputDirectory);
            java.nio.file.Path path = directory.resolve(filename);
            
            // Create directory if it doesn't exist
            java.nio.file.Files.createDirectories(directory);
            
            // Write HTML to file
            java.nio.file.Files.write(path, html.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            
            System.out.println("✓ Debug HTML saved to: " + path.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Warning: Could not save debug HTML: " + e.getMessage());
            // Don't fail PDF generation if debug save fails
        }
    }
    
    /**
     * Creates and configures the Thymeleaf template engine
     */
    private TemplateEngine createTemplateEngine() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML");
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(cacheTemplates); // Configurable caching
        
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(templateResolver);
        
        return engine;
    }
}
