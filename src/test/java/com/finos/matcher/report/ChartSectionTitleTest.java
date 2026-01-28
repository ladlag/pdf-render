package com.finos.matcher.report;

import com.finos.matcher.report.config.FontConfig;
import com.finos.matcher.report.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to demonstrate customization of chart section title
 */
public class ChartSectionTitleTest {

    private static final String TEST_OUTPUT_DIR = "test-output";

    @BeforeAll
    public static void setupTestOutputDirectory() throws IOException {
        Path outputDir = Paths.get(TEST_OUTPUT_DIR);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }
    }

    @Test
    public void testCustomChartSectionTitle() throws IOException {
        ReportService service = new ReportService();
        service.setUseHtmlPipeline(true);
        
        // Enable HTML debug output
        service.getHtmlRenderer().setDebugHtmlEnabled(true);
        service.getHtmlRenderer().setDebugHtmlOutputDirectory(TEST_OUTPUT_DIR);
        service.getHtmlRenderer().setDebugHtmlIncludeTimestamp(true);

        // Configure font
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif");
        service.getHtmlRenderer().setFontConfig(fontConfig);

        // Create report with custom chart section title
        ReportDataBuilder builder = ReportDataBuilder.create()
            .title("测试报告 - 自定义图表标题")
            .reportDate("2024-12-31")
            .reportNumber("TEST-001");

        // Add a section
        Section section1 = new Section("1.1 数据分析");
        section1.addParagraph("这是一个测试段落，演示自定义图表区域标题。");
        builder.addSection(section1);

        // Add a chart
        ChartData chart = new ChartData();
        chart.setTitle("销售数据分布图");
        chart.setChartType("bar");
        chart.setData(new HashMap<>());
        builder.addChart(chart);
        
        // Set custom chart section title
        builder.chartsSectionTitle("数据可视化展示");

        ReportData reportData = builder.build();

        byte[] pdfBytes = service.generatePdf(reportData, "matcher-report-final");

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        // Save to test-output directory
        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "chart_section_title_test.pdf");
        Files.write(outputPath, pdfBytes);
        System.out.println("✓ PDF with custom chart section title generated: " + outputPath.toAbsolutePath());
        
        // Verify the HTML contains the custom title (find most recent HTML file)
        Path htmlFile = Files.list(Paths.get(TEST_OUTPUT_DIR))
            .filter(p -> p.getFileName().toString().startsWith("matcher-report-final-") && p.toString().endsWith(".html"))
            .sorted((p1, p2) -> {
                try {
                    return Files.getLastModifiedTime(p2).compareTo(Files.getLastModifiedTime(p1));
                } catch (IOException e) {
                    return 0;
                }
            })
            .findFirst()
            .orElseThrow(() -> new RuntimeException("HTML debug file not found"));
        
        String htmlContent = new String(Files.readAllBytes(htmlFile), StandardCharsets.UTF_8);
        assertTrue(htmlContent.contains("数据可视化展示"), "HTML should contain custom chart section title");
        System.out.println("✓ Verified custom chart section title in HTML output");
    }
}
