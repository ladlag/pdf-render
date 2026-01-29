# Spring Boot 自动配置说明 Auto-Configuration Guide

## ⚠️ 重要：Bean 冲突问题 Important: Bean Conflict Issue

### 问题 Problem

如果您按照旧版文档手动创建 `PdfRenderConfig` 配置类并定义 `@Bean ReportService`，会遇到以下错误：

If you manually create a `PdfRenderConfig` class with `@Bean ReportService` following old documentation, you'll encounter:

```
The bean 'reportService' could not be registered. A bean with that name 
has already been defined in class path resource 
[com/mercury/pdf/render/config/PdfRenderAutoConfiguration.class]
```

### 原因 Cause

pdf-render 库包含 **Spring Boot 自动配置** (`PdfRenderAutoConfiguration`)，会自动创建和配置 `ReportService` bean。

The pdf-render library includes **Spring Boot auto-configuration** (`PdfRenderAutoConfiguration`) that automatically creates and configures the `ReportService` bean.

---

## ✅ 解决方案 Solutions

### 方案 1: 使用自动配置（推荐）Recommended: Use Auto-Configuration

**最简单的方式！只需配置 YAML，无需创建配置类！**

**Simplest way! Just configure YAML, no configuration class needed!**

#### 步骤 1: 配置 application.yml

```yaml
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    default-family: HarmonyOS Sans SC, DejaVu Sans, sans-serif
  template:
    default-name: report
    cache-enabled: true
```

#### 步骤 2: 直接注入使用

```java
import com.mercury.pdf.render.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class MyPdfService {
    
    @Autowired
    private ReportService reportService;  // ✅ 自动配置，直接注入
    
    public byte[] generatePdf(ReportData data) throws IOException {
        return reportService.generatePdf(data);
    }
}
```

**完成！就这么简单！**

---

### 方案 2: 自定义配置（高级）Advanced: Custom Configuration

如果您需要完全自定义配置，可以禁用自动配置：

If you need full custom configuration, disable auto-configuration:

#### 选项 A: 排除自动配置类

```java
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import com.mercury.pdf.render.config.PdfRenderAutoConfiguration;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {PdfRenderAutoConfiguration.class})
public class MyApplication {
    // ...
}
```

#### 选项 B: 在 application.yml 中排除

```yaml
spring:
  autoconfigure:
    exclude:
      - com.mercury.pdf.render.config.PdfRenderAutoConfiguration
```

#### 然后创建自定义配置

```java
import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.config.FontConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyCustomPdfConfig {
    
    @Bean
    public ReportService reportService() {
        ReportService service = new ReportService();
        
        // 自定义配置 Custom configuration
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/MyFont.ttf");
        fontConfig.setDefaultFontFamily("My Custom Font, sans-serif");
        service.getHtmlRenderer().setFontConfig(fontConfig);
        
        return service;
    }
}
```

---

### 方案 3: 覆盖 Bean（使用 @Primary）Override with @Primary

如果您想保留自动配置但覆盖某些设置：

If you want to keep auto-configuration but override some settings:

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class PdfRenderCustomization {
    
    @Bean
    @Primary  // ✅ 标记为主要bean，优先使用
    public ReportService customReportService() {
        ReportService service = new ReportService();
        
        // 您的自定义配置 Your custom configuration
        // ...
        
        return service;
    }
}
```

---

## 📋 快速参考 Quick Reference

### ✅ 推荐做法 Recommended Approach

```yaml
# application.yml
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    default-family: HarmonyOS Sans SC, DejaVu Sans, sans-serif
```

```java
// Service 类
@Service
public class MyService {
    @Autowired
    private ReportService reportService;  // ✅ 直接注入
    
    public byte[] generate() throws IOException {
        return reportService.generatePdf(data);
    }
}
```

### ❌ 错误做法 Wrong Approach (会导致 bean 冲突)

```java
// ❌ 不要这样做！会与自动配置冲突
@Configuration
public class PdfRenderConfig {
    @Bean
    public ReportService reportService() {  // ❌ 冲突！
        return new ReportService();
    }
}
```

---

## 🔍 验证自动配置 Verify Auto-Configuration

### 检查 Bean 是否正确创建

```java
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class BeanChecker implements CommandLineRunner {
    
    @Autowired
    private ApplicationContext context;
    
    @Override
    public void run(String... args) {
        // 检查 ReportService bean
        if (context.containsBean("reportService")) {
            ReportService service = context.getBean(ReportService.class);
            System.out.println("✅ ReportService bean 已自动配置");
            System.out.println("   Bean class: " + service.getClass().getName());
        } else {
            System.out.println("❌ ReportService bean 未找到");
        }
    }
}
```

### 启动时查看日志

启用 Spring Boot 自动配置日志：

```yaml
logging:
  level:
    org.springframework.boot.autoconfigure: DEBUG
