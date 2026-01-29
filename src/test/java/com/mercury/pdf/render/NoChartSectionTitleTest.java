package com.mercury.pdf.render;

import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.model.ChartData;
import com.mercury.pdf.render.model.ReportData;
import com.mercury.pdf.render.model.ReportDataBuilder;
import com.mercury.pdf.render.model.Section;
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
 * Test to verify that when no chart section title is provided, no title is rendered
 */
public class NoChartSectionTitleTest {

    private static final String TEST_OUTPUT_DIR = "test-output";

    @BeforeAll
    public static void setupTestOutputDirectory() throws IOException {
        Path outputDir = Paths.get(TEST_OUTPUT_DIR);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }
    }

    @Test
    public void testNoChartSectionTitle() throws IOException {
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

        // Create report WITHOUT chart section title
        ReportDataBuilder builder = ReportDataBuilder.create()
            .title("测试报告 - 无图表标题")
            .reportDate("2024-12-31")
            .reportNumber("TEST-002");

        // Add a section
        Section section1 = new Section("1.1 数据分析");
        section1.addParagraph("这是一个测试段落，演示不设置图表区域标题。");
        builder.addSection(section1);

        // Add a chart but NO section title
        ChartData chart = new ChartData();
        chart.setTitle("销售数据分布图");
        chart.setChartType("bar");
        chart.setData(new HashMap<>());
        builder.addChart(chart);
        
        // DO NOT set chartsSectionTitle - it should remain null

        ReportData reportData = builder.build();

        byte[] pdfBytes = service.generatePdf(reportData, "matcher-report-final");

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        // Save to test-output directory
        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "no_chart_section_title_test.pdf");
        Files.write(outputPath, pdfBytes);
        System.out.println("✓ PDF without chart section title generated: " + outputPath.toAbsolutePath());
        
        // Verify the HTML does NOT contain an h3 before the chart
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
        
        // Should still have the chart
        assertTrue(htmlContent.contains("销售数据分布图"), "HTML should contain the chart");
        
        // Should NOT have "图表" as a section title (since we didn't set chartsSectionTitle)
        // The word might appear elsewhere, so we check for the specific h3 pattern
        assertFalse(htmlContent.contains("<h3>图表</h3>"), "HTML should NOT contain default '图表' h3 title when chartsSectionTitle is null");
        
        System.out.println("✓ Verified no chart section title is rendered when not provided");
    }
}
