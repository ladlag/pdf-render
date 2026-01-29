package com.mercury.pdf.render.config;

import com.mercury.pdf.render.HtmlReportRenderer;
import com.mercury.pdf.render.ReportService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test that verifies PdfRenderAutoConfiguration correctly applies
 * font configuration from YAML properties to the ReportService.
 */
public class PdfRenderAutoConfigurationTest {

    @Test
    public void testFontConfigurationFromProperties() {
        // Create properties with font configuration
        PdfRenderProperties properties = new PdfRenderProperties();
        PdfRenderProperties.FontProperties fontProps = properties.getFonts();
        fontProps.setRegularPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontProps.setDefaultFamily("HarmonyOS Sans SC, DejaVu Sans, sans-serif");

        // Create auto-configuration
        PdfRenderAutoConfiguration autoConfig = new PdfRenderAutoConfiguration(properties);
        ReportService service = autoConfig.reportService();

        // Verify service is created
        assertNotNull(service, "ReportService should be created");

        // Verify HTML renderer is configured
        HtmlReportRenderer htmlRenderer = service.getHtmlRenderer();
        assertNotNull(htmlRenderer, "HtmlReportRenderer should be available");

        // Verify font config is applied
        FontConfig fontConfig = htmlRenderer.getFontConfig();
        assertNotNull(fontConfig, "FontConfig should be applied from YAML properties");
        assertEquals("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf", 
                     fontConfig.getRegularFontPath(),
                     "Regular font path should match YAML configuration");
        assertEquals("HarmonyOS Sans SC, DejaVu Sans, sans-serif",
                     fontConfig.getDefaultFontFamily(),
                     "Default font family should match YAML configuration");
    }

    @Test
    public void testNoFontConfigurationWhenPropertiesNotSet() {
        // Create properties WITHOUT font configuration
        PdfRenderProperties properties = new PdfRenderProperties();
        // Don't set any font properties

        // Create auto-configuration
        PdfRenderAutoConfiguration autoConfig = new PdfRenderAutoConfiguration(properties);
        ReportService service = autoConfig.reportService();

        // Verify service is created
        assertNotNull(service, "ReportService should be created");

        // Verify HTML renderer is configured
        HtmlReportRenderer htmlRenderer = service.getHtmlRenderer();
        assertNotNull(htmlRenderer, "HtmlReportRenderer should be available");

        // Verify no font config is applied (should be null or default)
        FontConfig fontConfig = htmlRenderer.getFontConfig();
        // FontConfig might be null or have null font paths
        if (fontConfig != null) {
            assertNull(fontConfig.getRegularFontPath(),
                      "Regular font path should be null when not configured");
        }
    }

    @Test
    public void testAllFontPropertiesApplied() {
        // Create properties with all font configuration
        PdfRenderProperties properties = new PdfRenderProperties();
        PdfRenderProperties.FontProperties fontProps = properties.getFonts();
        fontProps.setRegularPath("classpath:/fonts/regular.ttf");
        fontProps.setBoldPath("classpath:/fonts/bold.ttf");
        fontProps.setCjkPath("classpath:/fonts/cjk.otf");
        fontProps.setDefaultFamily("Custom Font, sans-serif");
        fontProps.setCjkFamily("CJK Font, sans-serif");

        // Create auto-configuration
        PdfRenderAutoConfiguration autoConfig = new PdfRenderAutoConfiguration(properties);
        ReportService service = autoConfig.reportService();

        // Verify all font properties are applied
        FontConfig fontConfig = service.getHtmlRenderer().getFontConfig();
        assertNotNull(fontConfig, "FontConfig should be created");
        assertEquals("classpath:/fonts/regular.ttf", fontConfig.getRegularFontPath());
        assertEquals("classpath:/fonts/bold.ttf", fontConfig.getBoldFontPath());
        assertEquals("classpath:/fonts/cjk.otf", fontConfig.getCjkFontPath());
        assertEquals("Custom Font, sans-serif", fontConfig.getDefaultFontFamily());
        assertEquals("CJK Font, sans-serif", fontConfig.getCjkFontFamily());
    }

    @Test
    public void testTemplatePropertiesApplied() {
        // Create properties with template configuration
        PdfRenderProperties properties = new PdfRenderProperties();
        properties.getTemplate().setDefaultName("custom-template");
        properties.getTemplate().setCacheEnabled(false);

        // Create auto-configuration
        PdfRenderAutoConfiguration autoConfig = new PdfRenderAutoConfiguration(properties);
        ReportService service = autoConfig.reportService();

        // Verify template properties are applied
        HtmlReportRenderer htmlRenderer = service.getHtmlRenderer();
        assertNotNull(htmlRenderer);
        assertEquals("custom-template", htmlRenderer.getDefaultTemplateName(),
                    "Default template name should match configuration");
        // Note: isCacheTemplates() is package-private, so we can't test it directly here
    }
}
