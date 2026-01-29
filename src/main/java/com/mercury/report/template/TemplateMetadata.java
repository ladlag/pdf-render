package com.mercury.report.template;

import java.util.ArrayList;
import java.util.List;

/**
 * Metadata describing a template's characteristics and capabilities.
 * This allows templates to be self-describing without requiring code changes.
 * 
 * Template metadata can be embedded in HTML comments or external configuration.
 */
public class TemplateMetadata {
    private String templateName;
    private String displayName;
    private String description;
    private String category;  // e.g., "report", "invoice", "certificate"
    private List<String> supportedSections;
    private List<String> requiredFields;
    private boolean supportsFlexibleSections;
    private boolean supportsLegacyStructure;
    
    public TemplateMetadata() {
        this.supportedSections = new ArrayList<>();
        this.requiredFields = new ArrayList<>();
        this.supportsFlexibleSections = true;
        this.supportsLegacyStructure = true;
    }
    
    public TemplateMetadata(String templateName, String displayName) {
        this();
        this.templateName = templateName;
        this.displayName = displayName;
    }
    
    // Fluent setters for builder pattern
    
    public TemplateMetadata setDescription(String description) {
        this.description = description;
        return this;
    }
    
    public TemplateMetadata setCategory(String category) {
        this.category = category;
        return this;
    }
    
    public TemplateMetadata setSupportsFlexibleSections(boolean supports) {
        this.supportsFlexibleSections = supports;
        return this;
    }
    
    public TemplateMetadata setSupportsLegacyStructure(boolean supports) {
        this.supportsLegacyStructure = supports;
        return this;
    }

    // Regular getters and setters
    
    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public List<String> getSupportedSections() {
        return supportedSections;
    }

    public void setSupportedSections(List<String> supportedSections) {
        this.supportedSections = supportedSections;
    }

    public List<String> getRequiredFields() {
        return requiredFields;
    }

    public void setRequiredFields(List<String> requiredFields) {
        this.requiredFields = requiredFields;
    }

    public boolean isSupportsFlexibleSections() {
        return supportsFlexibleSections;
    }

    public boolean isSupportsLegacyStructure() {
        return supportsLegacyStructure;
    }
}
