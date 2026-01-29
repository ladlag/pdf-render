# 数据与模板解耦实现总结

## 问题背景

**原始问题：** 当前系统依赖标题前缀（"1.", "2.", "3.", "4."）来决定 section 的渲染位置，导致：
- 后端数据与模板紧密耦合
- 标题格式受限
- 数据结构不够通用
- 添加顺序混乱

**用户正确指出的设计问题：**
> 为什么你的代码里绑定了"精确匹配通过"、"chapter1" 等具体报告的元素？
> 我让你做的是一套通用的、兼容多模版的PDF渲染引擎，不应该在核心类中定义这么多定制化的内容。

## 最终解决方案

### 1. 核心设计：完全通用

**Section 类（核心模型）：**
```java
public class Section {
    private String title;           // 任意标题
    private String sectionType;     // 通用标识符（字符串，无预定义值）
    // ... 其他内容字段
}
```

**关键特点：**
- ✅ `sectionType` 只是一个普通字符串字段
- ✅ 没有任何预定义的值或常量
- ✅ 完全由用户和模板决定如何使用
- ✅ 核心代码保持100%通用

### 2. 模板实现：灵活识别

**matcher-report-final.html 模板：**
```html
<!-- 优先使用 sectionType，回退到标题前缀（向后兼容） -->
<div th:each="sec : ${sections}"
     th:if="${(sec.sectionType != null and sec.sectionType == 'chapter1') or 
             (sec.sectionType == null and sec.title != null and #strings.startsWith(sec.title,'1.'))}">
  <!-- 渲染第一章内容 -->
</div>
```

**特点：**
- ✅ 模板自主决定需要哪些 sectionType 值
- ✅ 支持向后兼容（标题前缀）
- ✅ sectionType 优先，title 前缀是 fallback

### 3. 示例代码：仅供参考

**MatcherReportSectionTypes（examples 包）：**
```java
// ⚠️ 这只是针对 matcher-report-final 模板的示例常量
// 不是核心引擎的一部分，用户可以选择使用或自定义
public final class MatcherReportSectionTypes {
    public static final String CHAPTER_1 = "chapter1";
    public static final String CHAPTER_2 = "chapter2";
    // ...
}
```

**特点：**
- ✅ 放在 examples 包，不是 model 包
- ✅ 清晰标注这是示例，非必需
- ✅ 用户可以定义自己的常量

## 使用方式

### 方式 1：直接使用字符串（最灵活）

```java
// 不依赖任何预定义常量
Section section = new Section("Introduction", "intro");
Section section = new Section("Main Content", "body");
Section section = new Section("Conclusion", "conclusion");
```

### 方式 2：在项目中自定义常量（推荐）

```java
// 在您自己的项目中定义
public class MyReportTypes {
    public static final String HEADER = "header";
    public static final String CONTENT = "content";
    public static final String FOOTER = "footer";
}

Section section = new Section("Header", MyReportTypes.HEADER);
```

### 方式 3：使用示例常量（可选）

```java
// 如果使用 matcher-report-final 模板，可以参考示例常量
import com.mercury.pdf.render.examples.MatcherReportSectionTypes;

Section section = new Section("匹配结果", MatcherReportSectionTypes.CHAPTER_1);
// 等同于
Section section = new Section("匹配结果", "chapter1");
```

## 架构分层

```
┌─────────────────────────────────────────┐
│         用户项目                         │
│  - 自定义 sectionType 常量              │
│  - 业务逻辑                             │
│  - 报告数据构建                          │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│     examples 包（可选参考）              │
│  - MatcherReportSectionTypes            │
│  - 其他模板的示例常量                    │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│      核心引擎（完全通用）                │
│  - Section (sectionType: String)        │
│  - ReportData                           │
│  - ReportService                        │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│          模板层                          │
│  - matcher-report-final.html           │
│  - flexible.html                       │
│  - 其他模板                             │
└─────────────────────────────────────────┘
```

## 关键原则

1. **核心通用性**
   - 核心类不包含任何业务逻辑
   - 不预定义任何 sectionType 值
   - 所有字段都是通用的

2. **模板自主性**
   - 每个模板定义自己需要的 sectionType 值
   - 模板决定如何解释和使用 sectionType
   - 不同模板可以使用完全不同的 sectionType 体系

3. **用户灵活性**
   - 用户在自己的项目中定义 sectionType 常量
   - 或直接使用字符串，无需任何常量
   - 完全控制命名和结构

4. **示例分离**
   - 特定模板的示例放在 examples 包
   - 清晰标注这些是示例，非核心
   - 用户可选择性参考或自定义

## 对比旧系统

### 旧系统（紧耦合）

```java
// 标题必须有特定前缀
Section s = new Section("1.1 精确匹配通过");
// 问题：
// - 标题格式受限
// - 与模板紧密耦合
// - 业务概念泄漏到标题中
```

### 新系统（解耦）

```java
// 标题和位置完全独立
Section s = new Section("精确匹配通过", "chapter1");
// 或者使用完全不同的体系
Section s = new Section("任意标题", "my-custom-type");
// 优势：
// - 标题自由
// - 数据与模板解耦
// - 完全通用
```

## 扩展性

### 为新模板添加支持

**步骤 1：** 定义新模板的 sectionType 体系（在您的项目中）

```java
public class InvoiceReportTypes {
    public static final String HEADER = "invoice-header";
    public static final String ITEMS = "invoice-items";
    public static final String TOTALS = "invoice-totals";
    public static final String FOOTER = "invoice-footer";
}
```

**步骤 2：** 在模板中使用这些值

```html
<div th:each="sec : ${sections}"
     th:if="${sec.sectionType == 'invoice-header'}">
  <!-- 发票头部 -->
</div>

<div th:each="sec : ${sections}"
     th:if="${sec.sectionType == 'invoice-items'}">
  <!-- 发票明细 -->
</div>
```

**步骤 3：** 使用

```java
Section header = new Section("Invoice Header", InvoiceReportTypes.HEADER);
Section items = new Section("Items", InvoiceReportTypes.ITEMS);
```

## 总结

✅ **核心引擎保持100%通用**
- 没有业务逻辑
- 没有预定义的 sectionType 值
- 完全灵活和可扩展

✅ **清晰的架构分层**
- 核心引擎
- 示例代码（可选）
- 用户项目（自定义）
- 模板层

✅ **完全向后兼容**
- 现有代码无需修改
- 模板支持 fallback 到标题前缀

✅ **用户完全控制**
- 自定义 sectionType 体系
- 或直接使用字符串
- 无任何限制

这是一个真正通用、灵活、可扩展的PDF渲染引擎设计。
