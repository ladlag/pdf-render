package com.finos.matcher.report;

import com.finos.matcher.report.config.FontConfig;
import com.finos.matcher.report.model.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ChartDebugTest {

    @Test
    public void testChartsInSection3() throws IOException {
        ReportService service = new ReportService();
        service.setUseHtmlPipeline(true);

        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif");
        service.getHtmlRenderer().setFontConfig(fontConfig);

        // Create minimal report data with summary table and charts
        ReportDataBuilder builder = ReportDataBuilder.create()
            .title("Chart Test Report")
            .reportDate("2024-12-31");

        // Add summary table (for section 3)
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
        
        // Debug output
        System.out.println("=== DEBUG INFO ===");
        System.out.println("Charts count: " + (reportData.getCharts() != null ? reportData.getCharts().size() : "NULL"));
        if (reportData.getCharts() != null && !reportData.getCharts().isEmpty()) {
            for (ChartData chart : reportData.getCharts()) {
                System.out.println("  Chart: " + chart.getTitle());
                System.out.println("  Has base64: " + (chart.getBase64Image() != null && !chart.getBase64Image().isEmpty()));
                if (chart.getBase64Image() != null) {
                    System.out.println("  Base64 prefix: " + chart.getBase64Image().substring(0, Math.min(50, chart.getBase64Image().length())));
                }
            }
        }

        byte[] pdfBytes = service.generatePdf(reportData, "matcher-report-final");
        
        Path outputPath = Paths.get("test-output", "chart_debug_test.pdf");
        Files.write(outputPath, pdfBytes);
        System.out.println("PDF generated: " + outputPath.toAbsolutePath());
    }
}
