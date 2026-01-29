# matcher-report-final 模板与 ReportData 映射说明文档

## 概述

本文档详细说明 `matcher-report-final.html` 模板如何与 `ReportData` 中的 `sections` 进行对应和数据映射。

`matcher-report-final` 是一个需求预审报告模板，专门用于生成匹配度分析报告。该模板通过 **Section 标题前缀匹配机制** 来动态组织和渲染不同部分的内容。

## 核心映射机制

### 基本原理

`matcher-report-final.html` 模板使用 **Section 标题前缀** 来判断每个 Section 应该渲染到报告的哪个部分。模板通过 Thymeleaf 的 `#strings.startsWith()` 函数检查 Section 的 title，从而决定将其渲染在哪个章节下。

### 映射规则表

| Section 标题前缀 | 渲染位置 | 说明 |
|---------------|---------|------|
| `1.` 开头 | 第一章：匹配结果详细列表 | 如 "1.1", "1.2", "1.3", "1.4" |
| `2.` 开头 | 第二章：详细分析内容 | 如 "2.1", "2.2", "2.3" |
| `3.` 开头 | 第三章：预审结果总结 | 如 "3.1", "3.2" |
| `4.` 开头 | 第四章：报告说明 | 如 "4.1" |
| **其他前缀/无序号** | **第三章末尾（Other sections）** | **不以 1-4 开头的标题** |

⚠️ **重要说明：如果标题没有序号（或序号不是 1-4 开头），该 Section 会被渲染在第三章的末尾。**

## 模板结构详解

### 第一章：匹配结果详细列表（Section title 以 "1." 开头）

**模板代码位置：** 第 258-339 行

```html
<!-- 一、匹配结果详细列表（按匹配度降序排列） -->
<h2>一、匹配结果详细列表（按匹配度降序排列）</h2>

<div th:each="sec : ${sections}"
     th:if="${sec.title != null and #strings.startsWith(sec.title,'1.')}"
     class="section-card">
  <h3 th:text="${sec.title}">1.1 ...</h3>
  <p th:each="p : ${sec.paragraphs}" th:text="${p}">段落</p>
  
  <!-- 渲染表格 -->
  <div th:each="tbl : ${sec.tables}">
    <table th:if="${tbl != null}">
      <!-- 表格内容 -->
    </table>
  </div>
  
  <!-- 渲染图表 -->
  <div th:each="c : ${sec.charts}" class="chart">
    <!-- 图表内容 -->
  </div>
</div>
```

**对应的 Java 代码示例：**

```java
// 创建第一章的 Section（标题必须以 "1." 开头）
Section section11 = new Section("1.1 精确匹配通过（匹配度≥0.90）");
section11.addTable(createExactMatchTable());

Section section12 = new Section("1.2 语义匹配通过（0.70≤匹配度＜0.90）");
section12.addTable(createSemanticMatchTable());

Section section13 = new Section("1.3 疑似匹配（需人工复核，0.50≤匹配度＜0.70）");
section13.addTable(createSuspectedMatchTable());

Section section14 = new Section("1.4 匹配失败（匹配度＜0.50）");
section14.addTable(createFailedMatchTable());

// 添加到 ReportData
builder.addSection(section11)
       .addSection(section12)
       .addSection(section13)
       .addSection(section14);
```

### 第二章：详细分析内容（Section title 以 "2." 开头）

**模板代码位置：** 第 368-432 行

```html
<!-- 二、详细分析内容 -->
<h2>二、详细分析内容</h2>

<div th:each="sec : ${sections}"
     th:if="${sec.title != null and #strings.startsWith(sec.title,'2.')}"
     class="section-card">
  <h3 th:text="${sec.title}">2.1 ...</h3>
  <p th:each="p : ${sec.paragraphs}" th:text="${p}">分析段落</p>
  
  <!-- 渲染表格、图表等 -->
</div>
```

**对应的 Java 代码示例：**

