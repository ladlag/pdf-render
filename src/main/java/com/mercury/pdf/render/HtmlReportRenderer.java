package com.mercury.pdf.render;

import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.config.PdfRenderProperties;
import com.mercury.pdf.render.model.ChartData;
import com.mercury.pdf.render.model.ReportData;
import com.mercury.pdf.render.model.Section;
import com.mercury.pdf.render.util.FontNameExtractor;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service for rendering PDF reports using HTML/CSS templates and Flying Saucer.
 * This approach provides stable table pagination without row loss.
 */
public class HtmlReportRenderer {
    
    private TemplateEngine templateEngine;
    private final ChartRenderer chartRenderer;
    private boolean cacheTemplates = true; // Enable caching by default for production
    private String defaultTemplateName = "report"; // Default template name
    private String templateLocation = "/templates/"; // Template location prefix
    private PdfRenderProperties.FontProperties fontProperties; // Optional font configuration
    private PdfRenderProperties.WatermarkProperties watermarkProperties; // Optional watermark configuration
    
    // Font cache to avoid extracting same font multiple times
    private final java.util.Map<String, String> fontPathCache = new java.util.concurrent.ConcurrentHashMap<>();
    
    /**
     * Unified font family alias for PDF rendering.
     * Using a space-free alias prevents CSS parser issues in Flying Saucer where
     * font names with spaces (e.g., "HarmonyOS Sans SC") can be incorrectly split.
     * All fonts (regular, bold, CJK) are registered with this same family name to
     * ensure proper font matching, especially for bold text.
     */
    private static final String PDF_FONT_FAMILY_ALIAS = "PDFFont";
    
    // Debug HTML configuration
    private boolean debugHtmlEnabled = false; // Whether to save intermediate HTML
    private String debugHtmlOutputDirectory = "debug-html"; // Directory for debug HTML files
    private boolean debugHtmlIncludeTimestamp = false; // Whether to include timestamp in filename
    
    public HtmlReportRenderer() {
        this.chartRenderer = new ChartRenderer();
        this.templateEngine = createTemplateEngine();
        this.fontProperties = null; // No custom fonts by default
        checkOpenPdfClasspathConflict();
    }
    
    /**
     * Checks for classpath conflicts between OpenPDF and old iText.
     * When both com.github.librepdf:openpdf and com.lowagie:itext are on the classpath,
     * the JVM may load PdfContentByte from the wrong JAR, causing NoSuchMethodError
     * at runtime (e.g. setColorFill(java.awt.Color) missing).
     */
    private void checkOpenPdfClasspathConflict() {
        try {
            // Verify that the PdfContentByte class has setColorFill(Color) — 
            // this method exists in OpenPDF but not in old iText 4.x
            java.lang.reflect.Method method = com.lowagie.text.pdf.PdfContentByte.class
                    .getMethod("setColorFill", java.awt.Color.class);
            if (method == null) {
                logOpenPdfConflictWarning();
            }
        } catch (NoSuchMethodException e) {
            logOpenPdfConflictWarning();
        } catch (Exception e) {
            // Ignore other reflection errors
        }
    }
    
