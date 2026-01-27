package com.finos.matcher.report;

import com.finos.matcher.report.model.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Demo application showing how to use the PDF report generation
 * with the HTML/CSS pipeline.
 */
public class ReportDemo {
    
    public static void main(String[] args) throws IOException {
        System.out.println("=== PDF Report Generation Demo ===\n");
        
        // Create sample report data
        ReportData reportData = createSampleReport();
        
        // Generate PDF using HTML/CSS pipeline
        ReportService service = new ReportService();
        System.out.println("Generating PDF using HTML/CSS pipeline...");
        
        long startTime = System.currentTimeMillis();
        byte[] pdfBytes = service.generatePdf(reportData);
        long duration = System.currentTimeMillis() - startTime;
        
        // Save to file
        String outputFile = "sample-report.pdf";
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(pdfBytes);
        }
        
        System.out.println("✓ PDF generated successfully!");
        System.out.println("  File: " + outputFile);
        System.out.println("  Size: " + formatBytes(pdfBytes.length));
        System.out.println("  Time: " + duration + "ms");
        System.out.println("\nFeatures:");
        System.out.println("  ✓ Stable table pagination (no lost rows)");
        System.out.println("  ✓ Repeated table headers on new pages");
        System.out.println("  ✓ Professional CSS-based styling");
        System.out.println("  ✓ Embedded charts as base64 images");
        System.out.println("  ✓ 4 sections: Cover, Tables, Analysis, Summary");
        
        // Also generate with legacy PDFBox for comparison
        System.out.println("\nGenerating comparison PDF with legacy PDFBox...");
        service.setUseHtmlPipeline(false);
        
        startTime = System.currentTimeMillis();
        byte[] legacyPdfBytes = service.generatePdf(reportData);
        duration = System.currentTimeMillis() - startTime;
        
        String legacyOutputFile = "sample-report-legacy.pdf";
        try (FileOutputStream fos = new FileOutputStream(legacyOutputFile)) {
            fos.write(legacyPdfBytes);
        }
        