```java
// 创建第二章的 Section（标题必须以 "2." 开头）
Section section21 = new Section("2.1 分模块匹配表现");
section21.addParagraph("按业务模块拆分匹配结果...");

Section section22 = new Section("2.2 匹配失败问题根源");
section22.addParagraph("• 需求与文档不同步：...");
section22.addParagraph("• 功能定义不细致：...");

Section section23 = new Section("2.3 文档规范性问题");
section23.addParagraph("预审中发现Excel需求清单存在3条重复条目...");

// 添加到 ReportData
builder.addSection(section21)
       .addSection(section22)
       .addSection(section23);
```

### 第三章：预审结果总结（Section title 以 "3." 开头）

**模板代码位置：** 第 440-538 行

```html
<!-- 三、预审结果总结 -->
<h2>三、预审结果总结</h2>

<div class="section-card">
  <!-- 汇总表格（使用 summaryTable 变量） -->
  <table class="summary-table" th:if="${summaryTable != null}">
    <!-- 汇总表格内容 -->
  </table>

  <!-- 3.x sections -->
  <div th:each="sec : ${sections}"
       th:if="${sec.title != null and #strings.startsWith(sec.title,'3.')}"
       style="margin-top: 10px;">
    <h3 th:text="${sec.title}">3.x ...</h3>
    <p th:each="p : ${sec.paragraphs}" th:text="${p}">总结段落</p>
  </div>
</div>
```

**重要提示：** 第三章有两种数据来源：
1. **summaryTable** - 直接在 ReportData 中设置的汇总表格（不在 Section 中）
2. **3.x Sections** - 标题以 "3." 开头的 Section，用于补充说明

**对应的 Java 代码示例：**

```java
// 方式1：使用独立的 summaryTable（推荐用于主汇总表）
TableData summaryTable = createSummaryTable();
builder.summaryTable(summaryTable);  // 直接设置到 ReportData

// 方式2：使用 3.x Section（用于核心结论、补充说明等）
Section section3Conclusion = new Section("3.1 核心结论");
section3Conclusion.addParagraph("本次预审针对《智能客户管理系统V2.0需求条目清单.xlsx》...");
section3Conclusion.addParagraph("整体匹配率80.0%，高优先级需求全部匹配...");
builder.addSection(section3Conclusion);

// 也可以在 Section 中添加表格
Section section3Details = new Section("3.2 详细统计");
section3Details.addTable(someDetailTable);
builder.addSection(section3Details);
```

### 第四章：报告说明（Section title 以 "4." 开头）

**模板代码位置：** 第 636-646 行

```html
<!-- 四、报告说明 -->
<h2>四、报告说明</h2>

<div th:each="sec : ${sections}"
     th:if="${sec.title != null and #strings.startsWith(sec.title,'4.')}"
     class="section-card">
  <p th:each="p : ${sec.paragraphs}" th:text="${p}" class="list-item">附件项</p>
</div>
```

**对应的 Java 代码示例：**

```java
// 创建第四章的 Section（标题必须以 "4." 开头）
Section section4 = new Section("4.1 报告说明");
section4.addParagraph("报告说明：文档部分内容由 业技融合共创平台- BA助手 生成");
section4.addParagraph("报告编号：AI-PRE-2024-001");
section4.addParagraph("报告日期：2024年12月31日");

builder.addSection(section4);
```

### 其他 Section（不以 1-4 开头）

**模板代码位置：** 第 541-620 行

模板会在第三章末尾渲染所有不以 "1.", "2.", "3.", "4." 开头的 Section：

```html
<div th:each="sec : ${sections}"
     th:if="${sec.title != null and 
             !#strings.startsWith(sec.title,'1.') and 
             !#strings.startsWith(sec.title,'2.') and 
             !#strings.startsWith(sec.title,'3.') and 
             !#strings.startsWith(sec.title,'4.')}"
     style="margin-top: 10px;">
  <h3 th:text="${sec.title}">Other Section</h3>
  <!-- 渲染内容 -->
</div>
```

**对应的 Java 代码示例：**

```java
// 可以创建任意标题的 Section，它们会被渲染在第三章末尾
Section customSection = new Section("附加信息");
customSection.addParagraph("这是一些额外的说明...");

Section notesSection = new Section("备注");
notesSection.addParagraph("特别注意事项...");

builder.addSection(customSection)
       .addSection(notesSection);
```

## 没有序号的标题如何处理？

