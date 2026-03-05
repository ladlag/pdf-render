package com.mercury.pdf.render;

import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.config.PdfRenderProperties;
import com.mercury.pdf.render.model.ReportData;
import com.lowagie.text.DocumentException;

import java.io.IOException;

/**
 * Main service for generating PDF reports.
 * Uses HTML/CSS -> PDF pipeline with Flying Saucer and OpenPDF.
 * Supports unlimited flexible sections through the Section model.
 *
 * <p>This class provides both PDF generation methods and convenient
 * configuration methods so users can configure fonts, templates,
 * watermarks, and debug settings directly without accessing the
 * underlying {@link HtmlReportRenderer}.
 *
 * <p>Example usage:
 * <pre>
 * PdfRenderService service = new PdfRenderService();
 * service.setDefaultTemplateName("invoice");
 * byte[] pdf = service.generatePdf(reportData);
 * </pre>
 */
public class PdfRenderService {
    
    private final HtmlReportRenderer htmlRenderer = new HtmlReportRenderer();

    /**
     * Generates a PDF report and returns it as a byte array.
     * Uses the flexible Section model for unlimited sections.
     */
    public byte[] generatePdf(ReportData reportData) throws IOException {
        return generatePdf(reportData, null);
    }
    
    /**
     * Generates a PDF report using a specific template and returns it as a byte array.
     * 
     * @param reportData Report data to render with flexible sections
     * @param templateName Template name without .html extension (e.g., "report", "invoice"). 
     *                     If null, uses the default template.
     * @return PDF content as byte array
     */
    public byte[] generatePdf(ReportData reportData, String templateName) throws IOException {
        try {
            if (templateName != null && !templateName.isEmpty()) {
                return htmlRenderer.generatePdf(reportData, templateName);
            } else {
                return htmlRenderer.generatePdf(reportData);
            }
        } catch (DocumentException e) {
            throw new IOException("Failed to generate PDF from HTML", e);
        }
    }
    
    /**
     * Gets the HTML report renderer for advanced configuration.
     * 
     * <p>For common configuration, prefer using the convenience methods on this class directly:
     * {@link #setDefaultTemplateName(String)}, {@link #setFontProperties(PdfRenderProperties.FontProperties)}, etc.
     * 
     * @return The HTML report renderer instance
     */
    public HtmlReportRenderer getHtmlRenderer() {
        return htmlRenderer;
    }
    
    // ===== Template configuration =====
    
    /**
     * Sets the default template name to use when no template is specified.
     * 
     * @param templateName Template name without .html extension (e.g., "report", "invoice")
     */
    public void setDefaultTemplateName(String templateName) {
        htmlRenderer.setDefaultTemplateName(templateName);
    }
    
    /**
     * Gets the current default template name.
     */
    public String getDefaultTemplateName() {
        return htmlRenderer.getDefaultTemplateName();
    }
    
    /**
     * Sets the template location prefix. Supports classpath: prefix.
     *
     * @param location Template location (e.g., "classpath:/templates/")
     */
    public void setTemplateLocation(String location) {
        htmlRenderer.setTemplateLocation(location);
    }
    
    /**
     * Gets the current template location prefix.
     */
    public String getTemplateLocation() {
        return htmlRenderer.getTemplateLocation();
    }
    
    /**
     * Sets whether templates should be cached (recommended for production).
     * 
     * @param cacheTemplates true to enable caching, false to disable
     */
    public void setCacheTemplates(boolean cacheTemplates) {
        htmlRenderer.setCacheTemplates(cacheTemplates);
    }
    
    // ===== Font configuration =====
    
    /**
     * Sets font configuration from Spring Boot properties.
     * This is the recommended way to configure fonts for Chinese/CJK support.
     *
     * @param fontProperties Font configuration properties
     */
    public void setFontProperties(PdfRenderProperties.FontProperties fontProperties) {
        htmlRenderer.setFontProperties(fontProperties);
    }
    
    /**
     * Gets the current font properties configuration.
     */
    public PdfRenderProperties.FontProperties getFontProperties() {
        return htmlRenderer.getFontProperties();
    }
    
    /**
     * Sets font configuration using the legacy FontConfig object.
     *
     * @param fontConfig Font configuration
     * @deprecated Use {@link #setFontProperties(PdfRenderProperties.FontProperties)} instead
     */
    @Deprecated
    public void setFontConfig(FontConfig fontConfig) {
        htmlRenderer.setFontConfig(fontConfig);
    }
    
    /**
     * Gets the current legacy font configuration.
     *
     * @deprecated Use {@link #getFontProperties()} instead
     */
    @Deprecated
    public FontConfig getFontConfig() {
        return htmlRenderer.getFontConfig();
    }
    
    // ===== Watermark configuration =====
    
    /**
     * Sets watermark configuration. When enabled, a text watermark is added to every page.
     *
     * @param watermarkProperties Watermark configuration properties
     */
    public void setWatermarkProperties(PdfRenderProperties.WatermarkProperties watermarkProperties) {
        htmlRenderer.setWatermarkProperties(watermarkProperties);
    }
    
    /**
     * Gets the current watermark configuration.
     */
    public PdfRenderProperties.WatermarkProperties getWatermarkProperties() {
        return htmlRenderer.getWatermarkProperties();
    }
    
    // ===== Debug configuration =====
    
    /**
     * Enables or disables saving intermediate HTML before PDF conversion.
     * Useful for debugging template rendering issues.
     *
     * @param enabled true to enable debug HTML output
     */
    public void setDebugHtmlEnabled(boolean enabled) {
        htmlRenderer.setDebugHtmlEnabled(enabled);
    }
    
    /**
     * Returns whether debug HTML output is enabled.
     */
    public boolean isDebugHtmlEnabled() {
        return htmlRenderer.isDebugHtmlEnabled();
    }
    
    /**
     * Sets the directory where debug HTML files will be saved.
     *
     * @param directory Output directory path (default: "debug-html")
     */
    public void setDebugHtmlOutputDirectory(String directory) {
        htmlRenderer.setDebugHtmlOutputDirectory(directory);
    }
    
    /**
     * Gets the current debug HTML output directory.
     */
    public String getDebugHtmlOutputDirectory() {
        return htmlRenderer.getDebugHtmlOutputDirectory();
    }
    
    /**
     * Sets whether to include a timestamp in the debug HTML filename.
     *
     * @param includeTimestamp true to include timestamp
     */
    public void setDebugHtmlIncludeTimestamp(boolean includeTimestamp) {
        htmlRenderer.setDebugHtmlIncludeTimestamp(includeTimestamp);
    }
    
    /**
     * Returns whether timestamps are included in debug HTML filenames.
     */
    public boolean isDebugHtmlIncludeTimestamp() {
        return htmlRenderer.isDebugHtmlIncludeTimestamp();
    }
}
