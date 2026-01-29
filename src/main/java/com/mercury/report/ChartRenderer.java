package com.mercury.report;

import com.mercury.report.model.ChartConfig;
import com.mercury.report.model.ChartData;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.AreaRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Generates charts as images for embedding in PDFs
 */
public class ChartRenderer {
    
    private static final int DEFAULT_CHART_WIDTH = 500;
    private static final int DEFAULT_CHART_HEIGHT = 300;

    /**
     * Generates a chart and returns it as a BufferedImage
     */
    public BufferedImage generateChart(ChartData chartData) {
        JFreeChart chart;
        
        switch (chartData.getChartType().toLowerCase()) {
            case "pie":
                chart = createPieChart(chartData);
                break;
            case "line":
                chart = createLineChart(chartData);
                break;
            case "area":
                chart = createAreaChart(chartData);
                break;
            case "stackedbar":
                chart = createStackedBarChart(chartData);
                break;
            case "bar":
            default:
                chart = createBarChart(chartData);
                break;
        }
        
        // Apply configuration
        applyChartConfig(chart, chartData.getConfig());
        
        int width = (chartData.getConfig() != null && chartData.getConfig().getWidth() != null) 
            ? chartData.getConfig().getWidth() : DEFAULT_CHART_WIDTH;
        int height = (chartData.getConfig() != null && chartData.getConfig().getHeight() != null) 
            ? chartData.getConfig().getHeight() : DEFAULT_CHART_HEIGHT;
        
        return chart.createBufferedImage(width, height);
    }
    
    /**
     * Generates a chart and returns it as a base64-encoded PNG string (raw base64 without data URI prefix).
     * For HTML embedding, consider using generateChartAsDataUri() instead.
     */
    public String generateChartAsBase64(ChartData chartData) throws IOException {
        BufferedImage image = generateChart(chartData);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        byte[] imageBytes = baos.toByteArray();
        
        return Base64.getEncoder().encodeToString(imageBytes);
    }
    
    /**
     * Generates a chart and returns it as a complete data URI string ready for HTML embedding.
     * Format: data:image/png;base64,{base64-encoded-png}
     */
    public String generateChartAsDataUri(ChartData chartData) throws IOException {
        return "data:image/png;base64," + generateChartAsBase64(chartData);
    }
    
    private JFreeChart createBarChart(ChartData chartData) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (Map.Entry<String, Double> entry : chartData.getData().entrySet()) {
            dataset.addValue(entry.getValue(), "Series", entry.getKey());
        }
        
        ChartConfig config = chartData.getConfig();
        String xLabel = config != null && config.getXAxisLabel() != null ? config.getXAxisLabel() : "Category";
        String yLabel = config != null && config.getYAxisLabel() != null ? config.getYAxisLabel() : "Value";
        
