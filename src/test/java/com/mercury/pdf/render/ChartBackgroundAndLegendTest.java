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
 * Test to verify chart background (white/transparent) and legend customization
 */
public class ChartBackgroundAndLegendTest {

    private static final String TEST_OUTPUT_DIR = "test-output";

    @BeforeAll
    public static void setupTestOutputDirectory() throws IOException {
        Path outputDir = Paths.get(TEST_OUTPUT_DIR);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }
    }

    @Test
    public void testDefaultWhiteBackground() throws IOException {
        PdfRenderService service = new PdfRenderService();
        
        // Enable HTML debug output
        service.getHtmlRenderer().setDebugHtmlEnabled(true);
        service.getHtmlRenderer().setDebugHtmlOutputDirectory(TEST_OUTPUT_DIR);

        // Configure font for Chinese support
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif");
        service.getHtmlRenderer().setFontConfig(fontConfig);

        // Create test data
        Map<String, Double> chartData = new LinkedHashMap<>();
        chartData.put("一月", 100.0);
        chartData.put("二月", 150.0);
        chartData.put("三月", 200.0);
        chartData.put("四月", 180.0);

        // Create report
        ReportDataBuilder builder = ReportDataBuilder.create()
            .title("图表背景测试 - 默认白色背景")
            .reportDate("2024-12-31")
            .reportNumber("BG-TEST-001");

        Section section = new Section("默认白色背景图表");
        section.addParagraph("此图表使用默认白色背景，与PDF背景完美匹配。");
        
        // Chart with NO config - should default to white background
        ChartData chart1 = new ChartData("默认背景 - 柱状图", "bar", chartData);
        section.addChart(chart1);

        builder.addSection(section);

        // Generate PDF
        byte[] pdf = service.generatePdf(builder.build());
        
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
        
        // Save to file
        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "chart_default_white_background.pdf");
        Files.write(outputPath, pdf);
        System.out.println("✓ PDF with default white background generated: " + outputPath);
    }

    @Test
    public void testTransparentBackground() throws IOException {
        PdfRenderService service = new PdfRenderService();
        
        // Enable HTML debug output
        service.getHtmlRenderer().setDebugHtmlEnabled(true);
        service.getHtmlRenderer().setDebugHtmlOutputDirectory(TEST_OUTPUT_DIR);

        // Configure font for Chinese support
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif");
        service.getHtmlRenderer().setFontConfig(fontConfig);

        // Create test data
        Map<String, Double> pieData = new LinkedHashMap<>();
        pieData.put("产品A", 35.0);
        pieData.put("产品B", 25.0);
        pieData.put("产品C", 20.0);
        pieData.put("产品D", 20.0);

        // Create report
        ReportDataBuilder builder = ReportDataBuilder.create()
            .title("图表背景测试 - 透明背景")
            .reportDate("2024-12-31")
            .reportNumber("BG-TEST-002");

        Section section = new Section("透明背景图表");
        section.addParagraph("此饼图使用透明背景设置。");
        
        // Chart with transparent background
        ChartConfig config = new ChartConfig();
        config.setTransparentBackground(true);
        config.setWidth(500);
        config.setHeight(400);
        
        ChartData chart = new ChartData("透明背景 - 饼图", "pie", pieData);
        chart.setConfig(config);
        section.addChart(chart);

        builder.addSection(section);

        // Generate PDF
        byte[] pdf = service.generatePdf(builder.build());
        
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
        
        // Save to file
        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "chart_transparent_background.pdf");
        Files.write(outputPath, pdf);
        System.out.println("✓ PDF with transparent background generated: " + outputPath);
    }

    @Test
    public void testLegendCustomization() throws IOException {
        PdfRenderService service = new PdfRenderService();
        
        // Enable HTML debug output
        service.getHtmlRenderer().setDebugHtmlEnabled(true);
        service.getHtmlRenderer().setDebugHtmlOutputDirectory(TEST_OUTPUT_DIR);

        // Configure font for Chinese support
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif");
        service.getHtmlRenderer().setFontConfig(fontConfig);

        // Create test data
        Map<String, Double> chartData = new LinkedHashMap<>();
        chartData.put("Q1", 100.0);
        chartData.put("Q2", 150.0);
        chartData.put("Q3", 200.0);
        chartData.put("Q4", 180.0);

        // Create report
        ReportDataBuilder builder = ReportDataBuilder.create()
            .title("图表图例自定义测试")
            .reportDate("2024-12-31")
            .reportNumber("LEGEND-TEST-001");

        Section section = new Section("图例位置测试");
        section.addParagraph("演示不同的图例位置配置。");
        
        // Chart 1: Legend on top
        ChartConfig topLegend = new ChartConfig();
        topLegend.setLegendPosition("top");
        topLegend.setWidth(500);
        topLegend.setHeight(300);
        ChartData chart1 = new ChartData("图例在顶部", "bar", chartData);
        chart1.setConfig(topLegend);
        section.addChart(chart1);

        // Chart 2: Legend on right
        ChartConfig rightLegend = new ChartConfig();
        rightLegend.setLegendPosition("right");
        rightLegend.setWidth(500);
        rightLegend.setHeight(300);
        ChartData chart2 = new ChartData("图例在右侧", "line", chartData);
        chart2.setConfig(rightLegend);
        section.addChart(chart2);

        // Chart 3: No legend
        ChartConfig noLegend = new ChartConfig();
        noLegend.setShowLegend(false);
        noLegend.setWidth(500);
        noLegend.setHeight(300);
        ChartData chart3 = new ChartData("无图例", "area", chartData);
        chart3.setConfig(noLegend);
        section.addChart(chart3);

        builder.addSection(section);

        // Generate PDF
        byte[] pdf = service.generatePdf(builder.build());
        
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
        
        // Save to file
        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "chart_legend_customization.pdf");
        Files.write(outputPath, pdf);
        System.out.println("✓ PDF with legend customization generated: " + outputPath);
    }

    @Test
    public void testCustomPlotBackground() throws IOException {
        PdfRenderService service = new PdfRenderService();
        
        // Enable HTML debug output
        service.getHtmlRenderer().setDebugHtmlEnabled(true);
        service.getHtmlRenderer().setDebugHtmlOutputDirectory(TEST_OUTPUT_DIR);

        // Configure font for Chinese support
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif");
        service.getHtmlRenderer().setFontConfig(fontConfig);

        // Create test data
        Map<String, Double> chartData = new LinkedHashMap<>();
        chartData.put("类别A", 85.0);
        chartData.put("类别B", 92.0);
        chartData.put("类别C", 78.0);
        chartData.put("类别D", 88.0);

        // Create report
        ReportDataBuilder builder = ReportDataBuilder.create()
            .title("图表背景色自定义")
            .reportDate("2024-12-31")
            .reportNumber("PLOTBG-TEST-001");

        Section section = new Section("绘图区背景色");
        section.addParagraph("演示自定义绘图区背景色。");
        
        // Chart with light gray plot background
        ChartConfig config = new ChartConfig();
        config.setPlotBackgroundColorHex("#F5F5F5"); // Light gray plot area
        config.setBackgroundColorHex("#FFFFFF");     // White chart background
        config.setWidth(600);
        config.setHeight(350);
        config.setColors(Arrays.asList(
            new Color(52, 152, 219),   // Blue
            new Color(46, 204, 113),   // Green
            new Color(155, 89, 182),   // Purple
            new Color(241, 196, 15)    // Yellow
        ));
        
        ChartData chart = new ChartData("浅灰色绘图区背景", "bar", chartData);
        chart.setConfig(config);
        section.addChart(chart);

        builder.addSection(section);

        // Generate PDF
        byte[] pdf = service.generatePdf(builder.build());
        
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
        
        // Save to file
        Path outputPath = Paths.get(TEST_OUTPUT_DIR, "chart_custom_plot_background.pdf");
        Files.write(outputPath, pdf);
        System.out.println("✓ PDF with custom plot background generated: " + outputPath);
    }
}
