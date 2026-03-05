# 问题排查指南 / Troubleshooting Guide

## 问题：NoSuchMethodError: PdfContentByte.setColorFill / Issue: iText/OpenPDF Classpath Conflict

如果您看到以下错误：
```
java.lang.NoSuchMethodError: com.lowagie.text.pdf.PdfContentByte.setColorFill(Ljava/awt/Color;)V
  at org.xhtmlrenderer.pdf.ITextOutputDevice.ensureFillColor(...)
```

**原因 / Cause:**

您的项目同时存在 `com.github.librepdf:openpdf`（pdf-render 使用）和 `com.lowagie:itext`（旧版 iText）。
两个 JAR 包含相同的类 `com.lowagie.text.pdf.PdfContentByte`，但旧版 iText 的方法签名不同，
导致 JVM 加载了错误的类版本。

常见引入 `com.lowagie:itext` 的依赖：JasperReports、iReport 等。

**解决方法 / Solution:**

运行以下命令找到冲突来源：
```bash
mvn dependency:tree | grep -i itext
```

然后在 pom.xml 中排除旧版 iText：
```xml
<!-- 示例：如果 jasperreports 引入了旧 iText -->
<dependency>
    <groupId>net.sf.jasperreports</groupId>
    <artifactId>jasperreports</artifactId>
    <version>...</version>
    <exclusions>
        <exclusion>
            <groupId>com.lowagie</groupId>
            <artifactId>itext</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

如果使用 Gradle：
```groovy
implementation('net.sf.jasperreports:jasperreports:...') {
    exclude group: 'com.lowagie', module: 'itext'
}
```

pdf-render 库会在启动时自动检测此冲突并输出警告日志。

---

## 问题：中文显示为方框 (□) / Issue: Chinese Shows as Boxes (□)

如果您的 PDF 中中文显示为方框，请按照以下步骤排查：

### 第一步：检查字体文件是否存在
Check if font files exist:

```bash
ls -la src/main/resources/fonts/
```

应该看到 / You should see:
- `HarmonyOS_Sans_SC_Regular.ttf` 或 `NotoSansCJKsc-Regular.otf`
- 文件大小应该 > 1MB (字体文件通常很大)

### 第二步：检查 application.yml 配置
Check your application.yml configuration:

```yaml
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf  # 注意：必须以 / 开头
    default-family: HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif
```

**常见错误 / Common mistakes:**
- ❌ `classpath:fonts/...` (缺少前导 `/`)
- ✅ `classpath:/fonts/...` (正确)

### 第三步：确认没有使用 @PostConstruct 覆盖配置
Confirm you're not using @PostConstruct:

❌ **错误的做法 - 这会覆盖 application.yml 配置:**
```java
@Service
public class PdfService {
    private final PdfRenderService pdfRenderService;
    
    @PostConstruct  // ❌ 不要这样做！
    public void init() {
        pdfRenderService = new PdfRenderService();
        FontConfig fontConfig = new FontConfig();
        // ... 手动配置
        pdfRenderService.getHtmlRenderer().setFontConfig(fontConfig);
    }
}
```

✅ **正确的做法 - 直接注入:**
```java
@Service
public class PdfService {
    private final PdfRenderService pdfRenderService;
    
    // ✅ 只需注入，Spring Boot 自动配置
    public PdfService(PdfRenderService pdfRenderService) {
        this.pdfRenderService = pdfRenderService;
    }
}
```

### 第四步：启用调试模式检查配置
Enable debug mode to check configuration:

```yaml
pdf-render:
  debug:
    enabled: true
    output-directory: debug-html
    include-timestamp: false
```

生成 PDF 后，检查 `debug-html/report.html`:
1. 打开 HTML 文件，查看 `<style>` 标签中的字体设置
2. 检查是否包含正确的 `font-family`

### 第五步：检查 Spring Boot 自动配置是否生效
Check if Spring Boot auto-configuration is active:

在您的应用启动日志中搜索：
```
PDF Render: Font configuration applied from properties
  - CJK font: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
  - Regular font: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
```

如果没有看到这些日志，说明：
1. `application.yml` 配置文件位置不对 (应该在 `src/main/resources/`)
2. 缩进格式不正确 (YAML 对缩进敏感)
3. 可能使用了 `@PostConstruct` 覆盖了配置

### 第六步：验证 PdfRenderService Bean 来源
Verify PdfRenderService bean source:

添加日志检查 bean 是从哪里来的：

```java
@Service
public class PdfService {
    
    private final PdfRenderService pdfRenderService;
    
