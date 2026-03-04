package com.mercury.pdf.render.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder class for creating ReportData instances with a fluent API.
 * Provides a convenient and robust way to construct reports with flexible sections.
 * 
 * Example usage:
 * <pre>
 * ReportData report = ReportDataBuilder.create()
 *     .title("Annual Report")
 *     .subtitle("Financial Year 2024")
 *     .reportDate("2024-12-31")
 *     .addSection(new Section("Executive Summary")
 *         .addParagraph("This report summarizes..."))
 *     .addSection(new Section("Financial Data")
 *         .addTable(table)
 *         .addChart(chart))
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
     * Sets the report title (required)
     */
    public ReportDataBuilder title(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Report title cannot be null or empty");
        }
        reportData.setTitle(title);
        return this;
    }
    
    /**
     * Sets the report subtitle (optional)
     */
    public ReportDataBuilder subtitle(String subtitle) {
        reportData.setSubtitle(subtitle);
        return this;
    }
    
    /**
     * Sets the report date (optional)
     */
    public ReportDataBuilder reportDate(String date) {
        reportData.setReportDate(date);
        return this;
    }
    
    /**
     * Sets the report number (optional)
     */
    public ReportDataBuilder reportNumber(String number) {
        reportData.setReportNumber(number);
        return this;
    }
    
    /**
     * Sets the report notice (optional)
     */
    public ReportDataBuilder reportNotice(String notice) {
        reportData.setReportNotice(notice);
        return this;
    }
    
    /**
     * Sets the metadata (optional)
     */
    public ReportDataBuilder metadata(String metadata) {
        reportData.setMetadata(metadata);
        return this;
    }
    
    /**
     * Sets the cover disclaimer text displayed at the bottom of the cover page (optional)
     */
    public ReportDataBuilder coverDisclaimer(String coverDisclaimer) {
        reportData.setCoverDisclaimer(coverDisclaimer);
        return this;
    }
    
    /**
     * Sets the footer text displayed at the bottom of every page (optional).
     * This text appears in the page footer area alongside page numbers.
     */
    public ReportDataBuilder footerText(String footerText) {
        reportData.setFooterText(footerText);
        return this;
    }
    
    /**
     * Adds a flexible section to the report.
     * Sections can contain any combination of paragraphs, tables, and charts.
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
     * Validates and builds the ReportData instance.
     * Ensures title and at least one section are present.
     */
    public ReportData build() {
        // Validate required fields
        if (reportData.getTitle() == null || reportData.getTitle().trim().isEmpty()) {
            throw new IllegalStateException("Report title is required");
        }
        
        // Initialize empty list if null
        if (reportData.getSections() == null) {
            reportData.setSections(new ArrayList<>());
        }
        
        // Validate at least one section exists
        if (reportData.getSections().isEmpty()) {
            throw new IllegalStateException("Report must contain at least one section");
        }
        
        return reportData;
    }
}