### 当前行为

如果 Section 的标题**没有以 "1.", "2.", "3.", "4." 开头**，模板会将其渲染在**第三章的末尾**（在汇总表和 3.x Sections 之后）。

**示例：**

```java
// 这些 Section 都会被渲染在第三章末尾
Section section1 = new Section("附加信息");  // 没有序号
Section section2 = new Section("备注");      // 没有序号
Section section3 = new Section("A. 附录");   // 序号不是 1-4
Section section4 = new Section("说明");      // 没有序号
Section section5 = new Section("5.1 其他"); // 序号是 5，不是 1-4

builder.addSection(section1)
       .addSection(section2)
       .addSection(section3)
       .addSection(section4)
       .addSection(section5);
```

**渲染结果：** 这些 Section 会按添加顺序出现在第三章的末尾。

### 解决方案

根据您的需求，有以下几种解决方案：

#### 方案 1：添加序号前缀（推荐）

最简单的方法是为标题添加相应的序号前缀，让模板自动将其归类到正确的章节。

```java
// ✅ 推荐：添加序号前缀
Section section = new Section("1.5 其他匹配项");  // 会渲染在第一章
Section section = new Section("2.4 其他分析");    // 会渲染在第二章
Section section = new Section("3.3 补充说明");    // 会渲染在第三章
Section section = new Section("4.2 附加说明");    // 会渲染在第四章
```

#### 方案 2：利用"第三章末尾"区域

如果您希望某些内容出现在第三章末尾（在主要汇总之后），可以有意使用没有序号的标题。

```java
// 第三章的主要内容（使用 3.x 序号）
builder.addSection(new Section("3.1 核心结论")
    .addParagraph("本次预审整体匹配率80.0%..."));

// 第三章的补充内容（没有序号，会出现在末尾）
builder.addSection(new Section("补充说明")
    .addParagraph("以下是一些额外的注意事项..."));

builder.addSection(new Section("备注")
    .addParagraph("特别提醒事项..."));
```

**渲染顺序：**
1. 第三章标题："三、预审结果总结"
2. summaryTable（汇总表格）
3. 3.x Sections（如 "3.1 核心结论"）
4. 其他 Sections（如 "补充说明"、"备注"）

#### 方案 3：使用中文章节标记

如果您的标题使用中文章节标记（如"一、"、"二、"等），需要改用阿拉伯数字：

```java
// ❌ 不会被识别（使用中文数字）
Section section = new Section("一、匹配结果");    // 不会被识别为第一章
Section section = new Section("二、详细分析");    // 不会被识别为第二章

// ✅ 正确方式（使用阿拉伯数字）
Section section = new Section("1. 匹配结果");     // ✅ 会渲染在第一章
Section section = new Section("2. 详细分析");     // ✅ 会渲染在第二章
```

#### 方案 4：使用 cssClass 增强可读性

如果标题没有序号但需要在第三章末尾显示，可以使用 cssClass 来增强样式：

```java
Section section = new Section("重要提示")
    .addParagraph("请注意以下事项...")
    .withCssClass("highlight-section");  // 添加特殊样式类
    
builder.addSection(section);
```

### 识别规则详解

模板使用 Thymeleaf 的 `#strings.startsWith()` 函数来判断标题前缀：

```html
<!-- 第一章：标题以 "1." 开头 -->
th:if="${sec.title != null and #strings.startsWith(sec.title,'1.')}"

<!-- 第二章：标题以 "2." 开头 -->
th:if="${sec.title != null and #strings.startsWith(sec.title,'2.')}"

<!-- 第三章：标题以 "3." 开头 -->
th:if="${sec.title != null and #strings.startsWith(sec.title,'3.')}"

<!-- 第四章：标题以 "4." 开头 -->
th:if="${sec.title != null and #strings.startsWith(sec.title,'4.')}"

<!-- 其他：不以 1., 2., 3., 4. 开头 -->
th:if="${sec.title != null and 
        !#strings.startsWith(sec.title,'1.') and 
        !#strings.startsWith(sec.title,'2.') and 
        !#strings.startsWith(sec.title,'3.') and 
        !#strings.startsWith(sec.title,'4.')}"
```

