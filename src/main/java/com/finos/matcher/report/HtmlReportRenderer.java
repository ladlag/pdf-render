package com.finos.matcher.report;

import com.finos.matcher.report.model.ChartData;
import com.finos.matcher.report.model.ReportData;
import com.lowagie.text.DocumentException;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for rendering PDF reports using HTML/CSS templates and Flying Saucer.
 * This approach provides stable table pagination without row loss.
 */
public class HtmlReportRenderer {
    
    private final TemplateEngine templateEngine;
    private final ChartRenderer chartRenderer;
    
    public HtmlReportRenderer() {
        this.templateEngine = createTemplateEngine();
        this.chartRenderer = new ChartRenderer();
    }
    
    /**
     * Generates a PDF from ReportData using the HTML/CSS pipeline
     */
    public byte[] generatePdf(ReportData reportData) throws IOException, DocumentException {
        // Step 1: Prepare data for template (including chart images)
        Map<String, Object> templateData = prepareTemplateData(reportData);
        
        // Step 2: Render HTML from template
        String html = renderHtml(templateData);
        
        // Step 3: Convert HTML to PDF using Flying Saucer
        return convertHtmlToPdf(html);
    }
    
    /**
     * Prepares data for the Thymeleaf template, including converting charts to base64 images
     */
    private Map<String, Object> prepareTemplateData(ReportData reportData) throws IOException {
        Map<String, Object> data = new HashMap<>();
        
        // Cover page data
        data.put("title", reportData.getTitle());
        data.put("subtitle", reportData.getSubtitle());
        data.put("reportDate", reportData.getReportDate());
        data.put("reportNumber", reportData.getReportNumber());
        
        // Section 1: Table blocks
        data.put("tableBlocks", reportData.getTableBlocks());
        
        // Section 2: Analysis paragraphs
        data.put("analysisParagraphs", reportData.getAnalysisParagraphs());
        
        // Section 3: Summary table
        data.put("summaryTable", reportData.getSummaryTable());
        
        // Section 3: Charts (convert to base64 images)
        if (reportData.getCharts() != null && !reportData.getCharts().isEmpty()) {
            for (ChartData chart : reportData.getCharts()) {
                String base64Image = chartRenderer.generateChartAsBase64(chart);
                chart.setBase64Image(base64Image);
            }
            data.put("charts", reportData.getCharts());
        }
        
        // Section 4: Notice and metadata
        data.put("reportNotice", reportData.getReportNotice());
        data.put("metadata", reportData.getMetadata());
        
        return data;
    }
    
    /**
     * Renders HTML from the Thymeleaf template
     */
    private String renderHtml(Map<String, Object> data) {
        Context context = new Context();
        context.setVariables(data);
        
        return templateEngine.process("report", context);
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
        templateResolver.setCacheable(false); // For development; enable for production
        
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(templateResolver);
        
        return engine;
    }
}
