package com.finos.matcher.report.model;

import java.util.Map;

/**
 * Represents chart data
 */
public class ChartData {
    private String title;
    private String chartType; // "bar", "pie", "line"
    private Map<String, Double> data;

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
}
