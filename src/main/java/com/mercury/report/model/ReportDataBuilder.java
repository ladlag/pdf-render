package com.mercury.report.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder class for creating ReportData instances with a fluent API.
 * Provides a more convenient and robust way to construct report data.
 * 
 * Example usage:
 * <pre>
 * ReportData report = ReportDataBuilder.create()
 *     .title("Annual Report")
 *     .subtitle("Financial Year 2024")
 *     .reportDate("2024-12-31")
 *     .addSection(new Section("Executive Summary")
 *         .addParagraph("This report summarizes..."))
 *     .addTableBlock(tableBlock)
 *     .addChart(chart)
 *     .build();
 * </pre>
 */
public class ReportDataBuilder {
    private final ReportData reportData;
    
    private ReportDataBuilder() {
        this.reportData = new ReportData();
    }
    
    /**
     * Creates a new builder instance
     */
    public static ReportDataBuilder create() {
        return new ReportDataBuilder();
    }
    
    /**
     * Sets the report title
     */
    public ReportDataBuilder title(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Report title cannot be null or empty");
        }
        reportData.setTitle(title);
        return this;
    }
    
    /**
     * Sets the report subtitle
     */
    public ReportDataBuilder subtitle(String subtitle) {
        reportData.setSubtitle(subtitle);
        return this;
    }
    
    /**
     * Sets the report date
     */
    public ReportDataBuilder reportDate(String date) {
        reportData.setReportDate(date);
        return this;
    }
    
    /**
     * Sets the report number
     */
    public ReportDataBuilder reportNumber(String number) {
        reportData.setReportNumber(number);
        return this;
    }
    
    /**
     * Adds a table block to the report
     */
    public ReportDataBuilder addTableBlock(TableBlock block) {
        if (block == null) {
            throw new IllegalArgumentException("TableBlock cannot be null");
        }
        List<TableBlock> blocks = reportData.getTableBlocks();
        if (blocks == null) {
            blocks = new ArrayList<>();
            reportData.setTableBlocks(blocks);
        }
        blocks.add(block);
        return this;
    }
    
    /**
     * Sets all table blocks at once
     */
    public ReportDataBuilder tableBlocks(List<TableBlock> blocks) {
        reportData.setTableBlocks(blocks);
        return this;
    }
    
    /**
     * Adds an analysis paragraph
     */
    public ReportDataBuilder addAnalysisParagraph(String paragraph) {
        if (paragraph == null || paragraph.trim().isEmpty()) {
            return this; // Skip empty paragraphs
        }
        List<String> paragraphs = reportData.getAnalysisParagraphs();
        if (paragraphs == null) {
            paragraphs = new ArrayList<>();
            reportData.setAnalysisParagraphs(paragraphs);
        }
        paragraphs.add(paragraph);
        return this;
    }
    
    /**
     * Sets all analysis paragraphs at once
     */
    public ReportDataBuilder analysisParagraphs(List<String> paragraphs) {
        reportData.setAnalysisParagraphs(paragraphs);
        return this;
    }
    
    /**
     * Sets the summary table
     */
    public ReportDataBuilder summaryTable(TableData table) {
        reportData.setSummaryTable(table);
        return this;
    }
    
    /**
     * Adds a chart to the report
     */
    public ReportDataBuilder addChart(ChartData chart) {
        if (chart == null) {
            throw new IllegalArgumentException("Chart cannot be null");
        }
        List<ChartData> charts = reportData.getCharts();
        if (charts == null) {
            charts = new ArrayList<>();
            reportData.setCharts(charts);
        }
        charts.add(chart);
        return this;
    }
    
    /**
     * Sets all charts at once
     */
    public ReportDataBuilder charts(List<ChartData> charts) {
        reportData.setCharts(charts);
        return this;
    }
    
    /**
     * Sets the title for the charts section
     * If not set (null), no section title will be rendered
     */
    public ReportDataBuilder chartsSectionTitle(String title) {
        reportData.setChartsSectionTitle(title);
        return this;
    }
    
    /**
     * Sets the report notice
     */
    public ReportDataBuilder reportNotice(String notice) {
        reportData.setReportNotice(notice);
        return this;
    }
    
    /**
     * Sets the metadata
     */
    public ReportDataBuilder metadata(String metadata) {
        reportData.setMetadata(metadata);
        return this;
    }
    
    /**
     * Adds a flexible section to the report (if sections are supported)
     */
    public ReportDataBuilder addSection(Section section) {
        if (section == null) {
            throw new IllegalArgumentException("Section cannot be null");
        }
        List<Section> sections = reportData.getSections();
        if (sections == null) {
            sections = new ArrayList<>();
            reportData.setSections(sections);
        }
        sections.add(section);
        return this;
    }
    
    /**
     * Validates and builds the ReportData instance
     */
    public ReportData build() {
        // Validate required fields
        if (reportData.getTitle() == null || reportData.getTitle().trim().isEmpty()) {
            throw new IllegalStateException("Report title is required");
        }
        
        // Initialize empty lists for null collections to prevent NPE
        if (reportData.getTableBlocks() == null) {
            reportData.setTableBlocks(new ArrayList<>());
        }
        if (reportData.getAnalysisParagraphs() == null) {
            reportData.setAnalysisParagraphs(new ArrayList<>());
        }
        if (reportData.getCharts() == null) {
            reportData.setCharts(new ArrayList<>());
        }
        if (reportData.getSections() == null) {
            reportData.setSections(new ArrayList<>());
        }
        
        return reportData;
    }
    
    /**
     * Builds without validation for backward compatibility
     */
    public ReportData buildUnchecked() {
        return reportData;
    }
}
