# 完整示例：中文PDF生成 / Complete Example: Chinese PDF Generation

## 示例1：纯Java项目（不使用Spring Boot）

### 步骤1：创建服务并配置字体

```java
package com.example;

import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.model.ReportData;
import com.mercury.pdf.render.model.Section;
import com.lowagie.text.DocumentException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ChinesePdfExample {
    
    public static void main(String[] args) {
        try {
            // 创建报表服务
            ReportService service = new ReportService();
            
            // ============================================
            // 关键步骤：配置中文字体
            // ============================================
            FontConfig fontConfig = new FontConfig();
            
            // 1. 设置字体文件路径（使用项目内置的字体）
            fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
            
            // 2. 设置字体族名称（必须与字体内部名称一致）
            fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, sans-serif");
            
            // 3. 应用字体配置
            service.getHtmlRenderer().setFontConfig(fontConfig);
            // ============================================
            
            // 创建包含中文的报表数据
            ReportData reportData = createChineseReport();
            
            // 生成PDF
            byte[] pdfBytes = service.generatePdf(reportData);
            
            // 保存到文件
            Files.write(Paths.get("chinese_report.pdf"), pdfBytes);
            
            System.out.println("✓ PDF已生成：chinese_report.pdf");
            System.out.println("  文件大小：" + (pdfBytes.length / 1024) + " KB");
            System.out.println("  请打开PDF验证中文是否正确显示");
            
        } catch (IOException | DocumentException e) {
            System.err.println("错误：" + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 创建包含中文内容的报表数据
     */
    private static ReportData createChineseReport() {
        ReportData data = new ReportData();
        
        // 封面信息
        data.setTitle("2024年第四季度业务报告");
        data.setSubtitle("Business Report Q4 2024");
        data.setReportDate("2024-12-31");
        data.setReportNumber("RPT-2024-Q4-001");
        data.setReportNotice("本报告内容为机密信息，仅限内部使用");
        data.setMetadata("财务部 | 生成日期：2024-12-31");
        
        // 第一章：概述
        Section section1 = new Section("第一章：业务概述");
        section1.addParagraph("本季度公司业务持续稳定增长，各项指标均达到预期目标。");
        section1.addParagraph("主要业务增长来自华东、华南两个区域，同比增长达到25%。");
        data.getSections().add(section1);
        
        // 第二章：数据分析
        Section section2 = new Section("第二章：数据分析");
        section2.addParagraph("根据市场调研数据显示，客户满意度持续提升。");
        
        // 添加表格
        com.mercury.pdf.render.model.TableData table = new com.mercury.pdf.render.model.TableData();
        table.setTitle("区域销售数据统计表");
        
        // 表头
        java.util.List<String> headers = java.util.Arrays.asList(
            "区域", "销售额（万元）", "同比增长", "市场份额"
        );
        table.setHeaders(headers);
        
        // 表格数据
        java.util.List<java.util.List<String>> rows = java.util.Arrays.asList(
            java.util.Arrays.asList("华东地区", "5,230", "+28%", "35%"),
            java.util.Arrays.asList("华南地区", "4,150", "+22%", "28%"),
            java.util.Arrays.asList("华北地区", "3,680", "+15%", "25%"),
            java.util.Arrays.asList("西南地区", "1,800", "+18%", "12%")
        );
        table.setRows(rows);
        
        section2.addTable(table);
        data.getSections().add(section2);
        
        // 第三章：总结与展望
        Section section3 = new Section("第三章：总结与展望");
        section3.addParagraph("展望下一季度，我们将继续深耕重点市场，优化产品结构。");
        section3.addParagraph("预计下季度增长率将保持在20%以上，市场份额进一步扩大。");
        data.getSections().add(section3);
        
        return data;
    }
}
```

### 运行示例

```bash
# 编译
javac -cp "pdf-render-1.0.1.jar:lib/*" ChinesePdfExample.java

# 运行
java -cp ".:pdf-render-1.0.1.jar:lib/*" com.example.ChinesePdfExample
```

---

## 示例2：Spring Boot项目

### 步骤1：添加依赖（pom.xml）

```xml
<dependency>
    <groupId>com.mercury</groupId>
    <artifactId>pdf-render</artifactId>
    <version>1.0.1</version>
</dependency>
```

### 步骤2：配置字体（application.yml）

```yaml
# application.yml
pdf-render:
  fonts:
    # 字体文件路径（使用项目内置字体）
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    bold-path: classpath:/fonts/HarmonyOS_Sans_SC_Bold.ttf
    
    # 字体族名称（必须与字体内部名称一致）
    default-family: HarmonyOS Sans SC, DejaVu Sans, sans-serif
```

### 步骤3：创建Service

```java
package com.example.service;

import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.model.ReportData;
import com.lowagie.text.DocumentException;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class PdfService {
    
    private final ReportService reportService;
    
    // ✅ 构造函数注入 - 自动配置已生效
    public PdfService(ReportService reportService) {
        this.reportService = reportService;
    }
    
    /**
     * 生成中文PDF报告
     */
    public byte[] generateChineseReport(ReportData reportData) 
            throws IOException, DocumentException {
        // 直接调用即可，字体配置已经从YAML加载
        return reportService.generatePdf(reportData);
    }
}
```

### 步骤4：在Controller中使用

