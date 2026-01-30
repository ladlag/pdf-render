# JAR 集成使用说明

本文档说明如何在普通Java项目（非Spring Boot）中集成和使用pdf-render库。

## 目录
- [环境要求](#环境要求)
- [Maven依赖](#maven依赖)
- [基本使用](#基本使用)
- [中文字体配置](#中文字体配置)
- [完整示例](#完整示例)
- [Spring Boot集成](#spring-boot集成)

---

## 环境要求

- **JDK**: 1.8 或更高版本 (基于JDK 8开发)
- **Maven**: 3.6+ (用于构建和依赖管理)

---

## Maven依赖

### 方式1: 使用本地Maven仓库（推荐）

1. **构建并安装库到本地Maven仓库：**
```bash
cd pdf-render
mvn clean install
```

这将生成以下文件：
- `pdf-render-1.0.1.jar` - 主JAR包
- `pdf-render-1.0.1-sources.jar` - 源代码JAR
- `pdf-render-1.0.1-javadoc.jar` - Javadoc文档

2. **在你的项目pom.xml中添加依赖：**
```xml
<dependency>
    <groupId>com.mercury</groupId>
    <artifactId>pdf-render</artifactId>
    <version>1.0.1</version>
</dependency>
```

### 方式2: 直接使用JAR文件

如果不使用Maven，可以直接将编译后的JAR添加到项目classpath：

```bash
# 构建项目
mvn clean package

# JAR文件位置
ls target/pdf-render-1.0.1.jar
```

将JAR及其依赖添加到项目的classpath中。

---

## 基本使用

### 1. 简单PDF生成（英文内容）

```java
import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.model.ReportData;
import com.mercury.pdf.render.model.ReportDataBuilder;
import com.mercury.pdf.render.model.Section;

import java.io.FileOutputStream;
import java.io.IOException;

public class SimplePdfDemo {
    public static void main(String[] args) throws IOException {
        // 创建服务
        ReportService service = new ReportService();
        
        // 构建报告数据
        ReportData reportData = ReportDataBuilder.create()
            .title("Sample Report")
            .subtitle("Generated with pdf-render")
            .date("2026-01-30")
            .addSection(new Section("Introduction")
                .addParagraph("This is a simple PDF report."))
            .addSection(new Section("Content")
                .addParagraph("Add your content here."))
            .build();
        
        // 生成PDF
        byte[] pdfBytes = service.generatePdf(reportData);
        
        // 保存到文件
        try (FileOutputStream fos = new FileOutputStream("output.pdf")) {
            fos.write(pdfBytes);
        }
        
        System.out.println("PDF generated: output.pdf");
    }
}
```

---

## 中文字体配置

**重要：** 如果PDF中需要显示中文、日文或韩文字符，必须配置字体，否则CJK文字会显示为方框（□）。

### 1. 准备字体文件

将字体文件放置在资源目录：
```
src/main/resources/fonts/
├── HarmonyOS_Sans_SC_Regular.ttf
└── NotoSansCJKsc-Regular.otf
```

### 2. 配置字体

```java
import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.config.PdfRenderProperties;

public class ChinesePdfDemo {
    public static void main(String[] args) throws IOException {
        ReportService service = new ReportService();
        
        // 方式1: 使用PdfRenderProperties（推荐）
        PdfRenderProperties properties = new PdfRenderProperties();
        PdfRenderProperties.FontProperties fonts = new PdfRenderProperties.FontProperties();
        fonts.setRegularPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fonts.setDefaultFamily("HarmonyOS Sans SC, DejaVu Sans, sans-serif");
        properties.setFonts(fonts);
        
        service.getHtmlRenderer().setFontProperties(fonts);
        
        // 构建包含中文的报告
        ReportData reportData = ReportDataBuilder.create()
            .title("测试报告")
            .subtitle("PDF中文字体测试")
            .date("2026年1月30日")
            .addSection(new Section("第一章")
                .addParagraph("这是中文段落内容。"))
            .build();
        
        // 生成PDF
        byte[] pdfBytes = service.generatePdf(reportData);
        
        // 保存
        try (FileOutputStream fos = new FileOutputStream("chinese-report.pdf")) {
            fos.write(pdfBytes);
        }
        
        System.out.println("中文PDF已生成: chinese-report.pdf");
    }
}
```

### 支持的字体路径格式

- `classpath:/fonts/font.ttf` - 从classpath加载（推荐）
- `/absolute/path/to/font.ttf` - 绝对路径
- `file:/path/to/font.ttf` - 文件URI

---

## 完整示例

### 带表格和图表的复杂报告

```java
import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.model.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class ComplexReportDemo {
    public static void main(String[] args) throws IOException {
        ReportService service = new ReportService();
        
        // 配置中文字体
        PdfRenderProperties.FontProperties fonts = new PdfRenderProperties.FontProperties();
        fonts.setRegularPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fonts.setDefaultFamily("HarmonyOS Sans SC, sans-serif");
        service.getHtmlRenderer().setFontProperties(fonts);
        
        // 创建表格数据
        TableData table = new TableData();
        table.setHeaders(Arrays.asList("产品", "销量", "收入"));
        table.addRow(Arrays.asList("产品A", "1000", "¥50,000"));
        table.addRow(Arrays.asList("产品B", "800", "¥40,000"));
        table.addRow(Arrays.asList("产品C", "1200", "¥60,000"));
        
        // 创建图表数据
        ChartData chart = new ChartData();
        chart.setTitle("月度销量");
        chart.setType("bar");
        chart.setLabels(Arrays.asList("1月", "2月", "3月"));
        chart.setData(Arrays.asList(100.0, 150.0, 200.0));
        
        // 构建完整报告
        ReportData reportData = ReportDataBuilder.create()
            .title("季度销售报告")
            .subtitle("2026年第一季度")
            .date("2026-01-30")
            .notice("机密文件 - 仅供内部使用")
            .addSection(new Section("执行摘要")
                .addParagraph("本季度销售业绩超出预期，同比增长25%。"))
            .addSection(new Section("详细数据")
                .addTable(table))
            .addSection(new Section("销售趋势")
                .addChart(chart))
            .addSection(new Section("结论")
                .addParagraph("建议继续扩大市场份额，加大产品C的推广力度。"))
            .build();
        
        // 生成PDF（使用默认模板）
        byte[] pdfBytes = service.generatePdf(reportData);
        
        // 或指定特定模板
        // byte[] pdfBytes = service.generatePdf(reportData, "flexible");
        
        // 保存
        try (FileOutputStream fos = new FileOutputStream("quarterly-report.pdf")) {
            fos.write(pdfBytes);
        }
        
        System.out.println("复杂报告已生成: quarterly-report.pdf");
    }
}
```

### 可用的内置模板

- `report` - 标准报告模板（默认）
- `flexible` - 灵活布局模板
- `invoice` - 发票模板
- `certificate` - 证书模板
- `executive-summary` - 执行摘要模板

---

## Spring Boot集成

如果你使用Spring Boot，集成更加简单，支持自动配置。

### 1. 添加依赖
```xml
<dependency>
    <groupId>com.mercury</groupId>
    <artifactId>pdf-render</artifactId>
    <version>1.0.1</version>
</dependency>
```

### 2. 配置application.yml
```yaml
pdf-render:
  template:
    default-name: report
    cache-enabled: true
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    default-family: HarmonyOS Sans SC, sans-serif
  debug:
    enabled: false
```

### 3. 注入使用
```java
@Service
public class PdfService {
    private final ReportService reportService;
    
    // Spring自动注入，无需手动配置
    public PdfService(ReportService reportService) {
        this.reportService = reportService;
    }
    
    public byte[] generatePdf(ReportData data) throws IOException {
        return reportService.generatePdf(data);
    }
}
```

**详细的Spring Boot集成说明请参考：** [SPRING_BOOT_INTEGRATION_GUIDE.md](SPRING_BOOT_INTEGRATION_GUIDE.md)

---

## 调试和故障排查

### 启用调试模式

调试模式会输出中间HTML文件，帮助定位问题：

```java
service.getHtmlRenderer().setDebugHtmlEnabled(true);
service.getHtmlRenderer().setDebugHtmlOutputDirectory("debug-html");
service.getHtmlRenderer().setDebugHtmlIncludeTimestamp(true);
```

生成的HTML文件将保存在 `debug-html/` 目录。

### 常见问题

1. **中文显示为方框？**
   - 确保已正确配置字体
   - 验证字体文件路径正确
   - 检查字体文件是否包含在JAR中

2. **找不到模板？**
   - 确保模板文件在 `src/main/resources/templates/` 目录
   - 模板名称不要包含 `.html` 扩展名

3. **图表不显示？**
   - 检查ChartData配置是否正确
   - 确保数据不为空

**更多问题请参考：** [TROUBLESHOOTING.md](TROUBLESHOOTING.md)

---

## 更多文档

- [README.md](README.md) - 项目概述
- [README_CN.md](README_CN.md) - 中文项目说明
- [FONT_CONFIGURATION.md](FONT_CONFIGURATION.md) - 字体配置详解
- [TEMPLATE_GUIDE.md](TEMPLATE_GUIDE.md) - 模板使用指南
- [EXAMPLE_APPLICATION.md](EXAMPLE_APPLICATION.md) - 完整应用示例
- [TROUBLESHOOTING.md](TROUBLESHOOTING.md) - 故障排查指南

---

## 许可证

本项目使用 Apache License 2.0 许可证。详见 [LICENSE](LICENSE) 文件。
