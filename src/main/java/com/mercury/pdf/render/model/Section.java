package com.mercury.pdf.render.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a flexible section in the report that can contain various content types.
 * Supports dynamic composition of titles, paragraphs, tables, and charts.
 * 
 * <p>Section identification can be done in two ways:
 * <ul>
 *   <li><b>Recommended:</b> Using {@code sectionType} for explicit, template-independent identification</li>
 *   <li><b>Legacy:</b> Using title prefix matching (e.g., "1.", "2.") - maintained for backward compatibility</li>
 * </ul>
 */
public class Section {
    private String title;
    private String subtitle;
    private String sectionType;  // Decouples section identification from title
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
    
    /**
     * Creates a section with both title and type.
     * This constructor allows explicit section type identification independent of the title.
     * 
     * @param title The section title (can be any text, not required to have numeric prefix)
     * @param sectionType The section type identifier (e.g., "chapter1", "chapter2", "summary")
     */
    public Section(String title, String sectionType) {
        this();
        this.title = title;
        this.sectionType = sectionType;
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
    
    /**
     * Sets the section type identifier.
     * This provides a template-independent way to identify sections.
     * 
     * @param sectionType The section type (e.g., "chapter1", "chapter2", "summary", "notes")
     * @return this Section for method chaining
     */
    public Section withSectionType(String sectionType) {
        this.sectionType = sectionType;
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
    
    public String getSectionType() {
        return sectionType;
    }
    
    public void setSectionType(String sectionType) {
        this.sectionType = sectionType;
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