```java
package com.example.controller;

import com.example.service.PdfService;
import com.mercury.pdf.render.model.ReportData;
import com.mercury.pdf.render.model.Section;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {
    
    private final PdfService pdfService;
    
    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }
    
    @GetMapping("/generate")
    public ResponseEntity<byte[]> generatePdf() {
        try {
            // 创建报表数据
            ReportData data = new ReportData();
            data.setTitle("中文测试报告");
            data.setSubtitle("Chinese Test Report");
            data.setReportDate("2024-12-31");
            
            Section section = new Section("测试章节");
            section.addParagraph("这是一段中文测试内容。");
            section.addParagraph("如果您能看到这些汉字，说明配置成功！");
            data.getSections().add(section);
            
            // 生成PDF
            byte[] pdfBytes = pdfService.generateChineseReport(data);
            
            // 返回PDF文件
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "report.pdf");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
                
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
```

### ⚠️ 重要：不要使用@PostConstruct

```java
// ❌ 错误示例 - 不要这样做！
@Service
public class PdfService {
    
    @Autowired
    private ReportService reportService;
    
    @PostConstruct  // ❌ 这会覆盖YAML配置！
    public void init() {
        FontConfig config = new FontConfig();
        config.setRegularFontPath("...");
        reportService.getHtmlRenderer().setFontConfig(config);
    }
}
```

```java
// ✅ 正确示例 - 直接注入
@Service
public class PdfService {
    
    private final ReportService reportService;
    
    public PdfService(ReportService reportService) {
        this.reportService = reportService; // 自动配置已生效
    }
}
```

---

## 示例3：使用图表（含中文标签）

```java
import com.mercury.pdf.render.model.ChartData;
import com.mercury.pdf.render.model.ChartConfig;

// 创建图表配置
ChartConfig chartConfig = new ChartConfig();
chartConfig.setType("bar");
chartConfig.setTitle("2024年各区域销售额");
chartConfig.setXAxisLabel("区域");
chartConfig.setYAxisLabel("销售额（万元）");
chartConfig.setWidth(600);
chartConfig.setHeight(400);

// 图表数据
ChartData chartData = new ChartData();
chartData.setConfig(chartConfig);

// X轴标签（中文）
chartData.setLabels(java.util.Arrays.asList(
    "华东地区", "华南地区", "华北地区", "西南地区"
));

// Y轴数据
java.util.Map<String, java.util.List<Double>> datasets = new java.util.HashMap<>();
datasets.put("销售额", java.util.Arrays.asList(5230.0, 4150.0, 3680.0, 1800.0));
chartData.setDatasets(datasets);

// 添加到Section
Section section = new Section("销售数据图表");
section.addChart(chartData);
```

**注意：图表中的中文标签会自动使用配置的字体，无需额外配置。**

---

## 故障排除 Troubleshooting

### 问题1：PDF中文仍然显示为方框

**解决方案：**

1. 运行诊断工具检查问题：
   ```bash
   mvn compile exec:java -Dexec.mainClass="com.mercury.pdf.render.FontConfigDiagnostic"
   ```

2. 确认字体文件存在于 `src/main/resources/fonts/` 目录

3. 确认路径使用 `classpath:/fonts/xxx.ttf`（注意 `/`）

4. 确认font-family名称与字体内部名称完全一致

### 问题2：Spring Boot项目中配置无效

**原因：** 可能使用了 `@PostConstruct` 覆盖了YAML配置

**解决方案：** 删除 `@PostConstruct` 方法，直接使用构造函数注入

### 问题3：JAR打包后无法显示中文

**原因：** 字体文件没有包含在JAR中

**解决方案：** 
1. 确认字体文件在 `src/main/resources/fonts/` 目录
2. 使用 `classpath:` 前缀路径
3. 库会自动提取字体到临时文件

### 问题4：如何验证字体是否正确嵌入？

**检查方法：**

1. 查看PDF文件大小：
   - 无字体：约2-5KB
   - 有字体：约50-200KB（取决于字体文件大小）

2. 打开PDF检查属性：
   - 文件 → 属性 → 字体
   - 应该看到 "HarmonyOS Sans SC (Embedded Subset)"

3. 查看生成日志：
   ```
   ✓ Font registered with Flying Saucer: /tmp/pdf-render-font-xxx.ttf
     Encoding: Identity-H | Embedded: true
     Font family name (for CSS): HarmonyOS Sans SC
   ✓ Total fonts registered for PDF: 1
   ```

---

## 更多示例

查看项目中的完整示例：

1. **ChineseFontConfigurationDemo.java** - 对比演示（有字体 vs 无字体）
2. **ChineseFontTest.java** - 单元测试示例
3. **PdfChineseVerificationTest.java** - 完整验证测试

运行演示：

```bash
# 运行对比演示
mvn exec:java -Dexec.mainClass="com.mercury.pdf.render.ChineseFontConfigurationDemo"

# 运行测试
mvn test -Dtest=ChineseFontTest
mvn test -Dtest=PdfChineseVerificationTest
```

---

## 相关文档

- [中文快速指南](CHINESE_QUICKSTART.md)
- [字体配置详细文档](FONT_CONFIGURATION.md)
- [Spring Boot集成指南](SPRING_BOOT_INTEGRATION_GUIDE.md)
- [常见问题](TROUBLESHOOTING.md)
