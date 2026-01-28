package com.finos.matcher.report;

import com.finos.matcher.report.config.FontConfig;
import com.finos.matcher.report.model.*;
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
 * Test to demonstrate new chart types and customization features
 */
public class ChartTypesAndConfigTest {

    private static final String TEST_OUTPUT_DIR = "test-output";

    @BeforeAll
    public static void setupTestOutputDirectory() throws IOException {
        Path outputDir = Paths.get(TEST_OUTPUT_DIR);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }
    }

    @Test
    public void testAllChartTypesWithCustomization() throws IOException {
        ReportService service = new ReportService();
        service.setUseHtmlPipeline(true);
        
        // Enable HTML debug output
        service.getHtmlRenderer().setDebugHtmlEnabled(true);
        service.getHtmlRenderer().setDebugHtmlOutputDirectory(TEST_OUTPUT_DIR);
        service.getHtmlRenderer().setDebugHtmlIncludeTimestamp(true);

        // Configure font for Chinese support
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif");
        service.getHtmlRenderer().setFontConfig(fontConfig);

        // Create test data
        Map<String, Double> chartData = new LinkedHashMap<>();
        chartData.put("Q1", 200000.0);
        chartData.put("Q2", 230000.0);
        chartData.put("Q3", 260000.0);
        chartData.put("Q4", 290000.0);

        // Create report with all chart types
        ReportDataBuilder builder = ReportDataBuilder.create()
            .title("图表类型和自定义配置演示")
            .reportDate("2024-12-31")
            .reportNumber("CHART-TEST-001");

        // Add introduction section
        Section intro = new Section("图表功能展示");
        intro.addParagraph("本报告演示了扩展的图表类型和自定义配置功能。");
        intro.addParagraph("支持的图表类型：柱状图、饼图、折线图、面积图、堆叠柱状图。");
        builder.addSection(intro);

        // 1. Bar chart with custom colors
        ChartConfig barConfig = new ChartConfig();
        barConfig.setWidth(600);
        barConfig.setHeight(350);
        barConfig.setColors(Arrays.asList(
            new Color(52, 152, 219),   // Blue
            new Color(46, 204, 113),   // Green
            new Color(155, 89, 182),   // Purple
            new Color(241, 196, 15)    // Yellow
        ));
        barConfig.setXAxisLabel("季度");
        barConfig.setYAxisLabel("利润 (元)");
        barConfig.setShowGridLines(true);
        
        ChartData barChart = new ChartData("季度利润 - 柱状图", "bar", chartData);
        barChart.setConfig(barConfig);
        builder.addChart(barChart);

        // 2. Pie chart with 3D effect
        Map<String, Double> pieData = new LinkedHashMap<>();
        pieData.put("工资", 40.0);
        pieData.put("运营", 30.0);
        pieData.put("营销", 20.0);
        pieData.put("其他", 10.0);
        
        ChartConfig pieConfig = new ChartConfig();
        pieConfig.setWidth(500);
        pieConfig.setHeight(400);
        pieConfig.setShow3D(true);
        pieConfig.setColors(Arrays.asList(
            new Color(231, 76, 60),    // Red
            new Color(52, 152, 219),   // Blue
            new Color(46, 204, 113),   // Green
            new Color(241, 196, 15)    // Yellow
        ));
        
        ChartData pieChart = new ChartData("费用分布 - 3D饼图", "pie", pieData);
        pieChart.setConfig(pieConfig);
        builder.addChart(pieChart);

        // 3. Line chart
        ChartConfig lineConfig = new ChartConfig();
        lineConfig.setWidth(600);
        lineConfig.setHeight(300);
        lineConfig.setColors(Arrays.asList(new Color(231, 76, 60)));
        lineConfig.setXAxisLabel("时间");
        lineConfig.setYAxisLabel("增长率 (%)");
        lineConfig.setShowGridLines(true);
        
        Map<String, Double> growthData = new LinkedHashMap<>();
        growthData.put("Q1", 15.0);
        growthData.put("Q2", 18.5);
        growthData.put("Q3", 22.3);
        growthData.put("Q4", 28.7);
        
        ChartData lineChart = new ChartData("增长趋势 - 折线图", "line", growthData);
        lineChart.setConfig(lineConfig);
        builder.addChart(lineChart);

        // 4. Area chart
        ChartConfig areaConfig = new ChartConfig();
        areaConfig.setWidth(600);
        areaConfig.setHeight(300);
        areaConfig.setColors(Arrays.asList(new Color(46, 204, 113)));
        areaConfig.setXAxisLabel("月份");
        areaConfig.setYAxisLabel("销售额");
        areaConfig.setBackgroundColorHex("#f8f9fa");
        
        Map<String, Double> salesData = new LinkedHashMap<>();
        salesData.put("1月", 45000.0);
        salesData.put("2月", 52000.0);
        salesData.put("3月", 48000.0);
        salesData.put("4月", 61000.0);
        
        ChartData areaChart = new ChartData("月度销售 - 面积图", "area", salesData);
        areaChart.setConfig(areaConfig);
        builder.addChart(areaChart);

        // 5. Stacked bar chart
        ChartConfig stackedConfig = new ChartConfig();
        stackedConfig.setWidth(600);
        stackedConfig.setHeight(350);
        stackedConfig.setColors(Arrays.asList(
            new Color(155, 89, 182),
            new Color(52, 152, 219)
        ));
        stackedConfig.setXAxisLabel("产品");
        stackedConfig.setYAxisLabel("数量");
        stackedConfig.setShowLegend(true);
        
        ChartData stackedChart = new ChartData("产品销售 - 堆叠柱状图", "stackedbar", chartData);
        stackedChart.setConfig(stackedConfig);
        builder.addChart(stackedChart);

        // 6. Chart without legend
        ChartConfig noLegendConfig = new ChartConfig();
        noLegendConfig.setShowLegend(false);
        noLegendConfig.setWidth(500);
        noLegendConfig.setHeight(250);
        
        ChartData noLegendChart = new ChartData("简化柱状图 (无图例)", "bar", chartData);
        noLegendChart.setConfig(noLegendConfig);
        builder.addChart(noLegendChart);

        ReportData reportData = builder.build();
        byte[] pdfBytes = service.generatePdf(reportData, "matcher-report-final");

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        // Save to test-output directory
        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "chart_types_and_config_test.pdf");
        Files.write(outputPath, pdfBytes);
        System.out.println("✓ PDF with all chart types and configurations generated: " + outputPath.toAbsolutePath());
    }

    @Test
    public void testChartWithCustomDimensions() throws IOException {
        ReportService service = new ReportService();
        service.setUseHtmlPipeline(true);

        Map<String, Double> data = new LinkedHashMap<>();
        data.put("A", 10.0);
        data.put("B", 20.0);
        data.put("C", 30.0);

        // Create chart with custom dimensions
        ChartConfig config = new ChartConfig();
        config.setWidth(800);
        config.setHeight(400);

        ChartData chart = new ChartData("Custom Size Chart", "bar", data);
        chart.setConfig(config);

        ReportDataBuilder builder = ReportDataBuilder.create()
            .title("Custom Dimensions Test")
            .reportDate("2024-12-31")
            .reportNumber("DIM-001");
        
        builder.addChart(chart);

        byte[] pdfBytes = service.generatePdf(builder.build());
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "chart_custom_dimensions.pdf");
        Files.write(outputPath, pdfBytes);
        System.out.println("✓ PDF with custom dimensions generated: " + outputPath.toAbsolutePath());
    }

    @Test
    public void testLineChartWithoutGridLines() throws IOException {
        ReportService service = new ReportService();
        service.setUseHtmlPipeline(true);

        Map<String, Double> data = new LinkedHashMap<>();
        data.put("Jan", 100.0);
        data.put("Feb", 150.0);
        data.put("Mar", 120.0);
        data.put("Apr", 180.0);

        ChartConfig config = new ChartConfig();
        config.setShowGridLines(false);
        config.setColors(Arrays.asList(new Color(230, 126, 34)));

        ChartData chart = new ChartData("Trend Without Grid", "line", data);
        chart.setConfig(config);

        ReportDataBuilder builder = ReportDataBuilder.create()
            .title("Grid Lines Test")
            .reportDate("2024-12-31")
            .reportNumber("GRID-001");
        
        builder.addChart(chart);

        byte[] pdfBytes = service.generatePdf(builder.build());
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "chart_no_gridlines.pdf");
        Files.write(outputPath, pdfBytes);
        System.out.println("✓ PDF without grid lines generated: " + outputPath.toAbsolutePath());
    }
}
