# PDF中文显示问题完整解决方案 / Complete Solution for Chinese Character Display

## 问题演进 / Problem Evolution

用户遇到的问题经历了以下阶段：

### 阶段1：中文显示为方框
**问题：** 生成的PDF无法显示中文字符（显示为 □）

**可能原因：**
- FontConfig未配置
- 字体路径错误
- defaultFontFamily未设置

### 阶段2：没有看到字体注册日志
**问题：** 没有看到 "Font registered with Flying Saucer" 的日志

**原因：**
- 忘记调用 setFontConfig()
- Spring Boot使用了@PostConstruct覆盖配置
- 配置在生成PDF之后才设置

### 阶段3：看到日志但字体文件大小异常
**问题：** 看到了字体注册日志，TTF文件是7.8M，担心加载了错误的字体

**真相：** HarmonyOS_Sans_SC_Regular.ttf 就是 7.88 MB！这是正常的完整版字体。

---

## 快速诊断流程 / Quick Diagnostic Process

### 步骤1：运行字体验证工具

```bash
mvn compile exec:java -Dexec.mainClass="com.mercury.pdf.render.FontFileVerifier"
```

这将显示：
- ✓ 项目中有哪些字体
- ✓ 每个字体的大小和内部名称
- ✓ 推荐的配置

**示例输出：**
```
字体 Font: HarmonyOS_Sans_SC_Regular.ttf
✓ 字体文件存在 Font file exists
  文件大小 File size: 7.88 MB
  字体内部名称 Internal name: HarmonyOS Sans SC

推荐配置 Recommended Configuration:
  regularFontPath: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
  defaultFontFamily: HarmonyOS Sans SC, sans-serif
```

### 步骤2：使用推荐配置

根据步骤1的输出，配置你的代码：

**纯Java项目：**
```java
ReportService service = new ReportService();

FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, sans-serif");

service.getHtmlRenderer().setFontConfig(fontConfig);

byte[] pdf = service.generatePdf(data);
```

**Spring Boot项目：**
```yaml
# application.yml
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    default-family: HarmonyOS Sans SC, sans-serif
```

```java
@Service
public class PdfService {
    private final ReportService reportService;
    
    // 构造函数注入 - 不要用@PostConstruct
    public PdfService(ReportService reportService) {
        this.reportService = reportService;
    }
    
    public byte[] generatePdf(ReportData data) throws Exception {
        return reportService.generatePdf(data);
    }
}
```

### 步骤3：运行最小化测试

```bash
mvn compile exec:java -Dexec.mainClass="com.mercury.pdf.render.MinimalFontTest"
```

这将：
- ✓ 验证字体配置是否正确
- ✓ 生成测试PDF
- ✓ 检查文件大小是否正常
- ✓ 显示是否成功

**期望输出：**
```
✓ FontConfig 已成功应用
✓ Font registered with Flying Saucer: /tmp/pdf-render-font-xxx.ttf
  Encoding: Identity-H | Embedded: true
  Font family name (for CSS): HarmonyOS Sans SC
✓ Total fonts registered for PDF: 1

✓✓✓ 成功！SUCCESS!
字体配置正确，中文应该能正常显示
```

---

## 常见问题诊断 / Common Issues Diagnostic

### 问题A：没有看到任何字体注册日志

**查看文档：** [NO_FONT_LOGS_TROUBLESHOOTING.md](NO_FONT_LOGS_TROUBLESHOOTING.md)

**快速检查：**
```java
// 检查1：是否调用了setFontConfig？
service.getHtmlRenderer().setFontConfig(fontConfig); // 必须调用！

// 检查2：配置是否为null？
System.out.println("Path: " + fontConfig.getRegularFontPath());
System.out.println("Family: " + fontConfig.getDefaultFontFamily());

// 检查3：是否在生成PDF之前配置？
service.getHtmlRenderer().setFontConfig(fontConfig); // 先配置
byte[] pdf = service.generatePdf(data);              // 再生成
```

### 问题B：看到日志但中文仍显示为方框