### 常见标题示例

| 标题示例 | 是否被识别 | 渲染位置 |
|---------|----------|---------|
| `"1.1 精确匹配"` | ✅ 识别为第一章 | 第一章 |
| `"1. 匹配结果"` | ✅ 识别为第一章 | 第一章 |
| `"1.匹配结果"` | ✅ 识别为第一章 | 第一章 |
| `"2.1 模块分析"` | ✅ 识别为第二章 | 第二章 |
| `"3.1 核心结论"` | ✅ 识别为第三章 | 第三章（主体） |
| `"4.1 报告说明"` | ✅ 识别为第四章 | 第四章 |
| `"5.1 其他内容"` | ❌ 不识别 | 第三章末尾 |
| `"附加信息"` | ❌ 不识别 | 第三章末尾 |
| `"一、匹配结果"` | ❌ 不识别 | 第三章末尾 |
| `" 1.1 前面有空格"` | ❌ 不识别 | 第三章末尾 |
| `"A.1 附录"` | ❌ 不识别 | 第三章末尾 |

### 最佳实践建议

1. **统一使用阿拉伯数字编号** - 如 "1.1", "2.1", "3.1", "4.1"
2. **避免标题前有空格** - 确保序号在标题最开头
3. **只使用 1-4 的序号** - 序号 5 及以上不会被识别
4. **有意使用无序号标题** - 如果确实需要内容出现在第三章末尾

## Section 支持的内容类型

每个 `Section` 对象都可以包含以下内容类型，模板会自动渲染所有内容：

### 1. 标题和副标题

```java
Section section = new Section("1.1 精确匹配通过");
section.withSubtitle("匹配度 ≥ 0.90");  // 可选的副标题
```

**模板渲染：**
```html
<h3 th:text="${sec.title}">1.1 精确匹配通过</h3>
<p class="tiny muted" th:if="${sec.subtitle != null}" th:text="${sec.subtitle}"></p>
```

### 2. 段落（Paragraphs）

```java
section.addParagraph("第一段内容");
section.addParagraph("第二段内容");
section.addParagraph("第三段内容");
```

**模板渲染：**
```html
<p th:each="p : ${sec.paragraphs}" th:text="${p}">段落</p>
```

### 3. 表格（Tables）

```java
TableData table = new TableData(
    Arrays.asList("需求编号", "需求名称", "匹配度"),  // 表头
    Arrays.asList(
        Arrays.asList("C001", "用户注册功能", "1.00"),  // 第一行
        Arrays.asList("C002", "用户登录功能", "0.98")   // 第二行
    )
);
section.addTable(table);

// 可以添加多个表格
section.addTable(table2);
section.addTable(table3);
```

**模板渲染：**
```html
<div th:each="tbl : ${sec.tables}">
  <table th:if="${tbl != null}">
    <thead>
      <tr>
        <th th:each="col : ${tbl.headers}" th:text="${col}">列名</th>
      </tr>
    </thead>
    <tbody>
      <tr th:each="row : ${tbl.rows}">
        <td th:each="cell : ${row}" th:text="${cell}">数据</td>
      </tr>
    </tbody>
  </table>
</div>
```

**特殊功能：** 模板会自动过滤掉标题为 "优先级" 的列。

### 4. TableBlocks（表格块）

```java
TableBlock block = new TableBlock("1.1", "精确匹配通过");
block.setTableData(table);
section.addTableBlock(block);
```

**模板渲染：**
```html
<div th:each="blk : ${sec.tableBlocks}">
  <h3>
    <span th:text="${blk.blockId}">1.1</span>
    <span th:text="${blk.blockTitle}">精确匹配通过</span>
  </h3>
  <table th:if="${blk.tableData != null}">
    <!-- 表格内容 -->
  </table>
</div>
```

### 5. 图表（Charts）

```java
// 使用 ChartRenderer 生成图表
ChartRenderer renderer = new ChartRenderer();

Map<String, Double> pieData = new LinkedHashMap<>();
pieData.put("精确匹配通过", 50.0);
pieData.put("语义匹配通过", 30.0);
pieData.put("疑似匹配", 10.0);
pieData.put("匹配失败", 10.0);

ChartData pieChart = new ChartData("匹配状态分布", "pie", pieData);
String pieBase64 = renderer.generateChartAsDataUri(pieChart);
pieChart.setBase64Image(pieBase64);

section.addChart(pieChart);
```

