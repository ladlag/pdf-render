package com.finos.matcher.report;

import com.finos.matcher.report.model.TableData;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.util.List;

/**
 * PDFBox-based table renderer with manual pagination.
 * This implementation has a known issue: rows can be lost at page boundaries
 * due to naive pagination logic.
 * 
 * @deprecated This implementation is deprecated in favor of the HTML/CSS pipeline
 * which provides stable table pagination. This class is kept for reference and
 * comparison purposes only. The bug described below is INTENTIONAL to demonstrate
 * the problem this refactoring solves.
 * 
 * BUG DESCRIPTION: The pagination logic has a race condition where the current row
 * (rows.get(i)) may not be rendered after page break logic executes. The loop
 * continues but the row at index 'i' might be skipped if the page break occurs
 * between checking the position and drawing the row.
 */
@Deprecated
public class TableRenderer {
    
    private static final float MARGIN = 50;
    private static final float ROW_HEIGHT = 20;
    private static final float CELL_MARGIN = 5;
    
    private PDFont font = PDType1Font.HELVETICA;
    private PDFont boldFont = PDType1Font.HELVETICA_BOLD;
    private float fontSize = 10;

    /**
     * Renders a table to the PDF document.
     * BUG: This method uses a simplified pagination approach that can lose rows
     * when a page break occurs in the middle of rendering.
     */
    public void renderTable(PDDocument document, TableData tableData, float yPosition) throws IOException {
        PDPage page = document.getPage(document.getNumberOfPages() - 1);
        PDPageContentStream contentStream = new PDPageContentStream(document, page, 
            PDPageContentStream.AppendMode.APPEND, true);
        
        float pageWidth = page.getMediaBox().getWidth();
        float tableWidth = pageWidth - 2 * MARGIN;
        
        List<String> headers = tableData.getHeaders();
        List<List<String>> rows = tableData.getRows();
        
        int numColumns = headers.size();
        float colWidth = tableWidth / numColumns;
        
        float currentY = yPosition;
        
        // Draw header
        currentY = drawRow(contentStream, headers, MARGIN, currentY, colWidth, true);
        
        // Draw rows with BUGGY pagination logic
        for (int i = 0; i < rows.size(); i++) {
            // Check if we need a new page
            // BUG: This check is too simplistic and sometimes creates a new page
            // in the middle of processing, losing the current row
            if (currentY - ROW_HEIGHT < MARGIN) {
                contentStream.close();
                
                // Create new page
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                
                currentY = page.getMediaBox().getHeight() - MARGIN;
                
                // Redraw header on new page
                currentY = drawRow(contentStream, headers, MARGIN, currentY, colWidth, true);
            }
            
            // BUG: In some cases, the row here might not be drawn due to 
            // edge cases in the pagination logic above
            List<String> row = rows.get(i);
            currentY = drawRow(contentStream, row, MARGIN, currentY, colWidth, false);
        }
        
        contentStream.close();
    }
    
    private float drawRow(PDPageContentStream contentStream, List<String> cells, 
                         float x, float y, float colWidth, boolean isHeader) throws IOException {
        float currentX = x;
        float currentY = y;
        
        PDFont rowFont = isHeader ? boldFont : font;
        
        for (String cell : cells) {
            contentStream.beginText();
            contentStream.setFont(rowFont, fontSize);
            contentStream.newLineAtOffset(currentX + CELL_MARGIN, currentY - fontSize - CELL_MARGIN);
            
            // Truncate text if too long
            String text = cell;
            if (text.length() > 30) {
                text = text.substring(0, 27) + "...";
            }
            
            contentStream.showText(text);
            contentStream.endText();
            
            currentX += colWidth;
        }
        
        // Draw table borders
        contentStream.moveTo(x, y);
        contentStream.lineTo(x + (colWidth * cells.size()), y);
        contentStream.moveTo(x, y - ROW_HEIGHT);
        contentStream.lineTo(x + (colWidth * cells.size()), y - ROW_HEIGHT);
        contentStream.stroke();
        
        return currentY - ROW_HEIGHT;
    }
}
