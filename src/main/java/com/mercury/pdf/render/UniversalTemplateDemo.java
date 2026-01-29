package com.mercury.pdf.render;

import com.mercury.pdf.render.model.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Demonstration of the universal template system.
 * 
 * KEY CONCEPT: One unified data model can generate multiple different document types
 * through different templates WITHOUT any code changes.
 * 
 * This proves that:
 * 1. Data structure is completely template-agnostic
 * 2. Adding new templates requires NO code changes
 * 3. All templates use the same unified interface
 * 4. No template-specific parsing or generation logic needed
 */
public class UniversalTemplateDemo {
    
    public static void main(String[] args) throws IOException {
        System.out.println("=== Universal Template System Demo ===");
        System.out.println("Demonstrating: One Data Model → Multiple Templates → No Code Changes\n");
        
        // Create ONE data model
        ReportData singleDataModel = createUniversalDataModel();
        
        // Generate MULTIPLE document types using the SAME data
        generateWithAllTemplates(singleDataModel);
        
        System.out.println("\n✓ Demo completed successfully!");
        System.out.println("\nKey Takeaways:");
        System.out.println("  1. Same data structure used for ALL templates");
        System.out.println("  2. No template-specific code needed");
        System.out.println("  3. Adding new templates = just add HTML file");
        System.out.println("  4. Data preparation logic is universal");
    }
    
    /**
     * Creates a single, universal data model that works with ALL templates.
     * This is the ONLY data preparation code needed.
     */
    private static ReportData createUniversalDataModel() {
        System.out.println("Creating universal data model...");
        
        // Build data using the flexible structure
        ReportData data = ReportDataBuilder.create()
            .title("2024 Annual Performance Review")
            .subtitle("Excellence in Business Operations")
            .reportDate("December 31, 2024")
            .reportNumber("John Smith")  // Repurposed for certificate recipient
            
            // Section 1: Executive Summary
            .addSection(new Section("John Smith")  // Can be used as recipient name
                .addParagraph("For outstanding leadership and exceptional contribution to organizational success.")
                .addParagraph("Demonstrated excellence in driving key initiatives, " +
                    "achieving remarkable results, and fostering team collaboration throughout 2024.")
                .addTable(createAchievementsTable()))
            
            // Section 2: Performance Metrics
            .addSection(new Section("Performance Metrics")
                .withSubtitle("Key Performance Indicators")
                .addParagraph("Exceeded all quarterly targets with consistent performance improvements.")
                .addTable(createMetricsTable())
                .addChart(createPerformanceChart()))
            
            // Section 3: Financial Overview
            .addSection(new Section("Financial Overview")
                .addTable(createFinancialTable())
                .addChart(createRevenueChart()))
            
            // Section 4: Strategic Achievements
            .addSection(new Section("Strategic Achievements")
                .addParagraph("Successfully led three major projects resulting in significant ROI.")
                .addParagraph("Implemented process improvements that increased efficiency by 25%."))
            
            .reportNotice("Confidential - Internal Use Only")
            .metadata("Performance Management System v2.0")
            .build();
        
        System.out.println("✓ Universal data model created\n");
        return data;
    }
    
