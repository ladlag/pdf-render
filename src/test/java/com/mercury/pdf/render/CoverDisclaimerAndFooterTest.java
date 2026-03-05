package com.mercury.pdf.render;

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
 * Tests for cover-disclaimer positioning at page bottom and custom footer text support.
 */
public class CoverDisclaimerAndFooterTest {

    private static final String TEST_OUTPUT_DIR = System.getProperty("test.output.dir", "test-output");

    @Test
    public void testCoverDisclaimerAndFooterWithFinancialReport() throws IOException {
        // Build a report with coverDisclaimer and footerText
        ReportData report = ReportDataBuilder.create()
            .title("测试报告")
            .subtitle("封面声明与页脚测试")
            .reportDate("2025-01-01")
            .reportNumber("RPT-2025-001")
            .coverDisclaimer("免责声明：本报告仅供内部参考，不构成投资建议")
            .footerText("机密文件 - 仅限内部使用")
            .addSection(new Section("第一章 概述")
                .addParagraph("本报告旨在测试封面声明显示在页面底部的功能。")
                .addParagraph("同时测试页脚自定义文本功能。"))
            .build();

        PdfRenderService service = new PdfRenderService();
        service.getHtmlRenderer().setDefaultTemplateName("financial-report");
        configureChineseFont(service);

        // Enable debug HTML for visual inspection
        service.getHtmlRenderer().setDebugHtmlEnabled(true);
        service.getHtmlRenderer().setDebugHtmlOutputDirectory("test-output/debug-html");

        byte[] pdfBytes = service.generatePdf(report);

        assertNotNull(pdfBytes, "PDF bytes should not be null");
        assertTrue(pdfBytes.length > 0, "PDF should not be empty");

        // Save the output PDF
        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "cover_disclaimer_footer_financial.pdf");
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, pdfBytes);
        System.out.println("✓ Cover disclaimer & footer test PDF (financial-report): " + outputPath.toAbsolutePath());
        System.out.println("  PDF size: " + pdfBytes.length + " bytes (" + (pdfBytes.length / 1024) + " KB)");
    }

    @Test
    public void testCoverDisclaimerAndFooterWithFlexibleTemplate() throws IOException {
        // Build a report with coverDisclaimer and footerText
        ReportData report = ReportDataBuilder.create()
            .title("灵活模板测试报告")
            .subtitle("封面声明与页脚测试")
            .reportDate("2025-01-01")
            .reportNumber("RPT-FLEX-001")
            .coverDisclaimer("注：本报告由系统自动生成，仅供参考")
            .footerText("© 2025 公司名称 版权所有")
            .addSection(new Section("概述")
                .addParagraph("使用灵活模板测试封面声明和页脚功能。"))
            .build();

        PdfRenderService service = new PdfRenderService();
        service.getHtmlRenderer().setDefaultTemplateName("flexible");
        configureChineseFont(service);

        byte[] pdfBytes = service.generatePdf(report);

        assertNotNull(pdfBytes, "PDF bytes should not be null");
        assertTrue(pdfBytes.length > 0, "PDF should not be empty");

        // Save the output PDF
        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "cover_disclaimer_footer_flexible.pdf");
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, pdfBytes);
        System.out.println("✓ Cover disclaimer & footer test PDF (flexible): " + outputPath.toAbsolutePath());
        System.out.println("  PDF size: " + pdfBytes.length + " bytes (" + (pdfBytes.length / 1024) + " KB)");
    }

    @Test
    public void testDefaultDisclaimerWhenNotSet() throws IOException {
        // Build a report WITHOUT coverDisclaimer to verify default behavior
        ReportData report = ReportDataBuilder.create()
            .title("默认声明测试")
            .subtitle("不设置自定义声明")
            .reportDate("2025-01-01")
            .addSection(new Section("概述")
                .addParagraph("测试默认声明文字。"))
            .build();

        PdfRenderService service = new PdfRenderService();
        service.getHtmlRenderer().setDefaultTemplateName("financial-report");
        configureChineseFont(service);

        byte[] pdfBytes = service.generatePdf(report);

        assertNotNull(pdfBytes, "PDF bytes should not be null");
        assertTrue(pdfBytes.length > 0, "PDF should not be empty");

        // Save the output PDF
        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "default_disclaimer_test.pdf");
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, pdfBytes);
        System.out.println("✓ Default disclaimer test PDF: " + outputPath.toAbsolutePath());
    }

    @Test
    public void testBuilderFieldsExist() {
        // Verify builder methods work correctly
        ReportData report = ReportDataBuilder.create()
            .title("Test")
            .coverDisclaimer("Test disclaimer")
            .footerText("Test footer")
            .addSection(new Section("Section")
                .addParagraph("Content"))
            .build();

        assertEquals("Test disclaimer", report.getCoverDisclaimer());
        assertEquals("Test footer", report.getFooterText());
    }

    private void configureChineseFont(PdfRenderService service) {
        PdfRenderProperties.FontProperties fontProps = new PdfRenderProperties.FontProperties();
        fontProps.setRegularPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontProps.setCjkPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        service.getHtmlRenderer().setFontProperties(fontProps);
    }
}
