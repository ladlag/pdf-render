package com.finos.matcher.report;

import com.finos.matcher.report.model.ChartData;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

/**
 * Generates charts as images for embedding in PDFs
 */
public class ChartRenderer {
    
    private static final int CHART_WIDTH = 500;
    private static final int CHART_HEIGHT = 300;

    /**
     * Generates a chart and returns it as a BufferedImage
     */
    public BufferedImage generateChart(ChartData chartData) {
        JFreeChart chart;
        
        switch (chartData.getChartType().toLowerCase()) {
            case "pie":
                chart = createPieChart(chartData);
                break;
            case "bar":
            default:
                chart = createBarChart(chartData);
                break;
        }
        
        return chart.createBufferedImage(CHART_WIDTH, CHART_HEIGHT);
    }
    
    /**
     * Generates a chart and returns it as a base64-encoded PNG string
     */
    public String generateChartAsBase64(ChartData chartData) throws IOException {
        BufferedImage image = generateChart(chartData);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        byte[] imageBytes = baos.toByteArray();
        
        return Base64.getEncoder().encodeToString(imageBytes);
    }
    
    private JFreeChart createBarChart(ChartData chartData) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (Map.Entry<String, Double> entry : chartData.getData().entrySet()) {
            dataset.addValue(entry.getValue(), "Series", entry.getKey());
        }
        
        return ChartFactory.createBarChart(
            null,  // No title in chart image - title is rendered in HTML
            "Category",
            "Value",
            dataset
        );
    }
    
    private JFreeChart createPieChart(ChartData chartData) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        
        for (Map.Entry<String, Double> entry : chartData.getData().entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }
        
        return ChartFactory.createPieChart(
            null,  // No title in chart image - title is rendered in HTML
            dataset,
            true,
            true,
            false
        );
    }
}