    private void logOpenPdfConflictWarning() {
        logWarn("╔══════════════════════════════════════════════════════════════════════╗");
        logWarn("║  ⚠️  CLASSPATH CONFLICT: iText / OpenPDF Version Mismatch!          ║");
        logWarn("╚══════════════════════════════════════════════════════════════════════╝");
        logWarn("");
        logWarn("pdf-render requires com.github.librepdf:openpdf, but your project");
        logWarn("has a conflicting com.lowagie:itext JAR on the classpath.");
        logWarn("Both contain com.lowagie.text.pdf.PdfContentByte, causing:");
        logWarn("  NoSuchMethodError: PdfContentByte.setColorFill(Ljava/awt/Color;)V");
        logWarn("");
        logWarn("FIX: Exclude the old iText dependency from your pom.xml:");
        logWarn("  <dependency>");
        logWarn("    <groupId>your-dependency-that-brings-itext</groupId>");
        logWarn("    <artifactId>...</artifactId>");
        logWarn("    <exclusions>");
        logWarn("      <exclusion>");
        logWarn("        <groupId>com.lowagie</groupId>");
        logWarn("        <artifactId>itext</artifactId>");
        logWarn("      </exclusion>");
        logWarn("    </exclusions>");
        logWarn("  </dependency>");
        logWarn("");
        logWarn("Run 'mvn dependency:tree | grep itext' to find the source.");
        logWarn("══════════════════════════════════════════════════════════════════════");
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
        
        // Also configure chart renderer with CJK font (preferred) or regular font for consistent rendering
        // This is wrapped in try-catch to ensure safe initialization
        try {
            if (fontProperties != null) {
                // PRIORITY: Use CJK font for charts if available (better Chinese character support)
                // Otherwise fall back to regular font
                String chartFontPath = fontProperties.getCjkPath();
                if (chartFontPath == null || chartFontPath.isEmpty()) {
                    chartFontPath = fontProperties.getRegularPath();
                }
                
                if (chartFontPath != null && !chartFontPath.isEmpty()) {
                    logInfo("✓ Configuring chart font: " + chartFontPath);
                    chartRenderer.setChartFont(chartFontPath);
                }
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
     * Sets watermark configuration for the PDF renderer.
     * When enabled, a text watermark is rendered on every page.
     *
     * @param watermarkProperties Watermark configuration properties
     */
    public void setWatermarkProperties(PdfRenderProperties.WatermarkProperties watermarkProperties) {
        this.watermarkProperties = watermarkProperties;
    }
    
    /**
     * Gets the current watermark configuration.
     *
     * @return Current watermark properties, or null if not set
     */
    public PdfRenderProperties.WatermarkProperties getWatermarkProperties() {
        return watermarkProperties;
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
        byte[] pdfBytes = convertHtmlToPdf(html);
        
        // Step 4: Add watermark at PDF level using OpenPDF
        // (Flying Saucer does not support CSS flexbox or transform, so watermark
        //  must be applied directly to the PDF for correct centering and rotation)
        if (watermarkProperties != null && watermarkProperties.isEnabled()
                && watermarkProperties.getText() != null && !watermarkProperties.getText().isEmpty()) {
            pdfBytes = addPdfWatermark(pdfBytes);
        }
        
        return pdfBytes;
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
        data.put("coverDisclaimer", reportData.getCoverDisclaimer());
        data.put("footerText", reportData.getFooterText());
        data.put("headerText", reportData.getHeaderText() != null ? reportData.getHeaderText() : (reportData.getTitle() != null ? reportData.getTitle() : ""));
        
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
            
            // NEW APPROACH: Use user-configured font family names instead of hardcoded alias
            // Fonts are now registered with BOTH their real names and alias, so user config works!
            // Font names with spaces are properly quoted to prevent CSS parser issues
            String fontFamilyCss;
            if (fontProperties.getDefaultFamily() != null && !fontProperties.getDefaultFamily().isEmpty()) {
                // User has configured a custom font family - use it with proper quoting and fallbacks
                fontFamilyCss = quoteFontFamilyIfNeeded(fontProperties.getDefaultFamily());
                fontFamilyCss = ensureFallbacks(fontFamilyCss);
            } else {
                // No user configuration - use unified alias only
                fontFamilyCss = "\"" + PDF_FONT_FAMILY_ALIAS + "\", sans-serif";
            }
            data.put("fontFamily", fontFamilyCss);
            logInfo("✓ PDF CSS font-family: " + fontFamilyCss);
            
            // CJK font family configuration
            String cjkFamilyCss;
            if (fontProperties.getCjkFamily() != null && !fontProperties.getCjkFamily().isEmpty()) {
                // User has configured a custom CJK font family - use it with proper quoting and fallbacks
                cjkFamilyCss = quoteFontFamilyIfNeeded(fontProperties.getCjkFamily());
                cjkFamilyCss = ensureFallbacks(cjkFamilyCss);
            } else if (fontProperties.getDefaultFamily() != null && !fontProperties.getDefaultFamily().isEmpty()) {
                // No CJK-specific config, but default family is configured - use default with fallbacks
                cjkFamilyCss = quoteFontFamilyIfNeeded(fontProperties.getDefaultFamily());
                cjkFamilyCss = ensureFallbacks(cjkFamilyCss);
            } else {
                // No configuration - use unified alias only
                cjkFamilyCss = "\"" + PDF_FONT_FAMILY_ALIAS + "\", sans-serif";
            }
            data.put("cjkFontFamily", cjkFamilyCss);
            logInfo("✓ PDF CSS CJK font-family: " + cjkFamilyCss);
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
     * Adds a text watermark to every page of the PDF using OpenPDF.
     * <p>
     * This method post-processes the generated PDF to overlay a rotated, centered,
     * semi-transparent watermark on each page. It bypasses Flying Saucer's CSS
     * limitations (no flexbox, no CSS transforms) by drawing directly on the PDF
     * content layer via {@link PdfContentByte}.
     *
     * @param pdfBytes The original PDF bytes
     * @return PDF bytes with watermark applied on every page
     * @throws IOException if the PDF cannot be read or written
     * @throws DocumentException if the watermark cannot be applied
     */
    private static final int DEFAULT_WATERMARK_R = 204;
    private static final int DEFAULT_WATERMARK_G = 204;
    private static final int DEFAULT_WATERMARK_B = 204;
    private static final String DEFAULT_WATERMARK_COLOR_HEX = "#cccccc";

    private byte[] addPdfWatermark(byte[] pdfBytes) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(pdfBytes);
        ByteArrayOutputStream stamped = new ByteArrayOutputStream();
        try {
            PdfStamper stamper = new PdfStamper(reader, stamped);
            try {
                // Resolve the watermark font — prefer the configured CJK font for
                // Chinese/Japanese/Korean watermark text, fall back to Helvetica.
                BaseFont watermarkFont;
                try {
                    String fontPath = null;
                    if (fontProperties != null && fontProperties.getCjkPath() != null) {
                        fontPath = resolveFontPath(fontProperties.getCjkPath());
                    } else if (fontProperties != null && fontProperties.getRegularPath() != null) {
                        fontPath = resolveFontPath(fontProperties.getRegularPath());
                    }
                    if (fontPath != null) {
                        watermarkFont = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                    } else {
                        watermarkFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
                    }
                } catch (Exception e) {
                    logWarn("⚠️ Could not load watermark font, falling back to Helvetica: " + e.getMessage());
                    watermarkFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
                }
                
                // Parse color from hex string (e.g. "#cccccc")
                String colorHex = watermarkProperties.getColor();
                if (colorHex == null) colorHex = DEFAULT_WATERMARK_COLOR_HEX;
                if (colorHex.startsWith("#")) colorHex = colorHex.substring(1);
                int r = DEFAULT_WATERMARK_R, g = DEFAULT_WATERMARK_G, b = DEFAULT_WATERMARK_B;
                if (colorHex.length() == 6) {
                    try {
                        r = Integer.parseInt(colorHex.substring(0, 2), 16);
                        g = Integer.parseInt(colorHex.substring(2, 4), 16);
                        b = Integer.parseInt(colorHex.substring(4, 6), 16);
                    } catch (NumberFormatException ignored) {
                        // keep defaults
                    }
                }
                
                float fontSize = watermarkProperties.getFontSize();
                float opacity = (float) watermarkProperties.getOpacity();
                float rotationDeg = watermarkProperties.getRotation();
                String text = watermarkProperties.getText();
                
                PdfGState gState = new PdfGState();
                gState.setFillOpacity(opacity);
                
                int totalPages = reader.getNumberOfPages();
                for (int i = 1; i <= totalPages; i++) {
                    PdfContentByte over = stamper.getOverContent(i);
                    
                    // Get page dimensions
                    com.lowagie.text.Rectangle pageSize = reader.getPageSizeWithRotation(i);
                    float pageWidth = pageSize.getWidth();
                    float pageHeight = pageSize.getHeight();
                    
                    over.saveState();
                    over.setGState(gState);
                    over.beginText();
                    over.setFontAndSize(watermarkFont, fontSize);
                    over.setColorFill(new java.awt.Color(r, g, b));
                    // Negate the rotation: CSS rotate() treats positive as clockwise,
                    // while OpenPDF showTextAligned treats positive as counterclockwise.
                    over.showTextAligned(Element.ALIGN_CENTER, text,
                            pageWidth / 2, pageHeight / 2, -rotationDeg);
                    over.endText();
                    over.restoreState();
                }
                
                logInfo("✓ Watermark applied at PDF level: \"" + text + "\" on " + totalPages + " page(s)"
                        + " (rotation=" + rotationDeg + "°, fontSize=" + fontSize + "pt, opacity=" + opacity + ")");
            } finally {
                stamper.close();
            }
        } finally {
            reader.close();
        }
        
        return stamped.toByteArray();
    }
    
    /**
     * Registers custom fonts with the Flying Saucer renderer for PDF embedding.
     * 
     * <p><b>Implementation follows official OpenPDF/Flying Saucer community guidelines for Chinese font support:</b>
     * 
     * <p><b>IMPORTANT: Unified Font Family Alias</b>
     * All fonts (regular, bold, CJK) are registered with the SAME family alias to:
     * <ul>
     * <li>Prevent CSS parser issues with font names containing spaces
     * <li>Ensure bold/italic variants are correctly matched to the same font family
     * <li>Simplify CSS font-family references with a consistent, space-free name
     * </ul>
     * 
     * <p><b>1. Font Registration Method (5-parameter addFont):</b>
     * <pre>
     * renderer.getFontResolver().addFont(
     *     fontPath,              // Path to font file
     *     PDF_FONT_FAMILY_ALIAS, // Unified alias (space-free) for CSS matching
     *     BaseFont.IDENTITY_H,   // REQUIRED for Chinese/Japanese/Korean characters
     *     true,                  // true - embeds font in PDF
     *     null                   // pathToPFB - only for Type 1 fonts
     * );
     * </pre>
     * 
     * <p><b>2. BaseFont.IDENTITY_H Encoding:</b> This is the REQUIRED encoding for CJK (Chinese/Japanese/Korean) 
     * character support. Without this encoding, Chinese characters will display as boxes (□).
     * This is documented in the official Flying Saucer community and widely used in production systems.
     * 
     * <p><b>3. Font Embedding (embedded=true):</b> Embeds the font file directly in the PDF for cross-platform 
     * compatibility. This ensures the PDF displays correctly regardless of whether the font is installed 
     * on the viewer's system.
     * 
     * <p><b>4. Unified Font Family Alias:</b> Using {@link #PDF_FONT_FAMILY_ALIAS} (a space-free name) prevents
     * Flying Saucer's CSS parser from incorrectly splitting font names with spaces (e.g., "HarmonyOS Sans SC"
     * would be split into multiple tokens, causing font matching to fail and falling back to Times-Roman).
     * 
     * <p><b>Official References:</b>
     * <ul>
     * <li>Flying Saucer User's Guide: https://flyingsaucerproject.github.io/flyingsaucer/r8/guide/users-guide-R8.html
     * <li>ITextFontResolver API: https://javadoc.io/doc/org.xhtmlrenderer/flying-saucer-pdf-openpdf/latest
     * <li>OpenPDF (backend): https://github.com/LibrePDF/OpenPDF
     * <li>Community Best Practices (Chinese):
     *   <ul>
     *   <li>https://blog.csdn.net/zhong_jianyu/article/details/96147949
     *   <li>https://blog.51cto.com/u_16175450/6642806
     *   <li>https://stackoverflow.com/questions/7525403/how-to-embed-font-in-pdf-created-from-html-with-itext-and-flying-saucer
     *   </ul>
     * </ul>
     * 
     * <p><b>Expected log output when fonts are successfully registered:</b>
     * <pre>
     *   INFO c.mercury.pdf.render.HtmlReportRenderer : ✓ Font registered with Flying Saucer: /tmp/pdf-render-font-xxx.ttf
     *   INFO c.mercury.pdf.render.HtmlReportRenderer :   Encoding: Identity-H | Embedded: true
     *   INFO c.mercury.pdf.render.HtmlReportRenderer :   Unified family alias: PDFFont
     *   INFO c.mercury.pdf.render.HtmlReportRenderer : ✓ Total fonts registered for PDF: 2
     * </pre>
     */
    private void registerFontsWithRenderer(ITextRenderer renderer) {
        try {
            int fontFilesProcessed = 0;
            java.util.Set<String> registeredNames = new java.util.LinkedHashSet<>();
            java.util.Set<String> disabledSubsetPaths = new java.util.LinkedHashSet<>();
            
            // Register CJK font first (if configured)
            // DUAL REGISTRATION: Register with both real font name AND unified alias for maximum compatibility
            if (fontProperties.getCjkPath() != null) {
                String fontPath = resolveFontPath(fontProperties.getCjkPath());
                fontFilesProcessed++;
                
                // Extract the real font family name from the font file
                String realFontName = null;
                try {
                    realFontName = FontNameExtractor.extractFontFamilyName(fontPath);
                    if (realFontName != null && !realFontName.isEmpty()) {
                        // Register with real font name first (allows user-configured font-family to work)
                        renderer.getFontResolver().addFont(fontPath, realFontName, BaseFont.IDENTITY_H, true, null);
                        registeredNames.add(realFontName);
                        logInfo("✓ CJK font registered with Flying Saucer: " + fontPath);
                        logInfo("  Font family name: " + realFontName);
                        logInfo("  Encoding: " + BaseFont.IDENTITY_H + " | Embedded: true");
                        
                        // Update user's CJK family config to match extracted name if not already set correctly
                        if (fontProperties.getCjkFamily() == null || fontProperties.getCjkFamily().isEmpty() ||
                            !containsFontName(fontProperties.getCjkFamily(), realFontName)) {
                            // Auto-correct the configuration to use extracted font name
                            fontProperties.setCjkFamily(realFontName + ", sans-serif");
                            logInfo("  Auto-configured CJK font-family: " + fontProperties.getCjkFamily());
                        }
                    }
                } catch (Exception e) {
                    logWarn("Could not extract font name from " + fontPath + ": " + e.getMessage());
                }
                
                // Also register with unified alias for backward compatibility
                renderer.getFontResolver().addFont(fontPath, PDF_FONT_FAMILY_ALIAS, BaseFont.IDENTITY_H, true, null);
                registeredNames.add(PDF_FONT_FAMILY_ALIAS);
                logInfo("  Also registered as alias: " + PDF_FONT_FAMILY_ALIAS);
                
                if (fontProperties.isDisableSubsetting()) {
                    disableFontSubsetting(fontPath, disabledSubsetPaths);
                }
            }
            
            // Register regular font with Identity-H encoding for Unicode support
            // DUAL REGISTRATION: Register with both real font name AND unified alias
            if (fontProperties.getRegularPath() != null) {
                String fontPath = resolveFontPath(fontProperties.getRegularPath());
                fontFilesProcessed++;
                
                // Extract the real font family name from the font file
                String realFontName = null;
                try {
                    realFontName = FontNameExtractor.extractFontFamilyName(fontPath);
                    if (realFontName != null && !realFontName.isEmpty()) {
                        // Register with real font name first (allows user-configured font-family to work)
                        renderer.getFontResolver().addFont(fontPath, realFontName, BaseFont.IDENTITY_H, true, null);
                        registeredNames.add(realFontName);
                        logInfo("✓ Regular font registered with Flying Saucer: " + fontPath);
                        logInfo("  Font family name: " + realFontName);
                        logInfo("  Encoding: " + BaseFont.IDENTITY_H + " | Embedded: true");
                        
                        // Update user's default family config to match extracted name if not already set correctly
                        if (fontProperties.getDefaultFamily() == null || fontProperties.getDefaultFamily().isEmpty() ||
                            !containsFontName(fontProperties.getDefaultFamily(), realFontName)) {
                            // Auto-correct the configuration to use extracted font name
                            fontProperties.setDefaultFamily(realFontName + ", sans-serif");
                            logInfo("  Auto-configured default font-family: " + fontProperties.getDefaultFamily());
                        }
                    }
                } catch (Exception e) {
                    logWarn("Could not extract font name from " + fontPath + ": " + e.getMessage());
                }
                
                // Also register with unified alias for backward compatibility
                renderer.getFontResolver().addFont(fontPath, PDF_FONT_FAMILY_ALIAS, BaseFont.IDENTITY_H, true, null);
                registeredNames.add(PDF_FONT_FAMILY_ALIAS);
                logInfo("  Also registered as alias: " + PDF_FONT_FAMILY_ALIAS);
                
                if (fontProperties.isDisableSubsetting()) {
                    disableFontSubsetting(fontPath, disabledSubsetPaths);
                }
            }
            
            // Register bold font with Identity-H encoding
            // DUAL REGISTRATION: Register with both real font name AND unified alias
            if (fontProperties.getBoldPath() != null) {
                String fontPath = resolveFontPath(fontProperties.getBoldPath());
                fontFilesProcessed++;
                
                // Extract the real font family name from the font file
                String realFontName = null;
                try {
                    realFontName = FontNameExtractor.extractFontFamilyName(fontPath);
                    if (realFontName != null && !realFontName.isEmpty()) {
                        // Register with real font name first (allows user-configured font-family to work)
                        renderer.getFontResolver().addFont(fontPath, realFontName, BaseFont.IDENTITY_H, true, null);
                        registeredNames.add(realFontName);
                        logInfo("✓ Bold font registered with Flying Saucer: " + fontPath);
                        logInfo("  Font family name: " + realFontName);
                        logInfo("  Encoding: " + BaseFont.IDENTITY_H + " | Embedded: true");
                    }
                } catch (Exception e) {
                    logWarn("Could not extract font name from " + fontPath + ": " + e.getMessage());
                }
                
                // Also register with unified alias for backward compatibility
                renderer.getFontResolver().addFont(fontPath, PDF_FONT_FAMILY_ALIAS, BaseFont.IDENTITY_H, true, null);
                registeredNames.add(PDF_FONT_FAMILY_ALIAS);
                logInfo("  Also registered as alias: " + PDF_FONT_FAMILY_ALIAS);
                
                if (fontProperties.isDisableSubsetting()) {
                    disableFontSubsetting(fontPath, disabledSubsetPaths);
                }
            }
            
            if (fontFilesProcessed > 0) {
                logInfo("✓ Total font files processed: " + fontFilesProcessed);
                logInfo("  Registered font names: " + String.join(", ", registeredNames));
            }
        } catch (Exception e) {
            // Log the error but don't fail - fall back to default fonts
            logWarn("✗ Warning: Failed to register custom fonts: " + e.getMessage());
            logWarn("Stack trace:", e);
        }
    }
    
    /**
     * Disables font subsetting for a registered font to ensure full font embedding.
     * 
     * <p><b>Why this is needed:</b> OpenPDF's CID font subsetting (all 1.3.x versions
     * through 1.3.43 and even 3.x) passes {@code includeCmap=false, includeExtras=false}
     * to {@code TrueTypeFontSubSet}, creating TrueType subsets containing only basic
     * tables (glyf, head, hhea, hmtx, loca, maxp) and stripping critical tables like
     * {@code cmap}, {@code OS/2}, {@code name}, and {@code post}.
     * While most PDF viewers (Chrome, Adobe Acrobat, MuPDF) handle these minimal subsets,
     * WPS Office requires the missing tables for correct CJK character rendering, causing
     * Chinese text to appear blank.
     * 
     * <p><b>Note:</b> No version of OpenPDF has fixed this root cause. Upgrading OpenPDF
     * alone will not resolve the issue. This workaround (disabling subsetting) remains
     * necessary for WPS Office compatibility.
     * 
     * <p>This method retrieves the cached {@link BaseFont} object (created by the prior
     * {@code addFont} call) and sets {@code subset = false}, so the complete font file
     * is embedded in the PDF with all tables preserved.
     * 
     * @param fontPath Path to the font file (must match the path used in addFont)
     * @param processedPaths Set of paths already processed, to avoid redundant calls
     */
    private void disableFontSubsetting(String fontPath, java.util.Set<String> processedPaths) {
        if (processedPaths.contains(fontPath)) {
            return; // Already disabled for this font file
        }
        try {
            // BaseFont.createFont caches by (path, encoding, embedded), so this returns
            // the same object that addFont stored internally — no new font is created
            BaseFont bf = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            bf.setSubset(false);
            processedPaths.add(fontPath);
            logInfo("  Font subsetting disabled for full embedding (WPS compatibility)");
        } catch (Exception e) {
            logWarn("Could not disable font subsetting: " + e.getMessage());
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

    private void logRegisteredFont(String fontType, String fontPath, String fontFamilyName) {
        logInfo("✓ Font configuration (" + fontType + "):");
        logInfo("  Source path: " + fontPath);
        logInfo("  Resolved family: " + fontFamilyName);
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
     * Checks if a font name is already present in a comma-separated font-family list.
     * Performs exact matching on trimmed font names to avoid false positives.
     * 
     * @param fontFamilyList Comma-separated font family list (e.g., "Arial, Helvetica, sans-serif")
     * @param fontName Font name to search for (e.g., "Arial")
     * @return true if fontName is present in the list
     */
    private boolean containsFontName(String fontFamilyList, String fontName) {
        if (fontFamilyList == null || fontFamilyList.isEmpty() || fontName == null || fontName.isEmpty()) {
            return false;
        }
        
        // Split by comma and check each font
        String[] fonts = fontFamilyList.split(",");
        String normalizedSearchName = fontName.trim().toLowerCase();
        
        for (String font : fonts) {
            // Remove quotes and trim
            String normalizedFont = font.trim()
                .replaceAll("^['\"]|['\"]$", "") // Remove leading/trailing quotes
                .toLowerCase();
            
            if (normalizedFont.equals(normalizedSearchName)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Adds unified alias and sans-serif fallback to a font-family list if not already present.
     * Ensures proper font fallback chain without duplication.
     * 
     * @param fontFamily Base font-family string
     * @return Font-family string with alias and sans-serif added
     */
    private String ensureFallbacks(String fontFamily) {
        if (fontFamily == null || fontFamily.isEmpty()) {
            return "\"" + PDF_FONT_FAMILY_ALIAS + "\", sans-serif";
        }
        
        String result = fontFamily;
        
        // Add unified alias if not present
        if (!containsFontName(result, PDF_FONT_FAMILY_ALIAS)) {
            result = result + ", \"" + PDF_FONT_FAMILY_ALIAS + "\"";
        }
        
        // Add sans-serif if not present
        if (!containsFontName(result, "sans-serif")) {
            result = result + ", sans-serif";
        }
        
        return result;
    }
    
    /**
     * Checks if a font name is a generic CSS font family.
     * Generic families should not be quoted as they serve as fallbacks.
     * 
     * @param fontName The font name to check
     * @return true if the font is a generic family (sans-serif, serif, monospace, etc.)
     */
    private boolean isGenericFontFamily(String fontName) {
        if (fontName == null || fontName.isEmpty()) {
            return false;
        }
        String normalized = fontName.trim().toLowerCase();
        return normalized.equals("sans-serif") || 
               normalized.equals("serif") || 
               normalized.equals("monospace") ||
               normalized.equals("cursive") ||
               normalized.equals("fantasy");
    }
    
    /**
     * Adds quotes around individual font names in a font-family list if they contain spaces.
     * This prevents CSS parser issues with font names like "HarmonyOS Sans SC".
     * Preserves commas and handles multiple fonts in the list.
     * 
     * Example: "HarmonyOS Sans SC, Arial, sans-serif" 
     *       -> "\"HarmonyOS Sans SC\", Arial, sans-serif"
     * 
     * @param fontFamily Font family string (may contain multiple comma-separated fonts)
     * @return Font family string with proper quoting
     */
    private String quoteFontFamilyIfNeeded(String fontFamily) {
        if (fontFamily == null || fontFamily.isEmpty()) {
            return fontFamily;
        }
        
        // Split by comma to handle multiple font names
        String[] fonts = fontFamily.split(",");
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < fonts.length; i++) {
            String font = fonts[i].trim();
            
            // Skip empty entries
            if (font.isEmpty()) {
                continue;
            }
            
            // Don't quote generic families
            if (isGenericFontFamily(font)) {
                result.append(font);
            } 
            // Don't double-quote already quoted fonts
            else if ((font.startsWith("\"") && font.endsWith("\"")) || 
                     (font.startsWith("'") && font.endsWith("'"))) {
                result.append(font);
            }
            // Quote fonts with spaces
            else if (font.contains(" ")) {
                result.append("\"").append(font).append("\"");
            }
            // Single-word fonts don't need quotes
            else {
                result.append(font);
            }
            
            // Add comma separator except for last font
            if (i < fonts.length - 1) {
                result.append(", ");
            }
        }
        
        return result.toString();
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
