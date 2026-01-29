# Section Type 系统：数据与模板解耦

## 概述

为了解决后端数据与模板紧密耦合的问题，我们引入了基于 `sectionType` 的标识系统。现在可以通过显式的 ID/Type 来标识 section，而不是依赖标题前缀。

## 问题背景

**旧系统的问题：**
- ❌ 后端数据依赖标题前缀（"1.", "2.", "3.", "4."）
- ❌ 标题格式与模板渲染位置紧密耦合
- ❌ 后端数据添加顺序混乱，无法清晰表达意图
- ❌ 不够通用和灵活

**新系统的优势：**
- ✅ 使用 `sectionType` 显式标识 section 类型
- ✅ 标题可以是任意文本，不需要数字前缀
- ✅ 后端数据与模板完全解耦
- ✅ Section 可以按任意顺序添加
- ✅ 清晰的语义表达
- ✅ 完全向后兼容

## 使用方式

### 方式 1：使用 SectionType 常量（推荐）

```java
import com.mercury.pdf.render.model.Section;
import com.mercury.pdf.render.model.SectionType;

// 创建 Section 时指定 sectionType
Section section = new Section("精确匹配通过", SectionType.CHAPTER_1);
section.addParagraph("这是第一章的内容");
section.addTable(table);

// 或使用 fluent API
Section section = new Section("精确匹配通过")
    .withSectionType(SectionType.CHAPTER_1)
    .addParagraph("这是第一章的内容")
    .addTable(table);

builder.addSection(section);
```

### 方式 2：使用字符串 sectionType

```java
// 直接使用字符串
Section section = new Section("详细分析", "chapter2");
section.addParagraph("这是第二章的内容");

builder.addSection(section);
```

### 方式 3：传统方式（向后兼容）

```java
// 仍然支持传统的标题前缀方式
Section section = new Section("1.1 精确匹配通过");
section.addParagraph("使用标题前缀，自动识别为第一章");

builder.addSection(section);
```

## 完整示例

```java
import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.model.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DecoupledReportExample {
    
    public static void main(String[] args) throws Exception {
        ReportService service = new ReportService();
        
        ReportDataBuilder builder = ReportDataBuilder.create()
            .title("需求预审报告")
            .reportDate("2024-12-31");
        
        // ===== 第一章：使用 SectionType，标题完全自定义 =====
        
        Section exactMatch = new Section("精确匹配通过", SectionType.CHAPTER_1);
        exactMatch.addParagraph("匹配度 ≥ 0.90 的需求");
        exactMatch.addTable(createMatchTable());
        builder.addSection(exactMatch);
        
        Section semanticMatch = new Section("语义匹配通过", SectionType.CHAPTER_1);
        semanticMatch.addParagraph("0.70 ≤ 匹配度 < 0.90");
        semanticMatch.addTable(createSemanticTable());
        builder.addSection(semanticMatch);
        
        // ===== 第二章：分析内容 =====
        
        Section analysis = new Section("模块匹配表现分析", SectionType.CHAPTER_2);
        analysis.addParagraph("按业务模块拆分匹配结果...");
        builder.addSection(analysis);
        
        Section problemAnalysis = new Section("问题根源分析", SectionType.CHAPTER_2);
        problemAnalysis.addParagraph("• 需求与文档不同步");
        problemAnalysis.addParagraph("• 功能定义不细致");
        builder.addSection(problemAnalysis);
        
        // ===== 第三章：汇总 =====
        
        Section summary = new Section("匹配结果汇总", SectionType.CHAPTER_3);
        summary.addTable(createSummaryTable());
        builder.addSection(summary);
        
        Section conclusion = new Section("核心结论", SectionType.CHAPTER_3);
        conclusion.addParagraph("整体匹配率80.0%，高优先级需求全部匹配。");
        builder.addSection(conclusion);
        
        // ===== 第四章：说明 =====
        
        Section notes = new Section("报告说明", SectionType.CHAPTER_4);
        notes.addParagraph("报告编号：AI-PRE-2024-001");
        notes.addParagraph("生成日期：2024-12-31");
        builder.addSection(notes);
        
        // ===== 附录（会渲染在第三章末尾）=====
        
        Section appendix = new Section("附录信息", SectionType.APPENDIX);
        appendix.addParagraph("补充说明...");
        builder.addSection(appendix);
        
        // 生成 PDF
        ReportData reportData = builder.build();
        byte[] pdfBytes = service.generatePdf(reportData, "matcher-report-final");
        Files.write(Paths.get("report.pdf"), pdfBytes);
        
        System.out.println("✓ 报告生成成功！");
    }
    
    private static TableData createMatchTable() {
        // 创建表格...
        return new TableData(/* ... */);
    }
    
    private static TableData createSemanticTable() {
        // 创建表格...
        return new TableData(/* ... */);
    }
    
    private static TableData createSummaryTable() {
        // 创建汇总表格...
        return new TableData(/* ... */);
    }
}
```

## SectionType 常量

| 常量 | 值 | 说明 | 渲染位置 |
|-----|-----|------|---------|
| `SectionType.CHAPTER_1` | `"chapter1"` | 第一章：匹配结果详细列表 | 第一章 |
| `SectionType.CHAPTER_2` | `"chapter2"` | 第二章：详细分析内容 | 第二章 |
| `SectionType.CHAPTER_3` | `"chapter3"` | 第三章：预审结果总结 | 第三章 |
| `SectionType.CHAPTER_4` | `"chapter4"` | 第四章：报告说明 | 第四章 |
| `SectionType.OTHER` | `"other"` | 其他内容 | 第三章末尾 |
| `SectionType.APPENDIX` | `"appendix"` | 附录 | 第三章末尾 |

