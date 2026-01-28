package com.finos.matcher.report;

import com.finos.matcher.report.model.*;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;

public class DebugDataTest {
    @Test
    public void debugReportData() throws Exception {
        MatcherReportFinalTest test = new MatcherReportFinalTest();
        Method method = test.getClass().getDeclaredMethod("createMatcherReportFinalData");
        method.setAccessible(true);
        ReportData data = (ReportData) method.invoke(test);
        
        System.out.println("=== REPORT DATA DEBUG ===");
        System.out.println("Title: " + data.getTitle());
        System.out.println("SummaryTable: " + data.getSummaryTable());
        System.out.println("Charts: " + data.getCharts());
        System.out.println("Charts size: " + (data.getCharts() != null ? data.getCharts().size() : "null"));
        System.out.println("Sections: " + (data.getSections() != null ? data.getSections().size() : "null"));
        
        if (data.getSummaryTable() != null) {
            System.out.println("\nSummary Table Details:");
            System.out.println("  Headers: " + data.getSummaryTable().getHeaders());
            System.out.println("  Rows: " + data.getSummaryTable().getRows().size());
        } else {
            System.out.println("\n*** SUMMARY TABLE IS NULL ***");
        }
        
        if (data.getCharts() != null && !data.getCharts().isEmpty()) {
            System.out.println("\nCharts Details:");
            for (ChartData c : data.getCharts()) {
                System.out.println("  - " + c.getTitle() + " (" + c.getChartType() + ")");
            }
        } else {
            System.out.println("\n*** CHARTS IS NULL OR EMPTY ***");
        }
    }
}
