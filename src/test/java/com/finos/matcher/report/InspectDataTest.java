package com.finos.matcher.report;

import com.finos.matcher.report.model.*;
import org.junit.jupiter.api.Test;
import java.io.IOException;

public class InspectDataTest {
    @Test
    public void inspectData() throws IOException {
        MatcherReportFinalTest test = new MatcherReportFinalTest();
        // Use reflection to call private method
        try {
            java.lang.reflect.Method method = test.getClass().getDeclaredMethod("createMatcherReportFinalData");
            method.setAccessible(true);
            ReportData data = (ReportData) method.invoke(test);
            
            System.out.println("=== REPORT DATA INSPECTION ===");
            System.out.println("Title: " + data.getTitle());
            System.out.println("Summary table: " + (data.getSummaryTable() != null));
            if (data.getSummaryTable() != null) {
                TableData table = data.getSummaryTable();
                System.out.println("  Headers: " + table.getHeaders());
                System.out.println("  Rows: " + table.getRows().size());
                for (int i = 0; i < table.getRows().size() && i < 3; i++) {
                    System.out.println("    Row " + i + ": " + table.getRows().get(i));
                }
            }
            System.out.println("Charts: " + (data.getCharts() != null ? data.getCharts().size() : "null"));
            if (data.getCharts() != null) {
                for (ChartData c : data.getCharts()) {
                    System.out.println("  Chart: " + c.getTitle());
                    System.out.println("    Type: " + c.getChartType());
                    System.out.println("    Has image: " + (c.getBase64Image() != null && !c.getBase64Image().isEmpty()));
                }
            }
            System.out.println("Sections: " + (data.getSections() != null ? data.getSections().size() : "null"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