```

查找输出：
```
PdfRenderAutoConfiguration matched:
   - @ConditionalOnClass found required class 'com.mercury.pdf.render.ReportService'
```

---

## ❓ 常见问题 FAQ

### Q1: 我需要创建配置类吗？

**A: 不需要！** 自动配置会处理一切。只需配置 YAML 文件。

**A: No!** Auto-configuration handles everything. Just configure YAML.

### Q2: 如何知道自动配置是否生效？

**A:** 启动应用，如果没有 bean 冲突错误，说明自动配置正在工作。可以直接注入 `ReportService`。

**A:** Start your app. If there's no bean conflict error, auto-configuration is working. You can directly inject `ReportService`.

### Q3: 我已经创建了 PdfRenderConfig，怎么办？

**A:** 删除配置类，改用 YAML 配置。或者排除自动配置（参见方案 2）。

**A:** Delete the config class and use YAML. Or exclude auto-configuration (see Solution 2).

### Q4: YAML 配置不生效怎么办？

**A:** 确保：
1. 配置格式正确（注意缩进）
2. 字体文件存在于指定路径
3. 重启应用

运行验证脚本：
```bash
bash validate-chinese-fonts.sh
```

### Q5: 如何完全自定义配置？

**A:** 排除自动配置类，然后创建自己的配置（参见方案 2）。

---

## 📚 完整示例 Complete Example

### 项目结构

```
my-spring-boot-app/
├── src/main/
│   ├── java/
│   │   └── com/example/myapp/
│   │       ├── MyApplication.java
│   │       ├── controller/
│   │       │   └── PdfController.java
│   │       └── service/
│   │           └── ReportService.java
│   └── resources/
│       ├── application.yml
│       └── fonts/
│           └── HarmonyOS_Sans_SC_Regular.ttf
└── pom.xml
```

### MyApplication.java

```java
package com.example.myapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication  // ✅ 自动配置启用
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

### application.yml

```yaml
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    default-family: HarmonyOS Sans SC, DejaVu Sans, sans-serif
  template:
    default-name: report
    cache-enabled: true
```

### PdfService.java

```java
package com.example.myapp.service;

import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.model.ReportData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class PdfService {
    
    @Autowired
    private ReportService reportService;  // ✅ 自动注入
    
    public byte[] generateReport(ReportData data) throws IOException {
        return reportService.generatePdf(data, "report");
    }
}
```

### PdfController.java

```java
package com.example.myapp.controller;

import com.example.myapp.service.PdfService;
import com.mercury.pdf.render.model.ReportData;
import com.mercury.pdf.render.model.ReportDataBuilder;
import com.mercury.pdf.render.model.Section;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class PdfController {
    
    @Autowired
    private PdfService pdfService;
    
    @GetMapping("/api/generate-pdf")
    public ResponseEntity<byte[]> generatePdf() throws IOException {
        // 创建报告数据
        ReportData data = ReportDataBuilder.create()
            .title("测试报告")
            .addSection(new Section("章节1").addParagraph("这是中文内容"))
            .build();
        
        // 生成 PDF
        byte[] pdf = pdfService.generateReport(data);
        
        // 返回 PDF
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "report.pdf");
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(pdf);
    }
}
```

---

## ✅ 检查清单 Checklist

开始使用前确认：

- [ ] **不要**创建自定义的 `PdfRenderConfig` 配置类
- [ ] 在 `application.yml` 中配置 `pdf-render` 属性
- [ ] 字体文件放在 `src/main/resources/fonts/` 目录
- [ ] 直接在服务类中 `@Autowired` 注入 `ReportService`
- [ ] 运行 `bash validate-chinese-fonts.sh` 验证配置

---

**最后更新 Last Updated**: 2026-01-29

**重要提示 Important**: 本文档替代了旧版本中的手动配置指南。
