package com.finos.matcher.report;

import com.finos.matcher.report.model.ChartData;
import com.finos.matcher.report.model.ReportData;
import com.finos.matcher.report.model.TableBlock;
import com.lowagie.text.DocumentException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Main service for generating PDF reports.
 * Now uses HTML/CSS -> PDF pipeline by default (Flying Saucer + OpenPDF).
 * The old PDFBox implementation is kept for reference but deprecated.
 */
public class ReportService {
    
    private static final float MARGIN = 50;
    private static final float TITLE_FONT_SIZE = 24;
    private static final float HEADING_FONT_SIZE = 16;
    private static final float NORMAL_FONT_SIZE = 12;
    private static final float LINE_HEIGHT = 15;
    private static final float TABLE_BLOCK_Y_POSITION = 180; // Y position offset for table blocks
    
    private boolean useHtmlPipeline = true; // Now defaults to true
    private TableRenderer tableRenderer = new TableRenderer();
    private ChartRenderer chartRenderer = new ChartRenderer();
    private HtmlReportRenderer htmlRenderer = new HtmlReportRenderer();

    /**
     * Generates a PDF report and returns it as a byte array.
     * This is the main public API that must remain consistent.
     */
    public byte[] generatePdf(ReportData reportData) throws IOException {
        return generatePdf(reportData, null);
    }
    
    /**
     * Generates a PDF report using a specific template and returns it as a byte array.
     * 
     * @param reportData Report data to render
     * @param templateName Template name without .html extension (e.g., "report", "invoice"). 
     *                     If null, uses the default template.
     * @return PDF content as byte array
     */
    public byte[] generatePdf(ReportData reportData, String templateName) throws IOException {
        if (useHtmlPipeline) {
            return generatePdfWithHtmlPipeline(reportData, templateName);
        } else {
            return generatePdfWithPdfBox(reportData);
        }
    }
    
    /**
     * Enable or disable the HTML/CSS pipeline (for testing/comparison)
     */
    public void setUseHtmlPipeline(boolean useHtmlPipeline) {
        this.useHtmlPipeline = useHtmlPipeline;
    }
    
    /**
     * Phase 1: PDFBox-based implementation (deprecated, kept for reference)
     * @deprecated Use HTML pipeline instead (default). This method has known issues
     * with table pagination where rows can be lost at page boundaries.
     */
    @Deprecated
    private byte[] generatePdfWithPdfBox(ReportData reportData) throws IOException {
        try (PDDocument document = new PDDocument()) {
            
            // Cover page
            addCoverPage(document, reportData);
            
            // Section 1: Detailed tables with grouped blocks (1.1-1.4)
            addSection1(document, reportData);
            
            // Section 2: Analysis paragraphs
            addSection2(document, reportData);
            
            // Section 3: Summary table and charts
            addSection3(document, reportData);
            
            // Section 4: Report notice and metadata
            addSection4(document, reportData);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }
    
    /**
     * Phase 2: HTML/CSS-based implementation (now the default)
     */
    private byte[] generatePdfWithHtmlPipeline(ReportData reportData, String templateName) throws IOException {
        try {
            if (templateName != null && !templateName.isEmpty()) {
                return htmlRenderer.generatePdf(reportData, templateName);
            } else {
                return htmlRenderer.generatePdf(reportData);
            }
        } catch (DocumentException e) {
            throw new IOException("Failed to generate PDF from HTML", e);
        }
    }
    
    private void addCoverPage(PDDocument document, ReportData reportData) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float y = page.getMediaBox().getHeight() - MARGIN - 100;
            
            // Title
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, TITLE_FONT_SIZE);
            contentStream.newLineAtOffset(MARGIN, y);
            contentStream.showText(reportData.getTitle() != null ? reportData.getTitle() : "Report");
            contentStream.endText();
            
            y -= 40;
            
