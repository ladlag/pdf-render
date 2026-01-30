package com.mercury.pdf.render;

import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.model.ChartData;
import com.mercury.pdf.render.model.ReportData;
import com.mercury.pdf.render.model.Section;
import com.mercury.pdf.render.util.FontNameExtractor;
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
    
    // Font cache to avoid extracting same font multiple times
    private final java.util.Map<String, String> fontPathCache = new java.util.concurrent.ConcurrentHashMap<>();
    
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
     * Also configures the chart renderer to use the same font for proper Chinese rendering.
     * 
     * This method is safe to call from @PostConstruct - font loading failures will be logged
     * but will not throw exceptions that could prevent application startup.
     * 
     * @param fontConfig Font configuration object
     */
    public void setFontConfig(FontConfig fontConfig) {
        this.fontConfig = fontConfig;
        
        // Also configure chart renderer with the same font for consistent rendering
        // This is wrapped in try-catch to ensure @PostConstruct safety
        try {
            if (fontConfig != null && fontConfig.getRegularFontPath() != null) {
                chartRenderer.setChartFont(fontConfig.getRegularFontPath());
            }
        } catch (Exception e) {
            // Log warning but don't throw - allows application to start even if font loading fails
            System.err.println("Warning: Failed to configure chart font in setFontConfig: " + e.getMessage());
            // Continue - PDF generation will work but may not render Chinese characters correctly
        }
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
        data.put("reportNotice", reportData.getReportNotice());
        data.put("metadata", reportData.getMetadata());
        
        // Process flexible sections
        List<Section> sections = reportData.getSections();
        if (sections != null && !sections.isEmpty()) {
            for (Section section : sections) {
                // Convert charts in sections to data URI format
                if (section.getCharts() != null && !section.getCharts().isEmpty()) {
                    for (ChartData chart : section.getCharts()) {
                        if (chart.getBase64Image() == null) {
                            String base64Image = chartRenderer.generateChartAsDataUri(chart);
                            chart.setBase64Image(base64Image);
                        }
                    }
                }
            }
        }
        data.put("sections", sections);
        
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
        
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(baos);
        
        return baos.toByteArray();
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
     * Resolves a font path, handling both classpath and file system paths.
     * For classpath resources, extracts them to a temporary file to ensure
     * compatibility when running from a JAR file. Uses caching to avoid
     * extracting the same font multiple times.
     */
    private String resolveFontPath(String path) throws IOException {
        if (path.startsWith("classpath:")) {
            // Check cache first
            String cachedPath = fontPathCache.get(path);
            if (cachedPath != null && new java.io.File(cachedPath).exists()) {
                return cachedPath;
            }
            
            // Load from classpath
            String resourcePath = path.substring("classpath:".length());
            
            // Validate resource path
            if (resourcePath.isEmpty() || !resourcePath.startsWith("/")) {
                throw new IOException("Invalid classpath resource path: " + resourcePath + 
                    " (must start with /)");
            }
            
            // Get resource as stream (works reliably from both filesystem and JAR)
            java.io.InputStream fontStream = getClass().getResourceAsStream(resourcePath);
            if (fontStream == null) {
                throw new IOException("Font not found in classpath: " + resourcePath);
            }
            
            try {
                // Extract font file name from path
                int lastSlash = resourcePath.lastIndexOf('/');
                String fileName = resourcePath.substring(lastSlash + 1);
                
                // Validate fileName contains an extension
                int lastDot = fileName.lastIndexOf('.');
                if (lastDot <= 0 || lastDot == fileName.length() - 1) {
                    throw new IOException("Font file must have a valid extension: " + fileName);
                }
                
                String extension = fileName.substring(lastDot);
                
                // Create temporary file with same extension
                java.io.File tempFile = java.io.File.createTempFile("pdf-render-font-", extension);
                tempFile.deleteOnExit(); // Clean up on JVM exit
                
                // Copy font data to temporary file
                try (java.io.FileOutputStream out = new java.io.FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = fontStream.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
                
                String resolvedPath = tempFile.getAbsolutePath();
                
                // Cache the resolved path
                fontPathCache.put(path, resolvedPath);
                
                // Use SLF4J if available, otherwise fall back to System.out
                try {
                    org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HtmlReportRenderer.class);
                    logger.info("Font extracted for PDF rendering: {}", fileName);
                } catch (NoClassDefFoundError e) {
                    // SLF4J not available, use System.out
                    System.out.println("✓ Font extracted for PDF rendering: " + fileName);
                }
                
                return resolvedPath;
                
            } finally {
                fontStream.close();
            }
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
