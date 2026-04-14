package com.mercury.pdf.render;

import com.mercury.pdf.render.config.PdfRenderProperties;
import com.mercury.pdf.render.model.ReportData;
import com.mercury.pdf.render.model.ReportDataBuilder;
import com.mercury.pdf.render.model.Section;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PDF watermark functionality.
 * Validates that watermark text can be configured and rendered in generated PDFs.
 */
public class WatermarkTest {

    private static final String TEST_OUTPUT_DIR = System.getProperty("test.output.dir", "test-output");

    @Test
    public void testWatermarkEnabled() throws IOException {
        ReportData report = ReportDataBuilder.create()
            .title("Watermark Test Report")
            .addSection(new Section("Content")
                .addParagraph("This report should have a watermark."))
            .build();

        PdfRenderService service = new PdfRenderService();
        service.getHtmlRenderer().setDefaultTemplateName("flexible");
        configureChineseFont(service);

        // Configure watermark
        PdfRenderProperties.WatermarkProperties watermark = new PdfRenderProperties.WatermarkProperties();
        watermark.setEnabled(true);
        watermark.setText("CONFIDENTIAL");
        watermark.setFontSize(60);
        watermark.setColor("#cccccc");
        watermark.setOpacity(0.3);
        watermark.setRotation(-30);
        service.getHtmlRenderer().setWatermarkProperties(watermark);

        // Enable debug HTML to verify watermark in HTML output
        service.getHtmlRenderer().setDebugHtmlEnabled(true);
        service.getHtmlRenderer().setDebugHtmlOutputDirectory("test-output/debug-html");
        service.getHtmlRenderer().setDebugHtmlIncludeTimestamp(true);

        byte[] pdfBytes = service.generatePdf(report);

        assertNotNull(pdfBytes, "PDF bytes should not be null");
        assertTrue(pdfBytes.length > 0, "PDF should not be empty");

        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "watermark_test.pdf");
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, pdfBytes);
        System.out.println("✓ Watermark PDF generated: " + outputPath.toAbsolutePath());
    }

    @Test
    public void testChineseWatermark() throws IOException {
        ReportData report = ReportDataBuilder.create()
            .title("中文水印测试")
            .addSection(new Section("内容")
                .addParagraph("本报告包含中文水印。"))
            .build();

        PdfRenderService service = new PdfRenderService();
        service.getHtmlRenderer().setDefaultTemplateName("flexible");
        configureChineseFont(service);

        // Configure Chinese watermark
        PdfRenderProperties.WatermarkProperties watermark = new PdfRenderProperties.WatermarkProperties();
        watermark.setEnabled(true);
        watermark.setText("内部资料");
        watermark.setFontSize(50);
        watermark.setColor("#dddddd");
        watermark.setOpacity(0.2);
        watermark.setRotation(-45);
        service.getHtmlRenderer().setWatermarkProperties(watermark);

        byte[] pdfBytes = service.generatePdf(report);

        assertNotNull(pdfBytes, "PDF bytes should not be null");
        assertTrue(pdfBytes.length > 0, "PDF should not be empty");

        // Verify CJK font is embedded
        String pdfContent = new String(pdfBytes, StandardCharsets.ISO_8859_1);
        assertTrue(pdfContent.contains("Identity-H"),
            "PDF must contain Identity-H encoding for CJK watermark text");

        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "watermark_chinese_test.pdf");
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, pdfBytes);
        System.out.println("✓ Chinese watermark PDF generated: " + outputPath.toAbsolutePath());
    }

    @Test
    public void testWatermarkDisabledByDefault() throws IOException {
        ReportData report = ReportDataBuilder.create()
            .title("No Watermark Report")
            .addSection(new Section("Content")
                .addParagraph("This report should NOT have a watermark."))
            .build();

        PdfRenderService service = new PdfRenderService();
        service.getHtmlRenderer().setDefaultTemplateName("flexible");

        // No watermark configured — default behavior
        byte[] pdfBytes = service.generatePdf(report);

        assertNotNull(pdfBytes, "PDF bytes should not be null");
        assertTrue(pdfBytes.length > 0, "PDF should not be empty");

        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "no_watermark_test.pdf");
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, pdfBytes);
        System.out.println("✓ No-watermark PDF generated: " + outputPath.toAbsolutePath());
    }

    @Test
    public void testWatermarkWithFinancialReportTemplate() throws IOException {
        ReportData report = ReportDataBuilder.create()
            .title("Financial Watermark Test")
            .subtitle("Watermark on financial report template")
            .reportDate("2025-06-30")
            .addSection(new Section("Summary")
                .withMarkdownContent("## Test\n\nThis financial report has a watermark.\n\n| Item | Value |\n|------|-------|\n| Revenue | 100 |\n| Cost | 80 |"))
            .build();

        PdfRenderService service = new PdfRenderService();
        service.getHtmlRenderer().setDefaultTemplateName("financial-report");
        configureChineseFont(service);

        // Configure watermark
        PdfRenderProperties.WatermarkProperties watermark = new PdfRenderProperties.WatermarkProperties();
        watermark.setEnabled(true);
        watermark.setText("DRAFT");
        watermark.setFontSize(80);
        watermark.setColor("#ff9999");
        watermark.setOpacity(0.15);
        watermark.setRotation(-25);
        service.getHtmlRenderer().setWatermarkProperties(watermark);

        byte[] pdfBytes = service.generatePdf(report);

        assertNotNull(pdfBytes, "PDF bytes should not be null");
        assertTrue(pdfBytes.length > 0, "PDF should not be empty");

        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "watermark_financial_report.pdf");
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, pdfBytes);
        System.out.println("✓ Financial report with watermark PDF generated: " + outputPath.toAbsolutePath());
    }

    @Test
    public void testWatermarkPropertiesDefaults() {
        PdfRenderProperties.WatermarkProperties props = new PdfRenderProperties.WatermarkProperties();
        
        assertFalse(props.isEnabled(), "Watermark should be disabled by default");
        assertEquals("", props.getText(), "Default text should be empty");
        assertEquals(60, props.getFontSize(), "Default font size should be 60");
        assertEquals("#cccccc", props.getColor(), "Default color should be #cccccc");
        assertEquals(0.3, props.getOpacity(), 0.001, "Default opacity should be 0.3");
        assertEquals(-30, props.getRotation(), "Default rotation should be -30");
    }

    /**
     * Configures Chinese font for CJK watermark text support.
     */
    private void configureChineseFont(PdfRenderService service) {
        PdfRenderProperties.FontProperties fontProps = new PdfRenderProperties.FontProperties();
        fontProps.setRegularPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontProps.setCjkPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        service.getHtmlRenderer().setFontProperties(fontProps);
    }
}
