# 模板自定义指南 / Template Customization Guide

本文档说明如何修改现有模板或创建新的PDF报表模板。

This document explains how to modify existing templates or create new PDF report templates.

---

## 目录 / Table of Contents

1. [模板位置 / Template Location](#模板位置--template-location)
2. [修改现有模板 / Modifying Existing Templates](#修改现有模板--modifying-existing-templates)
3. [创建新模板 / Creating New Templates](#创建新模板--creating-new-templates)
4. [使用指定模板生成PDF / Using Specific Templates](#使用指定模板生成pdf--using-specific-templates)
5. [模板最佳实践 / Template Best Practices](#模板最佳实践--template-best-practices)
6. [常见问题 / FAQ](#常见问题--faq)

---

## 模板位置 / Template Location

所有HTML模板文件存放在：
All HTML template files are located in:

```
src/main/resources/templates/
├── report.html      # 默认报表模板 / Default report template
├── invoice.html     # 发票样式模板示例 / Invoice-style template example
└── [your-template.html]  # 您的自定义模板 / Your custom templates
```

---

## 修改现有模板 / Modifying Existing Templates

### 步骤 / Steps:

1. **打开模板文件**
   Open the template file:
   ```bash
   # 编辑默认报表模板
   vim src/main/resources/templates/report.html
   ```

2. **修改HTML结构或CSS样式**
   Modify HTML structure or CSS styles:
   - HTML使用Thymeleaf语法（`th:text`, `th:each`, `th:if`等）
   - CSS在`<style>`标签内定义
   - HTML uses Thymeleaf syntax (`th:text`, `th:each`, `th:if`, etc.)
   - CSS is defined within `<style>` tags

3. **测试修改**
   Test your changes:
   ```bash
   mvn clean test
   # 或运行demo程序
   mvn exec:java -Dexec.mainClass="com.mercury.pdf.render.ReportDemo"
   ```

### 可修改的内容 / What You Can Modify:

- ✅ **CSS样式**: 颜色、字体、间距、边框等
- ✅ **布局**: 页面布局、表格样式、分页控制
- ✅ **内容显示**: 显示/隐藏某些字段，调整显示顺序
- ✅ **分页规则**: 通过CSS `page-break-*` 属性控制

---

## 创建新模板 / Creating New Templates

### 方法1: 复制现有模板 / Method 1: Copy Existing Template

```bash
# 复制默认模板
cp src/main/resources/templates/report.html \
   src/main/resources/templates/my-template.html

# 编辑新模板
vim src/main/resources/templates/my-template.html
```

### 方法2: 从头创建 / Method 2: Create from Scratch

创建新文件 `src/main/resources/templates/my-template.html`:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8"/>
    <title th:text="${title}">My Template</title>
    <style>
        @page {
            size: A4;
            margin: 1.5cm;
        }
        
        body {
            font-family: Arial, sans-serif;
            font-size: 10pt;
        }
        
        /* 表格分页控制 - 关键! */
        table {
            page-break-inside: auto;
        }
        
        thead {
            display: table-header-group; /* 每页重复表头 */
        }
        
        tr {
            page-break-inside: avoid; /* 防止行跨页分割 */
        }
    </style>
</head>
<body>
    <h1 th:text="${title}">Title</h1>
    
    <!-- 您的自定义内容 / Your custom content -->
    
</body>
</html>
```

### 可用的数据变量 / Available Data Variables

模板中可以访问以下变量：
The following variables are available in templates:

| 变量名 / Variable | 类型 / Type | 说明 / Description |
|---|---|---|
| `${title}` | String | 报表标题 / Report title |
| `${subtitle}` | String | 副标题 / Subtitle |
| `${reportDate}` | String | 报表日期 / Report date |
| `${reportNumber}` | String | 报表编号 / Report number |
| `${tableBlocks}` | List&lt;TableBlock&gt; | 表格数据块列表 / Table blocks |
| `${analysisParagraphs}` | List&lt;String&gt; | 分析段落 / Analysis paragraphs |
| `${summaryTable}` | TableData | 汇总表 / Summary table |
| `${charts}` | List&lt;ChartData&gt; | 图表列表 / Charts |
| `${reportNotice}` | String | 报表声明 / Report notice |
| `${metadata}` | String | 元数据 / Metadata |

### Thymeleaf语法示例 / Thymeleaf Syntax Examples

```html
<!-- 显示文本 / Display text -->
<h1 th:text="${title}">Fallback Title</h1>

<!-- 条件显示 / Conditional display -->
<div th:if="${subtitle != null}">
    <p th:text="${subtitle}"></p>
</div>

<!-- 循环遍历 / Loop iteration -->
<ul>
    <li th:each="paragraph : ${analysisParagraphs}" th:text="${paragraph}"></li>
</ul>

<!-- 嵌套对象访问 / Nested object access -->
<table>
    <thead>
        <tr>
            <th th:each="header : ${summaryTable.headers}" th:text="${header}">Header</th>
        </tr>
    </thead>
    <tbody>
        <tr th:each="row : ${summaryTable.rows}">
            <td th:each="cell : ${row}" th:text="${cell}">Cell</td>
        </tr>
    </tbody>
</table>

<!-- 图表（base64图片）/ Charts (base64 images) -->
<img th:src="'data:image/png;base64,' + ${chart.base64Image}" alt="Chart"/>
```

---

## 使用指定模板生成PDF / Using Specific Templates

### 在代码中使用 / Using in Code

```java
import com.mercury.pdf.render.PdfRenderService;

// 方法1: 使用默认模板
PdfRenderService service = new PdfRenderService();
        byte[] pdf = service.generatePdf(reportData);

        // 方法2: 指定模板名称（不含.html扩展名）
        byte[] pdf = service.generatePdf(reportData, "invoice");

        // 方法3: 使用自定义模板
        byte[] pdf = service.generatePdf(reportData, "my-template");
```

### 配置默认模板 / Configure Default Template

```java
import com.mercury.pdf.render.HtmlReportRenderer;

HtmlReportRenderer renderer = new HtmlReportRenderer();
renderer.

setDefaultTemplateName("invoice"); // 设置默认模板

// 之后所有调用都使用invoice模板
byte[] pdf = renderer.generatePdf(reportData);
```

---

## 模板最佳实践 / Template Best Practices

### 1. 表格分页控制 / Table Pagination Control

**关键CSS规则 / Critical CSS Rules:**

```css
table {
    page-break-inside: auto;  /* 允许表格跨页 */
}

thead {
    display: table-header-group;  /* 每页重复表头 */
}

tr {
    page-break-inside: avoid;  /* 防止行跨页分割 */
    page-break-after: auto;
}
```

### 2. 字体配置 / Font Configuration

```css
@font-face {
    font-family: 'CustomFont';
    src: url('classpath:/fonts/custom-font.ttf');
}

body {
    font-family: 'CustomFont', 'DejaVu Sans', Arial, sans-serif;
}
```

### 3. 页面设置 / Page Setup

```css
@page {
    size: A4;              /* 页面大小 */
    margin: 1.5cm;         /* 页边距 */
    
    @top-center {
        content: "Page " counter(page);  /* 页眉 */
    }
    
    @bottom-center {
        content: counter(page);  /* 页码 */
    }
}
```

### 4. 避免分页问题 / Avoid Page Break Issues

```css
/* 标题后不分页 */
h1, h2, h3 {
    page-break-after: avoid;
}

/* 保持元素完整 */
.keep-together {
    page-break-inside: avoid;
}

/* 特定元素前强制分页 */
.new-page {
    page-break-before: always;
}
```

### 5. 性能优化 / Performance Optimization

```java
// 开发时禁用缓存以即时看到修改
renderer.setCacheTemplates(false);

// 生产环境启用缓存提高性能
renderer.setCacheTemplates(true);  // 默认已启用
```

---

## 常见问题 / FAQ

### Q1: 如何调试模板？ / How to debug templates?

**A:** 
1. 先生成HTML查看：
```java
// 在HtmlReportRenderer中临时添加
System.out.println(html); // 在convertHtmlToPdf之前
```

2. 将HTML保存到文件在浏览器中查看：
```java
Files.write(Paths.get("debug.html"), html.getBytes());
```

### Q2: 模板不生效？ / Template not working?

**A:** 检查以下几点：
1. ✅ 模板文件在 `src/main/resources/templates/` 目录下
2. ✅ 文件名正确（不包含.html扩展名传入）
3. ✅ 已重新编译项目 (`mvn clean compile`)
4. ✅ 如果启用了缓存，尝试禁用: `setCacheTemplates(false)`

### Q3: 表格仍然丢失行？ / Table still losing rows?

**A:** 确保CSS包含：
```css
thead { display: table-header-group; }
tr { page-break-inside: avoid; }
```

### Q4: 如何支持中文/日文/韩文？ / How to support CJK fonts?

**A:** 
1. 将字体文件放在 `src/main/resources/fonts/`
2. 在模板CSS中添加：
```css
@font-face {
    font-family: 'NotoSansCJK';
    src: url('classpath:/fonts/NotoSansCJK.ttf');
}

body {
    font-family: 'NotoSansCJK', sans-serif;
}
```

### Q5: 可以使用CSS3特性吗？ / Can I use CSS3 features?

**A:** 
- ✅ 支持CSS2.1完整特性
- ⚠️ 部分CSS3特性不支持（flexbox、grid等）
- ✅ 推荐使用表格布局和基础CSS

### Q6: 如何为不同客户使用不同模板？ / How to use different templates for different clients?

**A:**
```java
// 根据客户选择模板
String template = "default";
if (customer.isVIP()) {
    template = "premium";
} else if (customer.getType() == CustomerType.INVOICE) {
    template = "invoice";
}

byte[] pdf = service.generatePdf(reportData, template);
```

---

## 示例模板 / Example Templates

### 当前可用模板 / Available Templates:

1. **report.html** - 默认报表模板，适合分析报告
2. **invoice.html** - 发票样式模板，适合财务单据

### 添加更多模板 / Adding More Templates:

参考 `invoice.html` 作为示例，创建您自己的模板风格：
- `contract.html` - 合同样式
- `certificate.html` - 证书样式  
- `statement.html` - 账单样式

---

## 技术支持 / Technical Support

如有问题，请查阅：
- [README.md](../../../README.md) - 项目文档
- [IMPLEMENTATION_SUMMARY.md](../../../IMPLEMENTATION_SUMMARY.md) - 实现细节

或在GitHub Issues中提问。
