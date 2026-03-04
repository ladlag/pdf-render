package com.mercury.pdf.render.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Main data model for report generation.
 * Uses flexible section-based structure for unlimited, dynamic content.
 * 
 * <p>Features:
 * <ul>
 *   <li>Unlimited dynamic sections with configurable content types</li>
 *   <li>Each section can contain titles, paragraphs, tables, and charts</li>
 *   <li>Flexible composition and ordering</li>
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
 *     .addSection(new Section("Financial Data")
 *         .addTable(table)
 *         .addChart(chart))
 *     .build();
 * </pre>
 */
public class ReportData {
    private String title;
    private String subtitle;
    private String reportDate;
    private String reportNumber;
    private String reportNotice;
    private String metadata;
    private String coverDisclaimer;
    private String footerText;
    private String headerText;
    
    // Flexible section-based structure
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

    public String getCoverDisclaimer() {
        return coverDisclaimer;
    }

    public void setCoverDisclaimer(String coverDisclaimer) {
        this.coverDisclaimer = coverDisclaimer;
    }

    public String getFooterText() {
        return footerText;
    }

    public void setFooterText(String footerText) {
        this.footerText = footerText;
    }

    public String getHeaderText() {
        return headerText;
    }

    public void setHeaderText(String headerText) {
        this.headerText = headerText;
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
        
        if (sections == null || sections.isEmpty()) {
            throw new IllegalStateException("Report must contain at least one section with content");
        }
    }
}