## Section 识别优先级

模板使用以下优先级来识别 Section 应该渲染的位置：

1. **优先：** 如果 `sectionType` 不为 null，使用 `sectionType` 判断
2. **回退：** 如果 `sectionType` 为 null，使用标题前缀判断（向后兼容）

**示例：**

```java
// 情况 1：明确指定 sectionType，标题可以是任意文本
Section s1 = new Section("自定义标题", SectionType.CHAPTER_1);
// 结果：渲染在第一章，因为 sectionType="chapter1"

// 情况 2：没有指定 sectionType，使用标题前缀
Section s2 = new Section("1.1 传统标题");
// 结果：渲染在第一章，因为标题以 "1." 开头

// 情况 3：同时有 sectionType 和标题前缀
Section s3 = new Section("1.1 混合标题", SectionType.CHAPTER_2);
// 结果：渲染在第二章，因为 sectionType 优先级更高

// 情况 4：没有 sectionType，标题也没有标准前缀
Section s4 = new Section("附加信息");
// 结果：渲染在第三章末尾（OTHER sections）
```

## 关键优势对比

### 旧方式（标题前缀）

```java
// 标题必须有数字前缀
Section section1 = new Section("1.1 精确匹配通过");
Section section2 = new Section("1.2 语义匹配通过");
Section section3 = new Section("2.1 详细分析");

// 问题：
// 1. 标题格式受限
// 2. 与模板耦合
// 3. 不够灵活
```

### 新方式（SectionType）

```java
// 标题可以是任意文本
Section section1 = new Section("精确匹配通过", SectionType.CHAPTER_1);
Section section2 = new Section("语义匹配通过", SectionType.CHAPTER_1);
Section section3 = new Section("详细分析", SectionType.CHAPTER_2);

// 优势：
// 1. 标题完全自定义
// 2. 数据与模板解耦
// 3. 语义清晰
// 4. 灵活可扩展
```

## 添加顺序无关

使用 `sectionType` 后，Section 的添加顺序不会影响渲染位置：

```java
// 可以按任意顺序添加 Section
builder.addSection(new Section("详细分析", SectionType.CHAPTER_2));
builder.addSection(new Section("精确匹配", SectionType.CHAPTER_1));
builder.addSection(new Section("核心结论", SectionType.CHAPTER_3));
builder.addSection(new Section("报告说明", SectionType.CHAPTER_4));

// 模板会自动按 sectionType 分组渲染：
// 第一章：精确匹配
// 第二章：详细分析
// 第三章：核心结论
// 第四章：报告说明
```

## 迁移指南

### 从旧方式迁移到新方式

**步骤 1：** 找到现有的 Section 创建代码

```java
// 旧代码
Section section = new Section("1.1 精确匹配通过");
```

**步骤 2：** 添加 SectionType 常量导入

```java
import com.mercury.pdf.render.model.SectionType;
```

**步骤 3：** 更新 Section 创建方式

```java
// 新代码（推荐）
Section section = new Section("精确匹配通过", SectionType.CHAPTER_1);

// 或保持标题不变也可以
Section section = new Section("1.1 精确匹配通过", SectionType.CHAPTER_1);
```

**步骤 4：** 逐步迁移所有 Section

**注意：** 可以分批迁移，新旧方式可以共存！

## 向后兼容性

- ✅ 所有现有代码无需修改即可继续工作
- ✅ 标题前缀匹配仍然支持
- ✅ 新旧方式可以混合使用
- ✅ 不会影响现有报告生成

## 扩展性

### 自定义 SectionType

除了使用预定义的常量，也可以使用自定义字符串：

```java
// 自定义 sectionType（如果模板支持）
Section section = new Section("特殊内容", "custom-section");
section.withSectionType("my-custom-type");
```

### 模板扩展

如果需要在模板中支持新的 section 类型，只需在模板中添加相应的判断条件：

```html
<!-- 示例：添加自定义 section 类型支持 -->
<div th:each="sec : ${sections}"
     th:if="${sec.sectionType != null and sec.sectionType == 'custom-type'}">
  <!-- 自定义渲染逻辑 -->
</div>
```

## 最佳实践

1. **新项目：** 优先使用 `sectionType` 常量
2. **现有项目：** 可以逐步迁移，新代码使用 `sectionType`
3. **标题设计：** 使用有意义的标题，不必包含数字前缀
4. **语义清晰：** 使用 `SectionType` 常量，而不是魔法字符串
5. **保持一致：** 在同一个项目中尽量使用统一的方式

## 工具方法

### SectionType.inferFromTitle()

自动从标题推断 sectionType（用于迁移）：

```java
String type = SectionType.inferFromTitle("1.1 精确匹配");
// 返回: "chapter1"

String type = SectionType.inferFromTitle("2.3 详细分析");
// 返回: "chapter2"

String type = SectionType.inferFromTitle("附加信息");
// 返回: null（无法推断）
```

### SectionType.isChapterType()

检查是否为标准章节类型：

```java
boolean isChapter = SectionType.isChapterType("chapter1");
// 返回: true

boolean isChapter = SectionType.isChapterType("other");
// 返回: false
```

## 参考资源

- Section 类：`src/main/java/com/mercury/pdf/render/model/Section.java`
- SectionType 常量：`src/main/java/com/mercury/pdf/render/model/SectionType.java`
- 测试用例：`src/test/java/com/mercury/pdf/render/SectionTypeDecouplingTest.java`
- 模板文件：`src/main/resources/templates/matcher-report-final.html`

## 问题反馈

如有任何疑问或建议，请在项目 GitHub 上提交 Issue。
