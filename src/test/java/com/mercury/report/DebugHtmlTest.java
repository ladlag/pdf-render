package com.mercury.report;

import com.mercury.report.config.FontConfig;
import com.mercury.report.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for debugging HTML output feature.
 * Demonstrates how to save intermediate HTML for debugging PDF generation.
 */
public class DebugHtmlTest {

    private static final String TEST_OUTPUT_DIR = "test-output";
    private static final String DEBUG_HTML_DIR = "test-output/debug-html";

    @BeforeAll
    public static void setupTestOutputDirectory() throws IOException {
        Path outputDir = Paths.get(TEST_OUTPUT_DIR);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }
        Path debugDir = Paths.get(DEBUG_HTML_DIR);
        if (!Files.exists(debugDir)) {
            Files.createDirectories(debugDir);
        }
        System.out.println("Test output will be saved to: " + outputDir.toAbsolutePath());
        System.out.println("Debug HTML will be saved to: " + debugDir.toAbsolutePath());
    }

    @Test
    public void testDebugHtmlOutput() throws IOException {
        ReportService service = new ReportService();
        service.setUseHtmlPipeline(true);

        // Configure font for Chinese text
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif");
        service.getHtmlRenderer().setFontConfig(fontConfig);

        // Enable debug HTML output using new configuration method
        service.getHtmlRenderer().setDebugHtmlEnabled(true);
        service.getHtmlRenderer().setDebugHtmlOutputDirectory(DEBUG_HTML_DIR);
        service.getHtmlRenderer().setDebugHtmlIncludeTimestamp(false);

        // Create simple test data
        ReportData reportData = createSimpleReportData();

        // Generate PDF (this will also save the HTML)
        byte[] pdfBytes = service.generatePdf(reportData, "matcher-report-1.0");

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        // Verify HTML file was created
        Path htmlPath = Paths.get(DEBUG_HTML_DIR, "matcher-report-1.0.html");
        assertTrue(Files.exists(htmlPath), "Debug HTML file should be created");
        
        String htmlContent = new String(Files.readAllBytes(htmlPath));
        assertTrue(htmlContent.contains("<!DOCTYPE html>"), "HTML should contain DOCTYPE");
        assertTrue(htmlContent.contains("需求预审报告"), "HTML should contain report title");

        // Save PDF to test-output directory
        Path pdfPath = Paths.get(TEST_OUTPUT_DIR, "matcher_report_with_debug.pdf");
        Files.write(pdfPath, pdfBytes);
        
        System.out.println("✓ PDF generated: " + pdfPath.toAbsolutePath());
        System.out.println("✓ Debug HTML saved: " + htmlPath.toAbsolutePath());
        System.out.println("  HTML size: " + htmlContent.length() + " characters");
        System.out.println("  You can open the HTML file in a browser to inspect the rendered template");
    }

    @Test
    public void testDebugHtmlWithTimestamp() throws IOException, InterruptedException {
        ReportService service = new ReportService();
        service.setUseHtmlPipeline(true);

        // Configure font for Chinese text
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif");
        service.getHtmlRenderer().setFontConfig(fontConfig);

        // Enable debug HTML output WITH timestamp
        service.getHtmlRenderer().setDebugHtmlEnabled(true);
        service.getHtmlRenderer().setDebugHtmlOutputDirectory(DEBUG_HTML_DIR);
        service.getHtmlRenderer().setDebugHtmlIncludeTimestamp(true);

        // Create simple test data
        ReportData reportData = createSimpleReportData();

        // Generate first PDF
        byte[] pdfBytes1 = service.generatePdf(reportData, "flexible");
        assertNotNull(pdfBytes1);

        // Wait a bit to ensure different timestamps
        Thread.sleep(1100);

        // Generate second PDF
        byte[] pdfBytes2 = service.generatePdf(reportData, "flexible");
        assertNotNull(pdfBytes2);

        // Verify multiple HTML files were created with different names
        Path debugDir = Paths.get(DEBUG_HTML_DIR);
        long htmlFileCount = Files.list(debugDir)
            .filter(p -> p.getFileName().toString().startsWith("flexible-"))
            .filter(p -> p.getFileName().toString().endsWith(".html"))
            .count();

        assertTrue(htmlFileCount >= 2, "Should create separate HTML files with timestamps");
        
        System.out.println("✓ Created " + htmlFileCount + " debug HTML files with timestamps");
        System.out.println("  Files are saved in: " + debugDir.toAbsolutePath());
    }

    private ReportData createSimpleReportData() {
        ReportDataBuilder builder = ReportDataBuilder.create()
            .title("需求预审报告")
            .reportDate("2024-12-31")
            .reportNumber("TEST-001");

        // Add a simple section with table
        Section section = new Section("1.1 测试匹配结果");
        section.addParagraph("这是一个简单的测试报告，用于演示调试HTML功能。");
        
        TableData table = new TableData(
            Arrays.asList("需求编号", "需求名称", "匹配度", "说明"),
            Arrays.asList(
                Arrays.asList("C001", "用户登录", "0.98", "精确匹配"),
                Arrays.asList("C002", "数据查询", "0.85", "语义匹配")
            )
        );
        section.addTable(table);
        builder.addSection(section);

        // Add summary
        TableData summaryTable = new TableData(
            Arrays.asList("状态", "数量", "占比"),
            Arrays.asList(
                Arrays.asList("精确匹配", "1", "50%"),
                Arrays.asList("语义匹配", "1", "50%")
            )
        );
        builder.summaryTable(summaryTable);

        return builder.build();
    }
}
