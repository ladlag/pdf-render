# 架构对比：解耦前 vs 解耦后

## 解耦前（紧耦合架构）

```
┌─────────────────────────────────────────────┐
│           用户代码                            │
│                                              │
│  Section s = new Section("1.1 精确匹配");   │
│  // 标题格式 = 渲染位置                      │
│  // 业务概念混入标题                         │
└─────────────────────────────────────────────┘
                    ↓
            ❌ 紧密耦合
                    ↓
┌─────────────────────────────────────────────┐
│          matcher-report-final.html          │
│                                              │
│  th:if="${sec.title.startsWith('1.')}"     │
│  // 解析标题前缀来决定位置                   │
└─────────────────────────────────────────────┘

问题：
❌ 标题格式受限（必须有 "1.", "2." 前缀）
❌ 业务逻辑泄漏到标题中
❌ 修改模板需要修改所有相关代码
❌ 不通用，难以扩展到其他模板
```

## 解耦后（通用架构）

```
┌─────────────────────────────────────────────┐
│           用户代码（完全自由）                │
│                                              │
│  // 方式 1: 直接使用字符串                   │
│  Section s = new Section("任意标题", "intro");│
│                                              │
│  // 方式 2: 自定义常量                       │
│  Section s = new Section(                   │
│      "任意标题",                             │
│      MyReportTypes.CUSTOM_TYPE              │
│  );                                         │
└─────────────────────────────────────────────┘
                    ↓
            ✅ 完全解耦
                    ↓
┌─────────────────────────────────────────────┐
│      核心引擎（100%通用）                     │
│                                              │
│  public class Section {                     │
│      private String sectionType;  // 通用字段│
│      // 无预定义值，无业务逻辑               │
│  }                                          │
└─────────────────────────────────────────────┘
                    ↓
                    ↓
┌─────────────────────────────────────────────┐
│     examples 包（可选参考，非核心）          │
│                                              │
│  MatcherReportSectionTypes.CHAPTER_1        │
│  // 仅针对特定模板的示例常量                 │
└─────────────────────────────────────────────┘
                    ↓
                    ↓
┌─────────────────────────────────────────────┐
│          模板层（自主解释）                   │
│                                              │
│  th:if="${sec.sectionType == 'chapter1'}"  │
│  // 或                                      │
│  th:if="${sec.sectionType == 'intro'}"     │
│  // 每个模板定义自己的 sectionType 值        │
└─────────────────────────────────────────────┘

优势：
✅ 标题完全自由（任意文本）
✅ 核心引擎完全通用（无业务逻辑）
✅ 模板自主定义 sectionType 体系
✅ 用户完全控制（可自定义或使用示例）
✅ 易于扩展到新模板
```

## 代码对比

### 解耦前

```java
// ❌ 标题必须有特定格式
Section section1 = new Section("1.1 精确匹配通过");
Section section2 = new Section("1.2 语义匹配通过");
Section section3 = new Section("2.1 详细分析");

// 问题：
// - 标题格式受限
// - "1.1", "2.1" 等是模板特定的
// - 不能用于其他模板
```

### 解耦后

```java
// ✅ 标题和类型独立
Section section1 = new Section("精确匹配通过", "chapter1");
Section section2 = new Section("语义匹配通过", "chapter1");
Section section3 = new Section("详细分析", "chapter2");

// 或者为其他模板使用不同的 sectionType
Section intro = new Section("Introduction", "intro");
Section body = new Section("Main Content", "body");

// 优势：
// - 标题自由
// - 可用于任何模板
// - 完全通用
```

## 扩展性对比

### 添加新模板 - 解耦前

```
❌ 困难：
1. 需要定义新的标题前缀规则（如 "A.", "B."？）
2. 用户代码需要知道新规则
3. 可能与现有规则冲突
4. 核心代码可能需要修改
```

### 添加新模板 - 解耦后

```
✅ 简单：
1. 在新模板中定义需要的 sectionType 值
2. 在用户项目中定义对应常量（可选）
3. 使用：new Section("标题", "新模板的sectionType")
4. 核心代码无需任何修改
```

## 示例：支持多种模板

### Invoice 模板

```java
// 用户项目中定义
public class InvoiceTypes {
    public static final String HEADER = "invoice-header";
    public static final String ITEMS = "invoice-items";
    public static final String TOTALS = "invoice-totals";
}

Section header = new Section("Invoice #12345", InvoiceTypes.HEADER);
Section items = new Section("Items", InvoiceTypes.ITEMS);
```

### Academic Paper 模板

```java
// 用户项目中定义
public class PaperTypes {
    public static final String ABSTRACT = "abstract";
    public static final String INTRODUCTION = "introduction";
    public static final String METHODOLOGY = "methodology";
    public static final String RESULTS = "results";
}

Section abs = new Section("Abstract", PaperTypes.ABSTRACT);
Section intro = new Section("Introduction", PaperTypes.INTRODUCTION);
```

### 同一个核心引擎，无需修改！

## 总结

| 维度 | 解耦前 | 解耦后 |
|-----|--------|--------|
| **核心引擎** | 包含业务逻辑 | 100%通用 |
| **标题格式** | 受限（必须有前缀） | 完全自由 |
| **扩展性** | 困难（规则冲突） | 简单（各模板独立） |
| **用户控制** | 受限 | 完全控制 |
| **模板数量** | 受限 | 无限 |
| **维护成本** | 高（牵一发动全身） | 低（各自独立） |

**结论：** 解耦后的架构是真正通用、灵活、可扩展的PDF渲染引擎设计。
