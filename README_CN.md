# PDF-Render - PDF报告生成库

一个基于Java的PDF报告生成库，使用HTML/CSS模板方式，结合Flying Saucer和OpenPDF实现可靠、稳定的表格分页。

## 🔴 PDF中文显示为方框（□）？

**如果您的PDF中文字符显示为方框，请立即查看：**

👉 **[官方字体注册指南 CHINESE_FONT_OFFICIAL_GUIDE.md](CHINESE_FONT_OFFICIAL_GUIDE.md)** 👈 **⭐ 新增！遵循 OpenPDF/Flying Saucer 官方文档**

**其他解决方案文档：**
- **[完整解决方案 CHINESE_DISPLAY_COMPLETE_SOLUTION.md](CHINESE_DISPLAY_COMPLETE_SOLUTION.md)**
- **[3步快速开始 CHINESE_QUICKSTART.md](CHINESE_QUICKSTART.md)**

**特定环境指南：**
- **[Windows兼容性 WINDOWS_COMPATIBILITY.md](WINDOWS_COMPATIBILITY.md)** - v1.0.1+自动处理路径兼容性
- **[容器云部署 CONTAINER_DEPLOYMENT.md](CONTAINER_DEPLOYMENT.md)** - Docker/Kubernetes部署指南 🐳
- **[OpenJDK 8 配置 JDK8_CUSTOM_TEMPDIR.md](JDK8_CUSTOM_TEMPDIR.md)** - JDK 8 和自定义临时目录 ☕

**运行诊断工具找出问题：**
```bash
# 验证字体文件和获取正确配置
mvn compile exec:java -Dexec.mainClass="com.mercury.pdf.render.FontFileVerifier"

# 测试字体配置是否正确
mvn compile exec:java -Dexec.mainClass="com.mercury.pdf.render.MinimalFontTest"
```

**常见问题诊断文档：**
- [没有看到字体注册日志](NO_FONT_LOGS_TROUBLESHOOTING.md)
- [有日志但仍显示方框](FONT_REGISTERED_BUT_BOXES.md)  
- [字体文件大小问题](FONT_SIZE_ISSUE.md)

**简单总结：必须配置FontConfig才能显示中文！** 详见快速指南。

---

## ⚠️ 重要：Spring Boot 用户注意

**如果您在使用 Spring Boot，请勿使用 `@PostConstruct` 手动配置！**

本库内置了 Spring Boot 自动配置。只需配置 `application.yml`，然后注入 `ReportService` 即可：

```yaml
# application.yml
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    default-family: HarmonyOS Sans SC, DejaVu Sans, sans-serif
```

```java
@Service
public class PdfService {
    private final ReportService reportService;
    
    // ✅ 直接注入即可 - 不需要 @PostConstruct！
    public PdfService(ReportService reportService) {
        this.reportService = reportService;
    }
    
    public byte[] generatePdf(ReportData data) throws IOException, DocumentException {
        return reportService.generatePdf(data);
    }
}
```

**📖 完整文档请查看 [Spring Boot 使用指南](SPRING_BOOT_USAGE.md)**

---

## 🎨 图表背景灰色？图例无法自定义？

**v1.0.1+ 已修复图表背景和图例自定义问题：**

- ✅ **默认白色背景** - 图表现在与PDF白色背景完美匹配
- ✅ **图例位置自定义** - 支持上/下/左/右位置
- ✅ **透明背景支持** - 满足特殊设计需求
- ✅ **自定义绘图区背景** - 精细化控制

**详见：**[图表自定义指南 - CHART_CUSTOMIZATION_GUIDE.md](CHART_CUSTOMIZATION_GUIDE.md)

---

## 目录

