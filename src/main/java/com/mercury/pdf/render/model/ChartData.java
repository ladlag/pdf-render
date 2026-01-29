package com.mercury.pdf.render.model;

import java.util.Map;

/**
 * Represents chart data
 */
public class ChartData {
    private String title;
    private String chartType; // "bar", "pie", "line", "area", "stackedbar"
    private Map<String, Double> data;
    private String base64Image; // For HTML rendering
    private ChartConfig config; // Optional configuration for customization

    public ChartData() {
    }

    public ChartData(String title, String chartType, Map<String, Double> data) {
        this.title = title;
        this.chartType = chartType;
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getChartType() {
        return chartType;
    }

    public void setChartType(String chartType) {
        this.chartType = chartType;
    }

    public Map<String, Double> getData() {
        return data;
    }

    public void setData(Map<String, Double> data) {
        this.data = data;
    }

    public String getBase64Image() {
        return base64Image;
    }

    public void setBase64Image(String base64Image) {
        this.base64Image = base64Image;
    }

    public ChartConfig getConfig() {
        return config;
    }

    public void setConfig(ChartConfig config) {
        this.config = config;
    }
}