**模板渲染：**
```html
<div th:each="c : ${sec.charts}" class="chart">
  <div class="chart-title" th:text="${c.title}">图表标题</div>
  <img class="chart-img" 
       th:if="${c.base64Image != null and c.base64Image != ''}" 
       th:src="${c.base64Image}" 
       alt="chart"/>
</div>
```

### 6. 自定义 HTML 内容

```java
section.withCustomContent("<div class='custom-style'>自定义HTML内容</div>");
```

**模板渲染：**
```html
<div th:if="${sec.customContent != null and sec.customContent != ''}"
     th:utext="${sec.customContent}"></div>
```

### 7. CSS 类名

```java
section.withCssClass("highlight-section");
```

**模板渲染：**
```html
<div class="section-card highlight-section">
  <!-- Section 内容 -->
</div>
```

## 完整示例代码

以下是一个完整的示例，展示如何创建 matcher-report-final 报告：

```java
package com.mercury.pdf.render;

import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.model.*;
import java.util.*;

public class MatcherReportExample {
    
    public static void main(String[] args) throws Exception {
        // 1. 创建 ReportService 并配置字体
        ReportService service = new ReportService();
        
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, sans-serif");
        service.getHtmlRenderer().setFontConfig(fontConfig);
        
        // 2. 创建 ReportData
        ReportDataBuilder builder = ReportDataBuilder.create()
            .title("需求预审报告")
            .reportDate("2024-12-31")
            .reportNumber("AI-PRE-2024-001");
        
        // 3. 添加第一章 Sections（标题以 "1." 开头）
        Section section11 = new Section("1.1 精确匹配通过（匹配度≥0.90）");
        section11.addTable(createMatchTable());
        builder.addSection(section11);
        
        Section section12 = new Section("1.2 语义匹配通过（0.70≤匹配度＜0.90）");
        section12.addTable(createSemanticMatchTable());
        builder.addSection(section12);
        
        // 4. 添加第二章 Sections（标题以 "2." 开头）
        Section section21 = new Section("2.1 分模块匹配表现");
        section21.addParagraph("按业务模块拆分匹配结果...");
        builder.addSection(section21);
        
        Section section22 = new Section("2.2 匹配失败问题根源");
        section22.addParagraph("• 需求与文档不同步：...");
        section22.addParagraph("• 功能定义不细致：...");
        builder.addSection(section22);
        
        // 5. 添加第三章内容
        // 方式1：直接设置汇总表（不在 Section 中）
        builder.summaryTable(createSummaryTable());
        
        // 方式2：添加 3.x Section
        Section section31 = new Section("3.1 核心结论");
        section31.addParagraph("本次预审整体匹配率80.0%...");
        builder.addSection(section31);
        
        // 6. 添加第四章 Section（标题以 "4." 开头）
        Section section4 = new Section("4.1 报告说明");
        section4.addParagraph("报告说明：文档部分内容由 BA助手 生成");
        section4.addParagraph("报告编号：AI-PRE-2024-001");
        builder.addSection(section4);
        
        // 7. 生成 PDF
        ReportData reportData = builder.build();
        byte[] pdfBytes = service.generatePdf(reportData, "matcher-report-final");
        
        // 8. 保存 PDF
        Files.write(Paths.get("matcher-report.pdf"), pdfBytes);
        System.out.println("✓ PDF 生成成功！");
    }
    
    private static TableData createMatchTable() {
        List<String> headers = Arrays.asList("需求编号", "需求名称", "匹配度", "匹配说明");
        List<List<String>> rows = Arrays.asList(
            Arrays.asList("C001", "用户注册功能", "1.00", "需求名称、描述完全一致"),
            Arrays.asList("C002", "用户登录功能", "0.98", "名称一致，包含所有核心场景")
        );
        return new TableData(headers, rows);
    }
    
    private static TableData createSemanticMatchTable() {
        List<String> headers = Arrays.asList("需求编号", "需求名称", "匹配度", "匹配说明");
        List<List<String>> rows = Arrays.asList(
            Arrays.asList("C005", "订单数据统计分析", "0.82", "语义高度一致")
        );
        return new TableData(headers, rows);
    }
    
    private static TableData createSummaryTable() {
        List<String> headers = Arrays.asList("匹配状态", "数量（条）", "占比");
        List<List<String>> rows = Arrays.asList(
            Arrays.asList("精确匹配通过", "10", "50.0%"),
            Arrays.asList("语义匹配通过", "6", "30.0%"),
            Arrays.asList("疑似匹配", "2", "10.0%"),
            Arrays.asList("匹配失败", "2", "10.0%")
        );
        return new TableData(headers, rows);
    }
}
```