    public PdfService(PdfRenderService pdfRenderService) {
        this.pdfRenderService = pdfRenderService;
        
        // 添加日志
        HtmlReportRenderer renderer = pdfRenderService.getHtmlRenderer();
        System.out.println("PdfRenderService bean injected");
        System.out.println("Font properties: " + renderer.getFontProperties());
        System.out.println("Debug enabled: " + renderer.isDebugHtmlEnabled());
    }
}
```

如果输出显示:
- `Font properties: null` - 说明配置未应用
- `Debug enabled: false` - 说明 debug 配置未应用

**这意味着您可能在某处手动创建了 `new PdfRenderService()`，而不是使用 Spring 注入的 bean！**

## 完整的工作示例 / Complete Working Example

### 1. 项目结构 / Project Structure

```
my-spring-boot-app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/
│   │   │       ├── Application.java
│   │   │       ├── controller/
│   │   │       │   └── PdfController.java
│   │   │       └── service/
│   │   │           └── PdfService.java
│   │   └── resources/
│   │       ├── application.yml  ← 配置文件
│   │       └── fonts/
│   │           └── HarmonyOS_Sans_SC_Regular.ttf  ← 字体文件
│   └── test/
└── pom.xml
```

### 2. pom.xml 依赖 / Dependencies

```xml
<dependencies>
    <!-- Spring Boot Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
        <version>2.7.18</version>
    </dependency>
    
    <!-- PDF Render Library -->
    <dependency>
        <groupId>com.mercury</groupId>
        <artifactId>pdf-render</artifactId>
        <version>1.0.1</version>
    </dependency>
</dependencies>
```

### 3. application.yml

```yaml
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    default-family: HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif
  
  debug:
    enabled: true
    output-directory: debug-html
```

### 4. Application.java

```java
package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 5. PdfService.java

```java
package com.example.service;

import com.lowagie.text.DocumentException;
import com.mercury.pdf.render.PdfRenderService;
import com.mercury.pdf.render.model.ReportData;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class PdfService {
    
    private final PdfRenderService pdfRenderService;
    
    // ✅ 构造函数注入 - Spring Boot 会自动注入配置好的 PdfRenderService
    public PdfService(PdfRenderService pdfRenderService) {
        this.pdfRenderService = pdfRenderService;
        
        // 验证配置（可选）
        System.out.println("PdfService initialized");
        System.out.println("  Font config: " + 
            pdfRenderService.getHtmlRenderer().getFontProperties());
        System.out.println("  Debug enabled: " + 
            pdfRenderService.getHtmlRenderer().isDebugHtmlEnabled());
    }
    
    public byte[] generatePdf(ReportData data) throws IOException, DocumentException {
        return pdfRenderService.generatePdf(data);
    }
}
```

### 6. PdfController.java

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
    
    @GetMapping("/pdf/test")
    public ResponseEntity<byte[]> generateTestPdf() throws Exception {
        // 创建包含中文的报告
        ReportData data = ReportDataBuilder.create()
            .title("测试报告")
            .subtitle("验证中文字体配置")
            .addSection(new Section("第一章")
                .addParagraph("这是一段中文文本。")
                .addParagraph("如果您看到这些字，说明配置成功！"))
            .build();
        
        // 生成 PDF
        byte[] pdfBytes = pdfService.generatePdf(data);
        
        // 返回
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "test.pdf");
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(pdfBytes);
    }
}
```

## 运行和测试 / Run and Test

### 1. 启动应用 / Start Application

```bash
mvn spring-boot:run
```

启动日志应该显示 / Startup logs should show:
```
PDF Render: Font configuration applied from properties
  - CJK font: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
  - Regular font: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
PDF Render: Debug HTML output enabled
  - Output directory: debug-html
  - Include timestamp: false
PdfService initialized
  Font config: FontProperties{regularPath='classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf', ...}
  Debug enabled: true
```

### 2. 测试 PDF 生成 / Test PDF Generation

```bash
curl http://localhost:8080/pdf/test --output test.pdf
```

### 3. 验证结果 / Verify Results

1. 打开 `test.pdf` - 中文应该正确显示
2. 查看 `debug-html/report.html` - 调试 HTML 应该已生成
3. 检查字体嵌入：使用 PDF 阅读器的属性功能查看嵌入的字体

## 常见问题解答 / FAQ

### Q: 为什么我的配置不生效？
**A:** 最常见的原因：
1. ❌ 使用了 `@PostConstruct` 手动配置，覆盖了 application.yml
2. ❌ 手动创建了 `new PdfRenderService()`，而不是使用注入的 bean
3. ❌ `application.yml` 文件不在 `src/main/resources/` 目录
4. ❌ YAML 缩进不正确（必须使用空格，不能用 Tab）

### Q: 如何确认字体文件路径正确？
**A:** 检查两件事：
1. 文件存在于 `src/main/resources/fonts/` 目录
2. 配置中的路径以 `/` 开头：`classpath:/fonts/xxx.ttf`

### Q: 中文还是显示方框，但配置看起来都对
**A:** 可能的原因：
1. 字体文件损坏或不完整
2. 字体文件不支持您使用的中文字符
3. 使用了错误的 `font-family` 名称（不匹配字体文件内部名称）

启用 debug 模式，查看警告信息。

### Q: Debug HTML 没有生成
**A:** 检查：
1. `pdf-render.debug.enabled` 是否设置为 `true`
2. 是否有写入权限到输出目录
3. 是否使用了注入的 `PdfRenderService` bean（不是手动创建的）

## 需要更多帮助？ / Need More Help?

如果以上步骤都无法解决问题，请提供：
1. 完整的 `application.yml` 配置
2. 应用启动日志
3. PDF Service 的代码
4. 字体文件的路径和大小信息

这样可以更准确地诊断问题。
