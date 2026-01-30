package com.mercury.pdf.render;

import com.mercury.pdf.render.model.ChartConfig;
import com.mercury.pdf.render.model.ChartData;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.AreaRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Generates charts as images for embedding in PDFs
 */
public class ChartRenderer {
    
    private static final int DEFAULT_CHART_WIDTH = 500;
    private static final int DEFAULT_CHART_HEIGHT = 300;
    
    // Font configuration for charts
    private Font chartFont = null; // Default is null, will use JFreeChart defaults

    /**
     * Sets a custom font for chart rendering (labels, legends, etc.)
     * This is essential for proper Chinese/CJK character rendering.
     * 
     * @param fontPath Path to the font file (e.g., "classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf")
     */
    public void setChartFont(String fontPath) {
        if (fontPath == null || fontPath.isEmpty()) {
            this.chartFont = null;
            return;
        }
        
        try {
            InputStream fontStream = resolveFontStream(fontPath);
            if (fontStream == null) {
                System.err.println("Warning: Chart font not found: " + fontPath);
                this.chartFont = null;
                return;
            }
            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            // Create font with default size (12pt) and style
            this.chartFont = baseFont.deriveFont(12f);
            fontStream.close();
            System.out.println("✓ Chart font loaded successfully: " + fontPath);
            System.out.println("  Chart font family: " + baseFont.getFamily(Locale.ROOT));
            System.out.println("  Chart font name: " + baseFont.getFontName(Locale.ROOT));
        } catch (Exception e) {
            System.err.println("Warning: Failed to load chart font from " + fontPath + ": " + e.getMessage());
            this.chartFont = null; // Fall back to default
        }
    }
    
    /**
     * Resolves a font path to an InputStream
     */
    private InputStream resolveFontStream(String path) throws IOException {
        if (path.startsWith("classpath:")) {
            String resourcePath = path.substring("classpath:".length());
            return getClass().getResourceAsStream(resourcePath);
        } else {
            java.io.File file = new java.io.File(path);
            if (!file.exists() && path.indexOf('\\') >= 0) {
                file = new java.io.File(path.replace('\\', '/'));
            }
            return new java.io.FileInputStream(file);
        }
    }
    
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
        
        // Apply custom font if configured (for Chinese/CJK support)
        if (chartFont != null) {
            applyChartFont(chart);
        }
        
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
        // Set default white background for chart if no config or transparent not requested
        if (config == null || config.getTransparentBackground() == null || !config.getTransparentBackground()) {
            // Default: white background for the chart
            if (config == null || config.getBackgroundColorHex() == null) {
                chart.setBackgroundPaint(Color.WHITE);
            } else {
                chart.setBackgroundPaint(Color.decode(config.getBackgroundColorHex()));
            }
        } else {
            // Transparent background requested
            chart.setBackgroundPaint(new Color(255, 255, 255, 0)); // Transparent white
        }
        
        // Set default white background for plot area if no config specified
        Plot plot = chart.getPlot();
        if (config == null || config.getPlotBackgroundColorHex() == null) {
            plot.setBackgroundPaint(Color.WHITE);
        } else {
            plot.setBackgroundPaint(Color.decode(config.getPlotBackgroundColorHex()));
        }
        
        if (config == null) {
            return;
        }
        
        // Apply legend settings (default is to show legend)
        if (config.getShowLegend() != null && !config.getShowLegend()) {
            chart.removeLegend();
        } else if (config.getLegendPosition() != null && chart.getLegend() != null) {
            // Apply legend position if specified
            LegendTitle legend = chart.getLegend();
            switch (config.getLegendPosition().toLowerCase()) {
                case "top":
                    legend.setPosition(RectangleEdge.TOP);
                    break;
                case "bottom":
                    legend.setPosition(RectangleEdge.BOTTOM);
                    break;
                case "left":
                    legend.setPosition(RectangleEdge.LEFT);
                    break;
                case "right":
                    legend.setPosition(RectangleEdge.RIGHT);
                    break;
                default:
                    // Keep default position (bottom)
                    break;
            }
        }
        
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
    
    /**
     * Applies the custom font to all text elements in the chart.
     * This ensures Chinese/CJK characters are rendered correctly.
     */
    private void applyChartFont(JFreeChart chart) {
        if (chartFont == null) {
            return;
        }
        
        Plot plot = chart.getPlot();
        
        // Apply font to category plot axes
        if (plot instanceof CategoryPlot) {
            CategoryPlot categoryPlot = (CategoryPlot) plot;
            
            // Domain axis (X-axis)
            if (categoryPlot.getDomainAxis() != null) {
                categoryPlot.getDomainAxis().setLabelFont(chartFont.deriveFont(Font.BOLD, 12f));
                categoryPlot.getDomainAxis().setTickLabelFont(chartFont.deriveFont(11f));
            }
            
            // Range axis (Y-axis)
            if (categoryPlot.getRangeAxis() != null) {
                categoryPlot.getRangeAxis().setLabelFont(chartFont.deriveFont(Font.BOLD, 12f));
                categoryPlot.getRangeAxis().setTickLabelFont(chartFont.deriveFont(11f));
            }
        }
        
        // Apply font to pie plot labels
        if (plot instanceof PiePlot) {
            PiePlot piePlot = (PiePlot) plot;
            piePlot.setLabelFont(chartFont.deriveFont(11f));
        }
        
        // Apply font to legend
        if (chart.getLegend() != null) {
            chart.getLegend().setItemFont(chartFont.deriveFont(11f));
        }
    }

    Font getChartFont() {
        return chartFont;
    }
}
