package com.mercury.pdf.render.config;

import com.mercury.pdf.render.HtmlReportRenderer;
import com.mercury.pdf.render.PdfRenderService;
import com.mercury.pdf.render.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
 *     default-family: HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif
 *     cjk-family: Noto Sans CJK, SimSun, sans-serif
 *     disable-subsetting: true
 *   debug:
 *     enabled: false
 *     output-directory: debug-html
 *     include-timestamp: false
 *   watermark:
 *     enabled: false
 *     text: CONFIDENTIAL
 *     font-size: 60
 *     color: "#cccccc"
 *     opacity: 0.3
 *     rotation: -30
 * </pre>
 */
@Configuration
@ConditionalOnClass(PdfRenderService.class)
@EnableConfigurationProperties(PdfRenderProperties.class)
public class PdfRenderAutoConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(PdfRenderAutoConfiguration.class);
    
    private final PdfRenderProperties properties;
    
    public PdfRenderAutoConfiguration(PdfRenderProperties properties) {
        this.properties = properties;
    }
    
    /**
     * Creates a configured PdfRenderService bean with full property configuration.
     * Uses @ConditionalOnMissingBean so host applications can override with their own bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public PdfRenderService pdfRenderService() {
        PdfRenderService service = new PdfRenderService();
        configureService(service);
        return service;
    }
    
    /**
     * Creates a backward-compatible ReportService bean.
     * This allows existing code that injects {@code ReportService} to continue working.
     *
     * @deprecated Inject {@link PdfRenderService} instead.
     */
    @Bean
    @ConditionalOnMissingBean(ReportService.class)
    @Deprecated
    public ReportService reportService() {
        ReportService service = new ReportService();
        configureService(service);
        return service;
    }
    
    /**
     * Applies all configuration properties to a PdfRenderService instance.
     */
    private void configureService(PdfRenderService service) {
        HtmlReportRenderer htmlRenderer = service.getHtmlRenderer();
        if (htmlRenderer != null) {
            // Configure template settings
            htmlRenderer.setDefaultTemplateName(properties.getTemplate().getDefaultName());
            htmlRenderer.setCacheTemplates(properties.getTemplate().isCacheEnabled());
            htmlRenderer.setTemplateLocation(properties.getTemplate().getLocation());
            
            // Configure font settings for Chinese character support
            configureFonts(htmlRenderer);
            
            // Configure debug HTML output settings
            configureDebugSettings(htmlRenderer);
            
            // Configure watermark settings
            configureWatermark(htmlRenderer);
        }
    }
    
    /**
     * Configures font settings from properties.
     * This is critical for Chinese character display - if fonts are not configured,
     * Chinese characters will appear as boxes (□) in the generated PDF.
     */
    private void configureFonts(HtmlReportRenderer htmlRenderer) {
        PdfRenderProperties.FontProperties fontProps = properties.getFonts();
        
        // Only configure if at least one font path is specified
        if (fontProps.getRegularPath() != null || 
            fontProps.getBoldPath() != null || 
            fontProps.getCjkPath() != null) {
            
            // Pass the FontProperties directly - no need for intermediate FontConfig object
            htmlRenderer.setFontProperties(fontProps);
            
            logger.info("PDF Render: Font configuration applied from properties");
            if (fontProps.getCjkPath() != null) {
                logger.info("  - CJK font: {}", fontProps.getCjkPath());
            }
            if (fontProps.getRegularPath() != null) {
                logger.info("  - Regular font: {}", fontProps.getRegularPath());
            }
        }
    }
    
    /**
     * Configures debug HTML output settings from properties.
     * When enabled, intermediate HTML will be saved before PDF conversion for debugging.
     */
    private void configureDebugSettings(HtmlReportRenderer htmlRenderer) {
        PdfRenderProperties.DebugProperties debugProps = properties.getDebug();
        
        if (debugProps.isEnabled()) {
            htmlRenderer.setDebugHtmlEnabled(true);
            htmlRenderer.setDebugHtmlOutputDirectory(debugProps.getOutputDirectory());
            htmlRenderer.setDebugHtmlIncludeTimestamp(debugProps.isIncludeTimestamp());
            
            logger.info("PDF Render: Debug HTML output enabled");
            logger.info("  - Output directory: {}", debugProps.getOutputDirectory());
            logger.info("  - Include timestamp: {}", debugProps.isIncludeTimestamp());
        }
    }
    
    /**
     * Configures watermark settings from properties.
     * When enabled, a text watermark is added to every page of the generated PDF.
     */
    private void configureWatermark(HtmlReportRenderer htmlRenderer) {
        PdfRenderProperties.WatermarkProperties watermarkProps = properties.getWatermark();
        
        if (watermarkProps.isEnabled()) {
            htmlRenderer.setWatermarkProperties(watermarkProps);
            
            logger.info("PDF Render: Watermark enabled");
            logger.info("  - Text: {}", watermarkProps.getText());
            logger.info("  - Font size: {}pt", watermarkProps.getFontSize());
            logger.info("  - Opacity: {}", watermarkProps.getOpacity());
        }
    }
}
