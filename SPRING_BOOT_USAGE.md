# Spring Boot Usage Guide

## 正确使用方式 (Correct Usage)

### ❌ 错误：不要使用 @PostConstruct 手动配置 (WRONG: Don't use @PostConstruct)

```java
@Service
public class PdfService {
    private final ReportService reportService;
    
    @PostConstruct  // ❌ 不需要！这是多余的！(NOT NEEDED! This is redundant!)
    public void init() {
        reportService = new ReportService();
        
        // ❌ 不要手动创建 FontConfig (Don't manually create FontConfig)
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
        fontConfig.setDefaultFontFamily("HarmonyOS Sans SC, DejaVu Sans, sans-serif");
        
        // ❌ 不要手动调用 setFontConfig (Don't manually call setFontConfig)
        reportService.getHtmlRenderer().setFontConfig(fontConfig);
    }
}
```

**为什么这是错误的？(Why is this wrong?)**
1. 这完全忽略了 `application.yml` 配置 (This completely ignores application.yml configuration)
2. `FontConfig` 类是多余的，它重复了 `PdfRenderProperties.FontProperties` (FontConfig is redundant)
3. Spring Boot 自动配置已经处理了所有这些 (Spring Boot auto-configuration already handles all of this)
4. 其他在 `application.yml` 中的配置会被忽略 (Other configurations in application.yml will be ignored)

---

### ✅ 正确：只需配置 application.yml (CORRECT: Just configure application.yml)

#### 第一步：配置 application.yml (Step 1: Configure application.yml)

```yaml
pdf-render:
  # 模板配置 (Template configuration)
  template:
    location: classpath:/templates/
    default-name: report
    cache-enabled: true
  
  # 字体配置 - 中文支持 (Font configuration - Chinese support)
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    bold-path: classpath:/fonts/HarmonyOS_Sans_SC_Bold.ttf
    default-family: HarmonyOS Sans SC, Noto Sans CJK SC, DejaVu Sans, Arial, sans-serif
    cjk-family: Noto Sans CJK, SimSun, sans-serif
  
  # 调试配置 (Debug configuration)
  debug:
    enabled: false
    output-directory: debug-html
    include-timestamp: false
```

#### 第二步：直接注入使用 (Step 2: Simply inject and use)

```java
@Service
public class PdfService {
    private final ReportService reportService;
    
    // ✅ 直接注入，Spring Boot 已经配置好了所有东西！
    // Simply inject - Spring Boot has already configured everything!
    public PdfService(ReportService reportService) {
        this.reportService = reportService;
    }
    
    // ✅ 直接使用，不需要任何额外配置
    // Simply use it - no additional configuration needed
    public byte[] generatePdf(ReportData data) throws IOException, DocumentException {
        return reportService.generatePdf(data);
    }
}
```