## 关键要点总结

### ✅ 必须遵守的规则

1. **Section 标题前缀决定渲染位置**
   - 标题以 "1." 开头 → 第一章
   - 标题以 "2." 开头 → 第二章
   - 标题以 "3." 开头 → 第三章
   - 标题以 "4." 开头 → 第四章
   - 其他标题 → 第三章末尾

2. **Section 添加顺序不影响渲染位置**
   - 模板会根据标题前缀自动分组
   - 可以按任意顺序添加 Section 到 ReportData

3. **每个 Section 可包含多种内容**
   - 标题 + 副标题
   - 多个段落
   - 多个表格
   - 多个图表
   - 多个 TableBlock
   - 自定义 HTML 内容

4. **特殊功能**
   - 表格会自动过滤 "优先级" 列
   - 表格使用稳定的分页机制，不会丢失行
   - 表头在新页面自动重复

### 💡 最佳实践

1. **使用有意义的标题编号**
   ```java
   new Section("1.1 精确匹配通过（匹配度≥0.90）")  // ✅ 好
   new Section("1.x 内容")                        // ⚠️ 可以，但不够清晰
   ```

2. **为第三章汇总表使用 summaryTable**
   ```java
   builder.summaryTable(summaryTable);  // ✅ 推荐
   // 而不是放在 Section 中
   ```

3. **按章节顺序添加 Section（便于维护）**
   ```java
   // ✅ 推荐（虽然不是强制的）
   builder.addSection(section11)
          .addSection(section12)
          .addSection(section21)
          .addSection(section22)
          .addSection(section31);
   ```

4. **使用 ChartRenderer 生成图表**
   ```java
   ChartRenderer renderer = new ChartRenderer();
   String base64Image = renderer.generateChartAsDataUri(chartData);
   chartData.setBase64Image(base64Image);
   ```

## ReportData 的其他属性

除了 `sections` 列表，`ReportData` 还支持以下属性：

```java
ReportDataBuilder builder = ReportDataBuilder.create()
    .title("报告标题")           // 必需：报告主标题
    .subtitle("副标题")          // 可选：副标题
    .reportDate("2024-12-31")   // 可选：报告日期
    .reportNumber("AI-001")     // 可选：报告编号
    .reportNotice("说明信息")    // 可选：报告说明（legacy）
    .metadata("元数据")          // 可选：元数据
    .summaryTable(table);       // 可选：第三章的汇总表
```

## 向后兼容性

`matcher-report-final.html` 模板同时支持：
- **新结构**：基于 `sections` 的灵活结构（推荐）
- **旧结构**：legacy 字段如 `tableBlocks`, `analysisParagraphs`, `reportNotice`

如果您的代码仍在使用旧结构，模板会自动渲染这些内容。但建议迁移到新的 `sections` 结构以获得更好的灵活性。

## 参考资源

- 模板文件：`src/main/resources/templates/matcher-report-final.html`
- 测试用例：`src/test/java/com/mercury/pdf/render/MatcherReportFinalTest.java`
- 数据模型：`src/main/java/com/mercury/pdf/render/model/ReportData.java`
- Section 模型：`src/main/java/com/mercury/pdf/render/model/Section.java`
- 中文字体配置：[FONT_CONFIGURATION.md](FONT_CONFIGURATION.md)

## 问题反馈

如有任何疑问或建议，请在项目 GitHub 上提交 Issue。