        return ChartFactory.createBarChart(
            null,  // No title in chart image - title is rendered in HTML
            xLabel,
            yLabel,
            dataset
        );
    }
    
    private JFreeChart createPieChart(ChartData chartData) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        
        for (Map.Entry<String, Double> entry : chartData.getData().entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }
        
        ChartConfig config = chartData.getConfig();
        boolean show3D = config != null && config.getShow3D() != null && config.getShow3D();
        
        if (show3D) {
            return ChartFactory.createPieChart3D(
                null,  // No title in chart image - title is rendered in HTML
                dataset,
                true,
                true,
                false
            );
        } else {
            return ChartFactory.createPieChart(
                null,  // No title in chart image - title is rendered in HTML
                dataset,
                true,
                true,
                false
            );
        }
    }
    
    private JFreeChart createLineChart(ChartData chartData) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (Map.Entry<String, Double> entry : chartData.getData().entrySet()) {
            dataset.addValue(entry.getValue(), "Series", entry.getKey());
        }
        
        ChartConfig config = chartData.getConfig();
        String xLabel = config != null && config.getXAxisLabel() != null ? config.getXAxisLabel() : "Category";
        String yLabel = config != null && config.getYAxisLabel() != null ? config.getYAxisLabel() : "Value";
        
        return ChartFactory.createLineChart(
            null,  // No title in chart image - title is rendered in HTML
            xLabel,
            yLabel,
            dataset
        );
    }
    
    private JFreeChart createAreaChart(ChartData chartData) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (Map.Entry<String, Double> entry : chartData.getData().entrySet()) {
            dataset.addValue(entry.getValue(), "Series", entry.getKey());
        }
        
        ChartConfig config = chartData.getConfig();
        String xLabel = config != null && config.getXAxisLabel() != null ? config.getXAxisLabel() : "Category";
        String yLabel = config != null && config.getYAxisLabel() != null ? config.getYAxisLabel() : "Value";
        
        return ChartFactory.createAreaChart(
            null,  // No title in chart image - title is rendered in HTML
            xLabel,
            yLabel,
            dataset
        );
    }
    
    private JFreeChart createStackedBarChart(ChartData chartData) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (Map.Entry<String, Double> entry : chartData.getData().entrySet()) {
            dataset.addValue(entry.getValue(), "Series", entry.getKey());
        }
        
        ChartConfig config = chartData.getConfig();
        String xLabel = config != null && config.getXAxisLabel() != null ? config.getXAxisLabel() : "Category";
        String yLabel = config != null && config.getYAxisLabel() != null ? config.getYAxisLabel() : "Value";
        
        return ChartFactory.createStackedBarChart(
            null,  // No title in chart image - title is rendered in HTML
            xLabel,
            yLabel,
            dataset
        );
    }
    
    /**
     * Applies configuration settings to a chart
     */
    private void applyChartConfig(JFreeChart chart, ChartConfig config) {
        if (config == null) {
            return;
        }
        
        // Apply background color
        if (config.getBackgroundColorHex() != null) {
            chart.setBackgroundPaint(Color.decode(config.getBackgroundColorHex()));
        }
        
        // Apply legend settings (default is to show legend)
        if (config.getShowLegend() != null && !config.getShowLegend()) {
            chart.removeLegend();
        }
        
        Plot plot = chart.getPlot();
        
        // Apply custom colors for category plots
        if (plot instanceof CategoryPlot && config.getColors() != null && !config.getColors().isEmpty()) {
            CategoryPlot categoryPlot = (CategoryPlot) plot;
            applyCategoryColors(categoryPlot, config.getColors());
        }
        
        // Apply custom colors for pie plots
        if (plot instanceof PiePlot && config.getColors() != null && !config.getColors().isEmpty()) {
            PiePlot piePlot = (PiePlot) plot;
            applyPieColors(piePlot, config.getColors());
        }
        
        // Apply grid lines settings (default is to show grid lines)
        if (plot instanceof CategoryPlot) {
            CategoryPlot categoryPlot = (CategoryPlot) plot;
            if (config.getShowGridLines() != null && !config.getShowGridLines()) {
                categoryPlot.setDomainGridlinesVisible(false);
                categoryPlot.setRangeGridlinesVisible(false);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void applyCategoryColors(CategoryPlot plot, List<Color> colors) {
        // For single-series charts, apply colors to each category
        if (plot.getDataset() != null && plot.getDataset().getRowCount() == 1) {
            // Color each category (column) in the dataset
            int categoryCount = plot.getDataset().getColumnCount();
            if (plot.getRenderer() instanceof org.jfree.chart.renderer.category.BarRenderer) {
                org.jfree.chart.renderer.category.BarRenderer renderer = 
                    (org.jfree.chart.renderer.category.BarRenderer) plot.getRenderer();
                for (int i = 0; i < categoryCount && i < colors.size(); i++) {
                    renderer.setSeriesPaint(0, colors.get(i % colors.size()));
                }
            } else if (plot.getRenderer() instanceof LineAndShapeRenderer) {
                LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
                renderer.setSeriesPaint(0, colors.get(0));
            } else if (plot.getRenderer() instanceof AreaRenderer) {
                AreaRenderer renderer = (AreaRenderer) plot.getRenderer();
                renderer.setSeriesPaint(0, colors.get(0));
            } else if (plot.getRenderer() instanceof StackedBarRenderer) {
                StackedBarRenderer renderer = (StackedBarRenderer) plot.getRenderer();
                renderer.setSeriesPaint(0, colors.get(0));
            }
        } else {
            // For multi-series charts, apply colors to each series
            if (plot.getRenderer() instanceof org.jfree.chart.renderer.category.BarRenderer) {
                org.jfree.chart.renderer.category.BarRenderer renderer = 
                    (org.jfree.chart.renderer.category.BarRenderer) plot.getRenderer();
                for (int i = 0; i < colors.size(); i++) {
                    renderer.setSeriesPaint(i, colors.get(i));
                }
            } else if (plot.getRenderer() instanceof LineAndShapeRenderer) {
                LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
                for (int i = 0; i < colors.size(); i++) {
                    renderer.setSeriesPaint(i, colors.get(i));
                }
            } else if (plot.getRenderer() instanceof AreaRenderer) {
                AreaRenderer renderer = (AreaRenderer) plot.getRenderer();
                for (int i = 0; i < colors.size(); i++) {
                    renderer.setSeriesPaint(i, colors.get(i));
                }
            } else if (plot.getRenderer() instanceof StackedBarRenderer) {
                StackedBarRenderer renderer = (StackedBarRenderer) plot.getRenderer();
                for (int i = 0; i < colors.size(); i++) {
                    renderer.setSeriesPaint(i, colors.get(i));
                }
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void applyPieColors(PiePlot plot, List<Color> colors) {
        int colorIndex = 0;
        for (Object key : plot.getDataset().getKeys()) {
            if (colorIndex < colors.size()) {
                plot.setSectionPaint((Comparable) key, colors.get(colorIndex));
                colorIndex++;
            }
        }
    }
}
