# 完整示例应用 / Complete Example Application

本目录包含一个完整的 Spring Boot 应用示例，展示如何正确使用 `pdf-render` 库。

This directory contains a complete Spring Boot application example showing correct usage of the `pdf-render` library.

## 目录结构 / Directory Structure

```
example-spring-boot-app/
├── pom.xml                          # Maven 配置
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/pdf/
│       │       ├── Application.java           # Spring Boot 主类
│       │       ├── controller/
│       │       │   └── PdfController.java     # REST API 控制器
│       │       └── service/
│       │           └── PdfService.java        # PDF 生成服务
│       └── resources/
│           ├── application.yml                # 配置文件 (重要!)
│           └── fonts/
│               └── HarmonyOS_Sans_SC_Regular.ttf  # 中文字体文件
```

## 关键点 / Key Points

### ✅ 正确的做法 (Correct Way)

1. **不使用 @PostConstruct** - 完全依赖 Spring Boot 自动配置
2. **通过构造函数注入 ReportService** - 使用 Spring 管理的 bean
3. **所有配置在 application.yml** - 集中管理，易于维护

### ❌ 错误的做法 (Wrong Way)

1. **使用 @PostConstruct 手动配置** - 覆盖 application.yml
2. **手动创建 `new ReportService()`** - 跳过 Spring 配置
3. **在代码中硬编码配置** - 难以维护和修改

## 完整代码 / Complete Code

### pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>pdf-example-app</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.18</version>
    </parent>

    <properties>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Web Starter -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- PDF Render Library -->
        <dependency>
            <groupId>com.mercury</groupId>
            <artifactId>pdf-render</artifactId>
            <version>1.0.1</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### application.yml

```yaml
# Spring Boot 配置
server:
  port: 8080

# PDF Render 配置 - 所有配置都在这里！
pdf-render:
  # 模板配置
  template:
    default-name: report
    cache-enabled: true   # 生产环境启用缓存以提高性能

  # 字体配置 - 必须配置才能显示中文！
  fonts:
    # 字体文件路径 (注意：必须以 / 开头)
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    # 字体family名称
    default-family: HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif
    cjk-family: HarmonyOS Sans SC, Noto Sans CJK, SimSun, sans-serif

  # 调试配置 (开发时很有用)
  debug:
    enabled: false        # 生产环境设为 false
    output-directory: debug-html
    include-timestamp: false

---
# 开发环境配置
spring:
  config:
    activate:
      on-profile: dev

pdf-render:
  template:
    cache-enabled: false  # 开发时禁用缓存，便于实时查看模板修改
  debug:
    enabled: true         # 开发时启用调试
    include-timestamp: true

---
# 生产环境配置
spring:
  config:
    activate:
      on-profile: prod

pdf-render:
  template:
    cache-enabled: true   # 生产环境启用缓存
  debug:
    enabled: false        # 生产环境禁用调试
```

### Application.java

```java
package com.example.pdf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 应用主类
 * 
 * 重要：不需要任何额外配置！
 * pdf-render 的自动配置会自动生效。
 */
@SpringBootApplication
public class Application {
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### PdfService.java

```java
package com.example.pdf.service;

import com.lowagie.text.DocumentException;
import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.model.ReportData;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * PDF 生成服务
 * 
 * 关键点：
 * 1. 使用构造函数注入 ReportService
 * 2. 不使用 @PostConstruct
 * 3. 不手动配置字体
 * 
 * Spring Boot 的自动配置会处理一切！
 */
@Service
public class PdfService {
    
    private final ReportService reportService;
    
    /**
     * 构造函数注入 - Spring Boot 会自动注入配置好的 ReportService
     * 
     * 这个 ReportService bean 已经包含了：
     * - application.yml 中配置的字体
     * - application.yml 中配置的调试设置
     * - application.yml 中配置的模板设置
     */
    public PdfService(ReportService reportService) {
        this.reportService = reportService;
        
        // 可选：打印配置信息用于验证
        System.out.println("===========================================");
        System.out.println("PdfService initialized with configuration:");
        System.out.println("  Font properties: " + 
            reportService.getHtmlRenderer().getFontProperties());
        System.out.println("  Debug enabled: " + 
            reportService.getHtmlRenderer().isDebugHtmlEnabled());
        System.out.println("  Default template: " + 
            reportService.getHtmlRenderer().getDefaultTemplateName());
        System.out.println("===========================================");
    }
    
    /**
     * 生成 PDF 报告
     * 
     * 所有配置已自动应用：
     * - 中文字体会正确渲染
     * - Debug HTML 会自动生成(如果启用)
     * - 使用默认模板(如果未指定)
     */
    public byte[] generatePdf(ReportData data) throws IOException, DocumentException {
        return reportService.generatePdf(data);
    }
    
    /**
     * 使用指定模板生成 PDF
     */
    public byte[] generatePdf(ReportData data, String templateName) 
            throws IOException, DocumentException {
        return reportService.generatePdf(data, templateName);
    }
}
```

### PdfController.java

```java
package com.example.pdf.controller;

import com.example.pdf.service.PdfService;
import com.mercury.pdf.render.model.ReportData;
import com.mercury.pdf.render.model.ReportDataBuilder;
import com.mercury.pdf.render.model.Section;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PDF 生成 REST API 控制器
 */
@RestController
@RequestMapping("/api/pdf")
public class PdfController {
    