- [概述](#概述)
- [核心特性](#核心特性)
- [快速开始](#快速开始)
- [详细使用示例](#详细使用示例)
- [数据填充顺序说明](#数据填充顺序说明)
- [自定义模板](#自定义模板)
- [模板映射说明](#模板映射说明)
- [Spring Boot集成](#spring-boot集成)
- [常见问题](#常见问题)

## 概述

该项目生成多章节PDF报告，支持：
- 封面页（标题和元数据）
- 动态章节（可包含标题、副标题、段落、表格、图表）
- 灵活的内容组合
- 多种模板样式

## 核心特性

### 🎯 通用模板系统

**关键优势：一个数据模型适用所有模板，添加模板无需编写代码！**

```java
// 创建数据一次
ReportData data = ReportDataBuilder.create()
    .title("季度报告")
    .addSection(section)
    .build();

// 生成不同格式 - 无需代码更改！
ReportService service = new ReportService();
byte[] reportPdf = service.generatePdf(data, "report");      // 标准报告
byte[] invoicePdf = service.generatePdf(data, "invoice");    // 发票
byte[] certPdf = service.generatePdf(data, "certificate");   // 证书
```

### ✨ 主要功能

- ✅ **稳定的表格分页** - 页面断点处不会丢失行
- ✅ **重复的表头** - 使用`<thead>`在新页面自动重复表头
- ✅ **页面断点控制** - CSS `page-break-inside: avoid`防止行跨页分割
- ✅ **易于自定义** - 修改模板和样式无需修改Java代码
- ✅ **专业样式** - 基于CSS的样式，支持@page规则和自定义字体
- ✅ **灵活的章节** - Section模型支持任意内容组合

## 快速开始

### ⚠️ 中文字体配置（重要！）

**如果您的PDF需要显示中文，必须配置 FontConfig，否则中文会显示为方框（□）**

```java
import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.config.FontConfig;

ReportService service = new ReportService();
service.

setUseHtmlPipeline(true);

// ✅ 必须配置 FontConfig 才能显示中文
// 方案1：使用鸿蒙字体（推荐用于中文显示）
FontConfig fontConfig = new FontConfig();
fontConfig.

setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.

setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, sans-serif");

// 方案2：使用思源黑体 Noto Sans（备选方案）
// fontConfig.setRegularFontPath("classpath:/fonts/NotoSansCJKsc-Regular.otf");
// fontConfig.setDefaultFontFamily("Noto Sans CJK SC, DejaVu Sans, sans-serif");

service.

getHtmlRenderer().

setFontConfig(fontConfig);

// 现在可以生成包含中文的PDF了
byte[] pdf = service.generatePdf(reportData);
```

**快速验证：** 运行演示程序查看对比效果
```bash
java -cp "target/classes:..." com.mercury.pdf.render.ChineseFontConfigurationDemo
```

**详细文档：**
- 📖 [中文字体配置完整指南](FONT_CONFIGURATION.md)
- 📖 [如何验证字体配置](HOW_TO_VERIFY_CHINESE_FONTS.md)
- 📖 [技术方案分析](TECHNICAL_ANALYSIS_CHINESE_FONTS.md)

### 环境要求

- Java 8 或更高版本 (JDK 1.8+)
- Maven 3.6+

### 构建项目

```bash
mvn clean install
```

这将生成：
- `pdf-render-1.0.0-SNAPSHOT.jar` - 主库JAR
- `pdf-render-1.0.0-SNAPSHOT-sources.jar` - 源码JAR
- `pdf-render-1.0.0-SNAPSHOT-javadoc.jar` - 文档JAR

### 运行测试

```bash
mvn test
```

测试生成的PDF将保存在`test-output/`目录中供检查。

## 详细使用示例

### 示例1：创建简单报告

```java
import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.model.ReportData;
import com.mercury.pdf.render.model.ReportDataBuilder;
import com.mercury.pdf.render.model.Section;

import java.nio.file.Files;
import java.nio.file.Paths;

public class SimpleReportExample {
    public static void main(String[] args) throws Exception {
        // 使用构建器创建报告数据
        ReportData reportData = ReportDataBuilder.create()
                .title("2024年度财务报告")
                .subtitle("第四季度业绩总结")
                .reportDate("2024-12-31")
                .reportNumber("RPT-2024-Q4-001")

                // 添加执行摘要章节
                .addSection(new Section("执行摘要")
                        .addParagraph("本季度公司业绩表现优异，营收同比增长25%。")
                        .addParagraph("运营效率持续提升，成本控制在合理范围内。"))

                .reportNotice("本报告为内部文件，仅供参考。")
                .metadata("生成时间：2024-12-31 | 生成系统：PDF-Render v1.0")
                .build();

        // 生成PDF
        ReportService service = new ReportService();
        byte[] pdfBytes = service.generatePdf(reportData);

        // 保存到文件
        Files.write(Paths.get("财务报告.pdf"), pdfBytes);

        System.out.println("PDF生成成功！");
    }
}
```

### 示例2：包含表格和图表的完整报告

```java
import java.util.*;

public class CompleteReportExample {
    public static void main(String[] args) throws Exception {
        // 创建财务数据表
        TableData financialTable = new TableData(
            Arrays.asList("季度", "营收", "成本", "利润", "增长率"),
            Arrays.asList(
                Arrays.asList("Q1", "￥120万", "￥80万", "￥40万", "12%"),
                Arrays.asList("Q2", "￥135万", "￥85万", "￥50万", "15%"),
                Arrays.asList("Q3", "￥145万", "￥90万", "￥55万", "18%"),
                Arrays.asList("Q4", "￥160万", "￥95万", "￥65万", "22%")
            )
        );
        
        // 创建图表数据
        Map<String, Double> chartData = new LinkedHashMap<>();
        chartData.put("Q1", 400000.0);
        chartData.put("Q2", 500000.0);
        chartData.put("Q3", 550000.0);
        chartData.put("Q4", 650000.0);
        ChartData profitChart = new ChartData("季度利润趋势", "bar", chartData);
        
        // 构建完整报告
        ReportData reportData = ReportDataBuilder.create()
            .title("年度财务分析报告")
            .subtitle("2024财年总结")
            .reportDate("2024-12-31")
            .reportNumber("FIN-2024-ANNUAL")
            
            // 第1章：财务概览
            .addSection(new Section("财务概览")
                .withSubtitle("全年业绩回顾")
                .addParagraph("2024年公司实现营业收入￥560万元，同比增长18%。")
                .addParagraph("净利润达到￥210万元，利润率稳定在37.5%。")
                .addTable(financialTable)
                .addChart(profitChart))
            
            // 第2章：运营分析
            .addSection(new Section("运营分析")
                .addParagraph("各项运营指标持续向好，客户满意度达92%。")
                .addTable(createOperationalMetrics()))
            
            // 第3章：战略规划
            .addSection(new Section("2025年战略规划")
                .addParagraph("继续深耕现有市场，拓展新业务领域。")
                .addParagraph("预计2025年营收增长目标：30%。"))
            
            .reportNotice("机密文件 - 仅限内部使用")
            .metadata("财务部 | 审批人：张经理")
            .build();
        
        // 生成PDF
        ReportService service = new ReportService();
        byte[] pdfBytes = service.generatePdf(reportData, "flexible");
        Files.write(Paths.get("完整财务报告.pdf"), pdfBytes);
        
        System.out.println("完整报告生成成功！");
    }
    
    private static TableData createOperationalMetrics() {
        return new TableData(
            Arrays.asList("指标", "目标", "实际", "状态"),
            Arrays.asList(
                Arrays.asList("客户满意度", "85%", "92%", "超额完成"),
                Arrays.asList("员工留任率", "90%", "94%", "超额完成"),
                Arrays.asList("市场份额", "25%", "27%", "超额完成")
            )
        );
    }
}
```

### 示例3：使用不同模板生成不同格式

```java
public class MultiTemplateExample {
    public static void main(String[] args) throws Exception {
        // 创建一次数据
        ReportData data = createUniversalData();
        
        ReportService service = new ReportService();
        
        // 1. 生成标准报告
        byte[] standardReport = service.generatePdf(data, "report");
        Files.write(Paths.get("标准报告.pdf"), standardReport);
        
        // 2. 生成灵活报告
        byte[] flexibleReport = service.generatePdf(data, "flexible");
        Files.write(Paths.get("灵活报告.pdf"), flexibleReport);
        
        // 3. 生成发票样式
        byte[] invoice = service.generatePdf(data, "invoice");
        Files.write(Paths.get("发票.pdf"), invoice);
        
        // 4. 生成证书样式
        byte[] certificate = service.generatePdf(data, "certificate");
        Files.write(Paths.get("证书.pdf"), certificate);
        
        // 5. 生成执行摘要
        byte[] summary = service.generatePdf(data, "executive-summary");
        Files.write(Paths.get("执行摘要.pdf"), summary);
        
        System.out.println("同一数据生成了5种不同格式的PDF！");
    }
    
    private static ReportData createUniversalData() {
        return ReportDataBuilder.create()
            .title("2024年度表彰")
            .subtitle("优秀员工奖")
            .reportDate("2024-12-31")
            .reportNumber("张小明")  // 在证书模板中会作为获奖人姓名
            .addSection(new Section("张小明")
                .addParagraph("因在2024年度工作中表现突出，特此表彰。")
                .addParagraph("获得年度最佳员工称号。"))
            .metadata("人力资源部")
            .build();
    }
}
```

## 数据填充顺序说明

### ❓ 问题：标题、段落、表格、图表多个顺序混杂，如何指定哪块数据填充到模版哪块？

### ✅ 答案：数据按添加顺序自动填充

#### 核心原则

**在Section中，内容按以下固定顺序渲染：**

1. **标题** (Section Title)
2. **副标题** (Section Subtitle)  
3. **段落** (Paragraphs) - 按添加顺序
4. **表格** (Tables) - 按添加顺序
5. **表格块** (TableBlocks) - 按添加顺序
6. **图表** (Charts) - 按添加顺序
7. **自定义内容** (Custom Content)

> ✅ 支持 Markdown（大模型输出常见格式）：使用 `withMarkdownContent(...)`，内容会先转换为 HTML，
> 并通过 `section.markdownHtml` 单独渲染（适合标题、列表、强调等格式化文本）。

#### 详细说明

```java
Section section = new Section("第一章：市场分析")
    // 1. 标题会显示在最上面
    .withSubtitle("2024年第四季度")
    // 2. 副标题显示在标题下方
    
    // 3. 段落按添加顺序显示
    .addParagraph("第一段：市场概况...")      // ← 第1段
    .addParagraph("第二段：竞争分析...")      // ← 第2段
    .addParagraph("第三段：趋势预测...")      // ← 第3段
    
    // 4. 表格按添加顺序显示
    .addTable(marketShareTable)              // ← 第1个表格
    .addTable(competitorTable)               // ← 第2个表格
    
    // 5. 图表按添加顺序显示
    .addChart(growthChart)                   // ← 第1个图表
    .addChart(distributionChart)             // ← 第2个图表
    
    // 6. 自定义HTML内容最后显示
    .withCustomContent("<div>补充说明...</div>");
```

#### 实际渲染效果

在PDF中，上述代码会按如下顺序显示：

```
┌─────────────────────────────────────┐
│ 第一章：市场分析                      │  ← 标题
│ 2024年第四季度                       │  ← 副标题
├─────────────────────────────────────┤
│ 第一段：市场概况...                   │  ← 段落1
│                                     │
│ 第二段：竞争分析...                   │  ← 段落2
│                                     │
│ 第三段：趋势预测...                   │  ← 段落3
├─────────────────────────────────────┤
│ [市场份额表格]                        │  ← 表格1
├─────────────────────────────────────┤
│ [竞争对手表格]                        │  ← 表格2
├─────────────────────────────────────┤
│ [增长趋势图表]                        │  ← 图表1
├─────────────────────────────────────┤
│ [分布情况图表]                        │  ← 图表2
├─────────────────────────────────────┤
│ [补充说明HTML内容]                    │  ← 自定义内容
└─────────────────────────────────────┘
```

### 💡 实践技巧

#### 技巧1：使用多个Section控制布局

```java
ReportData report = ReportDataBuilder.create()
    .title("综合分析报告")
    
    // 章节1：只有文字
    .addSection(new Section("引言")
        .addParagraph("本报告分析..."))
    
    // 章节2：先文字后表格
    .addSection(new Section("数据分析")
        .addParagraph("根据以下数据...")
        .addTable(dataTable))
    
    // 章节3：先图表后说明
    .addSection(new Section("趋势预测")
        .addChart(trendChart)
        .addParagraph("如图所示..."))
    
    // 章节4：复杂组合
    .addSection(new Section("综合评估")
        .addParagraph("综合来看...")
        .addTable(summaryTable)
        .addChart(comparisonChart)
        .addParagraph("结论：..."))
    
    .build();
```

#### 技巧2：控制内容顺序

```java
// 如果需要特定顺序：段落 → 图表 → 表格 → 段落
Section section = new Section("灵活布局")
    .addParagraph("开篇说明")         // 1
    .addChart(chart)                 // 2
    .addTable(table)                 // 3  
    .addParagraph("总结说明");        // 4

// 注意：实际渲染会是：段落(1,4) → 表格(3) → 图表(2)
// 因为模板按类型分组渲染

// 如果需要精确控制顺序，使用多个Section：
reportData
    .addSection(new Section("第1部分").addParagraph("开篇"))
    .addSection(new Section("第2部分").addChart(chart))
    .addSection(new Section("第3部分").addTable(table))
    .addSection(new Section("第4部分").addParagraph("总结"));
```

#### 技巧3：使用自定义HTML精确控制

```java
Section section = new Section("精确控制顺序")
    .withCustomContent(
        "<p>第一段文字</p>" +
        "<img src='data:image/png;base64,...' />" +  // 图表
        "<table><tr><td>表格内容</td></tr></table>" +
        "<p>第二段文字</p>"
    );
// 自定义HTML可以完全控制渲染顺序
```

### 📊 完整示例：复杂报告

```java
public class ComplexOrderExample {
    public static void main(String[] args) throws Exception {
        ReportData report = ReportDataBuilder.create()
            .title("2024年度销售分析报告")
            .subtitle("全年业绩回顾与展望")
            .reportDate("2024-12-31")
            
            // 第1章：概述（只有文字）
            .addSection(new Section("一、概述")
                .addParagraph("2024年公司销售业绩稳步增长...")
                .addParagraph("主要增长来源于..."))
            
            // 第2章：数据分析（文字+表格+图表）
            .addSection(new Section("二、销售数据分析")
                .withSubtitle("按季度统计")
                .addParagraph("全年销售数据如下表所示：")
                .addTable(quarterlyData)           // 季度数据表
                .addParagraph("从图表可以看出增长趋势：")
                .addChart(salesTrendChart)         // 趋势图
                .addParagraph("分析结论：持续增长态势明显。"))
            
            // 第3章：区域分析（多个表格和图表）
            .addSection(new Section("三、区域市场分析")
                .addParagraph("各区域表现如下：")
                .addTable(northRegionTable)        // 北方市场
                .addTable(southRegionTable)        // 南方市场
                .addChart(regionComparisonChart)   // 对比图
                .addChart(marketShareChart))       // 份额图
            
            // 第4章：总结（纯文字）
            .addSection(new Section("四、总结与展望")
                .addParagraph("总结：2024年目标全部达成。")
                .addParagraph("展望：2025年继续保持增长。"))
            
            .reportNotice("本报告为商业机密")
            .metadata("销售部 | 日期：2024-12-31")
            .build();
        
        // 生成PDF
        ReportService service = new ReportService();
        byte[] pdf = service.generatePdf(report, "flexible");
        Files.write(Paths.get("销售分析报告.pdf"), pdf);
        
        System.out.println("复杂报告生成成功！");
        System.out.println("内容按章节顺序，章节内按固定规则（标题→段落→表格→图表）排列");
    }
}
```

## 自定义模板

### 添加新模板（无需编写代码！）

1. **创建HTML模板文件**
   ```bash
   # 复制现有模板
   cp src/main/resources/templates/flexible.html \
      src/main/resources/templates/my-template.html
   ```

2. **编辑模板**
   - 修改HTML结构和CSS样式
   - 所有模板都可以访问相同的数据变量
   - 无需修改任何Java代码

3. **使用新模板**
   ```java
   byte[] pdf = service.generatePdf(reportData, "my-template");
   ```

### 可用的数据变量

所有模板都可以访问以下变量：

| 变量 | 类型 | 说明 |
|-----|------|------|
| `${title}` | String | 报告标题 |
| `${subtitle}` | String | 副标题 |
| `${reportDate}` | String | 报告日期 |
| `${reportNumber}` | String | 报告编号 |
| `${sections}` | List&lt;Section&gt; | 章节列表 |
| `${sections[0].title}` | String | 第一个章节的标题 |
| `${sections[0].paragraphs}` | List&lt;String&gt; | 第一个章节的段落 |
| `${sections[0].tables}` | List&lt;TableData&gt; | 第一个章节的表格 |
| `${sections[0].charts}` | List&lt;ChartData&gt; | 第一个章节的图表 |

详细的模板自定义指南请参考：
- [ADDING_TEMPLATES.md](ADDING_TEMPLATES.md) - 通用模板添加指南
- [MATCHER_REPORT_FINAL_MAPPING_CN.md](MATCHER_REPORT_FINAL_MAPPING_CN.md) - matcher-report-final 模板与 ReportData 映射详解
- [SECTION_TYPE_DECOUPLING_CN.md](SECTION_TYPE_DECOUPLING_CN.md) - **⭐ 新功能：通用 sectionType 解耦数据与模板**

## 模板映射说明

### matcher-report-final 模板

`matcher-report-final` 是一个需求预审报告模板，专门用于生成匹配度分析报告。

#### ⭐ 推荐方式：使用 sectionType（数据与模板解耦）

**新增功能：** 现在支持使用 `sectionType` 来标识 section，不再依赖标题前缀！

```java
// ✅ 推荐：使用 sectionType，标题可以是任意文本
// sectionType 的值完全由您和您的模板决定
Section section = new Section("精确匹配通过", "chapter1");
section.addTable(table);
builder.addSection(section);

// 优势：
// - 标题完全自定义，无需数字前缀
// - 数据与模板完全解耦
// - Section 可以按任意顺序添加
// - sectionType 值可以是任意字符串
```

**对于 matcher-report-final 模板的 sectionType 映射：**

| sectionType 值 | 渲染位置 | 示例标题（可自定义） |
|---------------|---------|------------------|
| `"chapter1"` | 第一章：匹配结果详细列表 | "精确匹配通过", "语义匹配通过" |
| `"chapter2"` | 第二章：详细分析内容 | "模块匹配表现", "问题根源分析" |
| `"chapter3"` | 第三章：预审结果总结 | "匹配结果汇总", "核心结论" |
| `"chapter4"` | 第四章：报告说明 | "报告说明", "免责声明" |
| `"appendix"` 或 `"other"` | 第三章末尾 | "附录信息", "补充说明" |

**⚠️ 注意：** 上述 sectionType 值（"chapter1", "chapter2" 等）仅适用于 matcher-report-final 模板。
其他模板可以定义自己的 sectionType 值。

📖 **详细文档：** [通用 SectionType 解耦指南](SECTION_TYPE_DECOUPLING_CN.md)

#### 传统方式：标题前缀匹配（向后兼容）

该模板仍然支持传统的 **Section 标题前缀匹配机制**（向后兼容）。

| Section 标题前缀 | 渲染位置 | 示例 |
|---------------|---------|------|
| `1.` 开头 | 第一章：匹配结果详细列表 | "1.1 精确匹配通过", "1.2 语义匹配通过" |
| `2.` 开头 | 第二章：详细分析内容 | "2.1 分模块匹配表现", "2.2 匹配失败问题根源" |
| `3.` 开头 | 第三章：预审结果总结 | "3.1 核心结论", "3.2 详细统计" |
| `4.` 开头 | 第四章：报告说明 | "4.1 报告说明" |
| 其他 | 第三章末尾 | 任意其他标题 |

#### 快速示例

**使用新的 sectionType 特性（推荐）：**

```java
ReportDataBuilder builder = ReportDataBuilder.create()
    .title("需求预审报告")
    .reportDate("2024-12-31");

// 第一章：使用 sectionType="chapter1"（标题可自定义）
builder.addSection(new Section("精确匹配通过", "chapter1")
    .addTable(matchTable));

builder.addSection(new Section("语义匹配通过", "chapter1")
    .addTable(semanticMatchTable));

// 第二章：使用 sectionType="chapter2"
builder.addSection(new Section("分模块匹配表现", "chapter2")
    .addParagraph("按业务模块拆分匹配结果..."));

builder.addSection(new Section("匹配失败问题根源", "chapter2")
    .addParagraph("• 需求与文档不同步")
    .addParagraph("• 功能定义不细致"));

// 第三章：使用 sectionType="chapter3"
builder.addSection(new Section("匹配汇总", "chapter3")
    .addTable(createSummaryTable()));

builder.addSection(new Section("核心结论", "chapter3")
    .addParagraph("本次预审整体匹配率80.0%..."));

// 第四章：使用 sectionType="chapter4"
builder.addSection(new Section("报告说明", "chapter4")
    .addParagraph("报告说明：使用 sectionType 特性"));

// 生成 PDF
byte[] pdf = service.generatePdf(builder.build(), "matcher-report-final");
```

**传统方式（仍然支持）：**

```java
ReportDataBuilder builder = ReportDataBuilder.create()
    .title("需求预审报告")
    .reportDate("2024-12-31");

// 第一章：标题以 "1." 开头
builder.addSection(new Section("1.1 精确匹配通过（匹配度≥0.90）")
    .addTable(matchTable));

builder.addSection(new Section("1.2 语义匹配通过（0.70≤匹配度＜0.90）")
    .addTable(semanticMatchTable));

// 第二章：标题以 "2." 开头
builder.addSection(new Section("2.1 分模块匹配表现")
    .addParagraph("按业务模块拆分匹配结果..."));

builder.addSection(new Section("2.2 匹配失败问题根源")
    .addParagraph("• 需求与文档不同步：...")
    .addParagraph("• 功能定义不细致：..."));

// 第三章：使用 3.x Sections
Section section3Summary = new Section("3. 汇总");
section3Summary.addTable(createSummaryTable());
builder.addSection(section3Summary);

builder.addSection(new Section("3.1 核心结论")
    .addParagraph("本次预审整体匹配率80.0%..."));

// 第四章：标题以 "4." 开头
builder.addSection(new Section("4.1 报告说明")
    .addParagraph("报告说明：文档部分内容由 BA助手 生成"));

// 生成 PDF
byte[] pdf = service.generatePdf(builder.build(), "matcher-report-final");
```

#### 重要说明

1. **⭐ 推荐使用 sectionType** - 新代码建议使用 `sectionType` 来标识 section，标题可以是任意文本
2. **Section 标题前缀仍然支持** - 传统的标题前缀（1., 2., 3., 4.）方式仍然有效（向后兼容）
3. **添加顺序不影响渲染位置** - 可以按任意顺序添加 Section，模板会根据 sectionType 或标题前缀自动分组
4. **每个 Section 可包含多种内容** - 标题、副标题、段落、表格、图表、TableBlock、自定义 HTML
5. **完全向后兼容** - 现有代码无需修改即可继续工作

#### 常见问题：如何选择使用方式？

**问题：** 我应该使用 sectionType 还是标题前缀？

**答案：**
- **新项目或新代码（推荐）：** 使用 `sectionType`，标题可以完全自定义
  ```java
  new Section("精确匹配通过", "chapter1")  // ✅ 推荐
  ```
- **现有项目：** 继续使用标题前缀，完全向后兼容
  ```java
  new Section("1.1 精确匹配通过")  // ✅ 仍然有效
  ```
- **混合使用：** 可以在同一报告中混合使用两种方式
  ```java
  builder.addSection(new Section("精确匹配", "chapter1"))  // sectionType
         .addSection(new Section("1.2 语义匹配"));         // 标题前缀
  ```

#### 详细文档

完整的映射规则、代码示例和最佳实践，请参考：
**[matcher-report-final 模板与 ReportData 映射详解](MATCHER_REPORT_FINAL_MAPPING_CN.md)**

## Spring Boot集成

**📘 完整集成指南和示例:** 
- **[Spring Boot 集成指南（中文）](SPRING_BOOT_INTEGRATION_GUIDE.md)** - 详细的配置说明、完整代码示例、资源路径配置、最佳实践
- **[Spring Boot Examples](SPRING_BOOT_EXAMPLES.md)** - 快速开始示例、项目结构、Docker支持

本节提供快速概览，完整内容请参考上述指南文档。

### Maven依赖

在Spring Boot项目的`pom.xml`中添加：

```xml
<dependency>
    <groupId>com.mercury</groupId>
    <artifactId>pdf-render</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 配置文件

在`application.yml`中配置：

```yaml
pdf-render:
  template:
    location: classpath:/templates/
    default-name: flexible
    cache-enabled: true
  output:
    directory: /var/pdfs
    save-to-directory: false
  fonts:
    cjk-path: classpath:/fonts/NotoSansCJK-Regular.otf
```

### Spring Service示例

```java
@Service
public class PdfReportService {
    
    private final ReportService reportService = new ReportService();
    
    public byte[] generateQuarterlyReport(QuarterlyData data) throws IOException {
        ReportData reportData = ReportDataBuilder.create()
            .title(data.getTitle())
            .reportDate(data.getDate())
            .addSection(new Section("业绩概览")
                .addParagraph(data.getSummary())
                .addTable(data.getMetricsTable()))
            .build();
        
        return reportService.generatePdf(reportData, "flexible");
    }
}
```

### REST Controller示例

```java
@RestController
@RequestMapping("/api/reports")
public class ReportController {
    
    @Autowired
    private PdfReportService pdfService;
    
    @PostMapping("/generate")
    public ResponseEntity<byte[]> generateReport(@RequestBody ReportRequest request) 
            throws IOException {
        byte[] pdf = pdfService.generateQuarterlyReport(request.getData());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "report.pdf");
        
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
```

## 常见问题

### Q1: 如何控制段落、表格、图表的显示顺序？

**A:** 在模板中，内容按固定顺序渲染：标题 → 副标题 → 段落 → 表格 → 图表 → 自定义内容。如需更灵活的顺序控制，可以：
1. 使用多个Section分别控制
2. 使用`customContent`添加自定义HTML

### Q2: 如何让表格和图表在PDF中紧邻显示？

**A:** 将它们放在同一个Section中：
```java
.addSection(new Section("数据展示")
    .addTable(table)
    .addChart(chart))
```

### Q3: 如何在段落之间插入图表？

**A:** 创建多个Section：
```java
.addSection(new Section("部分1").addParagraph("第一段"))
.addSection(new Section("图表").addChart(chart))
.addSection(new Section("部分2").addParagraph("第二段"))
```

### Q4: 支持哪些图表类型？

**A:** 支持以下图表类型：
- `bar` - 柱状图
- `pie` - 饼图（支持3D效果）
- `line` - 折线图
- `area` - 面积图
- `stackedbar` - 堆叠柱状图

**图表自定义配置示例：**

```java
import com.mercury.pdf.render.model.ChartConfig;

import java.awt.Color;

// 创建图表配置
ChartConfig config = new ChartConfig();
config.

        setWidth(800);                     // 宽度
config.

        setHeight(400);                    // 高度
config.

        setColors(Arrays.asList(
                new Color(52, 152,219),              // 蓝色
    new

        Color(46,204,113)               // 绿色
));
        config.

        setXAxisLabel("季度");              // X轴标签
config.

        setYAxisLabel("销售额（万元）");    // Y轴标签
config.

        setShowGridLines(true);            // 显示网格线
config.

        setShow3D(false);                  // 3D效果（饼图）

        // 应用配置到图表
        ChartData chart = new ChartData("季度销售", "bar", data);
chart.

        setConfig(config);
```

**配置选项说明：**

| 选项 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `width` | Integer | 500 | 图表宽度（像素） |
| `height` | Integer | 300 | 图表高度（像素） |
| `colors` | List<Color> | null | 自定义颜色列表 |
| `showLegend` | Boolean | true | 显示/隐藏图例 |
| `show3D` | Boolean | false | 3D效果（饼图） |
| `backgroundColorHex` | String | null | 背景色（十六进制） |
| `showGridLines` | Boolean | true | 显示/隐藏网格线 |
| `xAxisLabel` | String | "Category" | X轴标签 |
| `yAxisLabel` | String | "Value" | Y轴标签 |

**更多示例：**

```java
// 3D饼图
ChartConfig pieConfig = new ChartConfig();
pieConfig.setShow3D(true);
pieConfig.setWidth(500);
pieConfig.setHeight(400);
ChartData pieChart = new ChartData("费用分布", "pie", expenseData);
pieChart.setConfig(pieConfig);

// 折线图（自定义颜色）
ChartConfig lineConfig = new ChartConfig();
lineConfig.setColors(Arrays.asList(new Color(231, 76, 60)));
lineConfig.setShowGridLines(true);
ChartData lineChart = new ChartData("增长趋势", "line", growthData);
lineChart.setConfig(lineConfig);

// 面积图（带背景色）
ChartConfig areaConfig = new ChartConfig();
areaConfig.setBackgroundColorHex("#f8f9fa");
areaConfig.setColors(Arrays.asList(new Color(46, 204, 113)));
ChartData areaChart = new ChartData("月度销售", "area", salesData);
areaChart.setConfig(areaConfig);
```

### Q5: 如何添加自定义字体？中文显示为方框怎么办？

**A:** 本项目支持通过 `FontConfig` 配置中文字体。**详细指南请查看 [FONT_CONFIGURATION.md](FONT_CONFIGURATION.md)**

**编程方式：**

```java
import com.mercury.pdf.render.config.FontConfig;

FontConfig fontConfig = new FontConfig();
fontConfig.

setRegularFontPath("classpath:/fonts/HarmonyOS_SansSC_Regular.ttf");
fontConfig.

setBoldFontPath("classpath:/fonts/HarmonyOS_SansSC_Bold.ttf");
service.

getHtmlRenderer().

setFontConfig(fontConfig);
```

**Spring Boot配置方式：**
```yaml
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_SansSC_Regular.ttf
    bold-path: classpath:/fonts/HarmonyOS_SansSC_Bold.ttf
    default-family: HarmonyOS Sans SC, DejaVu Sans, sans-serif
```

**推荐中文字体：**
- 鸿蒙字体 (HarmonyOS Sans SC) - 免费商用
- 思源黑体 (Noto Sans CJK SC) - 开源免费
- 文泉驿字体 - 开源免费

**手动模板配置方式（不推荐）：**
1. 将字体文件放在`src/main/resources/fonts/`
2. 在模板的CSS中添加：
```css
@font-face {
    font-family: 'MyFont';
    src: url('classpath:/fonts/myfont.ttf');
}
body {
    font-family: 'MyFont', sans-serif;
}
```

### Q6: 生成的PDF可以合并吗？

**A:** 可以使用PDFBox或iText等库合并多个PDF：
```java
PDFMergerUtility merger = new PDFMergerUtility();
merger.addSource(new ByteArrayInputStream(pdf1));
merger.addSource(new ByteArrayInputStream(pdf2));
merger.setDestinationStream(output);
merger.mergeDocuments(null);
```

### Q7: 如何实现两栏布局（如签字栏和日期栏在同一行）？

**A:** 使用`customContent`添加自定义HTML和CSS实现两栏布局。以下是完整示例：

#### 方法1：使用表格布局（推荐，最稳定）

```java
Section signatureSection = new Section("签署信息")
    .withCustomContent(
        "<table style='width: 100%; border: none; margin-top: 30px;'>" +
        "  <tr>" +
        "    <td style='width: 50%; text-align: left; border: none; padding: 20px;'>" +
        "      <div style='margin-bottom: 10px;'>签字：</div>" +
        "      <div style='border-top: 1px solid #000; width: 200px; margin-top: 40px;'></div>" +
        "    </td>" +
        "    <td style='width: 50%; text-align: left; border: none; padding: 20px;'>" +
        "      <div style='margin-bottom: 10px;'>日期：</div>" +
        "      <div style='border-top: 1px solid #000; width: 200px; margin-top: 40px;'></div>" +
        "    </td>" +
        "  </tr>" +
        "</table>"
    );
```

#### 方法2：使用div和浮动布局

```java
Section signatureSection = new Section("签署信息")
    .withCustomContent(
        "<div style='width: 100%; overflow: hidden; margin-top: 30px;'>" +
        "  <div style='float: left; width: 45%; padding: 20px;'>" +
        "    <p style='margin-bottom: 10px;'>签字：</p>" +
        "    <div style='border-top: 2px solid #333; width: 200px; margin-top: 40px;'></div>" +
        "    <p style='margin-top: 5px; font-size: 9pt; color: #666;'>签字人</p>" +
        "  </div>" +
        "  <div style='float: right; width: 45%; padding: 20px;'>" +
        "    <p style='margin-bottom: 10px;'>日期：</p>" +
        "    <div style='border-top: 2px solid #333; width: 200px; margin-top: 40px;'></div>" +
        "    <p style='margin-top: 5px; font-size: 9pt; color: #666;'>_____年___月___日</p>" +
        "  </div>" +
        "</div>"
    );
```

#### 方法3：使用CSS Grid布局（更灵活）

```java
Section signatureSection = new Section("审批栏")
    .withCustomContent(
        "<div style='display: table; width: 100%; margin-top: 30px;'>" +
        "  <div style='display: table-row;'>" +
        "    <div style='display: table-cell; width: 33%; padding: 15px; border: 1px solid #ddd;'>" +
        "      <div style='font-weight: bold; margin-bottom: 5px;'>制表人：</div>" +
        "      <div style='height: 50px;'></div>" +
        "      <div style='font-size: 9pt; color: #666;'>日期：_______</div>" +
        "    </div>" +
        "    <div style='display: table-cell; width: 33%; padding: 15px; border: 1px solid #ddd;'>" +
        "      <div style='font-weight: bold; margin-bottom: 5px;'>审核人：</div>" +
        "      <div style='height: 50px;'></div>" +
        "      <div style='font-size: 9pt; color: #666;'>日期：_______</div>" +
        "    </div>" +
        "    <div style='display: table-cell; width: 33%; padding: 15px; border: 1px solid #ddd;'>" +
        "      <div style='font-weight: bold; margin-bottom: 5px;'>批准人：</div>" +
        "      <div style='height: 50px;'></div>" +
        "      <div style='font-size: 9pt; color: #666;'>日期：_______</div>" +
        "    </div>" +
        "  </div>" +
        "</div>"
    );
```

#### 完整示例代码

```java
public class SignatureLayoutExample {
    public static void main(String[] args) throws Exception {
        ReportData report = ReportDataBuilder.create()
            .title("项目验收报告")
            .subtitle("2024年度重点项目")
            .reportDate("2024-12-31")
            
            // 报告正文
            .addSection(new Section("项目概况")
                .addParagraph("本项目已按计划完成所有任务...")
                .addTable(createProjectTable()))
            
            .addSection(new Section("验收结论")
                .addParagraph("经验收小组审查，该项目符合验收标准。"))
            
            // 底部签字栏 - 两栏布局
            .addSection(new Section("")  // 无标题
                .withCustomContent(
                    "<div style='margin-top: 50px; page-break-inside: avoid;'>" +
                    "  <table style='width: 100%; border: none;'>" +
                    "    <tr>" +
                    "      <td style='width: 50%; text-align: center; border: none; padding: 20px;'>" +
                    "        <div style='margin-bottom: 60px;'>项目负责人签字：</div>" +
                    "        <div style='border-top: 2px solid #000; width: 200px; margin: 0 auto;'></div>" +
                    "      </td>" +
                    "      <td style='width: 50%; text-align: center; border: none; padding: 20px;'>" +
                    "        <div style='margin-bottom: 60px;'>日期：</div>" +
                    "        <div style='border-top: 2px solid #000; width: 200px; margin: 0 auto;'></div>" +
                    "      </td>" +
                    "    </tr>" +
                    "  </table>" +
                    "</div>"
                ))
            
            .reportNotice("本报告一式三份")
            .build();
        
        // 生成PDF
        ReportService service = new ReportService();
        byte[] pdf = service.generatePdf(report, "flexible");
        Files.write(Paths.get("验收报告.pdf"), pdf);
        
        System.out.println("带签字栏的报告生成成功！");
    }
    
    private static TableData createProjectTable() {
        return new TableData(
            Arrays.asList("项目名称", "完成情况", "验收结果"),
            Arrays.asList(
                Arrays.asList("系统开发", "100%", "通过"),
                Arrays.asList("文档编写", "100%", "通过")
            )
        );
    }
}
```

#### 样式建议

**推荐做法：**
1. ✅ 使用`<table>`布局最稳定，PDF渲染兼容性最好
2. ✅ 设置`page-break-inside: avoid`防止签字栏跨页分割
3. ✅ 使用内联样式（inline style）确保样式生效
4. ✅ 预留足够的签字空间（60-80px高度）

**避免：**
- ❌ 不要使用flexbox（Flying Saucer不完全支持）
- ❌ 避免使用CSS Grid高级特性
- ❌ 不要使用绝对定位（可能导致内容重叠）

## 性能优化

- **模板缓存**：生产环境启用模板缓存（默认已启用）
- **图表优化**：图表会转换为PNG格式嵌入，控制图表数量和尺寸
- **批量生成**：使用线程池并行生成多个PDF

## 项目结构

```
pdf-render/
├── src/main/java/com/finos/matcher/report/
│   ├── model/                 # 数据模型
│   │   ├── ReportData.java    # 报告数据
│   │   ├── Section.java       # 章节
│   │   ├── TableData.java     # 表格
│   │   ├── ChartData.java     # 图表
│   │   └── ReportDataBuilder.java  # 构建器
│   ├── ReportService.java     # 主服务
│   ├── HtmlReportRenderer.java # 渲染引擎
│   └── util/
│       └── ReportDataValidator.java  # 验证器
├── src/main/resources/
│   └── templates/             # HTML模板
│       ├── report.html        # 标准报告
│       ├── flexible.html      # 灵活报告
│       ├── invoice.html       # 发票
│       ├── certificate.html   # 证书
│       └── executive-summary.html  # 摘要
└── src/test/java/            # 测试用例
```

## 相关文档

- **[SPRING_BOOT_JAR_INTEGRATION_STEPS.md](SPRING_BOOT_JAR_INTEGRATION_STEPS.md)** - **Spring Boot JAR集成完整步骤指南** ⭐⭐⭐
- [JAR_INTEGRATION.md](JAR_INTEGRATION.md) - JAR集成使用指南（含字体配置示例）
- [SPRING_BOOT_INTEGRATION_GUIDE.md](SPRING_BOOT_INTEGRATION_GUIDE.md) - Spring Boot集成指南
- [CONFIGURATION_FAQ.md](CONFIGURATION_FAQ.md) - **配置常见问题（YAML vs 代码配置）** ⭐
- [FONT_CONFIGURATION.md](FONT_CONFIGURATION.md) - 字体配置详解
- [TEMPLATE_GUIDE.md](TEMPLATE_GUIDE.md) - 模板自定义详细指南
- [TROUBLESHOOTING.md](TROUBLESHOOTING.md) - 故障排查指南
- [ADDING_TEMPLATES.md](ADDING_TEMPLATES.md) - 模板添加指南
- [IMPLEMENTATION_COMPLETE.md](IMPLEMENTATION_COMPLETE.md) - 实现总结

## 许可证

本项目采用开源许可证。

## 贡献

欢迎贡献！请确保：
1. 所有测试通过 (`mvn test`)
2. 代码符合现有风格
3. 新功能包含测试
4. 更新相关文档

---

**更多示例和详细说明，请查看项目中的Demo文件：**
- `FlexibleReportDemo.java` - 灵活章节示例
- `UniversalTemplateDemo.java` - 通用模板示例
