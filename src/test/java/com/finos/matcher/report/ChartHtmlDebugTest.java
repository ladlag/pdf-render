package com.finos.matcher.report;

import com.finos.matcher.report.config.FontConfig;
import com.finos.matcher.report.model.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ChartHtmlDebugTest {

    @Test
    public void testChartsHtmlOutput() throws IOException {
        ReportService service = new ReportService();
        service.setUseHtmlPipeline(true);
        
        // Enable debug HTML to see the generated HTML
        service.getHtmlRenderer().setDebugHtmlEnabled(true);
        service.getHtmlRenderer().setDebugHtmlOutputDirectory("test-output");

        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif");
        service.getHtmlRenderer().setFontConfig(fontConfig);

        // Create report with charts
        ReportDataBuilder builder = ReportDataBuilder.create()
            .title("图表调试报告")
            .reportDate("2024-12-31");

        // Add summary table
        List<String> headers = Arrays.asList("匹配状态", "数量（条）", "占比");
        List<List<String>> rows = Arrays.asList(
            Arrays.asList("精确匹配通过", "10", "50.0%"),
            Arrays.asList("语义匹配通过", "6", "30.0%"),
            Arrays.asList("疑似匹配（需复核）", "2", "10.0%"),
            Arrays.asList("匹配失败", "2", "10.0%"),
            Arrays.asList("合计", "20", "100.0%")
        );
        builder.summaryTable(new TableData(headers, rows));

        // Add charts using ChartRenderer
        ChartRenderer renderer = new ChartRenderer();
        
        // Chart 1: Pie chart
        Map<String, Double> pieData = new LinkedHashMap<>();
        pieData.put("精确匹配通过", 50.0);
        pieData.put("语义匹配通过", 30.0);
        pieData.put("疑似匹配", 10.0);
        pieData.put("匹配失败", 10.0);
        ChartData pieChart = new ChartData("匹配状态分布", "pie", pieData);
        String pieBase64 = renderer.generateChartAsBase64(pieChart);
        pieChart.setBase64Image("data:image/png;base64," + pieBase64);
        builder.addChart(pieChart);

        // Chart 2: Bar chart
        Map<String, Double> barData = new LinkedHashMap<>();
        barData.put("精确匹配", 10.0);
        barData.put("语义匹配", 6.0);
        barData.put("疑似匹配", 2.0);
        barData.put("匹配失败", 2.0);
        ChartData barChart = new ChartData("各状态需求数量", "bar", barData);
        String barBase64 = renderer.generateChartAsBase64(barChart);
        barChart.setBase64Image("data:image/png;base64," + barBase64);
        builder.addChart(barChart);

        ReportData reportData = builder.build();
        
        System.out.println("Charts to render: " + reportData.getCharts().size());
        for (ChartData chart : reportData.getCharts()) {
            System.out.println("  - " + chart.getTitle() + " (has image: " + (chart.getBase64Image() != null) + ")");
        }

        byte[] pdfBytes = service.generatePdf(reportData, "matcher-report-final");
        
        Path outputPath = Paths.get("test-output", "chart_html_debug_test.pdf");
        Files.write(outputPath, pdfBytes);
        System.out.println("\nPDF generated: " + outputPath.toAbsolutePath());
        System.out.println("Check test-output directory for debug HTML file");
    }
}
