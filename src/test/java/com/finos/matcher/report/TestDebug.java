package com.finos.matcher.report;

import com.finos.matcher.report.config.FontConfig;
import com.finos.matcher.report.model.*;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class TestDebug {
    @Test
    public void test() throws IOException {
        ReportService service = new ReportService();
        service.setUseHtmlPipeline(true);
        service.getHtmlRenderer().setDebugHtmlEnabled(true);
        service.getHtmlRenderer().setDebugHtmlOutputDirectory("test-output");

        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif");
        service.getHtmlRenderer().setFontConfig(fontConfig);

        // Simple test data
        ReportDataBuilder builder = ReportDataBuilder.create()
            .title("测试报告")
            .reportDate("2024-12-31");

        // Add summary table
        List<String> headers = Arrays.asList("匹配状态", "数量（条）", "占比");
        List<List<String>> rows = Arrays.asList(
            Arrays.asList("精确匹配通过", "10", "50.0%"),
            Arrays.asList("语义匹配通过", "6", "30.0%"),
            Arrays.asList("合计", "20", "100.0%")
        );
        builder.summaryTable(new TableData(headers, rows));

        // Add charts
        ChartRenderer renderer = new ChartRenderer();
        Map<String, Double> pieData = new LinkedHashMap<>();
        pieData.put("精确匹配通过", 50.0);
        pieData.put("语义匹配通过", 30.0);
        ChartData pieChart = new ChartData("匹配状态分布", "pie", pieData);
        String pieBase64 = renderer.generateChartAsBase64(pieChart);
        pieChart.setBase64Image("data:image/png;base64," + pieBase64);
        builder.addChart(pieChart);

        ReportData reportData = builder.build();
        
        System.out.println("=== DEBUG ===");
        System.out.println("Summary table: " + (reportData.getSummaryTable() != null));
        if (reportData.getSummaryTable() != null) {
            System.out.println("  Headers: " + reportData.getSummaryTable().getHeaders());
            System.out.println("  Rows: " + reportData.getSummaryTable().getRows().size());
        }
        System.out.println("Charts: " + (reportData.getCharts() != null ? reportData.getCharts().size() : 0));
        if (reportData.getCharts() != null) {
            for (ChartData c : reportData.getCharts()) {
                System.out.println("  Chart: " + c.getTitle() + " hasImage=" + (c.getBase64Image() != null));
            }
        }

        byte[] pdfBytes = service.generatePdf(reportData, "matcher-report-final");
        Path outputPath = Paths.get("test-output", "test_debug.pdf");
        Files.write(outputPath, pdfBytes);
        System.out.println("PDF: " + outputPath.toAbsolutePath());
    }
}
