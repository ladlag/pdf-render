package com.mercury.pdf.render;

import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.config.PdfRenderAutoConfiguration;
import com.mercury.pdf.render.config.PdfRenderProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for Spring Boot auto-configuration.
 * Verifies that PdfRenderProperties are correctly applied to ReportService.
 */
public class PdfRenderAutoConfigurationTest {

    @Test
    public void testAutoConfigurationAppliesFontSettings() {
        // Create properties with font configuration
        PdfRenderProperties properties = new PdfRenderProperties();
        properties.getFonts().setRegularPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        properties.getFonts().setBoldPath("classpath:/fonts/HarmonyOS_Sans_SC_Bold.ttf");
        properties.getFonts().setCjkPath("classpath:/fonts/NotoSansCJKsc-Regular.otf");
        properties.getFonts().setDefaultFamily("HarmonyOS Sans SC, DejaVu Sans, sans-serif");
        properties.getFonts().setCjkFamily("Noto Sans CJK SC, SimSun, sans-serif");

        // Create auto-configuration
        PdfRenderAutoConfiguration config = new PdfRenderAutoConfiguration(properties);
        ReportService service = config.reportService();

        // Verify font configuration was applied
        assertNotNull(service);
        assertNotNull(service.getHtmlRenderer());
        
        FontConfig fontConfig = service.getHtmlRenderer().getFontConfig();
        assertNotNull(fontConfig, "FontConfig should be applied when font paths are provided");
        assertEquals("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf", fontConfig.getRegularFontPath());
        assertEquals("classpath:/fonts/HarmonyOS_Sans_SC_Bold.ttf", fontConfig.getBoldFontPath());
        assertEquals("classpath:/fonts/NotoSansCJKsc-Regular.otf", fontConfig.getCjkFontPath());
        assertEquals("HarmonyOS Sans SC, DejaVu Sans, sans-serif", fontConfig.getDefaultFontFamily());
        assertEquals("Noto Sans CJK SC, SimSun, sans-serif", fontConfig.getCjkFontFamily());
        
        System.out.println("✓ Font configuration correctly applied from PdfRenderProperties");
        System.out.println("  Regular font: " + fontConfig.getRegularFontPath());
        System.out.println("  Bold font: " + fontConfig.getBoldFontPath());
        System.out.println("  CJK font: " + fontConfig.getCjkFontPath());
    }

    @Test
    public void testAutoConfigurationAppliesDebugSettings() {
        // Create properties with debug configuration
        PdfRenderProperties properties = new PdfRenderProperties();
        properties.getDebug().setEnabled(true);
        properties.getDebug().setOutputDirectory("custom-debug-dir");
        properties.getDebug().setIncludeTimestamp(true);

        // Create auto-configuration
        PdfRenderAutoConfiguration config = new PdfRenderAutoConfiguration(properties);
        ReportService service = config.reportService();

        // Verify debug configuration was applied
        assertNotNull(service);
        assertNotNull(service.getHtmlRenderer());
        
        HtmlReportRenderer renderer = service.getHtmlRenderer();
        assertTrue(renderer.isDebugHtmlEnabled(), "Debug HTML should be enabled");
        assertEquals("custom-debug-dir", renderer.getDebugHtmlOutputDirectory());
        assertTrue(renderer.isDebugHtmlIncludeTimestamp(), "Timestamp should be included");
        
        System.out.println("✓ Debug configuration correctly applied from PdfRenderProperties");
        System.out.println("  Debug enabled: " + renderer.isDebugHtmlEnabled());
        System.out.println("  Output directory: " + renderer.getDebugHtmlOutputDirectory());
        System.out.println("  Include timestamp: " + renderer.isDebugHtmlIncludeTimestamp());
    }

    @Test
    public void testAutoConfigurationWithCjkPathOnly() {
        // Test with only CJK path configured (common use case for Chinese-only projects)
        PdfRenderProperties properties = new PdfRenderProperties();
        properties.getFonts().setCjkPath("classpath:/fonts/NotoSansCJKsc-Regular.otf");
        properties.getFonts().setCjkFamily("Noto Sans CJK SC, sans-serif");

        // Create auto-configuration
        PdfRenderAutoConfiguration config = new PdfRenderAutoConfiguration(properties);
        ReportService service = config.reportService();

        // Verify CJK font configuration was applied
        assertNotNull(service);
        FontConfig fontConfig = service.getHtmlRenderer().getFontConfig();
        assertNotNull(fontConfig, "FontConfig should be created when only CJK path is provided");
        assertNull(fontConfig.getRegularFontPath(), "Regular font path should be null");
        assertNull(fontConfig.getBoldFontPath(), "Bold font path should be null");
        assertEquals("classpath:/fonts/NotoSansCJKsc-Regular.otf", fontConfig.getCjkFontPath());
        assertEquals("Noto Sans CJK SC, sans-serif", fontConfig.getCjkFontFamily());
        
        System.out.println("✓ CJK-only configuration correctly applied");
        System.out.println("  CJK font: " + fontConfig.getCjkFontPath());
        System.out.println("  CJK family: " + fontConfig.getCjkFontFamily());
    }

    @Test
    public void testAutoConfigurationWithNoFontSettings() {
        // Test with no font configuration (should not create FontConfig)
        PdfRenderProperties properties = new PdfRenderProperties();
        // Don't set any font paths

        // Create auto-configuration
        PdfRenderAutoConfiguration config = new PdfRenderAutoConfiguration(properties);
        ReportService service = config.reportService();

        // Verify no font configuration was created
        assertNotNull(service);
        FontConfig fontConfig = service.getHtmlRenderer().getFontConfig();
        assertNull(fontConfig, "FontConfig should be null when no font paths are configured");
        
        System.out.println("✓ No font configuration created when paths are not provided (as expected)");
    }

    @Test
    public void testAutoConfigurationWithTemplateSettings() {
        // Create properties with template configuration
        PdfRenderProperties properties = new PdfRenderProperties();
        properties.getTemplate().setDefaultName("custom-template");
        properties.getTemplate().setCacheEnabled(false);

        // Create auto-configuration
        PdfRenderAutoConfiguration config = new PdfRenderAutoConfiguration(properties);
        ReportService service = config.reportService();

        // Verify template configuration was applied
        assertNotNull(service);
        HtmlReportRenderer renderer = service.getHtmlRenderer();
        assertEquals("custom-template", renderer.getDefaultTemplateName());
        // Note: We can't easily test cache setting without reflection or exposing the value
        
        System.out.println("✓ Template configuration correctly applied from PdfRenderProperties");
        System.out.println("  Default template name: " + renderer.getDefaultTemplateName());
    }
}