**就是这么简单！(That's it! That simple!)**

---

## Properties 的作用 (Purpose of Properties)

`PdfRenderProperties` 是 Spring Boot 的配置属性类，它的作用是：

1. **统一配置源 (Unified Configuration Source)**
   - 所有配置都在 `application.yml` 中定义
   - 不需要在代码中硬编码配置
   - 可以根据环境（dev/prod）使用不同配置

2. **自动配置 (Auto-Configuration)**
   - `PdfRenderAutoConfiguration` 会自动读取这些属性
   - 自动创建并配置 `ReportService` bean
   - 自动设置字体、模板、调试等所有配置

3. **类型安全 (Type Safety)**
   - 配置有明确的类型和默认值
   - IDE 可以提供自动完成
   - 编译时检查配置错误

---

## 其他配置是否生效？(Are Other Configurations Effective?)

**是的！所有配置都会生效。(Yes! All configurations are effective.)**

### 模板配置会生效 (Template Configuration Works)

```yaml
pdf-render:
  template:
    default-name: invoice  # ✅ 会使用 invoice.html 模板
    cache-enabled: false   # ✅ 开发时禁用缓存
```

### 调试配置会生效 (Debug Configuration Works)

```yaml
pdf-render:
  debug:
    enabled: true                    # ✅ 启用调试 HTML 输出
    output-directory: target/debug   # ✅ 调试文件保存到这里
    include-timestamp: true          # ✅ 文件名包含时间戳
```

### 字体配置会生效 (Font Configuration Works)

```yaml
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf  # ✅ 自动加载
    default-family: HarmonyOS Sans SC, DejaVu Sans, sans-serif    # ✅ 自动应用
```

---

## 环境特定配置 (Environment-Specific Configuration)

### 开发环境 (Development)

```yaml
spring:
  config:
    activate:
      on-profile: dev

pdf-render:
  template:
    cache-enabled: false  # 开发时禁用缓存，便于实时查看修改
  debug:
    enabled: true         # 开发时启用调试输出
    include-timestamp: true
```

### 生产环境 (Production)

```yaml
spring:
  config:
    activate:
      on-profile: prod

pdf-render:
  template:
    cache-enabled: true   # 生产环境启用缓存以提高性能
  debug:
    enabled: false        # 生产环境禁用调试输出
```

---

## 常见问题 (FAQ)

### Q: 为什么我之前需要使用 @PostConstruct？

**A:** 这是一个设计缺陷。`FontConfig` 类是多余的，它重复了 `PdfRenderProperties.FontProperties` 的功能。现在 `FontConfig` 已被标记为 `@Deprecated`。

### Q: 我的旧代码使用了 FontConfig，会出问题吗？

**A:** 不会。为了向后兼容，`HtmlReportRenderer` 仍然支持 `setFontConfig()` 方法，但它已被标记为 `@Deprecated`。建议迁移到使用 properties。

### Q: 如何迁移旧代码？

**A:** 
1. 删除 `@PostConstruct` 方法
2. 删除手动创建 `FontConfig` 的代码
3. 删除手动调用 `setFontConfig()` 的代码
4. 在 `application.yml` 中配置 `pdf-render.fonts`
5. 通过构造函数注入 `ReportService`

### Q: 配置不生效怎么办？

**A:** 检查以下几点：
1. ✅ `application.yml` 文件在 `src/main/resources/` 目录下
2. ✅ 缩进正确（YAML 对缩进敏感）
3. ✅ 没有使用 `@PostConstruct` 手动覆盖配置
4. ✅ `pdf-render` 依赖在 classpath 中

---

## 完整示例 (Complete Example)

### application.yml

```yaml
pdf-render:
  template:
    default-name: report
    cache-enabled: true
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    bold-path: classpath:/fonts/HarmonyOS_Sans_SC_Bold.ttf
    default-family: HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif
  debug:
    enabled: false
```

### PdfService.java

```java
package com.example.service;

import com.lowagie.text.DocumentException;
import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.model.ReportData;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class PdfService {
    
    private final ReportService reportService;
    
    // ✅ 构造函数注入 - Spring Boot 自动配置
    public PdfService(ReportService reportService) {
        this.reportService = reportService;
    }
    
    // ✅ 直接使用 - 所有配置已自动应用
    public byte[] generatePdf(ReportData data) throws IOException, DocumentException {
        return reportService.generatePdf(data);
    }
    
    // ✅ 可以使用自定义模板
    public byte[] generateInvoicePdf(ReportData data) throws IOException, DocumentException {
        return reportService.generatePdf(data, "invoice");
    }
}
```

### Controller.java

```java
package com.example.controller;

import com.example.service.PdfService;
import com.mercury.pdf.render.model.ReportData;
import com.mercury.pdf.render.model.ReportDataBuilder;
import com.mercury.pdf.render.model.Section;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PdfController {
    
    private final PdfService pdfService;
    
    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }
    
    @GetMapping("/pdf/report")
    public ResponseEntity<byte[]> generateReport() throws Exception {
        // 构建报告数据
        ReportData data = ReportDataBuilder.create()
            .title("测试报告")
            .subtitle("Spring Boot 集成示例")
            .addSection(new Section("概述")
                .addParagraph("这是一个使用 Spring Boot properties 配置的示例。")
                .addParagraph("无需 @PostConstruct，无需手动配置！"))
            .build();
        
        // 生成 PDF - 所有配置已自动应用
        byte[] pdfBytes = pdfService.generatePdf(data);
        
        // 返回 PDF
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "report.pdf");
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(pdfBytes);
    }
}
```

---

## 总结 (Summary)

### ❌ 不要这样做 (Don't Do This)
- 使用 `@PostConstruct` 手动配置
- 手动创建 `FontConfig` 对象
- 手动调用 `setFontConfig()`
- 在代码中硬编码配置

### ✅ 应该这样做 (Do This)
- 在 `application.yml` 中配置所有设置
- 通过构造函数注入 `ReportService`
- 直接使用，让 Spring Boot 自动配置处理一切
- 使用环境配置文件（dev/prod）管理不同环境的配置

**Properties 的意义就是让配置外部化、统一管理，避免代码中硬编码！**
**(The purpose of properties is to externalize and centrally manage configuration, avoiding hardcoding in code!)**
