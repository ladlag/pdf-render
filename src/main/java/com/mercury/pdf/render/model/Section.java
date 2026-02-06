package com.mercury.pdf.render.model;

import com.mercury.pdf.render.util.MarkdownRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a flexible section in the report that can contain various content types.
 * Supports dynamic composition of titles, paragraphs, tables, and charts.
 * 
 * <p><b>Section Identification:</b>
 * <p>The {@code sectionType} field provides a generic way to identify and categorize sections
 * without depending on title format or content. This allows complete decoupling between
 * your data structure and template rendering logic.
 * 
 * <p><b>Usage:</b>
 * <ul>
 *   <li>Set {@code sectionType} to any string value that your template can recognize</li>
 *   <li>Templates can use {@code sectionType} to determine where/how to render sections</li>
 *   <li>No predefined values - you define what makes sense for your use case</li>
 *   <li>Backward compatible - if {@code sectionType} is null, templates may fallback to title-based logic</li>
 * </ul>
 * 
 * <p><b>Example:</b>
 * <pre>
 * // Simple usage with custom section types
 * Section intro = new Section("Introduction", "intro");
 * Section body = new Section("Main Content", "body");  
 * Section conclusion = new Section("Conclusion", "conclusion");
 * 
 * // Or use fluent API
 * Section section = new Section("My Section")
 *     .withSectionType("custom-type")
 *     .addParagraph("Content...")
 *     .addTable(table);
 * </pre>
 */
public class Section {
    private String title;
    private String subtitle;
    private String sectionType;  // Generic identifier for template-independent section categorization
    private List<String> paragraphs;
    private List<TableData> tables;
    private List<ChartData> charts;
    private List<TableBlock> tableBlocks;
    private String customContent;
    private String markdownContent;
    private String renderedMarkdownContent;
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
     * 
     * @param title The section title (any text)
     * @param sectionType A custom identifier for this section (e.g., "intro", "body", "conclusion")
     *                    The value is determined by your template's needs
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

    public Section withMarkdownContent(String markdownContent) {
        setMarkdownContent(markdownContent);
        return this;
    }
    
    public Section withCssClass(String cssClass) {
        this.cssClass = cssClass;
        return this;
    }
    
    /**
     * Sets the section type identifier.
     * 
     * @param sectionType A custom string identifier that your template can use to categorize this section
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

    public String getMarkdownContent() {
        return markdownContent;
    }

    /**
     * Returns rendered Markdown as HTML for templates to display.
     *
     * @return HTML string for markdown content, or null if no markdown was provided
     */
    public String getMarkdownHtml() {
        if (markdownContent == null || markdownContent.trim().isEmpty()) {
            return null;
        }
        if (renderedMarkdownContent == null) {
            renderedMarkdownContent = MarkdownRenderer.toHtml(markdownContent);
        }
        return renderedMarkdownContent;
    }

    public void setMarkdownContent(String markdownContent) {
        this.markdownContent = markdownContent;
        this.renderedMarkdownContent = null;
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
               (customContent != null && !customContent.trim().isEmpty()) ||
               (markdownContent != null && !markdownContent.trim().isEmpty());
    }
}
