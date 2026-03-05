package com.mercury.pdf.render;

import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test case specifically for Chinese character rendering in charts.
 * Verifies that when FontConfig is set, both PDF text AND chart labels display Chinese correctly.
 */
public class ChineseChartTest {

    private static final String TEST_OUTPUT_DIR = "test-output";

    @BeforeAll
    public static void setupTestOutputDirectory() throws IOException {
        Path outputDir = Paths.get(TEST_OUTPUT_DIR);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }
        System.out.println("Test output directory: " + outputDir.toAbsolutePath());
    }

    @Test
    public void testChineseCharactersInCharts() throws IOException {
        // Create service with Chinese font configuration
        PdfRenderService service = new PdfRenderService();
        
        // Enable HTML debug output for manual verification
        service.getHtmlRenderer().setDebugHtmlEnabled(true);
        service.getHtmlRenderer().setDebugHtmlOutputDirectory(TEST_OUTPUT_DIR);

        // Configure HarmonyOS Sans SC font for Chinese support
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif");
        service.getHtmlRenderer().setFontConfig(fontConfig);

        // Create report with Chinese charts
        ReportDataBuilder builder = ReportDataBuilder.create()
            .title("中文图表测试报告")
            .subtitle("验证图表标签中的中文字符正确显示")
            .reportDate("2024-12-31")
            .reportNumber("CHART-CN-001");

        // Add introduction
        Section intro = new Section("测试说明");
        intro.addParagraph("本测试验证图表中的中文字符能否正确显示。");
        intro.addParagraph("图表应显示中文标签，包括：坐标轴标签、图例、数据标签等。");
        builder.addSection(intro);

        // Add bar chart with Chinese labels
        Section barChartSection = new Section("柱状图测试");
        barChartSection.addParagraph("柱状图应正确显示中文坐标轴标签和类别名称。");
        
        Map<String, Double> barData = new LinkedHashMap<>();
        barData.put("北京", 2154.0);
        barData.put("上海", 2424.0);
        barData.put("广州", 1868.0);
        barData.put("深圳", 2767.0);
        
        ChartConfig barConfig = new ChartConfig();
        barConfig.setWidth(600);
        barConfig.setHeight(400);
        barConfig.setXAxisLabel("城市");
        barConfig.setYAxisLabel("GDP (亿元)");
        barConfig.setColors(Arrays.asList(
            new Color(52, 152, 219),
            new Color(46, 204, 113),
            new Color(155, 89, 182),
            new Color(241, 196, 15)
        ));
        
        ChartData barChart = new ChartData("2023年主要城市GDP", "bar", barData);
        barChart.setConfig(barConfig);
        barChartSection.addChart(barChart);
        builder.addSection(barChartSection);

        // Add pie chart with Chinese labels
        Section pieChartSection = new Section("饼图测试");
        pieChartSection.addParagraph("饼图应正确显示中文数据标签和图例。");
        
        Map<String, Double> pieData = new LinkedHashMap<>();
        pieData.put("研发部", 35.0);
        pieData.put("销售部", 28.0);
        pieData.put("市场部", 18.0);
        pieData.put("运营部", 12.0);
        pieData.put("财务部", 7.0);
        
        ChartConfig pieConfig = new ChartConfig();
        pieConfig.setWidth(500);
        pieConfig.setHeight(400);
        pieConfig.setShow3D(true);
        pieConfig.setColors(Arrays.asList(
            new Color(231, 76, 60),
            new Color(52, 152, 219),
            new Color(46, 204, 113),
            new Color(241, 196, 15),
            new Color(155, 89, 182)
        ));
        
        ChartData pieChart = new ChartData("部门人员分布", "pie", pieData);
        pieChart.setConfig(pieConfig);
        pieChartSection.addChart(pieChart);
        builder.addSection(pieChartSection);

        // Add line chart with Chinese labels
        Section lineChartSection = new Section("折线图测试");
        lineChartSection.addParagraph("折线图应正确显示中文坐标轴标签和月份名称。");
        
        Map<String, Double> lineData = new LinkedHashMap<>();
        lineData.put("一月", 45.0);
        lineData.put("二月", 52.0);
        lineData.put("三月", 61.0);
        lineData.put("四月", 58.0);
        lineData.put("五月", 67.0);
        lineData.put("六月", 73.0);
        
        ChartConfig lineConfig = new ChartConfig();
        lineConfig.setWidth(600);
        lineConfig.setHeight(350);
        lineConfig.setXAxisLabel("月份");
        lineConfig.setYAxisLabel("销售额 (万元)");
        lineConfig.setColors(Arrays.asList(new Color(52, 152, 219)));
        
        ChartData lineChart = new ChartData("2024年上半年销售趋势", "line", lineData);
        lineChart.setConfig(lineConfig);
        lineChartSection.addChart(lineChart);
        builder.addSection(lineChartSection);

        // Generate PDF
        ReportData reportData = builder.build();
        byte[] pdfBytes = service.generatePdf(reportData, "flexible");

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        // Save to test-output directory
        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "chinese_chart_rendering_test.pdf");
        Files.write(outputPath, pdfBytes);
        System.out.println("✓ Chinese chart rendering test PDF generated: " + outputPath.toAbsolutePath());
        System.out.println("  IMPORTANT: Open the PDF to verify that:");
        System.out.println("  - All Chinese characters in chart labels are displayed correctly");
        System.out.println("  - No boxes (□) appear in chart axis labels, legends, or data labels");
        System.out.println("  - Both PDF text AND chart images render Chinese characters properly");
    }

    @Test
    public void testChineseChartsWithoutFontConfig() throws IOException {
        // Test without font configuration to show the difference
        PdfRenderService service = new PdfRenderService();
        
        // DO NOT configure fonts - this should result in boxes in charts

        // Create simple report with Chinese chart
        Map<String, Double> data = new LinkedHashMap<>();
        data.put("产品A", 100.0);
        data.put("产品B", 150.0);
        data.put("产品C", 120.0);
        
        ChartConfig config = new ChartConfig();
        config.setXAxisLabel("产品");
        config.setYAxisLabel("销量");
        
        ChartData chart = new ChartData("产品销量对比", "bar", data);
        chart.setConfig(config);
        
        ReportData reportData = ReportDataBuilder.create()
            .title("Default Fonts Test")
            .addSection(new Section("Chart")
                .addChart(chart))
            .build();

        byte[] pdfBytes = service.generatePdf(reportData, "flexible");

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "chinese_chart_default_fonts.pdf");
        Files.write(outputPath, pdfBytes);
        System.out.println("✓ Chinese chart (default fonts) PDF generated: " + outputPath.toAbsolutePath());
        System.out.println("  Expected: Chinese characters in charts may appear as boxes (□)");
    }
}
