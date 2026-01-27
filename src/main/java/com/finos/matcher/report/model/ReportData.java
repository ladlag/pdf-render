package com.finos.matcher.report.model;

import java.util.List;

/**
 * Main data model for report generation
 */
public class ReportData {
    private String title;
    private String subtitle;
    private String reportDate;
    private String reportNumber;
    
    // Section 1: Detailed tables with grouped blocks
    private List<TableBlock> tableBlocks;
    
    // Section 2: Analysis paragraphs
    private List<String> analysisParagraphs;
    
    // Section 3: Summary table and charts
    private TableData summaryTable;
    private List<ChartData> charts;
    
    // Section 4: Notice and metadata
    private String reportNotice;
    private String metadata;

    public ReportData() {
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
}
