# 添加新模板指南 / Adding New Templates Guide

## 核心原则 / Core Principle

**重要**: 添加或修改模板时 **无需编写任何Java代码**！所有模板共享统一的数据模型。

**Important**: When adding or modifying templates, **NO Java code is needed**! All templates share a unified data model.

---

## 快速开始 / Quick Start

### 步骤 / Steps:

1. **创建HTML模板文件** / Create HTML template file
   ```bash
   # 在 templates 目录创建新文件
   # Create new file in templates directory
   cp src/main/resources/templates/flexible.html \
      src/main/resources/templates/my-template.html
   ```

2. **编辑模板** / Edit template
   - 使用任何文本编辑器修改HTML和CSS
   - Modify HTML and CSS using any text editor
   - 无需重启应用（开发模式下）
   - No restart needed (in development mode)

3. **使用模板** / Use template
   ```java
   ReportService service = new ReportService();
   byte[] pdf = service.generatePdf(reportData, "my-template");
   // 就这么简单！ / That's it!
   ```

**没有代码修改！没有重新编译！没有部署！**
**No code changes! No recompilation! No deployment!**

---

## 统一数据模型 / Unified Data Model

所有模板都可以访问相同的数据结构，无需任何代码更改：
All templates have access to the same data structure without any code changes:

### 可用变量 / Available Variables

| 变量 / Variable | 类型 / Type | 说明 / Description |
|---|---|---|
| `${title}` | String | 报告标题 / Report title |
| `${subtitle}` | String | 副标题 / Subtitle |
| `${reportDate}` | String | 日期 / Date |
| `${reportNumber}` | String | 编号 / Number |
| `${sections}` | List&lt;Section&gt; | 动态章节列表 / Dynamic sections |
| `${tableBlocks}` | List&lt;TableBlock&gt; | 表格块（传统） / Table blocks (legacy) |
| `${analysisParagraphs}` | List&lt;String&gt; | 分析段落 / Analysis paragraphs |
| `${summaryTable}` | TableData | 汇总表 / Summary table |
| `${charts}` | List&lt;ChartData&gt; | 图表列表 / Charts list |
| `${reportNotice}` | String | 声明 / Notice |
| `${metadata}` | String | 元数据 / Metadata |

### Section对象结构 / Section Object Structure

```java
Section {
    title: String           // 章节标题 / Section title
    subtitle: String        // 副标题 / Subtitle
    paragraphs: List<String>   // 段落 / Paragraphs
    tables: List<TableData>    // 表格 / Tables
    charts: List<ChartData>    // 图表 / Charts
    tableBlocks: List<TableBlock>  // 表格块 / Table blocks
    customContent: String      // 自定义HTML / Custom HTML
    cssClass: String          // CSS类名 / CSS class
}
```

---

## 模板示例 / Template Examples

### 示例1：最小化模板 / Example 1: Minimal Template

