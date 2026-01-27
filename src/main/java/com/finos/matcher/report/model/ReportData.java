package com.finos.matcher.report.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Main data model for report generation.
 * Supports both legacy fixed-section structure and new flexible section-based structure.
 * 
 * <p>Legacy structure (backward compatible):
 * <ul>
 *   <li>Title, subtitle, date, number</li>
 *   <li>Section 1: Detailed tables with grouped blocks</li>
 *   <li>Section 2: Analysis paragraphs</li>
 *   <li>Section 3: Summary table and charts</li>
 *   <li>Section 4: Notice and metadata</li>
 * </ul>
 * 
 * <p>New flexible structure:
 * <ul>
 *   <li>Dynamic sections with configurable content types</li>
 *   <li>Each section can contain titles, paragraphs, tables, and charts</li>
 * </ul>
 * 
 * <p>Example using builder:
 * <pre>
 * ReportData report = ReportDataBuilder.create()
 *     .title("Annual Report")
 *     .subtitle("FY 2024")
 *     .reportDate("2024-12-31")
 *     .addSection(new Section("Executive Summary")
 *         .addParagraph("Key findings..."))
 *     .build();
 * </pre>
 */
public class ReportData {
    private String title;
    private String subtitle;
    private String reportDate;
    private String reportNumber;
    
    // Legacy fixed sections (maintained for backward compatibility)
    private List<TableBlock> tableBlocks;
    private List<String> analysisParagraphs;
    private TableData summaryTable;
    private List<ChartData> charts;
    private String reportNotice;
    private String metadata;
    
    // New flexible section-based structure
    private List<Section> sections;

    public ReportData() {
        this.sections = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getReportDate() {
        return reportDate;
    }

    public void setReportDate(String reportDate) {
        this.reportDate = reportDate;
    }

    public String getReportNumber() {
        return reportNumber;
    }

    public void setReportNumber(String reportNumber) {
        this.reportNumber = reportNumber;
    }

    public List<TableBlock> getTableBlocks() {
        return tableBlocks;
    }

    public void setTableBlocks(List<TableBlock> tableBlocks) {
        this.tableBlocks = tableBlocks;
    }

    public List<String> getAnalysisParagraphs() {
        return analysisParagraphs;
    }

    public void setAnalysisParagraphs(List<String> analysisParagraphs) {
        this.analysisParagraphs = analysisParagraphs;
    }

    public TableData getSummaryTable() {
        return summaryTable;
    }

    public void setSummaryTable(TableData summaryTable) {
        this.summaryTable = summaryTable;
    }

    public List<ChartData> getCharts() {
        return charts;
    }

    public void setCharts(List<ChartData> charts) {
        this.charts = charts;
    }

    public String getReportNotice() {
        return reportNotice;
    }

    public void setReportNotice(String reportNotice) {
        this.reportNotice = reportNotice;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections != null ? sections : new ArrayList<>();
    }
    
    /**
     * Validates the report data for completeness
     * @throws IllegalStateException if required fields are missing
     */
    public void validate() {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalStateException("Report title is required");
        }
        
        // Check if there's any content
        boolean hasContent = false;
        
        if ((tableBlocks != null && !tableBlocks.isEmpty()) ||
            (analysisParagraphs != null && !analysisParagraphs.isEmpty()) ||
            (summaryTable != null) ||
            (charts != null && !charts.isEmpty()) ||
            (sections != null && !sections.isEmpty())) {
            hasContent = true;
        }
        
        if (!hasContent) {
            throw new IllegalStateException("Report must contain at least one section with content");
        }
    }
    
    /**
     * Returns true if using new flexible section-based structure
     */
    public boolean hasSections() {
        return sections != null && !sections.isEmpty();
    }
    
    /**
     * Returns true if using legacy fixed-section structure
     */
    public boolean hasLegacyContent() {
        return (tableBlocks != null && !tableBlocks.isEmpty()) ||
               (analysisParagraphs != null && !analysisParagraphs.isEmpty()) ||
               (summaryTable != null) ||
               (charts != null && !charts.isEmpty());
    }
}
