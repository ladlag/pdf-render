package com.mercury.pdf.render;

import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.config.PdfRenderProperties;
import com.mercury.pdf.render.model.ChartData;
import com.mercury.pdf.render.model.ReportData;
import com.mercury.pdf.render.model.Section;
import com.mercury.pdf.render.util.FontNameExtractor;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
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
    
    private static final String DEFAULT_FONT_ALIAS = "CJK_MAIN";
    private static final String DEFAULT_FONT_ALIAS_FAMILY = DEFAULT_FONT_ALIAS + ", sans-serif";

    private TemplateEngine templateEngine;
    private final ChartRenderer chartRenderer;
    private boolean cacheTemplates = true; // Enable caching by default for production
    private String defaultTemplateName = "report"; // Default template name
    private String templateLocation = "/templates/"; // Template location prefix
    private PdfRenderProperties.FontProperties fontProperties; // Optional font configuration
    
    // Font cache to avoid extracting same font multiple times
    private final java.util.Map<String, String> fontPathCache = new java.util.concurrent.ConcurrentHashMap<>();
    
    // Debug HTML configuration
    private boolean debugHtmlEnabled = false; // Whether to save intermediate HTML
    private String debugHtmlOutputDirectory = "debug-html"; // Directory for debug HTML files
    private boolean debugHtmlIncludeTimestamp = false; // Whether to include timestamp in filename
    
    public HtmlReportRenderer() {
        this.chartRenderer = new ChartRenderer();
        this.templateEngine = createTemplateEngine();
        this.fontProperties = null; // No custom fonts by default
    }
    
    /**
     * Sets the default template name to use when no template is specified.
     * Template files should be placed in the configured template location
     * (default: src/main/resources/templates/) with .html extension.
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
     * Sets the template location prefix. Supports classpath: prefix.
     *
     * @param location Template location (e.g., "classpath:/templates/").
     *                 If null or empty, resets to default location.
     */
    public void setTemplateLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            this.templateLocation = "/templates/";
        } else {
            this.templateLocation = normalizeTemplateLocation(location);
        }
        this.templateEngine = createTemplateEngine();
        logInfo("✓ Template location set: " + templateLocation);
    }

    /**
     * Gets the current template location prefix.
     */
    public String getTemplateLocation() {
        return templateLocation;
    }
    
    /**
     * Sets whether to cache templates. Disable for development, enable for production.
     */
    public void setCacheTemplates(boolean cacheTemplates) {
        this.cacheTemplates = cacheTemplates;
        this.templateEngine = createTemplateEngine();
        logInfo("✓ Template cache enabled: " + cacheTemplates);
    }
    
    /**
     * Sets the font properties for custom fonts.
     * This allows specification of custom fonts for regular text, bold text, and CJK text.
     * Also configures the chart renderer to use the same font for proper Chinese rendering.
     * 
     * This method is safe to call during initialization - font loading failures will be logged
     * but will not throw exceptions that could prevent application startup.
     * 
     * @param fontProperties Font properties object from application configuration
     */
    public void setFontProperties(PdfRenderProperties.FontProperties fontProperties) {
        this.fontProperties = fontProperties;
        logFontProperties("FontProperties", fontProperties);
        
        // Also configure chart renderer with the same font for consistent rendering
        // This is wrapped in try-catch to ensure safe initialization
        try {
            if (fontProperties != null && fontProperties.getRegularPath() != null) {
                logInfo("✓ Configuring chart font from regular font: " + fontProperties.getRegularPath());
                chartRenderer.setChartFont(fontProperties.getRegularPath());
            }
        } catch (Exception e) {
            // Log warning but don't throw - allows application to start even if font loading fails
            System.err.println("Warning: Failed to configure chart font in setFontProperties: " + e.getMessage());
            e.printStackTrace(); // Include stack trace for debugging
            // Continue - PDF generation will work but may not render Chinese characters correctly
        }
    }
    
    /**
     * Gets the current font properties
     * 
     * @return Current font properties, or null if not set
     */
    public PdfRenderProperties.FontProperties getFontProperties() {
        return fontProperties;
    }
    
    /**
     * Sets font configuration using the legacy FontConfig object.
     * This method provides backward compatibility for existing code.
     * 
     * @param fontConfig Font configuration object
     * @deprecated Use setFontProperties(PdfRenderProperties.FontProperties) instead.
     *             FontConfig is redundant with PdfRenderProperties.FontProperties.
     */
    @Deprecated
    public void setFontConfig(FontConfig fontConfig) {
        if (fontConfig == null) {
            this.fontProperties = null;
            return;
        }
        
        // Convert FontConfig to FontProperties for internal use
        PdfRenderProperties.FontProperties props = new PdfRenderProperties.FontProperties();
        props.setRegularPath(fontConfig.getRegularFontPath());
        props.setBoldPath(fontConfig.getBoldFontPath());
        props.setCjkPath(fontConfig.getCjkFontPath());
        props.setDefaultFamily(fontConfig.getDefaultFontFamily());
        props.setCjkFamily(fontConfig.getCjkFontFamily());
        
        setFontProperties(props);
    }
    
    /**
     * Gets the current font configuration as a FontConfig object.
     * This method provides backward compatibility for existing code.
     * 
     * @return Current font configuration, or null if not set
     * @deprecated Use getFontProperties() instead.
     *             FontConfig is redundant with PdfRenderProperties.FontProperties.
     */
    @Deprecated
    public FontConfig getFontConfig() {
        if (fontProperties == null) {
            return null;
        }
        
        // Convert FontProperties to FontConfig for backward compatibility
        FontConfig config = new FontConfig();
        config.setRegularFontPath(fontProperties.getRegularPath());
        config.setBoldFontPath(fontProperties.getBoldPath());
        config.setCjkFontPath(fontProperties.getCjkPath());
        config.setDefaultFontFamily(fontProperties.getDefaultFamily());
        config.setCjkFontFamily(fontProperties.getCjkFamily());
        
        return config;
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
        if (fontProperties != null) {
            data.put("fontFaceDeclaration", ""); // No @font-face needed with Identity-H encoding

            if (hasCustomFontPaths()) {
                data.put("fontFamily", DEFAULT_FONT_ALIAS_FAMILY);
                data.put("cjkFontFamily", DEFAULT_FONT_ALIAS_FAMILY);
                logInfo("✓ PDF CSS font-family (alias): " + DEFAULT_FONT_ALIAS_FAMILY);
                logInfo("  Font family fallback: " + (fontProperties.getDefaultFamily() != null
                    ? fontProperties.getDefaultFamily()
                    : "(not configured)"));
            } else {
                // Use actual font family names for reliable CSS matching in JAR deployments
                // Extract the font's internal family name to match what's registered
                StringBuilder fontFamily = new StringBuilder();
                
                try {
                    // Primary font: use font's internal family name
                    if (fontProperties.getRegularPath() != null) {
                        String fontPath = resolveFontPath(fontProperties.getRegularPath());
                        String familyName = FontNameExtractor.extractFontFamilyName(fontPath);
                        fontFamily.append(familyName);
                    }
                    
                    // Add CJK font's internal family name if configured
                    if (fontProperties.getCjkPath() != null) {
                        String fontPath = resolveFontPath(fontProperties.getCjkPath());
                        String familyName = FontNameExtractor.extractFontFamilyName(fontPath);
                        if (fontFamily.length() > 0) {
                            fontFamily.append(", ");
                        }
                        fontFamily.append(familyName);
                    }
                } catch (Exception e) {
                    System.err.println("Warning: Failed to extract font family names: " + e.getMessage());
                    // Fall back to configured family names if extraction fails
                    if (fontProperties.getDefaultFamily() != null && !fontProperties.getDefaultFamily().isEmpty()) {
                        fontFamily.append(fontProperties.getDefaultFamily());
                    }
                }
                
                // Fallback to sans-serif (standard CSS fallback)
                if (fontFamily.length() > 0) {
                    fontFamily.append(", sans-serif");
                } else {
                    fontFamily.append("sans-serif");
                }
                
                String fontFamilyCss = fontFamily.toString();
                data.put("fontFamily", fontFamilyCss);
                logInfo("✓ PDF CSS font-family: " + fontFamilyCss);
                
                // Set CJK font family with actual font name
                StringBuilder cjkFamily = new StringBuilder();
                try {
                    if (fontProperties.getCjkPath() != null) {
                        String fontPath = resolveFontPath(fontProperties.getCjkPath());
                        String familyName = FontNameExtractor.extractFontFamilyName(fontPath);
                        cjkFamily.append(familyName);
                    }
                    if (fontProperties.getRegularPath() != null) {
                        String fontPath = resolveFontPath(fontProperties.getRegularPath());
                        String familyName = FontNameExtractor.extractFontFamilyName(fontPath);
                        if (cjkFamily.length() > 0) {
                            cjkFamily.append(", ");
                        }
                        cjkFamily.append(familyName);
                    }
                } catch (Exception e) {
                    System.err.println("Warning: Failed to extract CJK font family names: " + e.getMessage());
                    // Fall back to configured family names if extraction fails
                    if (fontProperties.getCjkFamily() != null && !fontProperties.getCjkFamily().isEmpty()) {
                        cjkFamily.append(fontProperties.getCjkFamily());
                    }
                }
                
                if (cjkFamily.length() > 0) {
                    cjkFamily.append(", sans-serif");
                } else {
                    cjkFamily.append("sans-serif");
                }
                
                String cjkFamilyCss = cjkFamily.toString();
                data.put("cjkFontFamily", cjkFamilyCss);
                logInfo("✓ PDF CSS CJK font-family: " + cjkFamilyCss);
            }
        } else {
            // Provide empty strings as defaults
            data.put("fontFaceDeclaration", "");
            data.put("fontFamily", "sans-serif");
            data.put("cjkFontFamily", "");
            logWarn("⚠️ No font configuration provided; using default CSS font-family: sans-serif");
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
        if (fontProperties != null) {
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
     * Fonts are explicitly embedded in the PDF for cross-platform compatibility.
     * 
     * Uses the font's internal family name as the alias to ensure reliable font matching
     * regardless of system fonts. This is critical for JAR deployments.
     * 
     * Also validates that CSS font-family matches the registered font's internal name.
     * 
     * Logging format when SLF4J is configured (e.g., in Spring Boot):
     *   INFO c.mercury.pdf.render.HtmlReportRenderer : Font extracted for PDF rendering: HarmonyOS_Sans_SC_Regular.ttf
     *   INFO c.mercury.pdf.render.HtmlReportRenderer : Resolved font temp path: /tmp/pdf-render-font-xxx.ttf
     *   INFO c.mercury.pdf.render.HtmlReportRenderer : ✓ Font registered with Flying Saucer: /tmp/pdf-render-font-xxx.ttf
     *   INFO c.mercury.pdf.render.HtmlReportRenderer :   Encoding: Identity-H | Embedded: true
     *   INFO c.mercury.pdf.render.HtmlReportRenderer :   Font family name (for CSS): HarmonyOS Sans SC
     *   INFO c.mercury.pdf.render.HtmlReportRenderer : Font extracted for PDF rendering: HarmonyOS_Sans_SC_Bold.ttf
     *   INFO c.mercury.pdf.render.HtmlReportRenderer : Resolved font temp path: /tmp/pdf-render-font-yyy.ttf
     *   INFO c.mercury.pdf.render.HtmlReportRenderer : ✓ Bold font registered with Flying Saucer: /tmp/pdf-render-font-yyy.ttf
     *   INFO c.mercury.pdf.render.HtmlReportRenderer :   Font family name: HarmonyOS Sans SC
     *   INFO c.mercury.pdf.render.HtmlReportRenderer : ✓ Total fonts registered for PDF: 2
     */
    private void registerFontsWithRenderer(ITextRenderer renderer) {
        try {
            int fontsRegistered = 0;
            
            // Register regular font with Identity-H encoding for Unicode support
            // Use font's internal name as alias for reliable CSS matching
            if (fontProperties.getRegularPath() != null) {
                String fontPath = resolveFontPath(fontProperties.getRegularPath());
                
                // Extract the font's actual internal family name
                String fontFamilyName = FontNameExtractor.extractFontFamilyName(fontPath);
                
                // Use BaseFont constants for explicit encoding and embedding
                // IDENTITY_H: Unicode encoding for CJK character support
                // EMBEDDED: Embeds font in PDF for cross-platform compatibility
                // ALIAS: Use font's internal name for reliable CSS matching
                renderer.getFontResolver().addFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, DEFAULT_FONT_ALIAS);
                fontsRegistered++;
                logInfo("✓ Font registered with Flying Saucer: " + fontPath);
                logInfo("  Encoding: " + BaseFont.IDENTITY_H + " | Embedded: " + BaseFont.EMBEDDED);
                logInfo("  Font family alias (for CSS): " + DEFAULT_FONT_ALIAS);
                logInfo("  Font internal family name: " + fontFamilyName);
                logRegisteredFont("regular", fontPath, fontFamilyName, DEFAULT_FONT_ALIAS);
            }
            
            // Register bold font with Identity-H encoding
            // Use font's internal name as alias
            if (fontProperties.getBoldPath() != null) {
                String fontPath = resolveFontPath(fontProperties.getBoldPath());
                String fontFamilyName = FontNameExtractor.extractFontFamilyName(fontPath);
                renderer.getFontResolver().addFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, DEFAULT_FONT_ALIAS);
                fontsRegistered++;
                logInfo("✓ Bold font registered with Flying Saucer: " + fontPath);
                logInfo("  Font family alias: " + DEFAULT_FONT_ALIAS);
                logInfo("  Font internal family name: " + fontFamilyName);
                logRegisteredFont("bold", fontPath, fontFamilyName, DEFAULT_FONT_ALIAS);
            }
            
            // Register CJK font with Identity-H encoding (essential for CJK characters)
            // Use font's internal name as alias
            if (fontProperties.getCjkPath() != null) {
                String fontPath = resolveFontPath(fontProperties.getCjkPath());
                String fontFamilyName = FontNameExtractor.extractFontFamilyName(fontPath);
                
                renderer.getFontResolver().addFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, DEFAULT_FONT_ALIAS);
                fontsRegistered++;
                logInfo("✓ CJK font registered with Flying Saucer: " + fontPath);
                logInfo("  Font family alias: " + DEFAULT_FONT_ALIAS);
                logInfo("  Font internal family name: " + fontFamilyName);
                logRegisteredFont("cjk", fontPath, fontFamilyName, DEFAULT_FONT_ALIAS);
            }
            
            if (fontsRegistered > 0) {
                logInfo("✓ Total fonts registered for PDF: " + fontsRegistered);
            }
        } catch (Exception e) {
            // Log the error but don't fail - fall back to default fonts
            logWarn("✗ Warning: Failed to register custom fonts: " + e.getMessage());
            logWarn("Stack trace:", e);
        }
    }
    
    /**
     * Validates that the CSS font-family contains the font file's internal name.
     * Logs warnings if mismatch is detected, which would cause CJK characters to display as boxes.
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
                logWarn("╔════════════════════════════════════════════════════════════════╗");
                logWarn("║  ⚠️  WARNING: Font Configuration Mismatch Detected!          ║");
                logWarn("╚════════════════════════════════════════════════════════════════╝");
                logWarn("");
                logWarn("Font type: " + fontType);
                logWarn("Font file: " + fontPath);
                logWarn("");
                logWarn("  CSS font-family:        " + cssFontFamily);
                logWarn("  Font's internal name:   " + internalName);
                logWarn("");
                logWarn("⚠️  CSS references a font name that doesn't match the file!");
                logWarn("⚠️  This will cause Chinese/CJK characters to show as boxes (□)");
                logWarn("");
                logWarn("To fix, update your FontConfig:");
                logWarn("  fontConfig.setDefaultFontFamily(\"" + internalName + ", DejaVu Sans, sans-serif\");");
                logWarn("");
                logWarn("════════════════════════════════════════════════════════════════");
            }
        } catch (Exception e) {
            // If validation fails, just log it - don't block PDF generation
            logWarn("Warning: Could not validate " + fontType + " font configuration: " + e.getMessage());
        }
    }
    
    /**
     * Resolves a font path, handling both classpath and file system paths.
     * For classpath resources, extracts them to a temporary file to ensure
     * compatibility when running from a JAR file. Uses caching to avoid
     * extracting the same font multiple times.
     * 
     * Logging behavior:
     * - When SLF4J is available: Logs via logger.info() without checkmark
     *   Example: "Font extracted for PDF rendering: HarmonyOS_Sans_SC_Regular.ttf"
     * - When SLF4J is not available: Falls back to System.out with checkmark
     *   Example: "✓ Font extracted for PDF rendering: HarmonyOS_Sans_SC_Regular.ttf"
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
                
                // Get absolute path and normalize for cross-platform compatibility
                // On Windows, paths use backslashes which can cause issues with PDF libraries
                // Convert to forward slashes which work on all platforms
                String resolvedPath = tempFile.getAbsolutePath().replace('\\', '/');
                
                // Cache the resolved path
                fontPathCache.put(path, resolvedPath);
                
                // Use SLF4J if available, otherwise fall back to System.out
                try {
                    org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HtmlReportRenderer.class);
                    logger.info("Font extracted for PDF rendering: {}", fileName);
                    logger.info("Resolved font temp path: {}", resolvedPath);
                } catch (NoClassDefFoundError e) {
                    // SLF4J not available, use System.out
                    System.out.println("✓ Font extracted for PDF rendering: " + fileName);
                    System.out.println("  Resolved font temp path: " + resolvedPath);
                }
                
                return resolvedPath;
                
            } finally {
                fontStream.close();
            }
        } else {
            // Direct file path - normalize for cross-platform compatibility
            // On Windows, file paths may contain backslashes which should be converted
            return path.replace('\\', '/');
        }
    }

    private void logFontProperties(String source, PdfRenderProperties.FontProperties props) {
        if (props == null) {
            logWarn("⚠️ Font configuration source " + source + " was null");
            return;
        }
        logInfo("✓ Font configuration (" + source + "):");
        logInfo("  Regular path: " + (props.getRegularPath() != null ? props.getRegularPath() : "(not configured)"));
        logInfo("  Bold path: " + (props.getBoldPath() != null ? props.getBoldPath() : "(not configured)"));
        logInfo("  CJK path: " + (props.getCjkPath() != null ? props.getCjkPath() : "(not configured)"));
        logInfo("  Default family: " + (props.getDefaultFamily() != null ? props.getDefaultFamily() : "(not configured)"));
        logInfo("  CJK family: " + (props.getCjkFamily() != null ? props.getCjkFamily() : "(not configured)"));
    }

    private void logRegisteredFont(String fontType, String fontPath, String fontFamilyName, String fontAlias) {
        logInfo("✓ Font configuration (" + fontType + "):");
        logInfo("  Source path: " + fontPath);
        logInfo("  Resolved family: " + fontFamilyName);
        logInfo("  Resolved alias: " + fontAlias);
    }

    private boolean hasCustomFontPaths() {
        if (fontProperties == null) {
            return false;
        }
        return fontProperties.getRegularPath() != null
            || fontProperties.getBoldPath() != null
            || fontProperties.getCjkPath() != null;
    }

    private void logInfo(String message) {
        try {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HtmlReportRenderer.class);
            logger.info(message);
        } catch (NoClassDefFoundError e) {
            System.out.println(message);
        }
    }

    private void logWarn(String message) {
        try {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HtmlReportRenderer.class);
            logger.warn(message);
        } catch (NoClassDefFoundError e) {
            System.err.println(message);
        }
    }

    private void logWarn(String message, Throwable exception) {
        try {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HtmlReportRenderer.class);
            logger.warn(message, exception);
        } catch (NoClassDefFoundError e) {
            System.err.println(message);
            exception.printStackTrace();
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
        templateResolver.setPrefix(templateLocation);
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML");
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(cacheTemplates); // Configurable caching
        
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(templateResolver);
        
        return engine;
    }

    private String normalizeTemplateLocation(String location) {
        String normalized = location.trim();
        if (normalized.startsWith("classpath:")) {
            normalized = normalized.substring("classpath:".length());
        }
        if (normalized.isEmpty()) {
            return "/templates/";
        }
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        if (!normalized.endsWith("/")) {
            normalized = normalized + "/";
        }
        return normalized;
    }
}