**查看文档：** [FONT_REGISTERED_BUT_BOXES.md](FONT_REGISTERED_BUT_BOXES.md)

**关键检查：** defaultFontFamily是否与字体内部名称匹配

```java
// 日志显示：
// Font family name (for CSS): HarmonyOS Sans SC

// 你的配置必须包含这个名称：
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, sans-serif");

// ❌ 错误示例：
fontConfig.setDefaultFontFamily("Arial, sans-serif"); // 不包含HarmonyOS Sans SC
```

### 问题C：字体文件大小看起来不对

**查看文档：** [FONT_SIZE_ISSUE.md](FONT_SIZE_ISSUE.md)

**正常大小范围：**
- HarmonyOS Sans SC: 2-8 MB（完整版是7.88 MB）
- Noto Sans CJK SC: 15-17 MB
- Source Han Sans: 15-20 MB

**关键：** 只要日志显示的字体内部名称与你配置的defaultFontFamily匹配，就是正确的！

---

## 字体选择建议 / Font Selection Guide

### HarmonyOS Sans SC（推荐用于简体中文）

**优点：**
- ✓ 现代、清晰的显示效果
- ✓ 文件大小适中（7.88 MB）
- ✓ 完整的简体中文字符支持
- ✓ 适合商务报告

**配置：**
```java
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, sans-serif");
```

### Noto Sans CJK SC（推荐用于多语言）

**优点：**
- ✓ 完整的CJK字符集（中日韩）
- ✓ Google开源，兼容性好
- ✓ 适合需要多语言支持的场景

**缺点：**
- ⚠️ 文件较大（15.68 MB）
- ⚠️ 生成的PDF也会较大

**配置：**
```java
fontConfig.setRegularFontPath("classpath:/fonts/NotoSansCJKsc-Regular.otf");
fontConfig.setDefaultFontFamily("Noto Sans CJK SC, sans-serif");
```

---

## 验证成功的标志 / Success Indicators

### 1. 日志输出正确

```
✓ Font registered with Flying Saucer: /tmp/pdf-render-font-xxx.ttf
  Encoding: Identity-H | Embedded: true
  Font family name (for CSS): HarmonyOS Sans SC
✓ Total fonts registered for PDF: 1
```

### 2. PDF文件大小正常

| 字体 | 无字体PDF | 有字体PDF | 增加量 |
|------|----------|-----------|--------|
| HarmonyOS | 2-5 KB | 50-120 KB | ~2-4 MB |
| Noto Sans CJK | 2-5 KB | 100-200 KB | ~5-8 MB |

### 3. 中文正常显示

打开PDF文件：
- ✓ 所有中文字符清晰显示
- ✓ 没有方框□
- ✓ 字体属性中显示嵌入的字体

---

## 诊断工具汇总 / Diagnostic Tools Summary

### 1. FontFileVerifier - 字体文件验证
```bash
mvn compile exec:java -Dexec.mainClass="com.mercury.pdf.render.FontFileVerifier"
```
**用途：** 查看所有可用字体及其内部名称

### 2. MinimalFontTest - 最小化测试
```bash
mvn compile exec:java -Dexec.mainClass="com.mercury.pdf.render.MinimalFontTest"
```
**用途：** 验证字体配置是否正确

### 3. FontConfigDiagnostic - 完整诊断
```bash
mvn compile exec:java -Dexec.mainClass="com.mercury.pdf.render.FontConfigDiagnostic"
```
**用途：** 生成对比PDF，完整诊断所有问题

### 4. ChineseFontConfigurationDemo - 对比演示
```bash
mvn exec:java -Dexec.mainClass="com.mercury.pdf.render.ChineseFontConfigurationDemo"
```
**用途：** 生成有字体和无字体的对比PDF

---

## 完整配置示例 / Complete Configuration Examples

### 纯Java项目完整示例

