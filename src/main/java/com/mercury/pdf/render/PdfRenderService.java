package com.mercury.pdf.render;

import com.mercury.pdf.render.model.ReportData;
import com.lowagie.text.DocumentException;

import java.io.IOException;

/**
 * Main service for generating PDF reports.
 * Uses HTML/CSS -> PDF pipeline with Flying Saucer and OpenPDF.
 * Supports unlimited flexible sections through the Section model.
 *
 * <p>Usage:
 * <pre>
 * PdfRenderService service = new PdfRenderService();
 * // Configure via HtmlReportRenderer
 * service.getHtmlRenderer().setDefaultTemplateName("flexible");
 * service.getHtmlRenderer().setFontProperties(fontProps);
 * // Generate PDF
 * byte[] pdf = service.generatePdf(reportData);
 * </pre>
 *
 * <p>In a Spring Boot project, this bean is auto-configured via
 * {@link com.mercury.pdf.render.config.PdfRenderAutoConfiguration}.
 * Simply inject it:
 * <pre>
 * &#064;Autowired
 * private PdfRenderService pdfRenderService;
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
     * Gets the HTML report renderer for configuration purposes.
     * Use this to configure templates, caching, fonts, watermarks, debug settings, etc.
     *
     * <p>Example:
     * <pre>
     * HtmlReportRenderer renderer = service.getHtmlRenderer();
     * renderer.setDefaultTemplateName("flexible");
     * renderer.setFontProperties(fontProps);
     * renderer.setWatermarkProperties(watermarkProps);
     * renderer.setDebugHtmlEnabled(true);
     * </pre>
     * 
     * @return The HTML report renderer instance
     */
    public HtmlReportRenderer getHtmlRenderer() {
        return htmlRenderer;
    }
}
