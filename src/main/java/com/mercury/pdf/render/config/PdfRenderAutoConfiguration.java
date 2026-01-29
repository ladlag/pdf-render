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
            // Configure template settings
            htmlRenderer.setDefaultTemplateName(properties.getTemplate().getDefaultName());
            htmlRenderer.setCacheTemplates(properties.getTemplate().isCacheEnabled());
            
            // Configure font settings for Chinese/CJK character support
            PdfRenderProperties.FontProperties fontProps = properties.getFonts();
            if (fontProps != null && (fontProps.getRegularPath() != null || fontProps.getCjkPath() != null)) {
                FontConfig fontConfig = new FontConfig();
                
                if (fontProps.getRegularPath() != null) {
                    fontConfig.setRegularFontPath(fontProps.getRegularPath());
                }
                if (fontProps.getBoldPath() != null) {
                    fontConfig.setBoldFontPath(fontProps.getBoldPath());
                }
                if (fontProps.getCjkPath() != null) {
                    fontConfig.setCjkFontPath(fontProps.getCjkPath());
                }
                if (fontProps.getDefaultFamily() != null) {
                    fontConfig.setDefaultFontFamily(fontProps.getDefaultFamily());
                }
                if (fontProps.getCjkFamily() != null) {
                    fontConfig.setCjkFontFamily(fontProps.getCjkFamily());
                }
                
                htmlRenderer.setFontConfig(fontConfig);
            }
            
            // Configure debug HTML output settings
            PdfRenderProperties.DebugProperties debugProps = properties.getDebug();
            if (debugProps != null) {
                htmlRenderer.setDebugHtmlEnabled(debugProps.isEnabled());
                if (debugProps.getOutputDirectory() != null) {
                    htmlRenderer.setDebugHtmlOutputDirectory(debugProps.getOutputDirectory());
                }
                htmlRenderer.setDebugHtmlIncludeTimestamp(debugProps.isIncludeTimestamp());
            }
        }
        
        return service;
    }
}
