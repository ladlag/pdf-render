package com.mercury.pdf.render;

import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.config.PdfRenderAutoConfiguration;
import com.mercury.pdf.render.config.PdfRenderProperties;
import com.mercury.pdf.render.model.ReportData;
import com.mercury.pdf.render.model.ReportDataBuilder;
import com.mercury.pdf.render.model.Section;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for Spring Boot auto-configuration.
 * Verifies that font and debug settings from properties are properly applied.
 */
public class SpringBootAutoConfigTest {
    
    private static final String TEST_OUTPUT_DIR = "test-output";
    private static final String DEBUG_HTML_DIR = "test-output/autoconfig-debug";

    @Test
    public void testFontConfigurationFromProperties() throws IOException {
        // Setup properties as they would be loaded from application.yml
        PdfRenderProperties properties = new PdfRenderProperties();
        
        // Configure fonts (simulating application.yml settings)
        properties.getFonts().setRegularPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        properties.getFonts().setDefaultFamily("HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif");
        properties.getFonts().setCjkPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        properties.getFonts().setCjkFamily("HarmonyOS Sans SC, SimSun, sans-serif");
        
        // Create auto-configuration
        PdfRenderAutoConfiguration autoConfig = new PdfRenderAutoConfiguration(properties);
        ReportService service = autoConfig.reportService();
        
        // Verify that font configuration was applied
        assertNotNull(service.getHtmlRenderer());
        FontConfig fontConfig = service.getHtmlRenderer().getFontConfig();
        assertNotNull(fontConfig, "Font configuration should be applied from properties");
        
        // Verify font paths are set
        assertEquals("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf", fontConfig.getRegularFontPath());
        assertEquals("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf", fontConfig.getCjkFontPath());
        
        // Verify font families are set
        assertTrue(fontConfig.getDefaultFontFamily().contains("HarmonyOS Sans SC"));
        assertTrue(fontConfig.getCjkFontFamily().contains("HarmonyOS Sans SC"));
        
        // Test PDF generation with Chinese text
        ReportData reportData = createChineseReportData();
        byte[] pdfBytes = service.generatePdf(reportData, "flexible");
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        
        // Save for verification
        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "autoconfig_chinese_test.pdf");
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, pdfBytes);
        
        System.out.println("✓ Spring Boot auto-configuration: Font config applied successfully");
        System.out.println("  - PDF generated with Chinese fonts: " + outputPath.toAbsolutePath());
    }

    @Test
    public void testDebugConfigurationFromProperties() throws IOException {
        // Setup properties as they would be loaded from application.yml
        PdfRenderProperties properties = new PdfRenderProperties();
        
        // Configure debug settings
        properties.getDebug().setEnabled(true);
        properties.getDebug().setOutputDirectory(DEBUG_HTML_DIR);
        properties.getDebug().setIncludeTimestamp(false);
        
        // Configure minimal font to avoid warnings
        properties.getFonts().setRegularPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        properties.getFonts().setDefaultFamily("HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif");
        
        // Create auto-configuration
        PdfRenderAutoConfiguration autoConfig = new PdfRenderAutoConfiguration(properties);
        ReportService service = autoConfig.reportService();
        
        // Verify that debug configuration was applied
        assertTrue(service.getHtmlRenderer().isDebugHtmlEnabled(), "Debug HTML should be enabled from properties");
        assertEquals(DEBUG_HTML_DIR, service.getHtmlRenderer().getDebugHtmlOutputDirectory());
        assertFalse(service.getHtmlRenderer().isDebugHtmlIncludeTimestamp());
        
        // Generate PDF to trigger debug HTML output
        ReportData reportData = createSimpleReportData();
        byte[] pdfBytes = service.generatePdf(reportData, "flexible");
        
        assertNotNull(pdfBytes);
        
        // Verify debug HTML file was created
        Path debugHtmlPath = Paths.get(DEBUG_HTML_DIR, "flexible.html");
        assertTrue(Files.exists(debugHtmlPath), "Debug HTML file should be created");
        
        String htmlContent = new String(Files.readAllBytes(debugHtmlPath));
        assertTrue(htmlContent.contains("<!DOCTYPE html>"), "HTML should contain DOCTYPE");
        assertTrue(htmlContent.contains("测试报告"), "HTML should contain Chinese text");
        
        System.out.println("✓ Spring Boot auto-configuration: Debug settings applied successfully");
        System.out.println("  - Debug HTML created: " + debugHtmlPath.toAbsolutePath());
    }

    @Test
    public void testAutoConfigWithAllPropertiesSet() throws IOException {
        // Setup properties with all values configured
        PdfRenderProperties properties = new PdfRenderProperties();
        
        // Template settings
        properties.getTemplate().setDefaultName("flexible");
        properties.getTemplate().setLocation("classpath:/templates");
        properties.getTemplate().setCacheEnabled(false);
        
        // Font settings (including CJK)
        properties.getFonts().setRegularPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        properties.getFonts().setBoldPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        properties.getFonts().setCjkPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        properties.getFonts().setDefaultFamily("HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif");
        properties.getFonts().setCjkFamily("HarmonyOS Sans SC, Noto Sans CJK, SimSun, sans-serif");
        
        // Debug settings
        properties.getDebug().setEnabled(true);
        properties.getDebug().setOutputDirectory(DEBUG_HTML_DIR);
        properties.getDebug().setIncludeTimestamp(true);
        
        // Create auto-configuration
        PdfRenderAutoConfiguration autoConfig = new PdfRenderAutoConfiguration(properties);
        ReportService service = autoConfig.reportService();
        
        // Verify all settings were applied
        assertNotNull(service.getHtmlRenderer());
        
        // Check template settings
        assertEquals("flexible", service.getHtmlRenderer().getDefaultTemplateName());
        assertEquals("/templates/", service.getHtmlRenderer().getTemplateLocation());
        
        // Check font settings
        FontConfig fontConfig = service.getHtmlRenderer().getFontConfig();
        assertNotNull(fontConfig);
        assertEquals("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf", fontConfig.getRegularFontPath());
        assertEquals("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf", fontConfig.getBoldFontPath());
        assertEquals("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf", fontConfig.getCjkFontPath());
        
        // Check debug settings
        assertTrue(service.getHtmlRenderer().isDebugHtmlEnabled());
        assertEquals(DEBUG_HTML_DIR, service.getHtmlRenderer().getDebugHtmlOutputDirectory());
        assertTrue(service.getHtmlRenderer().isDebugHtmlIncludeTimestamp());
        
        // Test PDF generation
        ReportData reportData = createChineseReportData();
        byte[] pdfBytes = service.generatePdf(reportData);
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        
        System.out.println("✓ Spring Boot auto-configuration: All properties applied successfully");
        System.out.println("  - Template: " + service.getHtmlRenderer().getDefaultTemplateName());
        System.out.println("  - Fonts configured: Regular, Bold, CJK");
        System.out.println("  - Debug enabled with timestamp");
    }

    @Test
    public void testTemplateLocationNormalization() {
        ReportService service = new ReportService();

        HtmlReportRenderer renderer = service.getHtmlRenderer();
        renderer.setTemplateLocation("templates");
        assertEquals("/templates/", renderer.getTemplateLocation());

        renderer.setTemplateLocation("/custom/path");
        assertEquals("/custom/path/", renderer.getTemplateLocation());

        renderer.setTemplateLocation("classpath:/custom/path/");
        assertEquals("/custom/path/", renderer.getTemplateLocation());

        renderer.setTemplateLocation("   ");
        assertEquals("/templates/", renderer.getTemplateLocation());
    }

    @Test
    public void testAutoConfigWithNoFontsConfigured() {
        // Setup properties with NO explicit fonts configured
        PdfRenderProperties properties = new PdfRenderProperties();
        
        // Create auto-configuration
        PdfRenderAutoConfiguration autoConfig = new PdfRenderAutoConfiguration(properties);
        ReportService service = autoConfig.reportService();
        
        // FontProperties defaults to bundled HarmonyOS font for out-of-the-box CJK support
        FontConfig fontConfig = service.getHtmlRenderer().getFontConfig();
        assertNotNull(fontConfig,
                   "Font config should be auto-configured with bundled CJK font");
        assertEquals("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf", fontConfig.getRegularFontPath(),
                   "Default regular font should be the bundled HarmonyOS font");
        
        System.out.println("✓ Spring Boot auto-configuration: Default CJK font auto-configured");
    }

    /**
     * Creates test data with Chinese text
     */
    private ReportData createChineseReportData() {
        return ReportDataBuilder.create()
            .title("中文字体测试报告")
            .subtitle("Spring Boot 自动配置测试")
            .reportDate("2024-12-31")
            .reportNumber("AUTO-CONFIG-TEST-001")
            .addSection(new Section("第一章：测试内容")
                .addParagraph("这是一个测试Spring Boot自动配置的报告。")
                .addParagraph("如果配置正确，中文应该能够正常显示。"))
            .reportNotice("本报告用于测试自动配置")
            .build();
    }

    /**
     * Creates simple test data
     */
    private ReportData createSimpleReportData() {
        return ReportDataBuilder.create()
            .title("测试报告")
            .reportDate("2024-12-31")
            .addSection(new Section("测试章节")
                .addParagraph("这是一个简单的测试报告。"))
            .build();
    }
}
