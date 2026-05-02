# Chart Chinese Font Configuration Guide

## 问题：为什么图表中文无法显示？

### 简答

**图表中的中文确实会被正确使用和显示 - 只要正确配置了字体！**

## 技术原理

### 字体配置流程

```
FontConfig/PdfRenderProperties
    ↓
HtmlReportRenderer.setFontProperties()
    ↓
ChartRenderer.setChartFont()
    ↓
Font.createFont() - 加载字体文件
    ↓
applyChartFont() - 应用到图表的所有文本元素
```

### 字体应用到哪些图表元素？

ChartRenderer 会将配置的中文字体应用到以下所有元素：

#### 1. 柱状图/折线图/面积图（CategoryPlot）
- ✅ **X轴标签**（如：城市名称"北京"、"上海"）
- ✅ **X轴坐标值**（如：月份"一月"、"二月"）
- ✅ **Y轴标签**（如："GDP (亿元)"）
- ✅ **Y轴坐标值**（数字通常不需要中文字体）
- ✅ **图例**（如："销售额"、"利润"）

#### 2. 饼图（PiePlot）
- ✅ **扇形标签**（如："研发部"、"销售部"）
- ✅ **百分比标签**（如果启用）
- ✅ **图例**（如："产品A"、"产品B"）

#### 3. 所有图表类型
- ✅ **图例项**（Legend items）

### 代码实现详解

在 `ChartRenderer.java` 的 `applyChartFont()` 方法中：

```java
// X轴：应用字体到域轴（这里显示中文类别名称）
categoryPlot.getDomainAxis().setTickLabelFont(chartFont.deriveFont(11f));
categoryPlot.getDomainAxis().setLabelFont(chartFont.deriveFont(Font.BOLD, 12f));

// Y轴：应用字体到范围轴
categoryPlot.getRangeAxis().setTickLabelFont(chartFont.deriveFont(11f));
categoryPlot.getRangeAxis().setLabelFont(chartFont.deriveFont(Font.BOLD, 12f));

// 饼图：应用字体到标签
piePlot.setLabelFont(chartFont.deriveFont(11f));

// 图例：应用字体到图例项
chart.getLegend().setItemFont(chartFont.deriveFont(11f));
```

**结论：字体不仅仅设置了样式，而是完整地加载并应用到了所有文本元素！**

## 配置示例

### 方式1：Spring Boot（推荐）

```yaml
# application.yml
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    default-family: HarmonyOS Sans SC, sans-serif
```

```java
@Service
public class PdfService {
    private final ReportService reportService;
    
    // 自动注入，字体配置自动应用到图表
    public PdfService(ReportService reportService) {
        this.reportService = reportService;
    }
}
```

### 方式2：手动配置

```java
ReportService service = new ReportService();

// 配置字体 - 会自动应用到图表
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif");
service.getHtmlRenderer().setFontConfig(fontConfig);

// 现在生成的所有图表都会正确显示中文
Map<String, Double> chartData = new LinkedHashMap<>();
chartData.put("北京", 2154.0);
chartData.put("上海", 2424.0);

ChartData chart = new ChartData("城市GDP", "bar", chartData);
// ... 图表中的"北京"、"上海"会正确显示
```

### 方式3：显式使用CJK字体（可选）

```java
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.setCjkFontPath("classpath:/fonts/NotoSansCJK-Regular.otf"); // 可选
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, sans-serif");
service.getHtmlRenderer().setFontConfig(fontConfig);
```

**注意：** 如果设置了 `cjkFontPath`，图表会优先使用CJK字体。如果未设置，会自动使用 `regularFontPath` 作为后备。

## 验证字体是否工作

### 方法1：查看日志

启用日志后，你会看到：

```
✓ Chart font loaded successfully: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
  Chart font family: HarmonyOS Sans SC
  Chart font name: HarmonyOS Sans SC Regular
Applying custom font to chart: HarmonyOS Sans SC (supports Chinese/CJK characters)
  ✓ Applied font to X-axis labels (e.g., Chinese category names)
  ✓ Applied font to Y-axis labels
  ✓ Applied font to legend (Chinese series names)
```

### 方法2：检查生成的图表

生成PDF后，打开查看：
- ✅ 中文字符应该清晰显示
- ❌ 如果显示为方框（□），说明字体未正确配置

### 方法3：运行测试

```bash
mvn test -Dtest=ChineseChartTest
```

查看生成的PDF：`test-output/chinese_chart_rendering_test.pdf`

## 常见问题

### Q1: 设置了regularFontPath，为什么还是不显示？

**A:** 检查以下几点：

1. **字体文件路径是否正确？**
   ```java
   // 正确：使用classpath前缀
   fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
   
   // 或使用绝对路径
   fontConfig.setRegularFontPath("/absolute/path/to/font.ttf");
   ```

2. **字体文件是否存在？**
   ```bash
   # 检查资源文件
   ls -la src/main/resources/fonts/
   ```

