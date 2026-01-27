package com.finos.matcher.report.config;

import com.finos.matcher.report.HtmlReportRenderer;
import com.finos.matcher.report.ReportService;

/**
 * Spring Boot auto-configuration for PDF rendering.
 * This class will be automatically detected by Spring Boot when the library is on the classpath.
 * 
 * Configuration can be provided in application.yml:
 * <pre>
 * pdf-render:
 *   template:
 *     location: classpath:/templates/
 *     default-name: report
 *     cache-enabled: true
 *   output:
 *     directory: /var/pdfs
 *     save-to-directory: false
 *   fonts:
 *     regular-path: classpath:/fonts/custom-regular.ttf
 *     bold-path: classpath:/fonts/custom-bold.ttf
 *     cjk-path: classpath:/fonts/NotoSansCJK-Regular.otf
 * </pre>
 */
public class PdfRenderAutoConfiguration {
    
    private final PdfRenderProperties properties;
    
    public PdfRenderAutoConfiguration(PdfRenderProperties properties) {
        this.properties = properties;
    }
    
    /**
     * Creates a configured ReportService bean
     */
    public ReportService reportService() {
        ReportService service = new ReportService();
        
        // Configure HTML renderer with properties
        HtmlReportRenderer htmlRenderer = service.getHtmlRenderer();
        if (htmlRenderer != null) {
            htmlRenderer.setDefaultTemplateName(properties.getTemplate().getDefaultName());
            htmlRenderer.setCacheTemplates(properties.getTemplate().isCacheEnabled());
        }
        
        return service;
    }
    
    /**
     * Creates a PdfRenderProperties bean that can be injected into other components
     */
    public PdfRenderProperties pdfRenderProperties() {
        return properties;
    }
}
