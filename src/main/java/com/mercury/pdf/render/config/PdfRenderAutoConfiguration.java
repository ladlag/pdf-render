package com.mercury.pdf.render.config;

import com.mercury.pdf.render.HtmlReportRenderer;
import com.mercury.pdf.render.ReportService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
@Configuration
@ConditionalOnClass(ReportService.class)
@EnableConfigurationProperties(PdfRenderProperties.class)
public class PdfRenderAutoConfiguration {
    
    private final PdfRenderProperties properties;
    
    public PdfRenderAutoConfiguration(PdfRenderProperties properties) {
        this.properties = properties;
    }
    
    /**
     * Creates a configured ReportService bean
     */
    @Bean
    public ReportService reportService() {
        ReportService service = new ReportService();
        
        // Configure HTML renderer with properties
        HtmlReportRenderer htmlRenderer = service.getHtmlRenderer();
        if (htmlRenderer != null) {
            htmlRenderer.setDefaultTemplateName(properties.getTemplate().getDefaultName());
            htmlRenderer.setCacheTemplates(properties.getTemplate().isCacheEnabled());
            
            // Apply font configuration from YAML if configured
            PdfRenderProperties.FontProperties fontProps = properties.getFonts();
            if (fontProps != null && (fontProps.getRegularPath() != null || fontProps.getCjkPath() != null)) {
                FontConfig fontConfig = new FontConfig();
                
                // Set font paths
                if (fontProps.getRegularPath() != null) {
                    fontConfig.setRegularFontPath(fontProps.getRegularPath());
                }
                if (fontProps.getBoldPath() != null) {
                    fontConfig.setBoldFontPath(fontProps.getBoldPath());
                }
                if (fontProps.getCjkPath() != null) {
                    fontConfig.setCjkFontPath(fontProps.getCjkPath());
                }
                
                // Set font families
                if (fontProps.getDefaultFamily() != null) {
                    fontConfig.setDefaultFontFamily(fontProps.getDefaultFamily());
                }
                if (fontProps.getCjkFamily() != null) {
                    fontConfig.setCjkFontFamily(fontProps.getCjkFamily());
                }
                
                // Apply font config to renderer
                htmlRenderer.setFontConfig(fontConfig);
            }
        }
        
        return service;
    }
}