    private final PdfService pdfService;
    
    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }
    
    /**
     * 生成测试报告 - 验证中文字体配置
     * 
     * GET http://localhost:8080/api/pdf/test
     */
    @GetMapping("/test")
    public ResponseEntity<byte[]> generateTestReport() throws Exception {
        // 创建包含中文的报告数据
        ReportData data = ReportDataBuilder.create()
            .title("测试报告")
            .subtitle("验证 Spring Boot 配置是否生效")
            .reportDate("2024-12-31")
            .reportNumber("TEST-001")
            .addSection(new Section("第一章：配置验证")
                .addParagraph("这是一段中文文本，用于验证字体配置。")
                .addParagraph("如果您看到方框（□），说明配置未生效。")
                .addParagraph("如果您看到正确的中文，说明配置成功！"))
            .addSection(new Section("第二章：重要提示")
                .addParagraph("1. 确保 application.yml 在 src/main/resources/ 目录")
                .addParagraph("2. 确保字体文件在 src/main/resources/fonts/ 目录")
                .addParagraph("3. 不要使用 @PostConstruct 手动配置")
                .addParagraph("4. 通过构造函数注入 ReportService"))
            .reportNotice("本报告用于验证 Spring Boot 自动配置")
            .build();
        
        // 生成 PDF
        byte[] pdfBytes = pdfService.generatePdf(data);
        
        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "test-report.pdf");
        headers.setContentLength(pdfBytes.length);
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(pdfBytes);
    }
    
    /**
     * 生成简单报告
     * 
     * GET http://localhost:8080/api/pdf/simple
     */
    @GetMapping("/simple")
    public ResponseEntity<byte[]> generateSimpleReport() throws Exception {
        ReportData data = ReportDataBuilder.create()
            .title("简单报告")
            .addSection(new Section("概述")
                .addParagraph("这是一个简单的报告示例。"))
            .build();
        
        byte[] pdfBytes = pdfService.generatePdf(data);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "simple-report.pdf");
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(pdfBytes);
    }
}
```

## 运行应用 / Running the Application

### 1. 准备字体文件

将 `HarmonyOS_Sans_SC_Regular.ttf` 复制到 `src/main/resources/fonts/` 目录。

字体下载：
- HarmonyOS Sans: https://developer.harmonyos.com/cn/design/resource
- Noto Sans CJK: https://github.com/googlefonts/noto-cjk

### 2. 编译并运行

```bash
# 编译
mvn clean package

# 运行 (开发环境)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 或者直接运行 JAR
java -jar target/pdf-example-app-1.0.0.jar
```

### 3. 测试 API

```bash
# 生成测试报告
curl http://localhost:8080/api/pdf/test --output test.pdf

# 生成简单报告
curl http://localhost:8080/api/pdf/simple --output simple.pdf
```

### 4. 验证结果

1. 打开 `test.pdf` - 检查中文是否正确显示
2. 如果启用了 debug 模式，检查 `debug-html/report.html`
3. 查看控制台日志，确认配置已加载

## 启动日志示例 / Startup Log Example

正确配置后，您应该看到类似的日志：

```
2024-12-31 12:00:00.000  INFO --- [main] com.example.pdf.Application : Starting Application
2024-12-31 12:00:01.000  INFO --- [main] c.m.p.r.c.PdfRenderAutoConfiguration : PDF Render: Font configuration applied from properties
2024-12-31 12:00:01.000  INFO --- [main] c.m.p.r.c.PdfRenderAutoConfiguration :   - Regular font: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
2024-12-31 12:00:01.000  INFO --- [main] c.m.p.r.c.PdfRenderAutoConfiguration : PDF Render: Debug HTML output enabled
2024-12-31 12:00:01.000  INFO --- [main] c.m.p.r.c.PdfRenderAutoConfiguration :   - Output directory: debug-html
===========================================
PdfService initialized with configuration:
  Font properties: FontProperties{regularPath='classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf', ...}
  Debug enabled: true
  Default template: report
===========================================
2024-12-31 12:00:02.000  INFO --- [main] com.example.pdf.Application : Started Application in 2.5 seconds
```

## 常见问题 / Troubleshooting

### 问题：中文显示为方框

**原因**：字体配置未生效

**解决方案**：
1. 检查 `application.yml` 是否在 `src/main/resources/` 目录
2. 检查字体文件是否在 `src/main/resources/fonts/` 目录  
3. 检查是否使用了 `@PostConstruct` 覆盖配置
4. 检查是否手动创建了 `new ReportService()`

### 问题：Debug HTML 没有生成

**原因**：debug 配置未生效或路径权限问题

**解决方案**：
1. 确认 `pdf-render.debug.enabled: true`
2. 检查输出目录是否有写入权限
3. 确认使用的是注入的 `ReportService` bean

### 问题：配置不生效

**原因**：YAML 格式错误或 Spring Profile 问题

**解决方案**：
1. 检查 YAML 缩进（使用空格，不要用 Tab）
2. 确认 Spring Profile 设置正确
3. 启用 debug 日志查看配置加载情况：`logging.level.com.mercury=DEBUG`

## 总结 / Summary

这个示例展示了：
1. ✅ 使用 Spring Boot 自动配置 - 不需要 @PostConstruct
2. ✅ 所有配置在 application.yml - 集中管理
3. ✅ 构造函数注入 - 使用 Spring 管理的 bean
4. ✅ 中文字体正确渲染 - 通过配置文件设置
5. ✅ Debug HTML 自动生成 - 通过配置文件设置

**记住：properties 的意义就是避免在代码中硬编码配置！**