3. **字体是否支持中文？**
   - ✅ HarmonyOS Sans SC - 支持简体中文
   - ✅ Noto Sans CJK - 支持中日韩文字
   - ❌ Arial、Times New Roman - 不支持中文

### Q2: 只设置了字体样式吗？

**A:** 不是！完整的字体加载和应用流程：

1. **字体加载**：`Font.createFont()` 从文件加载完整字体
2. **字体派生**：`baseFont.deriveFont(size)` 创建不同大小的字体实例
3. **字体应用**：设置到JFreeChart的所有文本元素

这不仅仅是CSS样式，而是实际的字体嵌入和渲染！

### Q3: PDF中文正常，但图表中文是方框？

**A:** 这说明PDF字体配置正确，但图表字体未配置。确保：

```java
// 设置FontConfig后，会自动配置ChartRenderer
service.getHtmlRenderer().setFontConfig(fontConfig);

// 内部会调用：
chartRenderer.setChartFont(fontConfig.getRegularFontPath());
```

### Q4: 如何确认字体真的被使用了？

**A:** 添加日志配置：

```xml
<!-- logback.xml -->
<logger name="com.mercury.pdf.render.ChartRenderer" level="INFO"/>
```

或检查System.out输出（如果SLF4J不可用）。

### Q5: 图表包含中文但未配置字体会怎样？

**A:** 系统会显示警告：

```
WARNING: Chart contains Chinese characters but no custom font is configured.
Chinese characters may not display correctly (will show as boxes □).
Use ChartRenderer.setChartFont() or configure fonts via FontConfig/PdfRenderProperties.
```

## 技术细节

### 字体优先级

```
CJK Font Path → Regular Font Path → System Default Font
     (最优)         (推荐)              (不推荐)
```

### 字体后备机制

HtmlReportRenderer.setFontProperties() 中的逻辑：

```java
String chartFontPath = fontProperties.getCjkPath();
if (chartFontPath == null || chartFontPath.isEmpty()) {
    chartFontPath = fontProperties.getRegularPath(); // 自动后备
}
```

### 为什么推荐 HarmonyOS Sans SC？

1. ✅ 完整的简体中文字符支持
2. ✅ 清晰的渲染效果
3. ✅ 开源免费
4. ✅ 文件大小适中（约4-6MB）

## 示例代码

### 完整示例：带中文的图表

```java
import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.model.*;

import java.util.*;

public class ChineseChartExample {
    public static void main(String[] args) throws Exception {
        // 1. 创建服务
        ReportService service = new ReportService();
        
        // 2. 配置中文字体（关键步骤！）
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, sans-serif");
        service.getHtmlRenderer().setFontConfig(fontConfig);
        
        // 3. 创建包含中文的图表数据
        Map<String, Double> data = new LinkedHashMap<>();
        data.put("北京", 2154.0);
        data.put("上海", 2424.0);
        data.put("广州", 1868.0);
        data.put("深圳", 2767.0);
        
        // 4. 配置图表（包含中文标签）
        ChartConfig config = new ChartConfig();
        config.setXAxisLabel("城市");
        config.setYAxisLabel("GDP (亿元)");
        
        ChartData chart = new ChartData("2023年主要城市GDP", "bar", data);
        chart.setConfig(config);
        
        // 5. 生成报告
        ReportData reportData = ReportDataBuilder.create()
            .title("中文图表示例")
            .addSection(new Section("经济数据").addChart(chart))
            .build();
        
        // 6. 生成PDF
        byte[] pdf = service.generatePdf(reportData);
        
        // 7. 保存文件
        Files.write(Paths.get("chinese_chart_example.pdf"), pdf);
        System.out.println("✓ PDF生成成功，所有中文字符正确显示！");
    }
}
```

## 总结

### ✅ 字体真的被使用了！

1. **完整加载**：字体文件被完整加载（不是仅样式）
2. **全面应用**：应用到图表的所有文本元素
3. **正确渲染**：JFreeChart使用配置的字体渲染中文
4. **PDF嵌入**：生成的图表图片包含正确渲染的中文

### 📋 配置检查清单

- [ ] 字体文件存在且路径正确
- [ ] 字体支持中文字符（如HarmonyOS Sans SC）
- [ ] 调用了 `setFontConfig()` 或配置了 YAML
- [ ] 没有在代码中覆盖字体配置
- [ ] 查看日志确认字体加载成功

### 🎯 如果还有问题

1. 运行测试：`mvn test -Dtest=ChineseChartTest`
2. 查看测试输出的PDF
3. 检查日志中的字体加载信息
4. 参考本项目的测试代码：`src/test/java/com/mercury/pdf/render/ChineseChartTest.java`

---

**文档更新日期：** 2026-02-02  
**相关文档：**
- [CHINESE_FONT_OFFICIAL_GUIDE.md](CHINESE_FONT_OFFICIAL_GUIDE.md) - PDF中文字体配置
- [FONT_CONFIGURATION.md](FONT_CONFIGURATION.md) - 字体配置详解
- [CONFIGURATION_FAQ.md](CONFIGURATION_FAQ.md) - 配置常见问题
