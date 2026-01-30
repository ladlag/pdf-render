# 图表自定义指南 / Chart Customization Guide

[中文](#中文) | [English](#english)

---

## 中文

### 概述

本指南介绍如何自定义图表外观，特别是背景色和图例配置。

### 问题背景

**v1.0.0 之前的问题：**
- ❌ 图表背景是灰色，与PDF白色背景不匹配
- ❌ 图例无法自定义位置
- ❌ 不支持透明背景

**v1.0.1+ 已修复：**
- ✅ 默认白色背景，与PDF完美匹配
- ✅ 支持图例位置自定义（上/下/左/右）
- ✅ 支持透明背景
- ✅ 支持自定义绘图区背景色

### 快速开始

#### 1. 默认白色背景（推荐）

无需任何配置，图表自动使用白色背景：

```java
Map<String, Double> data = new LinkedHashMap<>();
data.put("Q1", 100.0);
data.put("Q2", 150.0);
data.put("Q3", 200.0);

// 自动使用白色背景
ChartData chart = new ChartData("季度销售", "bar", data);
section.addChart(chart);
```

**效果：**
- 图表背景：白色
- 绘图区背景：白色
- 与PDF背景完美融合

#### 2. 透明背景

适用于需要完全透明效果的场景：

```java
ChartConfig config = new ChartConfig();
config.setTransparentBackground(true);

ChartData chart = new ChartData("销售趋势", "line", data);
chart.setConfig(config);
```

#### 3. 自定义图例位置

```java
ChartConfig config = new ChartConfig();
config.setLegendPosition("top");  // 图例在顶部

ChartData chart = new ChartData("产品对比", "pie", data);
chart.setConfig(config);
```

**支持的位置：**
- `"top"` - 顶部
- `"bottom"` - 底部（默认）
- `"left"` - 左侧
- `"right"` - 右侧

#### 4. 隐藏图例

```java
ChartConfig config = new ChartConfig();
config.setShowLegend(false);

ChartData chart = new ChartData("简洁图表", "bar", data);
chart.setConfig(config);
```

### 高级配置

#### 自定义背景色

```java
ChartConfig config = new ChartConfig();
// 图表整体背景
config.setBackgroundColorHex("#FFFFFF");      // 白色
// 绘图区背景（数据显示区域）
config.setPlotBackgroundColorHex("#F5F5F5");  // 浅灰色

ChartData chart = new ChartData("对比分析", "bar", data);
chart.setConfig(config);
```

#### 组合配置示例

```java
ChartConfig config = new ChartConfig();
// 尺寸
config.setWidth(600);
config.setHeight(400);

// 背景
config.setBackgroundColorHex("#FFFFFF");
config.setPlotBackgroundColorHex("#F8F9FA");

// 图例
config.setLegendPosition("right");

// 网格线
config.setShowGridLines(true);

// 自定义颜色
config.setColors(Arrays.asList(
    new Color(52, 152, 219),   // 蓝色
    new Color(46, 204, 113),   // 绿色
    new Color(155, 89, 182),   // 紫色
    new Color(241, 196, 15)    // 黄色
));

ChartData chart = new ChartData("综合报表", "bar", data);
chart.setConfig(config);
```

### 配置选项速查表

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `width` | Integer | 500 | 图表宽度（像素） |
| `height` | Integer | 300 | 图表高度（像素） |
| `backgroundColorHex` | String | `"#FFFFFF"` | 图表背景色（十六进制） |
| `plotBackgroundColorHex` | String | `"#FFFFFF"` | 绘图区背景色 |
| `transparentBackground` | Boolean | false | 是否使用透明背景 |
| `showLegend` | Boolean | true | 是否显示图例 |
| `legendPosition` | String | `"bottom"` | 图例位置 |
| `showGridLines` | Boolean | true | 是否显示网格线 |
| `colors` | List&lt;Color&gt; | 默认调色板 | 自定义颜色列表 |
| `xAxisLabel` | String | null | X轴标签 |
| `yAxisLabel` | String | null | Y轴标签 |
| `show3D` | Boolean | false | 是否使用3D效果（仅饼图） |

### 最佳实践

#### 1. PDF报表（推荐）

```java
// 使用默认白色背景，无需配置
ChartData chart = new ChartData("报表图表", "bar", data);
```

**优点：**
- 与PDF白色背景完美匹配
- 打印效果最佳
- 文件大小最小

#### 2. 透明背景

```java
ChartConfig config = new ChartConfig();
config.setTransparentBackground(true);
```

**适用场景：**
- 需要叠加在背景图上
- 特殊设计需求

#### 3. 图例位置选择

- **底部**（默认）：适合宽图表
- **右侧**：适合高图表或数据项较多
- **顶部**：适合强调图例重要性
- **左侧**：较少使用
- **隐藏**：数据项少且明显时

### 迁移指南

#### 从 v1.0.0 迁移

**旧代码（v1.0.0）：**
```java
ChartData chart = new ChartData("图表", "bar", data);
// 图表背景是灰色
```

**新代码（v1.0.1+）：**
```java
ChartData chart = new ChartData("图表", "bar", data);
// 图表背景自动是白色，无需修改
```

✅ **向后兼容** - 现有代码自动获得白色背景改进！

#### 保持灰色背景（如果需要）

如果确实需要保持旧的灰色背景：

```java
ChartConfig config = new ChartConfig();
config.setBackgroundColorHex("#CCCCCC");  // 灰色
config.setPlotBackgroundColorHex("#CCCCCC");

ChartData chart = new ChartData("图表", "bar", data);
chart.setConfig(config);
```

### 常见问题

#### Q: 为什么图表背景自动变白了？

A: v1.0.1+ 默认使用白色背景，与PDF背景完美匹配。这是有意为之的改进。

#### Q: 如何让图表背景完全透明？

A: 设置 `config.setTransparentBackground(true)`

#### Q: 可以为图表和绘图区设置不同的背景色吗？

A: 可以！使用 `backgroundColorHex`（整体）和 `plotBackgroundColorHex`（绘图区）。

#### Q: 图例位置不支持自定义大小或字体吗？

A: 当前版本支持位置和显示/隐藏。字体会自动使用配置的中文字体。

#### Q: 透明背景在PDF中显示正常吗？

A: 是的，透明背景在PDF中正常工作。

### 示例代码

完整示例请参考：
- `ChartBackgroundAndLegendTest.java` - 背景和图例测试
- `ChartTypesAndConfigTest.java` - 图表类型和配置测试

---

## English

### Overview

This guide explains how to customize chart appearance, especially background colors and legend configuration.

### Background

**Problems before v1.0.0:**
- ❌ Chart background was gray, didn't match PDF white background
- ❌ Legend position couldn't be customized
- ❌ No transparent background support

**Fixed in v1.0.1+:**
- ✅ Default white background, perfect match with PDF
- ✅ Legend position customization (top/bottom/left/right)
- ✅ Transparent background support
- ✅ Custom plot background color

### Quick Start

#### 1. Default White Background (Recommended)

No configuration needed, charts automatically use white background:

```java
Map<String, Double> data = new LinkedHashMap<>();
data.put("Q1", 100.0);
data.put("Q2", 150.0);
data.put("Q3", 200.0);

// Automatically uses white background
ChartData chart = new ChartData("Quarterly Sales", "bar", data);
section.addChart(chart);
```

**Result:**
- Chart background: White
- Plot background: White
- Perfect integration with PDF background

#### 2. Transparent Background

For scenarios requiring fully transparent effect:

```java
ChartConfig config = new ChartConfig();
config.setTransparentBackground(true);

ChartData chart = new ChartData("Sales Trend", "line", data);
chart.setConfig(config);
```

#### 3. Custom Legend Position

```java
ChartConfig config = new ChartConfig();
config.setLegendPosition("top");  // Legend at top

ChartData chart = new ChartData("Product Comparison", "pie", data);
chart.setConfig(config);
```

**Supported positions:**
- `"top"` - Top
- `"bottom"` - Bottom (default)
- `"left"` - Left
- `"right"` - Right

#### 4. Hide Legend

```java
ChartConfig config = new ChartConfig();
config.setShowLegend(false);

ChartData chart = new ChartData("Clean Chart", "bar", data);
chart.setConfig(config);
```

### Advanced Configuration

#### Custom Background Colors

```java
ChartConfig config = new ChartConfig();
// Overall chart background
config.setBackgroundColorHex("#FFFFFF");      // White
// Plot area background (data display area)
config.setPlotBackgroundColorHex("#F5F5F5");  // Light gray

ChartData chart = new ChartData("Comparison", "bar", data);
chart.setConfig(config);
```

#### Combined Configuration Example

```java
ChartConfig config = new ChartConfig();
// Dimensions
config.setWidth(600);
config.setHeight(400);

// Background
config.setBackgroundColorHex("#FFFFFF");
config.setPlotBackgroundColorHex("#F8F9FA");

// Legend
config.setLegendPosition("right");

// Grid lines
config.setShowGridLines(true);

// Custom colors
config.setColors(Arrays.asList(
    new Color(52, 152, 219),   // Blue
    new Color(46, 204, 113),   // Green
    new Color(155, 89, 182),   // Purple
    new Color(241, 196, 15)    // Yellow
));

ChartData chart = new ChartData("Comprehensive Report", "bar", data);
chart.setConfig(config);
```

### Configuration Quick Reference

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `width` | Integer | 500 | Chart width (pixels) |
| `height` | Integer | 300 | Chart height (pixels) |
| `backgroundColorHex` | String | `"#FFFFFF"` | Chart background (hex) |
| `plotBackgroundColorHex` | String | `"#FFFFFF"` | Plot area background |
| `transparentBackground` | Boolean | false | Use transparent background |
| `showLegend` | Boolean | true | Show legend |
| `legendPosition` | String | `"bottom"` | Legend position |
| `showGridLines` | Boolean | true | Show grid lines |
| `colors` | List&lt;Color&gt; | Default palette | Custom color list |
| `xAxisLabel` | String | null | X-axis label |
| `yAxisLabel` | String | null | Y-axis label |
| `show3D` | Boolean | false | 3D effect (pie only) |

### Best Practices

#### 1. PDF Reports (Recommended)

```java
// Use default white background, no configuration needed
ChartData chart = new ChartData("Report Chart", "bar", data);
```

**Advantages:**
- Perfect match with PDF white background
- Best print quality
- Smallest file size

#### 2. Transparent Background

```java
ChartConfig config = new ChartConfig();
config.setTransparentBackground(true);
```

**Use cases:**
- Overlay on background images
- Special design requirements

#### 3. Legend Position Guidelines

- **Bottom** (default): Good for wide charts
- **Right**: Good for tall charts or many data items
- **Top**: Emphasize legend importance
- **Left**: Less common
- **Hidden**: Few, obvious data items

### Migration Guide

#### From v1.0.0

**Old code (v1.0.0):**
```java
ChartData chart = new ChartData("Chart", "bar", data);
// Chart background was gray
```

**New code (v1.0.1+):**
```java
ChartData chart = new ChartData("Chart", "bar", data);
// Chart background is automatically white, no changes needed
```

✅ **Backward compatible** - Existing code automatically gets white background improvement!

#### Keep Gray Background (if needed)

If you really need to keep the old gray background:

```java
ChartConfig config = new ChartConfig();
config.setBackgroundColorHex("#CCCCCC");  // Gray
config.setPlotBackgroundColorHex("#CCCCCC");

ChartData chart = new ChartData("Chart", "bar", data);
chart.setConfig(config);
```

### FAQ

#### Q: Why did my chart background automatically turn white?

A: v1.0.1+ defaults to white background for perfect PDF integration. This is an intentional improvement.

#### Q: How do I make the chart background fully transparent?

A: Set `config.setTransparentBackground(true)`

#### Q: Can I set different backgrounds for chart and plot area?

A: Yes! Use `backgroundColorHex` (overall) and `plotBackgroundColorHex` (plot area).

#### Q: Does legend position support custom size or font?

A: Current version supports position and show/hide. Font automatically uses configured Chinese font.

#### Q: Does transparent background work properly in PDF?

A: Yes, transparent background works correctly in PDF.

### Example Code

For complete examples, see:
- `ChartBackgroundAndLegendTest.java` - Background and legend tests
- `ChartTypesAndConfigTest.java` - Chart types and configuration tests