        System.out.println("✓ Legacy PDF generated for comparison");
        System.out.println("  File: " + legacyOutputFile);
        System.out.println("  Size: " + formatBytes(legacyPdfBytes.length));
        System.out.println("  Time: " + duration + "ms");
        System.out.println("\n⚠ Note: Legacy version may have table pagination issues!");
    }
    
    private static ReportData createSampleReport() {
        ReportData report = new ReportData();
        
        // Cover page
        report.setTitle("Annual Financial Report 2024");
        report.setSubtitle("Comprehensive Analysis and Performance Review");
        report.setReportDate("January 27, 2024");
        report.setReportNumber("FIN-2024-001");
        
        // Section 1: Multiple table blocks with large datasets
        List<TableBlock> tableBlocks = new ArrayList<>();
        
        tableBlocks.add(new TableBlock(
            "1.1",
            "Revenue Breakdown by Product Line",
            createProductRevenueTable(40)
        ));
        
        tableBlocks.add(new TableBlock(
            "1.2",
            "Expense Analysis by Department",
            createExpenseTable(35)
        ));
        
        tableBlocks.add(new TableBlock(
            "1.3",
            "Customer Metrics by Region",
            createCustomerMetricsTable(30)
        ));
        
        tableBlocks.add(new TableBlock(
            "1.4",
            "Quarterly Performance Indicators",
            createPerformanceIndicatorsTable(25)
        ));
        
        report.setTableBlocks(tableBlocks);
        
        // Section 2: Analysis paragraphs
        List<String> analysis = Arrays.asList(
            "The fiscal year 2024 demonstrated robust growth across all major product lines, " +
            "with a notable 23% increase in total revenue compared to the previous year. " +
            "This growth was primarily driven by strong performance in our enterprise software " +
            "division and expanded market share in emerging economies.",
            
            "Operational efficiency improvements resulted in a 15% reduction in operational costs " +
            "while maintaining service quality standards. The implementation of automated workflows " +
            "and process optimization initiatives contributed significantly to these gains. " +
            "Our investment in technology infrastructure is showing measurable returns.",
            
            "Customer satisfaction metrics reached an all-time high, with an average NPS score of 72. " +
            "The expansion of our customer support team and enhanced self-service capabilities " +
            "have been instrumental in improving customer experience. Retention rates increased " +
            "to 94%, demonstrating strong customer loyalty.",
            
            "Looking ahead to 2025, we anticipate continued growth with projected revenue increases " +
            "of 18-22% based on current market conditions and our strategic initiatives. Key focus " +
            "areas include product innovation, market expansion, and strategic partnerships that " +
            "will position us for sustained long-term success."
        );
        report.setAnalysisParagraphs(analysis);
        
        // Section 3: Summary table
        report.setSummaryTable(createQuarterlySummaryTable());
        
        // Section 3: Charts
        List<ChartData> charts = new ArrayList<>();
        
        Map<String, Double> quarterlyProfit = new LinkedHashMap<>();
        quarterlyProfit.put("Q1 2024", 2150000.0);
        quarterlyProfit.put("Q2 2024", 2580000.0);
        quarterlyProfit.put("Q3 2024", 2920000.0);
        quarterlyProfit.put("Q4 2024", 3100000.0);
        charts.add(new ChartData("Quarterly Profit Trend", "bar", quarterlyProfit));
        
        Map<String, Double> expenseDistribution = new LinkedHashMap<>();
        expenseDistribution.put("Personnel", 45.0);
        expenseDistribution.put("Infrastructure", 25.0);
        expenseDistribution.put("Marketing", 18.0);
        expenseDistribution.put("R&D", 8.0);
        expenseDistribution.put("Other", 4.0);
        charts.add(new ChartData("Expense Distribution", "pie", expenseDistribution));
        
        report.setCharts(charts);
        
        // Section 4: Notice and metadata
        report.setReportNotice(
            "CONFIDENTIAL: This report contains proprietary business information and is intended " +
            "solely for internal use by authorized personnel. Unauthorized distribution or disclosure " +
            "is strictly prohibited."
        );
        
        report.setMetadata(
            "Generated by PDF Render v1.0 | HTML/CSS Pipeline | " +
            "Contact: finance@example.com | Report ID: " + UUID.randomUUID().toString().substring(0, 8)
        );
        
        return report;
    }
    
    private static TableData createProductRevenueTable(int numRows) {
        List<String> headers = Arrays.asList("Product ID", "Product Name", "Category", "Q1 Revenue", "Q2 Revenue", "Q3 Revenue", "Q4 Revenue", "Total");
        List<List<String>> rows = new ArrayList<>();
        
        String[] categories = {"Software", "Hardware", "Services", "Consulting", "Support"};
        String[] products = {"Enterprise Suite", "Cloud Platform", "Analytics Tool", "Mobile App", "API Gateway"};
        
        for (int i = 1; i <= numRows; i++) {
            String productName = products[i % products.length] + " v" + (i / products.length + 1);
            double q1 = 50000 + (i * 1234.56);
            double q2 = 52000 + (i * 1456.78);
            double q3 = 55000 + (i * 1678.90);
            double q4 = 58000 + (i * 1890.12);
            double total = q1 + q2 + q3 + q4;
            
            rows.add(Arrays.asList(
                "PRD-" + String.format("%04d", i),
                productName,
                categories[i % categories.length],
                formatCurrency(q1),
                formatCurrency(q2),
                formatCurrency(q3),
                formatCurrency(q4),
                formatCurrency(total)
            ));
        }
        
        return new TableData(headers, rows);
    }
    
    private static TableData createExpenseTable(int numRows) {
        List<String> headers = Arrays.asList("Dept ID", "Department", "Personnel", "Equipment", "Operations", "Travel", "Total");
        List<List<String>> rows = new ArrayList<>();
        
        String[] departments = {"Engineering", "Sales", "Marketing", "HR", "Finance", "Operations", "Support"};
        
        for (int i = 1; i <= numRows; i++) {
            double personnel = 100000 + (i * 2345.67);
            double equipment = 20000 + (i * 456.78);
            double operations = 15000 + (i * 678.90);
            double travel = 5000 + (i * 123.45);
            double total = personnel + equipment + operations + travel;
            
            rows.add(Arrays.asList(
                "DEPT-" + String.format("%03d", i),
                departments[i % departments.length] + " " + ((i / departments.length) + 1),
                formatCurrency(personnel),
                formatCurrency(equipment),
                formatCurrency(operations),
                formatCurrency(travel),
                formatCurrency(total)
            ));
        }
        
        return new TableData(headers, rows);
    }
    
    private static TableData createCustomerMetricsTable(int numRows) {
        List<String> headers = Arrays.asList("Region", "New Customers", "Active Users", "Churn Rate", "Avg Revenue", "Satisfaction");
        List<List<String>> rows = new ArrayList<>();
        
        String[] regions = {"North America", "Europe", "Asia Pacific", "Latin America", "Middle East", "Africa"};
        
        for (int i = 1; i <= numRows; i++) {
            rows.add(Arrays.asList(
                regions[i % regions.length],
                String.valueOf(500 + (i * 23)),
                String.valueOf(5000 + (i * 234)),
                String.format("%.1f%%", 2.5 + (i * 0.1)),
                formatCurrency(1500 + (i * 45)),
                String.format("%.1f", 4.2 + (i * 0.02))
            ));
        }
        
        return new TableData(headers, rows);
    }
    
    private static TableData createPerformanceIndicatorsTable(int numRows) {
        List<String> headers = Arrays.asList("Metric", "Target", "Actual", "Variance", "Status");
        List<List<String>> rows = new ArrayList<>();
        
        String[] metrics = {"Revenue Growth", "Cost Reduction", "Customer Acquisition", "Market Share", "Employee Satisfaction"};
        
        for (int i = 1; i <= numRows; i++) {
            double target = 100 + (i * 5);
            double actual = target + (i % 3 == 0 ? 5 : -2);
            double variance = ((actual - target) / target) * 100;
            String status = variance >= 0 ? "On Track" : "Below Target";
            
            rows.add(Arrays.asList(
                metrics[i % metrics.length] + " M" + i,
                String.format("%.1f", target),
                String.format("%.1f", actual),
                String.format("%+.1f%%", variance),
                status
            ));
        }
        
        return new TableData(headers, rows);
    }
    
    private static TableData createQuarterlySummaryTable() {
        List<String> headers = Arrays.asList("Quarter", "Revenue", "Expenses", "Operating Profit", "Margin %");
        List<List<String>> rows = Arrays.asList(
            Arrays.asList("Q1 2024", "$5,250,000", "$3,100,000", "$2,150,000", "41.0%"),
            Arrays.asList("Q2 2024", "$5,800,000", "$3,220,000", "$2,580,000", "44.5%"),
            Arrays.asList("Q3 2024", "$6,350,000", "$3,430,000", "$2,920,000", "46.0%"),
            Arrays.asList("Q4 2024", "$6,900,000", "$3,800,000", "$3,100,000", "44.9%"),
            Arrays.asList("Total 2024", "$24,300,000", "$13,550,000", "$10,750,000", "44.2%")
        );
        
        return new TableData(headers, rows);
    }
    
    private static String formatCurrency(double amount) {
        return String.format("$%,.2f", amount);
    }
    
    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "i";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