创建一个只显示标题和段落的简单模板：
Create a simple template showing only title and paragraphs:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8"/>
    <title th:text="${title}">Simple Report</title>
    <style>
        body { font-family: Arial; padding: 2cm; }
        h1 { color: #333; }
    </style>
</head>
<body>
    <h1 th:text="${title}">Title</h1>
    
    <!-- 显示所有章节的段落 / Display all section paragraphs -->
    <div th:each="section : ${sections}">
        <h2 th:text="${section.title}">Section</h2>
        <p th:each="para : ${section.paragraphs}" th:text="${para}">Paragraph</p>
    </div>
</body>
</html>
```

**使用 / Usage:**
```java
byte[] pdf = service.generatePdf(reportData, "simple");
```

### 示例2：表格重点模板 / Example 2: Table-Focused Template

只显示表格数据的模板：
Template showing only table data:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8"/>
    <title th:text="${title}">Data Tables</title>
    <style>
        table { width: 100%; border-collapse: collapse; }
        th, td { border: 1px solid #ddd; padding: 8px; }
        thead { background: #4CAF50; color: white; }
    </style>
</head>
<body>
    <h1 th:text="${title}">Title</h1>
    
    <!-- 显示所有章节中的所有表格 / Display all tables from all sections -->
    <div th:each="section : ${sections}">
        <h2 th:if="${section.title}" th:text="${section.title}">Section</h2>
        
        <table th:each="table : ${section.tables}">
            <thead>
                <tr>
                    <th th:each="header : ${table.headers}" th:text="${header}">Header</th>
                </tr>
            </thead>
            <tbody>
                <tr th:each="row : ${table.rows}">
                    <td th:each="cell : ${row}" th:text="${cell}">Cell</td>
                </tr>
            </tbody>
        </table>
    </div>
</body>
</html>
```

### 示例3：数据可视化模板 / Example 3: Data Visualization Template

专注于图表的模板：
Template focused on charts:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8"/>
    <title th:text="${title}">Visual Report</title>
    <style>
        .chart { text-align: center; margin: 30px 0; }
        img { max-width: 600px; }
    </style>
</head>
<body>
    <h1 th:text="${title}">Title</h1>
    
    <!-- 显示所有图表 / Display all charts -->
    <div th:each="section : ${sections}">
        <div th:each="chart : ${section.charts}" class="chart">
            <h3 th:text="${chart.title}">Chart Title</h3>
            <img th:src="'data:image/png;base64,' + ${chart.base64Image}" 
                 th:alt="${chart.title}"/>
        </div>
    </div>
</body>
</html>
```

---

## 实际案例 / Real-World Examples

### 案例1：同一数据，多种输出 / Case 1: Same Data, Multiple Outputs

```java
// 1. 创建一次数据 / Create data once
ReportData data = ReportDataBuilder.create()
    .title("Q4 2024 Report")
    .addSection(new Section("Overview")
        .addParagraph("Performance exceeded expectations")
        .addTable(metricsTable)
        .addChart(revenueChart))
    .build();

// 2. 生成不同格式 / Generate different formats
ReportService service = new ReportService();

// 详细报告 / Detailed report
byte[] detailedPdf = service.generatePdf(data, "report");

// 执行摘要 / Executive summary
byte[] summaryPdf = service.generatePdf(data, "executive-summary");

// 发票 / Invoice
byte[] invoicePdf = service.generatePdf(data, "invoice");

// 证书 / Certificate
byte[] certPdf = service.generatePdf(data, "certificate");
```

**关键点**: 相同的数据，不同的模板，不同的输出 - 无需任何代码更改！
**Key Point**: Same data, different templates, different outputs - NO code changes!

### 案例2：动态选择模板 / Case 2: Dynamic Template Selection

```java
public byte[] generateDocument(ReportData data, String userRole) {
    ReportService service = new ReportService();
    
    // 根据用户角色选择模板 - 无需特殊代码！
    // Select template based on user role - NO special code needed!
    String template = selectTemplateForRole(userRole);
    
    return service.generatePdf(data, template);
}

private String selectTemplateForRole(String role) {
    switch (role) {
        case "EXECUTIVE": return "executive-summary";
        case "ANALYST": return "flexible";
        case "FINANCE": return "invoice";
        default: return "report";
    }
}
```

---

## 高级技巧 / Advanced Tips

### 1. 条件显示 / Conditional Display

```html
<!-- 只在有数据时显示 / Only show when data exists -->
<div th:if="${sections != null and !sections.isEmpty()}">
    <!-- 内容 / Content -->
</div>

<!-- 检查特定属性 / Check specific property -->
<p th:if="${reportNotice != null and !reportNotice.isEmpty()}" 
   th:text="${reportNotice}">Notice</p>
```

### 2. 数据转换 / Data Transformation

```html
<!-- 格式化数字 / Format numbers -->
<td th:text="${#numbers.formatDecimal(value, 1, 2)}">0.00</td>

<!-- 日期格式化 / Format dates -->
<span th:text="${#dates.format(date, 'yyyy-MM-dd')}">Date</span>

<!-- 文本大写 / Uppercase text -->
<h1 th:text="${#strings.toUpperCase(title)}">TITLE</h1>
```

### 3. 循环和索引 / Loops and Indexes

```html
<!-- 带索引的循环 / Loop with index -->
<div th:each="section, stat : ${sections}">
    <h2 th:text="'Section ' + ${stat.count} + ': ' + ${section.title}">
        Section 1: Title
    </h2>
</div>

<!-- 仅显示前N项 / Show only first N items -->
<div th:each="section : ${sections}" th:if="${sectionStat.index < 3}">
    <!-- 内容 / Content -->
</div>
```

### 4. 样式控制 / Style Control

```html
<!-- 使用Section的cssClass / Use Section's cssClass -->
<div th:each="section : ${sections}" 
     th:class="${section.cssClass != null ? section.cssClass : 'default-section'}">
    <!-- 内容 / Content -->
</div>

<!-- 条件样式 / Conditional styling -->
<tr th:each="row, stat : ${table.rows}"
    th:class="${stat.odd} ? 'odd-row' : 'even-row'">
    <!-- 内容 / Content -->
</tr>
```

---

## 常见问题 / FAQ

### Q1: 需要添加新的数据字段吗？/ Do I need to add new data fields?

**A:** 不需要！使用现有的Section模型就足够了。如果需要特殊数据，可以使用：
**A:** No! The existing Section model is sufficient. For special data, use:

- `customContent` - 自定义HTML / Custom HTML
- `cssClass` - 标识特殊用途 / Identify special purpose
- 创造性地重用现有字段 / Creatively reuse existing fields

### Q2: 如何测试新模板？/ How to test new templates?

**A:**
```java
@Test
public void testMyNewTemplate() throws IOException {
    ReportData data = createTestData();
    ReportService service = new ReportService();
    
    byte[] pdf = service.generatePdf(data, "my-new-template");
    
    assertNotNull(pdf);
    Files.write(Paths.get("test-output/my-template.pdf"), pdf);
}
```

### Q3: 模板可以互相包含吗？/ Can templates include each other?

**A:** 是的！使用Thymeleaf fragments:
**A:** Yes! Use Thymeleaf fragments:

```html
<!-- 在 common-fragments.html 中 -->
<div th:fragment="header">
    <h1 th:text="${title}">Title</h1>
</div>

<!-- 在你的模板中 -->
<div th:replace="~{common-fragments :: header}"></div>
```

### Q4: 如何注册新模板？/ How to register new templates?

**A:** 可选的，用于管理目的：
**A:** Optional, for management purposes:

```java
TemplateRegistry.registerTemplate(
    new TemplateMetadata("my-template", "My Template")
        .setDescription("Custom template for special reports")
        .setCategory("report")
);
```

但这**不是必需的**，模板会自动工作！
But this is **NOT required** - templates work automatically!

---

## 最佳实践 / Best Practices

### ✅ 推荐 / DO:

1. **使用语义化的CSS类名** / Use semantic CSS class names
2. **添加条件检查避免null** / Add null checks with conditions
3. **提供回退内容** / Provide fallback content
4. **测试空数据情况** / Test with empty data
5. **文档化模板用途** / Document template purpose

### ❌ 避免 / DON'T:

1. **修改Java代码** / Modify Java code
2. **硬编码数据** / Hard-code data
3. **假设数据总是存在** / Assume data always exists
4. **过度复杂的逻辑** / Overly complex logic
5. **忽略向后兼容性** / Ignore backward compatibility

---

## 总结 / Summary

**关键要点** / **Key Takeaways:**

1. ✅ **统一接口** - 所有模板使用相同的数据模型
   **Unified Interface** - All templates use the same data model

2. ✅ **零代码变更** - 添加模板只需HTML/CSS
   **Zero Code Changes** - Adding templates requires only HTML/CSS

3. ✅ **通用机制** - Thymeleaf自动处理数据绑定
   **Universal Mechanism** - Thymeleaf handles data binding automatically

4. ✅ **完全灵活** - 每个模板自由选择显示什么
   **Complete Flexibility** - Each template freely chooses what to display

**没有模板特定的代码！没有数据解析逻辑！只需HTML！**
**No template-specific code! No data parsing logic! Just HTML!**
