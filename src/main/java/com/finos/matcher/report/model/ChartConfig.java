package com.finos.matcher.report.model;

import java.awt.Color;
import java.util.List;

/**
 * Configuration for customizing chart appearance
 */
public class ChartConfig {
    private Integer width;
    private Integer height;
    private List<Color> colors;
    private Boolean showLegend;
    private Boolean show3D;
    private String backgroundColorHex;
    private Boolean showGridLines;
    private String xAxisLabel;
    private String yAxisLabel;

    public ChartConfig() {
        // Fields initialized as null - defaults applied during rendering
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public List<Color> getColors() {
        return colors;
    }

    public void setColors(List<Color> colors) {
        this.colors = colors;
    }

    public Boolean getShowLegend() {
        return showLegend;
    }

    public void setShowLegend(Boolean showLegend) {
        this.showLegend = showLegend;
    }

    public Boolean getShow3D() {
        return show3D;
    }

    public void setShow3D(Boolean show3D) {
        this.show3D = show3D;
    }

    public String getBackgroundColorHex() {
        return backgroundColorHex;
    }

    public void setBackgroundColorHex(String backgroundColorHex) {
        this.backgroundColorHex = backgroundColorHex;
    }

    public Boolean getShowGridLines() {
        return showGridLines;
    }

    public void setShowGridLines(Boolean showGridLines) {
        this.showGridLines = showGridLines;
    }

    public String getXAxisLabel() {
        return xAxisLabel;
    }

    public void setXAxisLabel(String xAxisLabel) {
        this.xAxisLabel = xAxisLabel;
    }

    public String getYAxisLabel() {
        return yAxisLabel;
    }

    public void setYAxisLabel(String yAxisLabel) {
        this.yAxisLabel = yAxisLabel;
    }
}