            // Subtitle
            if (reportData.getSubtitle() != null) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, HEADING_FONT_SIZE);
                contentStream.newLineAtOffset(MARGIN, y);
                contentStream.showText(reportData.getSubtitle());
                contentStream.endText();
                y -= 30;
            }
            
            // Report metadata
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, NORMAL_FONT_SIZE);
            contentStream.newLineAtOffset(MARGIN, y);
            contentStream.showText("Report Number: " + (reportData.getReportNumber() != null ? reportData.getReportNumber() : "N/A"));
            contentStream.endText();
            
            y -= LINE_HEIGHT;
            
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, NORMAL_FONT_SIZE);
            contentStream.newLineAtOffset(MARGIN, y);
            contentStream.showText("Report Date: " + (reportData.getReportDate() != null ? reportData.getReportDate() : "N/A"));
            contentStream.endText();
        }
    }
    
    private void addSection1(PDDocument document, ReportData reportData) throws IOException {
        if (reportData.getTableBlocks() == null || reportData.getTableBlocks().isEmpty()) {
            return;
        }
        
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float y = page.getMediaBox().getHeight() - MARGIN;
            
            // Section heading
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, HEADING_FONT_SIZE);
            contentStream.newLineAtOffset(MARGIN, y);
            contentStream.showText("Section 1: Detailed Analysis");
            contentStream.endText();
            
            y -= 40;
        }
        
        // Render each table block
        for (TableBlock block : reportData.getTableBlocks()) {
            PDPage currentPage = document.getPage(document.getNumberOfPages() - 1);
            
            try (PDPageContentStream contentStream = new PDPageContentStream(document, currentPage, 
                PDPageContentStream.AppendMode.APPEND, true)) {
                
                float y = MARGIN + TABLE_BLOCK_Y_POSITION;
                
                // Block title
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, NORMAL_FONT_SIZE);
                contentStream.newLineAtOffset(MARGIN, y);
                contentStream.showText(block.getBlockId() + " - " + block.getBlockTitle());
                contentStream.endText();
            }
            
            // Render table (this is where rows can be lost in the deprecated PDFBox implementation!)
            tableRenderer.renderTable(document, block.getTableData(), MARGIN + TABLE_BLOCK_Y_POSITION);
        }
    }
    
    private void addSection2(PDDocument document, ReportData reportData) throws IOException {
        if (reportData.getAnalysisParagraphs() == null || reportData.getAnalysisParagraphs().isEmpty()) {
            return;
        }
        
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float y = page.getMediaBox().getHeight() - MARGIN;
            
            // Section heading
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, HEADING_FONT_SIZE);
            contentStream.newLineAtOffset(MARGIN, y);
            contentStream.showText("Section 2: Analysis");
            contentStream.endText();
            
            y -= 40;
            
            // Render paragraphs
            contentStream.setFont(PDType1Font.HELVETICA, NORMAL_FONT_SIZE);
            for (String paragraph : reportData.getAnalysisParagraphs()) {
                contentStream.beginText();
                contentStream.newLineAtOffset(MARGIN, y);
                
                // Simple text wrapping
                String[] words = paragraph.split(" ");
                StringBuilder line = new StringBuilder();
                float maxWidth = page.getMediaBox().getWidth() - 2 * MARGIN;
                
                for (String word : words) {
                    String testLine = line.length() > 0 ? line + " " + word : word;
                    float textWidth = PDType1Font.HELVETICA.getStringWidth(testLine) / 1000 * NORMAL_FONT_SIZE;
                    
                    if (textWidth > maxWidth) {
                        contentStream.showText(line.toString());
                        contentStream.endText();
                        y -= LINE_HEIGHT;
                        contentStream.beginText();
                        contentStream.newLineAtOffset(MARGIN, y);
                        line = new StringBuilder(word);
                    } else {
                        line = new StringBuilder(testLine);
                    }
                }
                
                if (line.length() > 0) {
                    contentStream.showText(line.toString());
                }
                
                contentStream.endText();
                y -= LINE_HEIGHT * 2; // Extra space between paragraphs
            }
        }
    }
    
    private void addSection3(PDDocument document, ReportData reportData) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float y = page.getMediaBox().getHeight() - MARGIN;
            
            // Section heading
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, HEADING_FONT_SIZE);
            contentStream.newLineAtOffset(MARGIN, y);
            contentStream.showText("Section 3: Summary");
            contentStream.endText();
            
            y -= 40;
        }
        
        // Summary table
        if (reportData.getSummaryTable() != null) {
            tableRenderer.renderTable(document, reportData.getSummaryTable(), 
                page.getMediaBox().getHeight() - MARGIN - 60);
        }
        
        // Charts
        if (reportData.getCharts() != null && !reportData.getCharts().isEmpty()) {
            float y = 400; // Position for charts
            
            for (ChartData chartData : reportData.getCharts()) {
                BufferedImage chartImage = chartRenderer.generateChart(chartData);
                
                // Save chart to temp file and add to PDF
                File tempFile = File.createTempFile("chart_", ".png");
                ImageIO.write(chartImage, "PNG", tempFile);
                
                PDPage currentPage = document.getPage(document.getNumberOfPages() - 1);
                PDImageXObject pdImage = PDImageXObject.createFromFile(tempFile.getAbsolutePath(), document);
                
                try (PDPageContentStream contentStream = new PDPageContentStream(document, currentPage, 
                    PDPageContentStream.AppendMode.APPEND, true)) {
                    
                    contentStream.drawImage(pdImage, MARGIN, y, 300, 200);
                }
                
                tempFile.delete();
                y -= 220;
            }
        }
    }
    
    private void addSection4(PDDocument document, ReportData reportData) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float y = page.getMediaBox().getHeight() - MARGIN;
            
            // Section heading
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, HEADING_FONT_SIZE);
            contentStream.newLineAtOffset(MARGIN, y);
            contentStream.showText("Section 4: Notice and Metadata");
            contentStream.endText();
            
            y -= 40;
            
            // Notice
            if (reportData.getReportNotice() != null) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, NORMAL_FONT_SIZE);
                contentStream.newLineAtOffset(MARGIN, y);
                contentStream.showText(reportData.getReportNotice());
                contentStream.endText();
                y -= LINE_HEIGHT * 2;
            }
            
            // Metadata
            if (reportData.getMetadata() != null) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, NORMAL_FONT_SIZE);
                contentStream.newLineAtOffset(MARGIN, y);
                contentStream.showText(reportData.getMetadata());
                contentStream.endText();
            }
        }
    }
}
