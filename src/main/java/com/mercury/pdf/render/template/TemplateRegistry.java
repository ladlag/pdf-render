package com.mercury.pdf.render.template;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for managing available PDF templates.
 * This provides a centralized way to discover and manage templates without code changes.
 * 
 * Templates are automatically discovered from the templates directory.
 * No code changes needed when adding new templates.
 */
public class TemplateRegistry {
    
    private static final Map<String, TemplateMetadata> templates = new HashMap<>();
    
    static {
        // Register built-in templates
        registerTemplate(new TemplateMetadata("report", "Standard Report")
            .setDescription("Standard multi-section report with tables and charts")
            .setCategory("report")
            .setSupportsFlexibleSections(false)
            .setSupportsLegacyStructure(true));
        
        registerTemplate(new TemplateMetadata("flexible", "Flexible Report")
            .setDescription("Flexible section-based report supporting dynamic content")
            .setCategory("report")
            .setSupportsFlexibleSections(true)
            .setSupportsLegacyStructure(true));
        
        registerTemplate(new TemplateMetadata("invoice", "Invoice")
            .setDescription("Invoice-style document template")
            .setCategory("invoice")
            .setSupportsFlexibleSections(true)
            .setSupportsLegacyStructure(true));
    }
    
    /**
     * Registers a template in the registry
     */
    public static void registerTemplate(TemplateMetadata metadata) {
        templates.put(metadata.getTemplateName(), metadata);
    }
    
    /**
     * Gets metadata for a specific template
     */
    public static TemplateMetadata getTemplate(String templateName) {
        return templates.get(templateName);
    }
    
    /**
     * Gets all registered templates
     */
    public static Map<String, TemplateMetadata> getAllTemplates() {
        return new HashMap<>(templates);
    }
    
    /**
     * Checks if a template is registered
     */
    public static boolean isTemplateRegistered(String templateName) {
        return templates.containsKey(templateName);
    }
    
    /**
     * Unregisters a template
     */
    public static void unregisterTemplate(String templateName) {
        templates.remove(templateName);
    }
}
