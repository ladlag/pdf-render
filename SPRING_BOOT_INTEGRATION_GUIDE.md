# Spring Boot 集成指南 - PDF渲染库

本指南提供完整的Spring Boot集成说明，确保以最优方式将pdf-render库集成到Spring Boot JDK 8项目中。

## 目录
1. [环境要求](#环境要求)
2. [Maven依赖配置](#maven依赖配置)
3. [配置文件设置](#配置文件设置)
4. [完整集成示例](#完整集成示例)
5. [资源路径配置](#资源路径配置)
6. [最佳实践](#最佳实践)
7. [常见问题](#常见问题)

---

## 环境要求

- **JDK**: 1.8 或更高版本
- **Spring Boot**: 2.x (推荐 2.7.18，与JDK 8完全兼容)
- **Maven**: 3.6+ 或 Gradle 6.x+

## Maven依赖配置

### 步骤1: 安装PDF渲染库到本地Maven仓库

```bash
# 克隆或下载pdf-render项目
cd pdf-render

# 构建并安装到本地Maven仓库
mvn clean install
```

### 步骤2: 在Spring Boot项目中添加依赖

在您的Spring Boot项目的 `pom.xml` 中添加：

```xml
<dependencies>
    <!-- PDF渲染库 -->
    <dependency>
        <groupId>com.mercury</groupId>
        <artifactId>pdf-render</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
    
    <!-- Spring Boot Web (如果需要REST API) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Boot Configuration Processor (可选，用于IDE自动补全) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-configuration-processor</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

---

## 配置文件设置

### application.yml 完整配置示例

在 `src/main/resources/application.yml` 中添加以下配置：

```yaml
# PDF渲染库配置
pdf-render:
  # 模板配置
  template:
    # 模板文件位置（默认: classpath:/templates/）
    location: classpath:/templates/
    # 默认模板名称，不含.html扩展名（默认: report）
    default-name: matcher-report-final
    # 是否启用模板缓存以提高性能（默认: true）
    # 开发环境建议设为false，生产环境设为true
    cache-enabled: true
  
  # 输出配置（可选）
  output:
    # PDF文件输出目录（可选）
    directory: ${user.home}/pdf-output
    # 是否自动保存PDF到输出目录（默认: false）
    save-to-directory: false
  
  # 中文字体配置（重要！显示中文必须配置）
  fonts:
    # 常规字体文件路径
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    # 粗体字体文件路径（可选）
    bold-path: classpath:/fonts/HarmonyOS_Sans_SC_Bold.ttf
    # 默认字体族
    default-family: HarmonyOS Sans SC, Noto Sans CJK SC, DejaVu Sans, Arial, sans-serif
  
  # 调试配置（可选）
  debug:
    # 是否启用HTML调试输出
    enabled: false
    # HTML调试文件输出目录
    output-directory: debug-html
    # 是否在文件名中包含时间戳
    include-timestamp: false

---
# 开发环境配置
spring:
  config:
    activate:
      on-profile: dev

pdf-render:
  template:
    cache-enabled: false  # 开发时禁用缓存，便于热加载
  debug:
    enabled: true         # 开发时启用调试输出
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

### application.properties 配置示例（可选）

如果您使用 `application.properties`，配置如下：

```properties
# PDF渲染库配置
pdf-render.template.location=classpath:/templates/
pdf-render.template.default-name=matcher-report-final
pdf-render.template.cache-enabled=true

# 中文字体配置
pdf-render.fonts.regular-path=classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
pdf-render.fonts.default-family=HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif

# 调试配置
pdf-render.debug.enabled=false
pdf-render.debug.output-directory=debug-html
```

---

## 完整集成示例

### 项目结构

```
my-spring-boot-app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── myapp/
│   │   │               ├── MyApplication.java
│   │   │               ├── controller/
│   │   │               │   └── ReportController.java
│   │   │               ├── service/
│   │   │               │   └── PdfReportService.java
│   │   │               └── config/
│   │   │                   └── PdfRenderConfig.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── templates/
│   │       │   └── matcher-report-final.html  (从pdf-render复制)
│   │       └── fonts/
│   │           └── HarmonyOS_Sans_SC_Regular.ttf
│   └── test/
└── pom.xml
```

### 1. Spring Boot主应用类

```java
package com.example.myapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.mercury.pdf.render.config.PdfRenderProperties;

@SpringBootApplication
@EnableConfigurationProperties(PdfRenderProperties.class)
public class MyApplication {
   public static void main(String[] args) {
      SpringApplication.run(MyApplication.class, args);
   }
}
```

### 2. PDF渲染配置类

```java
package com.example.myapp.config;

import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.config.FontConfig;
import com.mercury.pdf.render.config.PdfRenderProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PdfRenderConfig {

    private final PdfRenderProperties properties;

    public PdfRenderConfig(PdfRenderProperties properties) {
        this.properties = properties;
    }

    /**
     * 创建ReportService Bean
     * 配置模板、字体等设置
     */
    @Bean
    public ReportService reportService() {
        ReportService service = new ReportService();

        // 启用HTML渲染管道（推荐）
        service.setUseHtmlPipeline(true);

        // 配置模板设置
        service.getHtmlRenderer().setDefaultTemplateName(
                properties.getTemplate().getDefaultName()
        );
        service.getHtmlRenderer().setCacheTemplates(
                properties.getTemplate().isCacheEnabled()
        );

        // 配置字体（用于中文显示）
        if (properties.getFonts().getRegularPath() != null) {
            FontConfig fontConfig = new FontConfig();
            fontConfig.setRegularFontPath(properties.getFonts().getRegularPath());
            fontConfig.setBoldFontPath(properties.getFonts().getBoldPath());
            fontConfig.setDefaultFontFamily(properties.getFonts().getDefaultFamily());
            service.getHtmlRenderer().setFontConfig(fontConfig);
        }

        // 配置调试模式
        if (properties.getDebug().isEnabled()) {
            service.getHtmlRenderer().setDebugHtmlEnabled(true);
            service.getHtmlRenderer().setDebugHtmlOutputDirectory(
                    properties.getDebug().getOutputDirectory()
            );
        }

        return service;
    }
}
```

### 3. PDF报告服务类

```java
package com.example.myapp.service;

import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.config.PdfRenderProperties;
import com.mercury.pdf.render.model.ReportData;
import com.mercury.pdf.render.model.ReportDataBuilder;
import com.mercury.pdf.render.model.Section;
import com.mercury.pdf.render.model.TableData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class PdfReportService {

   private static final Logger log = LoggerFactory.getLogger(PdfReportService.class);

   private final ReportService reportService;
   private final PdfRenderProperties properties;

   public PdfReportService(ReportService reportService, PdfRenderProperties properties) {
      this.reportService = reportService;
      this.properties = properties;
   }

   /**
    * 生成需求预审报告PDF
    *
    * @param reportData 报告数据
    * @return PDF字节数组
    * @throws IOException 如果生成失败
    */
   public byte[] generateMatcherReport(ReportData reportData) throws IOException {
      log.info("开始生成需求预审报告PDF，标题: {}", reportData.getTitle());

      // 使用matcher-report-final模板生成PDF
      byte[] pdfBytes = reportService.generatePdf(reportData, "matcher-report-final");

      // 如果配置了自动保存，则保存到文件
      if (properties.getOutput().isSaveToDirectory()
              && properties.getOutput().getDirectory() != null) {
         savePdfToFile(pdfBytes, reportData.getTitle());
      }

      log.info("需求预审报告PDF生成成功，大小: {} KB", pdfBytes.length / 1024);
      return pdfBytes;
   }

   /**
    * 生成通用报告（使用默认模板）
    *
    * @param reportData 报告数据
    * @return PDF字节数组
    * @throws IOException 如果生成失败
    */
   public byte[] generateReport(ReportData reportData) throws IOException {
      log.info("开始生成报告PDF，标题: {}", reportData.getTitle());
      byte[] pdfBytes = reportService.generatePdf(reportData);
      log.info("报告PDF生成成功");
      return pdfBytes;
   }

   /**
    * 生成自定义模板报告
    *
    * @param reportData 报告数据
    * @param templateName 模板名称（不含.html扩展名）
    * @return PDF字节数组
    * @throws IOException 如果生成失败
    */
   public byte[] generateCustomReport(ReportData reportData, String templateName) throws IOException {
      log.info("使用模板 {} 生成报告PDF", templateName);
      return reportService.generatePdf(reportData, templateName);
   }

   /**
    * 创建需求预审报告示例数据
    *
    * @return 报告数据
    */
   public ReportData createSampleMatcherReportData() {
      ReportDataBuilder builder = ReportDataBuilder.create()
              .title("需求预审报告")
              .reportDate("2024-12-31")
              .reportNumber("AI-PRE-2024-001");

      // 第一章节：匹配结果
      Section section11 = new Section("1.1 精确匹配通过（匹配度≥0.90）");
      List<String> headers = Arrays.asList("需求编号", "需求名称", "匹配的Word功能", "匹配度", "匹配说明");
      List<List<String>> rows = Arrays.asList(
              Arrays.asList("C001", "用户注册功能", "3.5.1-用户注册功能", "1.00", "需求名称、描述完全一致"),
              Arrays.asList("C002", "用户登录功能", "3.5.2-用户登录功能", "0.98", "名称一致，含密码和第三方登录")
      );
      section11.addTable(new TableData(headers, rows));
      builder.addSection(section11);

      // 第二章节：详细分析
      Section section21 = new Section("2.1 分模块匹配表现");
      section21.addParagraph("按业务模块拆分匹配结果，用户管理、客户管理模块匹配表现优异。");
      builder.addSection(section21);

      // 第三章节：预审结果总结
      builder.summaryTable(new TableData(
              Arrays.asList("匹配状态", "数量（条）", "占比"),
              Arrays.asList(
                      Arrays.asList("精确匹配通过", "10", "50.0%"),
                      Arrays.asList("语义匹配通过", "6", "30.0%")
              )
      ));

      return builder.build();
   }

   /**
    * 保存PDF到文件系统
    */
   private void savePdfToFile(byte[] pdfBytes, String title) throws IOException {
      String outputDir = properties.getOutput().getDirectory();
      Path dirPath = Paths.get(outputDir);

      // 创建目录（如果不存在）
      if (!Files.exists(dirPath)) {
         Files.createDirectories(dirPath);
      }

      // 生成文件名
      String fileName = title.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5-]", "_")
              + "_" + System.currentTimeMillis() + ".pdf";
      Path filePath = dirPath.resolve(fileName);

      // 保存文件
      try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
         fos.write(pdfBytes);
      }

      log.info("PDF已保存到: {}", filePath.toAbsolutePath());
   }
}
```

### 4. REST Controller

```java
package com.example.myapp.controller;

import com.example.myapp.service.PdfReportService;
import model.com.mercury.pdf.render.ReportData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * PDF报告生成REST API
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

   private static final Logger log = LoggerFactory.getLogger(ReportController.class);

   private final PdfReportService pdfReportService;

   public ReportController(PdfReportService pdfReportService) {
      this.pdfReportService = pdfReportService;
   }

   /**
    * 生成需求预审报告PDF
    *
    * POST /api/reports/matcher
    * Content-Type: application/json
    *
    * @param reportData 报告数据（JSON格式）
    * @return PDF文件
    */
   @PostMapping(value = "/matcher", produces = MediaType.APPLICATION_PDF_VALUE)
   public ResponseEntity<byte[]> generateMatcherReport(@RequestBody ReportData reportData) {
      log.info("收到生成需求预审报告请求");

      try {
         byte[] pdfBytes = pdfReportService.generateMatcherReport(reportData);

         // 设置响应头
         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_PDF);

         // 文件名支持中文
         String filename = reportData.getTitle() != null
                 ? reportData.getTitle() + ".pdf"
                 : "需求预审报告.pdf";

         // 使用RFC 5987编码文件名以支持中文
         String encodedFilename = new String(filename.getBytes(StandardCharsets.UTF_8),
                 StandardCharsets.ISO_8859_1);
         headers.setContentDispositionFormData("attachment", encodedFilename);
         headers.setContentLength(pdfBytes.length);

         return ResponseEntity.ok()
                 .headers(headers)
                 .body(pdfBytes);

      } catch (IOException e) {
         log.error("生成PDF失败", e);
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                 .body(null);
      }
   }

   /**
    * 生成示例需求预审报告（用于测试）
    *
    * GET /api/reports/matcher/sample
    *
    * @return PDF文件
    */
   @GetMapping(value = "/matcher/sample", produces = MediaType.APPLICATION_PDF_VALUE)
   public ResponseEntity<byte[]> generateSampleMatcherReport() {
      log.info("生成示例需求预审报告");

      try {
         ReportData sampleData = pdfReportService.createSampleMatcherReportData();
         byte[] pdfBytes = pdfReportService.generateMatcherReport(sampleData);

         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_PDF);
         headers.setContentDispositionFormData("attachment", "sample_matcher_report.pdf");

         return ResponseEntity.ok()
                 .headers(headers)
                 .body(pdfBytes);

      } catch (IOException e) {
         log.error("生成示例PDF失败", e);
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                 .body(null);
      }
   }

   /**
    * 生成通用报告
    *
    * POST /api/reports/generate
    *
    * @param reportData 报告数据
    * @param templateName 模板名称（可选）
    * @return PDF文件
    */
   @PostMapping(value = "/generate", produces = MediaType.APPLICATION_PDF_VALUE)
   public ResponseEntity<byte[]> generateReport(
           @RequestBody ReportData reportData,
           @RequestParam(required = false) String templateName) {

      log.info("生成报告，模板: {}", templateName);

      try {
         byte[] pdfBytes;
         if (templateName != null && !templateName.isEmpty()) {
            pdfBytes = pdfReportService.generateCustomReport(reportData, templateName);
         } else {
            pdfBytes = pdfReportService.generateReport(reportData);
         }

         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_PDF);
         headers.setContentDispositionFormData("attachment", "report.pdf");

         return ResponseEntity.ok()
                 .headers(headers)
                 .body(pdfBytes);

      } catch (IOException e) {
         log.error("生成PDF失败", e);
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                 .body(null);
      }
   }
}
```

---

## 资源路径配置

### 模板文件

模板文件应放置在：

```
src/main/resources/templates/
├── matcher-report-final.html   # 需求预审报告模板
├── report.html                  # 默认通用报告模板
└── custom-template.html         # 其他自定义模板
```

### 字体文件

中文字体文件应放置在：

```
src/main/resources/fonts/
├── HarmonyOS_Sans_SC_Regular.ttf
├── HarmonyOS_Sans_SC_Bold.ttf
└── NotoSansCJKsc-Regular.otf
```

**字体文件获取：**
- HarmonyOS Sans SC: [华为开发者网站](https://developer.harmonyos.com/cn/design/resource)
- Noto Sans CJK: [Google Fonts GitHub](https://github.com/googlefonts/noto-cjk)

### Classpath资源加载

在配置文件中使用 `classpath:` 前缀引用资源：

```yaml
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    # 或使用绝对路径
    # regular-path: /opt/fonts/HarmonyOS_Sans_SC_Regular.ttf
```

---

## 最佳实践

### 1. 字体配置

**必须配置中文字体以正确显示中文：**

```java
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, sans-serif");
```

### 2. 模板缓存

- **开发环境**：禁用缓存（`cache-enabled: false`）以便实时查看修改
- **生产环境**：启用缓存（`cache-enabled: true`）以提高性能

### 3. 异步生成

对于大型报告，建议使用异步方式生成：

```java
@Service
public class AsyncPdfReportService {
    
    @Async
    public CompletableFuture<byte[]> generateReportAsync(ReportData data) {
        try {
            byte[] pdf = reportService.generatePdf(data);
            return CompletableFuture.completedFuture(pdf);
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
```

### 4. 错误处理

使用 `@ControllerAdvice` 统一处理异常：

```java
@ControllerAdvice
public class ReportExceptionHandler {
    
    @ExceptionHandler(IOException.class)
    public ResponseEntity<String> handleIOException(IOException ex) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("PDF生成失败: " + ex.getMessage());
    }
}
```

### 5. 日志配置

在 `application.yml` 中配置日志级别：

```yaml
logging:
  level:
    com.mercury.pdf.render: DEBUG
    com.example.myapp: INFO
```

### 6. 性能优化

- 启用模板缓存
- 复用 `ReportService` 实例（Spring Bean单例）
- 对于频繁生成的报告，考虑添加Redis缓存

---

## 常见问题

### Q1: 中文显示为方框（□）？

**解决方案：**
1. 确保配置了中文字体：
   ```yaml
   pdf-render:
     fonts:
       regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
       default-family: HarmonyOS Sans SC, DejaVu Sans, sans-serif
   ```
2. 确保字体文件存在于 `src/main/resources/fonts/` 目录
3. 检查字体文件路径是否正确

### Q2: 找不到模板文件？

**解决方案：**
1. 确认模板文件在 `src/main/resources/templates/` 目录
2. 检查配置：
   ```yaml
   pdf-render:
     template:
       location: classpath:/templates/
   ```
3. 确认文件名正确（不含.html扩展名）

### Q3: Spring Boot启动失败？

**解决方案：**
1. 确保启用了 `@EnableConfigurationProperties`：
   ```java
   @EnableConfigurationProperties(PdfRenderProperties.class)
   ```
2. 检查依赖版本兼容性

### Q4: 生成的PDF文件过大？

**优化方案：**
1. 压缩图表图片
2. 使用更小的字体文件
3. 减少不必要的CSS样式

### Q5: 如何在Docker中运行？

**Dockerfile 示例：**

```dockerfile
FROM openjdk:8-jdk-alpine

# 安装字体
RUN apk add --no-cache fontconfig ttf-dejavu

# 复制应用
COPY target/my-app.jar /app.jar
COPY src/main/resources/fonts /fonts

ENTRYPOINT ["java", "-jar", "/app.jar"]
```

---

## 测试示例

### 单元测试

```java
@SpringBootTest
public class PdfReportServiceTest {
    
    @Autowired
    private PdfReportService pdfReportService;
    
    @Test
    public void testGenerateMatcherReport() throws IOException {
        ReportData data = pdfReportService.createSampleMatcherReportData();
        byte[] pdf = pdfReportService.generateMatcherReport(data);
        
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }
}
```

### 集成测试

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ReportControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    public void testGenerateSampleReport() throws Exception {
        mockMvc.perform(get("/api/reports/matcher/sample"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_PDF))
            .andExpect(header().exists("Content-Disposition"));
    }
}
```

---

## 部署建议

### 开发环境

```yaml
# application-dev.yml
pdf-render:
  template:
    cache-enabled: false
  debug:
    enabled: true
    output-directory: target/debug-html
```

### 生产环境

```yaml
# application-prod.yml
pdf-render:
  template:
    cache-enabled: true
  output:
    directory: /var/pdfs
    save-to-directory: true
  debug:
    enabled: false
```

---

## 进阶配置

### 多租户支持

```java
@Service
public class MultiTenantPdfService {
    
    public byte[] generateReport(ReportData data, String tenantId) {
        // 根据租户ID选择不同的模板或配置
        String template = "matcher-report-" + tenantId;
        return reportService.generatePdf(data, template);
    }
}
```

### 国际化支持

```java
public byte[] generateLocalizedReport(ReportData data, Locale locale) {
    // 根据语言选择不同的模板
    String template = locale.equals(Locale.CHINA) 
        ? "matcher-report-final-zh" 
        : "matcher-report-final-en";
    return reportService.generatePdf(data, template);
}
```

---

## 总结

本指南涵盖了将pdf-render库集成到Spring Boot JDK 8项目的完整流程：

1. ✅ Maven依赖配置
2. ✅ 配置文件设置（application.yml）
3. ✅ 完整的代码示例（Config、Service、Controller）
4. ✅ 资源路径配置
5. ✅ 最佳实践建议
6. ✅ 常见问题解决方案

按照本指南，您可以快速、可靠地将PDF生成功能集成到您的Spring Boot应用中。

如有问题，请参考项目README或提交Issue。