```java
package com.example;

import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.model.*;

import java.nio.file.Files;
import java.nio.file.Paths;

public class ChinesePdfGenerator {
    public static void main(String[] args) throws Exception {
        // 1. 创建服务
        ReportService service = new ReportService();
        
        // 2. 配置中文字体
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, sans-serif");
        
        // 3. 应用配置
        service.getHtmlRenderer().setFontConfig(fontConfig);
        
        // 4. 创建报告数据
        ReportData data = new ReportData();
        data.setTitle("中文报告");
        data.setSubtitle("Chinese Report");
        
        Section section = new Section("测试章节");
        section.addParagraph("这是中文测试内容。");
        data.getSections().add(section);
        
        // 5. 生成PDF
        byte[] pdf = service.generatePdf(data);
        
        // 6. 保存
        Files.write(Paths.get("chinese-report.pdf"), pdf);
        
        System.out.println("✓ PDF生成成功！");
        System.out.println("  文件大小: " + (pdf.length / 1024) + " KB");
    }
}
```

### Spring Boot项目完整示例

**pom.xml:**
```xml
<dependency>
    <groupId>com.mercury</groupId>
    <artifactId>pdf-render</artifactId>
    <version>1.0.1</version>
</dependency>
```

**application.yml:**
```yaml
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    default-family: HarmonyOS Sans SC, sans-serif
```

**PdfService.java:**
```java
package com.example.service;

import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.model.ReportData;
import org.springframework.stereotype.Service;

@Service
public class PdfService {
    private final ReportService reportService;
    
    public PdfService(ReportService reportService) {
        this.reportService = reportService;
    }
    
    public byte[] generatePdf(ReportData data) throws Exception {
        return reportService.generatePdf(data);
    }
}
```

**Controller.java:**
```java
package com.example.controller;

import com.example.service.PdfService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {
    private final PdfService pdfService;
    
    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }
    
    @GetMapping("/generate")
    public ResponseEntity<byte[]> generate() throws Exception {
        byte[] pdf = pdfService.generatePdf(createData());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "report.pdf");
        
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
```

---

## 相关文档 / Related Documentation

- **[CHINESE_QUICKSTART.md](CHINESE_QUICKSTART.md)** - 3步快速开始指南
- **[NO_FONT_LOGS_TROUBLESHOOTING.md](NO_FONT_LOGS_TROUBLESHOOTING.md)** - 没有日志的问题
- **[FONT_REGISTERED_BUT_BOXES.md](FONT_REGISTERED_BUT_BOXES.md)** - 有日志但仍显示方框
- **[FONT_SIZE_ISSUE.md](FONT_SIZE_ISSUE.md)** - 字体文件大小问题
- **[CHINESE_PDF_EXAMPLES.md](CHINESE_PDF_EXAMPLES.md)** - 完整代码示例
- **[FONT_CONFIGURATION.md](FONT_CONFIGURATION.md)** - 详细字体配置文档

---

## 总结 / Summary

### 核心要点

1. **必须同时配置两个参数：**
   - `regularFontPath`: 字体文件路径
   - `defaultFontFamily`: 字体内部名称（必须匹配！）

2. **HarmonyOS字体7.88 MB是正常的：**
   - 这是完整版，包含扩展字符集
   - 足够支持简体中文显示

3. **验证配置正确的方法：**
   - 运行 FontFileVerifier 获取正确的内部名称
   - 运行 MinimalFontTest 验证配置
   - 检查生成的PDF文件大小（应该>50KB）

### 最常见的错误

1. ❌ 只设置了 regularFontPath，没设置 defaultFontFamily
2. ❌ defaultFontFamily 名称与字体内部名称不匹配
3. ❌ Spring Boot 项目使用了 @PostConstruct
4. ❌ 忘记调用 setFontConfig()

### 正确的配置模板

```java
// 纯Java
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, sans-serif");
service.getHtmlRenderer().setFontConfig(fontConfig);
```

```yaml
# Spring Boot
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    default-family: HarmonyOS Sans SC, sans-serif
```

---

**如果按照本指南操作后仍有问题，请运行诊断工具并查看相关文档！**
