package com.mercury.report.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a flexible section in the report that can contain various content types.
 * Supports dynamic composition of titles, paragraphs, tables, and charts.
 */
public class Section {
    private String title;
    private String subtitle;
    private List<String> paragraphs;
    private List<TableData> tables;
    private List<ChartData> charts;
    private List<TableBlock> tableBlocks;
    private String customContent;
    private String cssClass;
    
    public Section() {
        this.paragraphs = new ArrayList<>();
        this.tables = new ArrayList<>();
        this.charts = new ArrayList<>();
        this.tableBlocks = new ArrayList<>();
    }
    
    public Section(String title) {
        this();
        this.title = title;
    }
    
    // Builder methods for fluent API
    public Section withSubtitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }
    
    public Section addParagraph(String paragraph) {
        if (paragraph != null && !paragraph.trim().isEmpty()) {
            this.paragraphs.add(paragraph);
        }
        return this;
    }
    
    public Section addTable(TableData table) {
        if (table != null) {
            this.tables.add(table);
        }
        return this;
    }
    
    public Section addChart(ChartData chart) {
        if (chart != null) {
            this.charts.add(chart);
        }
        return this;
    }
    
    public Section addTableBlock(TableBlock block) {
        if (block != null) {
            this.tableBlocks.add(block);
        }
        return this;
    }
    
    public Section withCustomContent(String content) {
        this.customContent = content;
        return this;
    }
    
    public Section withCssClass(String cssClass) {
        this.cssClass = cssClass;
        return this;
    }
    
    // Getters and setters
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
    
    public List<String> getParagraphs() {
        return paragraphs;
    }
    
    public void setParagraphs(List<String> paragraphs) {
        this.paragraphs = paragraphs != null ? paragraphs : new ArrayList<>();
    }
    
    public List<TableData> getTables() {
        return tables;
    }
    
    public void setTables(List<TableData> tables) {
        this.tables = tables != null ? tables : new ArrayList<>();
    }
    
    public List<ChartData> getCharts() {
        return charts;
    }
    
    public void setCharts(List<ChartData> charts) {
        this.charts = charts != null ? charts : new ArrayList<>();
    }
    
    public List<TableBlock> getTableBlocks() {
        return tableBlocks;
    }
    
    public void setTableBlocks(List<TableBlock> tableBlocks) {
        this.tableBlocks = tableBlocks != null ? tableBlocks : new ArrayList<>();
    }
    
    public String getCustomContent() {
        return customContent;
    }
    
    public void setCustomContent(String customContent) {
        this.customContent = customContent;
    }
    
    public String getCssClass() {
        return cssClass;
    }
    
    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }
    
    /**
     * Check if this section has any content
     */
    public boolean hasContent() {
        return (title != null && !title.trim().isEmpty()) ||
               (subtitle != null && !subtitle.trim().isEmpty()) ||
               (paragraphs != null && !paragraphs.isEmpty()) ||
               (tables != null && !tables.isEmpty()) ||
               (charts != null && !charts.isEmpty()) ||
               (tableBlocks != null && !tableBlocks.isEmpty()) ||
               (customContent != null && !customContent.trim().isEmpty());
    }
}
