# Section Type 系统：通用的数据与模板解耦方案

## 概述

`sectionType` 是 Section 类的一个通用字符串字段，用于在不依赖标题格式的情况下标识和分类 section。

**核心理念：** `sectionType` 没有预定义的值，完全由您的模板和业务需求决定。

## 设计原则

✅ **完全通用** - 不预设任何特定业务场景
✅ **灵活自定义** - sectionType 可以是任意字符串
✅ **模板自主** - 由模板决定如何使用 sectionType
✅ **无强制依赖** - sectionType 是可选的，可以为 null

## 基本用法

### 示例 1：简单的报告结构

```java
// 定义您自己的 section 类型（在您的代码中）
Section intro = new Section("引言", "intro");
Section body = new Section("正文", "body");
Section conclusion = new Section("结论", "conclusion");

builder.addSection(intro);
builder.addSection(body);
builder.addSection(conclusion);
```

### 示例 2：多级结构

```java
// 使用任意字符串作为 sectionType
Section header = new Section("头部", "header");
Section content1 = new Section("第一部分", "content-primary");
Section content2 = new Section("第二部分", "content-secondary");
Section footer = new Section("尾部", "footer");
```

### 示例 3：语义化标识

```java
// 使用语义化的 sectionType
Section summary = new Section("摘要", "summary");
Section details = new Section("详情", "details");
Section appendix = new Section("附录", "appendix");
```

## 模板如何使用 sectionType

在 Thymeleaf 模板中，您可以根据 `sectionType` 来决定如何渲染 section：

```html
<!-- 示例：根据 sectionType 渲染不同位置 -->

<!-- 渲染 intro 类型的 sections -->
<div th:each="sec : ${sections}"
     th:if="${sec.sectionType == 'intro'}">
  <h1 th:text="${sec.title}">Introduction</h1>
  <!-- intro 的特定样式 -->
</div>

<!-- 渲染 body 类型的 sections -->
<div th:each="sec : ${sections}"
     th:if="${sec.sectionType == 'body'}">
  <h2 th:text="${sec.title}">Body</h2>
  <!-- body 的特定样式 -->
</div>

<!-- 渲染 conclusion 类型的 sections -->
<div th:each="sec : ${sections}"
     th:if="${sec.sectionType == 'conclusion'}">
  <h3 th:text="${sec.title}">Conclusion</h3>
  <!-- conclusion 的特定样式 -->
</div>
```

## matcher-report-final 模板示例

对于 matcher-report-final 模板，我们提供了一个**示例常量类**（非必需）：

```java
// 这只是一个示例，您可以定义自己的常量
import com.mercury.pdf.render.examples.MatcherReportSectionTypes;

Section section = new Section("匹配结果", MatcherReportSectionTypes.CHAPTER_1);

// 或者直接使用字符串（推荐，更灵活）
Section section = new Section("匹配结果", "chapter1");
```

**⚠️ 重要：** `MatcherReportSectionTypes` 只是一个示例，不是核心引擎的一部分。

## 完整示例

```java
import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.model.*;

public class GenericReportExample {
    
    public static void main(String[] args) throws Exception {
        ReportService service = new ReportService();
        
        ReportDataBuilder builder = ReportDataBuilder.create()
            .title("通用报告");
        
        // 方式 1：直接使用字符串（推荐）
        builder.addSection(new Section("概述", "overview")
            .addParagraph("这是概述内容"));
        
        builder.addSection(new Section("详细内容", "details")
            .addParagraph("这是详细内容")
            .addTable(table));
        
        builder.addSection(new Section("总结", "summary")
            .addParagraph("这是总结"));
        
        // 方式 2：先创建后设置
        Section appendix = new Section("附录");
        appendix.withSectionType("appendix");
        appendix.addParagraph("附录内容");
        builder.addSection(appendix);
        
        // 生成 PDF
        byte[] pdf = service.generatePdf(builder.build(), "your-template");
    }
}
```

## 与标题前缀的区别

### 旧方式（依赖标题）

```java
// 标题格式决定渲染位置 - 紧耦合
Section section = new Section("1.1 第一章");
// 模板通过检查标题 "1." 前缀来判断
```

### 新方式（使用 sectionType）

```java
// 标题和位置独立 - 松耦合
Section section = new Section("第一章", "chapter1");
// 模板通过检查 sectionType 来判断
// 标题可以是任意文本
```

## 向后兼容

- ✅ `sectionType` 是可选的，可以为 null
- ✅ 模板可以同时支持 sectionType 和标题前缀
- ✅ 优先使用 sectionType，如果为 null 则回退到标题检查
- ✅ 现有代码无需修改即可继续工作

## 自定义您的 section 类型

**推荐做法：** 在您自己的项目中定义常量

```java
// 在您的项目代码中
public class MyReportSectionTypes {
    public static final String HEADER = "header";
    public static final String INTRODUCTION = "introduction";
    public static final String METHODOLOGY = "methodology";
    public static final String RESULTS = "results";
    public static final String DISCUSSION = "discussion";
    public static final String CONCLUSION = "conclusion";
    public static final String REFERENCES = "references";
}

// 使用
Section intro = new Section("Introduction", MyReportSectionTypes.INTRODUCTION);
```

## 关键要点

1. **sectionType 是完全自定义的** - 没有预定义的值
2. **由模板决定如何使用** - 不同模板可以定义不同的 sectionType 值
3. **保持核心引擎通用** - 引擎本身不关心 sectionType 的具体值
4. **业务逻辑在您的代码中** - 在您的项目中定义 sectionType 常量
5. **灵活且可扩展** - 可以随时添加新的 sectionType 值

## 参考资源

- Section 类：`src/main/java/com/mercury/pdf/render/model/Section.java`
- 示例常量（matcher-report-final）：`src/main/java/com/mercury/pdf/render/examples/MatcherReportSectionTypes.java`
- 测试示例：`src/test/java/com/mercury/pdf/render/SectionTypeDecouplingTest.java`

## 问题反馈

如有任何疑问或建议，请在项目 GitHub 上提交 Issue。
