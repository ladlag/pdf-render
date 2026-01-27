package com.finos.matcher.report;

import com.finos.matcher.report.model.ChartData;
import com.finos.matcher.report.model.ReportData;
import com.finos.matcher.report.model.Section;
import com.lowagie.text.DocumentException;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for rendering PDF reports using HTML/CSS templates and Flying Saucer.
 * This approach provides stable table pagination without row loss.
 */
public class HtmlReportRenderer {
    
    private final TemplateEngine templateEngine;
    private final ChartRenderer chartRenderer;
    private boolean cacheTemplates = true; // Enable caching by default for production
    private String defaultTemplateName = "report"; // Default template name
    
    public HtmlReportRenderer() {
        this.chartRenderer = new ChartRenderer();
        this.templateEngine = createTemplateEngine();
    }
    
    /**
     * Sets the default template name to use when no template is specified.
     * Template files should be placed in src/main/resources/templates/ with .html extension.
     * 
     * @param templateName Template name without the .html extension (e.g., "report", "invoice")
     */
    public void setDefaultTemplateName(String templateName) {
        this.defaultTemplateName = templateName;
    }
    
    /**
     * Gets the current default template name
     */
    public String getDefaultTemplateName() {
        return defaultTemplateName;
    }
    
    /**
     * Sets whether to cache templates. Disable for development, enable for production.
     */
    public void setCacheTemplates(boolean cacheTemplates) {
        this.cacheTemplates = cacheTemplates;
        // Recreate template engine with new cache setting
        this.templateEngine.getTemplateResolvers().forEach(resolver -> {
            if (resolver instanceof ClassLoaderTemplateResolver) {
                ((ClassLoaderTemplateResolver) resolver).setCacheable(cacheTemplates);
            }
        });
    }
    
    /**
     * Generates a PDF from ReportData using the HTML/CSS pipeline with the default template
     */
    public byte[] generatePdf(ReportData reportData) throws IOException, DocumentException {
        return generatePdf(reportData, defaultTemplateName);
    }
    
    /**
     * Generates a PDF from ReportData using the HTML/CSS pipeline with a specified template
     * 
     * @param reportData Report data to render
     * @param templateName Template name without .html extension (e.g., "report", "invoice")
     * @return PDF content as byte array
     */
    public byte[] generatePdf(ReportData reportData, String templateName) throws IOException, DocumentException {
        // Step 1: Prepare data for template (including chart images)
        Map<String, Object> templateData = prepareTemplateData(reportData);
        
        // Step 2: Render HTML from template
        String html = renderHtml(templateData, templateName);
        
        // Step 3: Convert HTML to PDF using Flying Saucer
        return convertHtmlToPdf(html);
    }
    
    /**
     * Prepares data for the Thymeleaf template, including converting charts to base64 images
     */
    private Map<String, Object> prepareTemplateData(ReportData reportData) throws IOException {
        Map<String, Object> data = new HashMap<>();
        
        // Cover page data
        data.put("title", reportData.getTitle() != null ? reportData.getTitle() : "");
        data.put("subtitle", reportData.getSubtitle());
        data.put("reportDate", reportData.getReportDate());
        data.put("reportNumber", reportData.getReportNumber());
        
        // Check if using new flexible sections or legacy structure
        if (reportData.hasSections()) {
            // Process flexible sections
            List<Section> sections = reportData.getSections();
            for (Section section : sections) {
                // Convert charts in sections to base64
                if (section.getCharts() != null && !section.getCharts().isEmpty()) {
                    for (ChartData chart : section.getCharts()) {
                        if (chart.getBase64Image() == null) {
                            String base64Image = chartRenderer.generateChartAsBase64(chart);
                            chart.setBase64Image(base64Image);
                        }
                    }
                }
            }
            data.put("sections", sections);
        } else {
            // Legacy structure
            data.put("sections", null);
            
            // Section 1: Table blocks
            data.put("tableBlocks", reportData.getTableBlocks());
            
            // Section 2: Analysis paragraphs
            data.put("analysisParagraphs", reportData.getAnalysisParagraphs());
            
            // Section 3: Summary table
            data.put("summaryTable", reportData.getSummaryTable());
            
            // Section 3: Charts (convert to base64 images)
            if (reportData.getCharts() != null && !reportData.getCharts().isEmpty()) {
                for (ChartData chart : reportData.getCharts()) {
                    if (chart.getBase64Image() == null) {
                        String base64Image = chartRenderer.generateChartAsBase64(chart);
                        chart.setBase64Image(base64Image);
                    }
                }
                data.put("charts", reportData.getCharts());
            }
            
            // Section 4: Notice and metadata
            data.put("reportNotice", reportData.getReportNotice());
            data.put("metadata", reportData.getMetadata());
        }
        
        return data;
    }
    
    /**
     * Renders HTML from the Thymeleaf template
     */
    private String renderHtml(Map<String, Object> data, String templateName) {
        Context context = new Context();
        context.setVariables(data);
        
        return templateEngine.process(templateName, context);
    }
    
    /**
     * Converts HTML to PDF using Flying Saucer + OpenPDF
     */
    private byte[] convertHtmlToPdf(String html) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(baos);
        
        return baos.toByteArray();
    }
    
    /**
     * Creates and configures the Thymeleaf template engine
     */
    private TemplateEngine createTemplateEngine() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML");
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(cacheTemplates); // Configurable caching
        
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(templateResolver);
        
        return engine;
    }
}