    /**
     * Generate documents using ALL available templates with the SAME data.
     * NO template-specific code or logic needed!
     */
    private static void generateWithAllTemplates(ReportData data) throws IOException {
        ReportService service = new ReportService();
        
        System.out.println("Generating documents with different templates using SAME data:\n");
        
        // Template 1: Standard Report
        System.out.println("1. Standard Report Template:");
        System.out.println("   - Structured multi-section report");
        System.out.println("   - Best for: Detailed analysis documents");
        generatePDF(service, data, "report", "universal-demo-report.pdf");
        
        // Template 2: Flexible Report
        System.out.println("\n2. Flexible Report Template:");
        System.out.println("   - Dynamic section-based layout");
        System.out.println("   - Best for: Custom reports with varying structure");
        generatePDF(service, data, "flexible", "universal-demo-flexible.pdf");
        
        // Template 3: Invoice (repurposing data)
        System.out.println("\n3. Invoice Template:");
        System.out.println("   - Professional invoice layout");
        System.out.println("   - Best for: Billing and financial documents");
        generatePDF(service, data, "invoice", "universal-demo-invoice.pdf");
        
        // Template 4: Certificate (creative repurposing)
        System.out.println("\n4. Certificate Template:");
        System.out.println("   - Award certificate layout");
        System.out.println("   - Best for: Recognition and achievement certificates");
        generatePDF(service, data, "certificate", "universal-demo-certificate.pdf");
        
        // Template 5: Executive Summary (data-focused)
        System.out.println("\n5. Executive Summary Template:");
        System.out.println("   - High-level overview layout");
        System.out.println("   - Best for: Executive presentations");
        generatePDF(service, data, "executive-summary", "universal-demo-executive.pdf");
        
        System.out.println("\n─────────────────────────────────────────");
        System.out.println("All 5 documents generated from SAME data!");
        System.out.println("No template-specific code was written!");
        System.out.println("─────────────────────────────────────────");
    }
    
    /**
     * Universal PDF generation - works with ANY template
     */
    private static void generatePDF(ReportService service, ReportData data, 
                                    String templateName, String outputFile) throws IOException {
        service.getHtmlRenderer().setDefaultTemplateName(templateName);
        byte[] pdfBytes = service.generatePdf(data);
        
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(pdfBytes);
        }
        
        System.out.println("   ✓ Generated: " + outputFile + " (" + pdfBytes.length + " bytes)");
    }
    
    // ===== Data Creation Helpers (Universal - Not Template-Specific) =====
    
    private static TableData createAchievementsTable() {
        return new TableData(
            Arrays.asList("Achievement", "Impact"),
            Arrays.asList(
                Arrays.asList("Project Alpha Delivery", "35% efficiency gain"),
                Arrays.asList("Team Leadership", "95% satisfaction score"),
                Arrays.asList("Revenue Growth", "$2.5M additional revenue"),
                Arrays.asList("Cost Optimization", "$500K savings")
            )
        );
    }
    
    private static TableData createMetricsTable() {
        return new TableData(
            Arrays.asList("Metric", "Target", "Actual", "Status"),
            Arrays.asList(
                Arrays.asList("Customer Satisfaction", "85%", "92%", "Exceeded"),
                Arrays.asList("Project Delivery", "90%", "98%", "Exceeded"),
                Arrays.asList("Budget Management", "±5%", "+2%", "Met"),
                Arrays.asList("Team Performance", "80%", "89%", "Exceeded")
            )
        );
    }
    
    private static TableData createFinancialTable() {
        return new TableData(
            Arrays.asList("Quarter", "Revenue", "Costs", "Profit"),
            Arrays.asList(
                Arrays.asList("Q1 2024", "$1.2M", "$800K", "$400K"),
                Arrays.asList("Q2 2024", "$1.5M", "$900K", "$600K"),
                Arrays.asList("Q3 2024", "$1.8M", "$950K", "$850K"),
                Arrays.asList("Q4 2024", "$2.1M", "$1.0M", "$1.1M")
            )
        );
    }
    
    private static ChartData createPerformanceChart() {
        Map<String, Double> data = new LinkedHashMap<>();
        data.put("Q1", 88.0);
        data.put("Q2", 91.0);
        data.put("Q3", 94.0);
        data.put("Q4", 96.0);
        return new ChartData("Quarterly Performance Score", "bar", data);
    }
    
    private static ChartData createRevenueChart() {
        Map<String, Double> data = new LinkedHashMap<>();
        data.put("Q1", 1200000.0);
        data.put("Q2", 1500000.0);
        data.put("Q3", 1800000.0);
        data.put("Q4", 2100000.0);
        return new ChartData("Quarterly Revenue", "bar", data);
    }
}
