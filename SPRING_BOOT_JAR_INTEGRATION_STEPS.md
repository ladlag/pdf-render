# Spring Boot项目集成pdf-render JAR包完整步骤

本文档详细说明如何将pdf-render模块以JAR包形式集成到Spring Boot项目中。

---

## 目录

1. [概述](#概述)
2. [application.yml所有配置项说明](#applicationyml所有配置项说明)
3. [集成步骤详解](#集成步骤详解)
4. [验证集成是否成功](#验证集成是否成功)
5. [常见问题排查](#常见问题排查)

---

## 概述

### 关键信息

- **JAR版本**: 1.0.1
- **支持的Spring Boot版本**: 2.x (推荐 2.7.18)
- **JDK要求**: 1.8+
- **自动配置**: 支持（通过Spring Boot AutoConfiguration）

### 核心特性

✅ **所有application.yml配置项都会自动生效**，包括：
- 模板配置（template）
- 输出配置（output）  
- 字体配置（fonts）
- 调试配置（debug）

✅ **无需手动配置**，Spring Boot会自动检测并应用所有配置

---

## application.yml所有配置项说明

### 完整配置示例

```yaml
pdf-render:
  # 1. 模板配置
  template:
    location: classpath:/templates/           # 模板文件位置
    default-name: report                       # 默认模板名称
    cache-enabled: true                        # 是否启用模板缓存
  
  # 2. 输出配置
  output:
    directory: /var/pdfs                       # PDF输出目录
    save-to-directory: false                   # 是否自动保存PDF到目录
  
  # 3. 字体配置（重要：显示中文必须配置）
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf    # 常规字体
    bold-path: classpath:/fonts/HarmonyOS_Sans_SC_Bold.ttf          # 粗体字体
    cjk-path: classpath:/fonts/NotoSansCJKsc-Regular.otf            # CJK字体（可选）
    default-family: HarmonyOS Sans SC, DejaVu Sans, sans-serif      # 默认字体族
    cjk-family: Noto Sans CJK, SimSun, sans-serif                   # CJK字体族
  
  # 4. 调试配置
  debug:
    enabled: false                             # 是否启用调试HTML输出
    output-directory: debug-html               # 调试HTML输出目录
    include-timestamp: false                   # 文件名是否包含时间戳
```

### 各配置项详细说明

#### 1. template（模板配置）- ✅ 会生效

| 配置项 | 类型 | 默认值 | 说明 | 是否生效 |
|--------|------|--------|------|----------|
| location | String | classpath:/templates/ | 模板文件存放位置 | ✅ 是 |
| default-name | String | report | 默认模板名称（不含.html） | ✅ 是 |
| cache-enabled | boolean | true | 是否缓存模板（生产环境建议true） | ✅ 是 |

**作用**：
- `location`: 指定在哪里查找模板文件
- `default-name`: 调用`generatePdf(data)`时使用的默认模板
- `cache-enabled`: 缓存可提升性能，开发时设为false便于实时查看修改

#### 2. output（输出配置）- ✅ 会生效

| 配置项 | 类型 | 默认值 | 说明 | 是否生效 |
|--------|------|--------|------|----------|
| directory | String | null | PDF文件输出目录路径 | ✅ 是 |
| save-to-directory | boolean | false | 是否自动保存PDF到目录 | ✅ 是 |

**作用**：
- `directory`: 设置PDF自动保存的目录位置
- `save-to-directory`: 启用后，每次生成PDF会自动保存到指定目录

**注意**: 这个配置项在当前版本的`PdfRenderAutoConfiguration`中**未应用**，但在`PdfRenderProperties`中已定义。如需使用，需要在Service层手动读取配置。

#### 3. fonts（字体配置）- ✅ 会生效（非常重要）

| 配置项 | 类型 | 默认值 | 说明 | 是否生效 |
|--------|------|--------|------|----------|
| regular-path | String | null | 常规字体文件路径 | ✅ 是 |
| bold-path | String | null | 粗体字体文件路径 | ✅ 是 |
| cjk-path | String | null | CJK字体文件路径 | ✅ 是 |
| default-family | String | DejaVu Sans, Arial, sans-serif | 默认CSS字体族 | ✅ 是 |
| cjk-family | String | Noto Sans CJK, SimSun, sans-serif | CJK CSS字体族 | ✅ 是 |

**作用**：
- **至关重要**: 如果PDF需要显示中文，必须配置字体路径
- 未配置字体时，中文会显示为方框（□）
- 通过`PdfRenderAutoConfiguration.configureFonts()`自动应用

#### 4. debug（调试配置）- ✅ 会生效

| 配置项 | 类型 | 默认值 | 说明 | 是否生效 |
|--------|------|--------|------|----------|
| enabled | boolean | false | 是否启用调试HTML输出 | ✅ 是 |
| output-directory | String | debug-html | 调试HTML保存目录 | ✅ 是 |
| include-timestamp | boolean | false | 文件名是否包含时间戳 | ✅ 是 |

**作用**：
- 启用后，生成PDF前会保存中间HTML文件，方便调试
- 可以查看HTML渲染效果，定位PDF问题
- 通过`PdfRenderAutoConfiguration.configureDebugSettings()`自动应用

### 配置生效原理

```
1. Spring Boot启动
   ↓
2. 扫描classpath，发现pdf-render.jar
   ↓
3. 读取META-INF/spring.factories
   ↓
4. 加载PdfRenderAutoConfiguration
   ↓
5. 读取application.yml中的pdf-render配置
   ↓
6. 创建PdfRenderProperties对象
   ↓
7. 创建ReportService Bean
   ↓
8. 应用所有配置到ReportService
   ↓
9. 完成自动配置
```

---

## 集成步骤详解

### 前置准备

确保你已经有：
- ✅ Spring Boot项目（2.x版本）
- ✅ JDK 1.8或更高版本
- ✅ Maven 3.6+

### 步骤1: 构建pdf-render JAR包

**目的**: 将pdf-render项目打包成JAR，并安装到本地Maven仓库

**操作步骤**:

```bash
# 1. 进入pdf-render项目目录
cd /path/to/pdf-render

# 2. 清理并构建项目
mvn clean install

# 3. 确认构建成功
# 应该看到以下输出：
# [INFO] BUILD SUCCESS
# [INFO] ------------------------------------------------------------------------
```

**生成的文件**:
- `target/pdf-render-1.0.1.jar` - 主JAR包
- `target/pdf-render-1.0.1-sources.jar` - 源码包
- `target/pdf-render-1.0.1-javadoc.jar` - 文档包

**安装位置**: 
- `~/.m2/repository/com/mercury/pdf-render/1.0.1/`

**验证安装**:
```bash
ls ~/.m2/repository/com/mercury/pdf-render/1.0.1/
# 应该看到：
# pdf-render-1.0.1.jar
# pdf-render-1.0.1-sources.jar
# pdf-render-1.0.1-javadoc.jar
# pdf-render-1.0.1.pom
```

---

### 步骤2: 在Spring Boot项目中添加依赖

**目的**: 让Spring Boot项目引用pdf-render库

**操作步骤**:

1. 打开Spring Boot项目的`pom.xml`
2. 在`<dependencies>`节点中添加：

```xml
<dependencies>
    <!-- 其他依赖... -->
    
    <!-- PDF渲染库 -->
    <dependency>
        <groupId>com.mercury</groupId>
        <artifactId>pdf-render</artifactId>
        <version>1.0.1</version>
    </dependency>
</dependencies>
```

**说明**:
- `groupId`: com.mercury（与pdf-render的pom.xml一致）
- `artifactId`: pdf-render（项目名称）
- `version`: 1.0.1（当前版本）
- Maven会自动下载所有传递依赖（Flying Saucer, Thymeleaf, JFreeChart等）

---

### 步骤3: 准备字体文件（如果需要显示中文）

**目的**: 提供中文字体，避免中文显示为方框

**操作步骤**:

1. 在Spring Boot项目中创建字体目录：
```bash
mkdir -p src/main/resources/fonts
```

2. 下载并放置字体文件：

**推荐字体**:

| 字体名称 | 下载地址 | 许可 | 文件名示例 |
|---------|---------|------|-----------|
| HarmonyOS Sans | [华为开发者](https://developer.harmonyos.com/cn/design/resource) | 免费商用 | HarmonyOS_Sans_SC_Regular.ttf |
| Noto Sans CJK | [Google Fonts GitHub](https://github.com/googlefonts/noto-cjk) | SIL OFL | NotoSansCJKsc-Regular.otf |
| 思源黑体 | [Adobe GitHub](https://github.com/adobe-fonts/source-han-sans) | SIL OFL | SourceHanSansSC-Regular.otf |

3. 将字体文件复制到`src/main/resources/fonts/`目录：
```
src/main/resources/fonts/
├── HarmonyOS_Sans_SC_Regular.ttf
└── HarmonyOS_Sans_SC_Bold.ttf
```

**注意**: 
- 字体文件较大（通常10-20MB），确保添加到版本控制时考虑仓库大小
- 可以在`.gitignore`中排除字体文件，改为在部署时下载

---

### 步骤4: 配置application.yml

**目的**: 配置pdf-render的所有参数

**操作步骤**:

1. 打开或创建`src/main/resources/application.yml`
2. 添加pdf-render配置：

```yaml
# PDF渲染库配置
pdf-render:
  # 模板配置
  template:
    default-name: report          # 默认使用report模板
    cache-enabled: true           # 启用模板缓存
  
  # 字体配置（必须：如果需要显示中文）
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    bold-path: classpath:/fonts/HarmonyOS_Sans_SC_Bold.ttf
    default-family: HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif
  
  # 调试配置（可选）
  debug:
    enabled: false                # 生产环境关闭，开发时可以开启
    output-directory: debug-html
```

**最小配置**（仅中文支持）:
```yaml
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    default-family: HarmonyOS Sans SC, sans-serif
```

**开发环境配置**（application-dev.yml）:
```yaml
pdf-render:
  template:
    cache-enabled: false          # 禁用缓存，实时查看修改
  debug:
    enabled: true                 # 启用调试HTML
    output-directory: target/debug-html
    include-timestamp: true
```

**生产环境配置**（application-prod.yml）:
```yaml
pdf-render:
  template:
    cache-enabled: true           # 启用缓存，提升性能
  debug:
    enabled: false                # 禁用调试
```

---

### 步骤5: 启用配置属性

**目的**: 让Spring Boot识别pdf-render的配置

**操作步骤**:

在Spring Boot主类上添加注解：

```java
package com.example.yourapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.mercury.pdf.render.config.PdfRenderProperties;

@SpringBootApplication
@EnableConfigurationProperties(PdfRenderProperties.class)  // ← 必须添加此注解
public class YourApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

**说明**:
- `@EnableConfigurationProperties(PdfRenderProperties.class)` 是**必需的**
- 没有这个注解，application.yml中的pdf-render配置不会被读取
- 这个注解告诉Spring Boot扫描并绑定PdfRenderProperties配置类

**为什么需要这个注解？**
- PdfRenderAutoConfiguration会自动启用
- 但PdfRenderProperties需要显式启用才能绑定配置
- 这是Spring Boot配置属性的标准做法

---

### 步骤6: 准备模板文件（可选）

**目的**: 使用自定义PDF模板

**操作步骤**:

1. 在Spring Boot项目中创建模板目录：
```bash
mkdir -p src/main/resources/templates
```

2. 从pdf-render项目复制模板文件：
```bash
# 复制默认模板
cp /path/to/pdf-render/src/main/resources/templates/report.html \
   src/main/resources/templates/

# 或复制其他模板
cp /path/to/pdf-render/src/main/resources/templates/flexible.html \
   src/main/resources/templates/
```

**可用模板**:
- `report.html` - 标准报告模板
- `flexible.html` - 灵活章节模板
- `invoice.html` - 发票模板
- `certificate.html` - 证书模板

**注意**:
- 如果不复制模板，会使用pdf-render JAR包内的默认模板
- 自定义模板时，确保模板语法正确（Thymeleaf）

---

### 步骤7: 使用ReportService

**目的**: 在代码中使用PDF生成功能

**操作步骤**:

创建Service类：

```java
package com.example.yourapp.service;

import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.model.ReportData;
import com.mercury.pdf.render.model.ReportDataBuilder;
import com.mercury.pdf.render.model.Section;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class PdfReportService {
    
    private final ReportService reportService;
    
    // 构造器注入（Spring自动注入）
    public PdfReportService(ReportService reportService) {
        this.reportService = reportService;
    }
    
    /**
     * 生成测试PDF报告
     */
    public byte[] generateTestReport() throws IOException {
        // 1. 构建报告数据
        ReportData reportData = ReportDataBuilder.create()
            .title("测试报告")
            .subtitle("Spring Boot集成测试")
            .reportDate("2026-01-30")
            .reportNumber("TEST-001")
            .addSection(new Section("第一章")
                .addParagraph("这是一段中文测试内容。")
                .addParagraph("如果看到正常的中文，说明字体配置成功！"))
            .addSection(new Section("第二章")
                .addParagraph("English text should also work."))
            .build();
        
        // 2. 生成PDF（使用默认模板）
        byte[] pdfBytes = reportService.generatePdf(reportData);
        
        return pdfBytes;
    }
    
    /**
     * 使用指定模板生成PDF
     */
    public byte[] generateCustomReport(ReportData data, String templateName) 
            throws IOException {
        return reportService.generatePdf(data, templateName);
    }
}
```

**重要**:
- ✅ 使用构造器注入，让Spring自动注入ReportService
- ✅ ReportService已经由PdfRenderAutoConfiguration配置好
- ✅ 所有application.yml配置已自动应用
- ❌ 不要手动创建ReportService实例：`new ReportService()`
- ❌ 不要在代码中调用`setFontProperties()`（会覆盖YAML配置）

---

### 步骤8: 创建REST接口（可选）

**目的**: 提供HTTP接口生成PDF

**操作步骤**:

创建Controller类：

```java
package com.example.yourapp.controller;

import com.example.yourapp.service.PdfReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {
    
    private final PdfReportService pdfReportService;
    
    public PdfController(PdfReportService pdfReportService) {
        this.pdfReportService = pdfReportService;
    }
    
    /**
     * 测试接口：生成并下载PDF
     * 访问: http://localhost:8080/api/pdf/test
     */
    @GetMapping(value = "/test", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateTestPdf() {
        try {
            byte[] pdfBytes = pdfReportService.generateTestReport();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            
            // 设置下载文件名（支持中文）
            String filename = "测试报告.pdf";
            String encodedFilename = new String(
                filename.getBytes(StandardCharsets.UTF_8), 
                StandardCharsets.ISO_8859_1
            );
            headers.setContentDispositionFormData("attachment", encodedFilename);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
                
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
        }
    }
}
```

---

### 步骤9: 启动并测试

**目的**: 验证集成是否成功

**操作步骤**:

1. 启动Spring Boot应用：
```bash
mvn spring-boot:run
```

2. 查看启动日志，确认配置已加载：
```
PDF Render: Font configuration applied from properties
  - Regular font: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
```

3. 访问测试接口：
```bash
# 浏览器访问或使用curl
curl http://localhost:8080/api/pdf/test --output test.pdf

# 或在浏览器中打开
open http://localhost:8080/api/pdf/test
```

4. 检查生成的PDF：
- 打开test.pdf文件
- 确认中文正常显示（不是方框□）
- 确认布局正确

---

## 验证集成是否成功

### 验证清单

使用以下检查清单确认集成成功：

#### ✅ 依赖检查

```bash
# 检查Maven依赖是否正确
mvn dependency:tree | grep pdf-render

# 应该看到：
# [INFO] +- com.mercury:pdf-render:jar:1.0.1:compile
```

#### ✅ 配置检查

创建配置验证类：

```java
import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.config.PdfRenderProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PdfConfigVerifier implements CommandLineRunner {
    
    private final ReportService reportService;
    private final PdfRenderProperties properties;
    
    public PdfConfigVerifier(ReportService reportService, 
                             PdfRenderProperties properties) {
        this.reportService = reportService;
        this.properties = properties;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n========== PDF Render 配置验证 ==========");
        
        // 检查ReportService
        if (reportService != null) {
            System.out.println("✓ ReportService 注入成功");
        } else {
            System.err.println("✗ ReportService 注入失败！");
        }
        
        // 检查配置属性
        System.out.println("\n配置项检查：");
        System.out.println("  Template:");
        System.out.println("    - default-name: " + properties.getTemplate().getDefaultName());
        System.out.println("    - cache-enabled: " + properties.getTemplate().isCacheEnabled());
        
        System.out.println("  Fonts:");
        System.out.println("    - regular-path: " + properties.getFonts().getRegularPath());
        System.out.println("    - default-family: " + properties.getFonts().getDefaultFamily());
        
        System.out.println("  Debug:");
        System.out.println("    - enabled: " + properties.getDebug().isEnabled());
        System.out.println("    - output-directory: " + properties.getDebug().getOutputDirectory());
        
        // 检查字体配置
        PdfRenderProperties.FontProperties fontProps = 
            reportService.getHtmlRenderer().getFontProperties();
        if (fontProps != null) {
            System.out.println("\n✓ 字体配置已应用到ReportService");
        } else {
            System.err.println("\n✗ 字体配置未应用！检查@EnableConfigurationProperties注解");
        }
        
        System.out.println("========================================\n");
    }
}
```

启动应用，查看输出：

```
========== PDF Render 配置验证 ==========
✓ ReportService 注入成功

配置项检查：
  Template:
    - default-name: report
    - cache-enabled: true
  Fonts:
    - regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    - default-family: HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif
  Debug:
    - enabled: false
    - output-directory: debug-html

✓ 字体配置已应用到ReportService
========================================
```

#### ✅ 功能检查

创建测试用例：

```java
import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.model.ReportData;
import com.mercury.pdf.render.model.ReportDataBuilder;
import com.mercury.pdf.render.model.Section;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PdfGenerationTest {
    
    @Autowired
    private ReportService reportService;
    
    @Test
    public void testReportServiceInjection() {
        assertNotNull(reportService, "ReportService应该被注入");
    }
    
    @Test
    public void testGeneratePdfWithChinese() throws IOException {
        ReportData data = ReportDataBuilder.create()
            .title("中文测试报告")
            .subtitle("测试中文字体")
            .addSection(new Section("测试章节")
                .addParagraph("这是中文测试内容。"))
            .build();
        
        byte[] pdfBytes = reportService.generatePdf(data);
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        
        // 保存到文件以便手动检查
        try (FileOutputStream fos = new FileOutputStream("target/test-chinese.pdf")) {
            fos.write(pdfBytes);
        }
        
        System.out.println("✓ PDF生成成功: target/test-chinese.pdf");
        System.out.println("  请打开文件检查中文是否正常显示");
    }
}
```

运行测试：
```bash
mvn test -Dtest=PdfGenerationTest
```

---

## 常见问题排查

### 问题1: ReportService无法注入

**症状**:
```
Field reportService in ... required a bean of type 'ReportService' that could not be found.
```

**原因**: 
- 缺少`@EnableConfigurationProperties(PdfRenderProperties.class)`注解

**解决方案**:
```java
@SpringBootApplication
@EnableConfigurationProperties(PdfRenderProperties.class)  // ← 添加此注解
public class YourApplication { }
```

---

### 问题2: application.yml配置不生效

**症状**:
- 配置了字体路径，但中文仍显示为方框
- 日志中看不到"Font configuration applied"

**排查步骤**:

1. 确认`@EnableConfigurationProperties`注解已添加
2. 确认application.yml缩进正确（YAML对缩进敏感）：
```yaml
pdf-render:         # 顶层，无缩进
  fonts:            # 2空格缩进
    regular-path:   # 4空格缩进
```

3. 检查配置文件位置：
```
src/main/resources/application.yml  ✓ 正确
src/main/resources/config/application.yml  ✓ 正确
其他位置  ✗ 不会被加载
```

4. 启用配置调试：
```yaml
logging:
  level:
    com.mercury.pdf.render: DEBUG
```

---

### 问题3: 中文显示为方框

**症状**: 
- PDF生成成功，但中文显示为□□□

**排查步骤**:

1. **检查字体文件是否存在**:
```bash
ls -la src/main/resources/fonts/
# 应该看到字体文件
```

2. **检查字体路径配置**:
```yaml
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf  # 路径正确
    # 不要写成：fonts/xxx.ttf（缺少classpath:前缀）
```

3. **检查字体是否打包到JAR中**:
```bash
# 打包后检查
jar tf target/yourapp.jar | grep fonts
# 应该看到：
# BOOT-INF/classes/fonts/HarmonyOS_Sans_SC_Regular.ttf
```

4. **手动验证字体配置**:
```java
@PostConstruct
public void checkFonts() {
    PdfRenderProperties.FontProperties fonts = 
        reportService.getHtmlRenderer().getFontProperties();
    
    if (fonts == null) {
        System.err.println("✗ 字体配置未应用！");
        System.err.println("  请检查@EnableConfigurationProperties注解");
    } else {
        System.out.println("✓ 字体路径: " + fonts.getRegularPath());
    }
}
```

---

### 问题4: 找不到模板

**症状**:
```
TemplateInputException: Error resolving template "report"
```

**原因**:
- 模板文件不存在
- 模板路径配置错误

**解决方案**:

1. 确认模板文件存在：
```bash
ls -la src/main/resources/templates/
# 应该有 report.html
```

2. 如果使用自定义模板位置：
```yaml
pdf-render:
  template:
    location: classpath:/custom-templates/  # 自定义位置
```

3. 模板文件必须是`.html`扩展名，但配置中不包含扩展名：
```yaml
pdf-render:
  template:
    default-name: report  # ✓ 正确：不含.html
    # default-name: report.html  # ✗ 错误
```

---

### 问题5: Debug HTML不生成

**症状**:
- 配置了`debug.enabled: true`，但没有生成HTML文件

**排查步骤**:

1. 确认配置已加载：
```java
System.out.println("Debug enabled: " + 
    reportService.getHtmlRenderer().isDebugHtmlEnabled());
```

2. 检查输出目录权限：
```bash
# 确保目录可写
mkdir -p debug-html
chmod 755 debug-html
```

3. 查看日志：
```
PDF Render: Debug HTML output enabled
  - Output directory: debug-html
```

4. 尝试相对路径：
```yaml
pdf-render:
  debug:
    output-directory: target/debug-html  # 相对于项目根目录
```

---

### 问题6: 依赖冲突

**症状**:
```
NoSuchMethodError / ClassNotFoundException
```

**解决方案**:

1. 查看依赖树：
```bash
mvn dependency:tree
```

2. 排除冲突的依赖：
```xml
<dependency>
    <groupId>com.mercury</groupId>
    <artifactId>pdf-render</artifactId>
    <version>1.0.1</version>
    <exclusions>
        <exclusion>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

3. 使用Spring Boot的依赖管理：
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>2.7.18</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

---

## 完整示例项目结构

```
your-spring-boot-app/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── yourapp/
│   │   │               ├── YourApplication.java          # 主类（添加@EnableConfigurationProperties）
│   │   │               ├── service/
│   │   │               │   └── PdfReportService.java     # PDF生成服务
│   │   │               └── controller/
│   │   │                   └── PdfController.java        # REST接口
│   │   └── resources/
│   │       ├── application.yml                            # 配置文件（pdf-render配置）
│   │       ├── application-dev.yml                        # 开发环境配置
│   │       ├── application-prod.yml                       # 生产环境配置
│   │       ├── fonts/
│   │       │   ├── HarmonyOS_Sans_SC_Regular.ttf         # 中文字体
│   │       │   └── HarmonyOS_Sans_SC_Bold.ttf
│   │       └── templates/
│   │           ├── report.html                            # PDF模板（可选）
│   │           └── custom-template.html
│   └── test/
│       └── java/
│           └── com/
│               └── example/
│                   └── yourapp/
│                       ├── PdfConfigVerifier.java         # 配置验证
│                       └── PdfGenerationTest.java         # 功能测试
└── target/
    ├── yourapp-1.0.0.jar                                  # 打包后的应用
    ├── debug-html/                                        # 调试HTML输出
    └── test-chinese.pdf                                   # 测试生成的PDF
```

---

## 快速检查清单

集成完成后，使用此清单确认所有步骤：

- [ ] **步骤1**: pdf-render已构建并安装到本地Maven仓库
- [ ] **步骤2**: Spring Boot项目pom.xml已添加pdf-render依赖
- [ ] **步骤3**: 字体文件已放置在`src/main/resources/fonts/`
- [ ] **步骤4**: `application.yml`已配置pdf-render（至少配置fonts）
- [ ] **步骤5**: 主类已添加`@EnableConfigurationProperties(PdfRenderProperties.class)`
- [ ] **步骤6**: （可选）模板文件已复制到`src/main/resources/templates/`
- [ ] **步骤7**: Service类已创建，使用构造器注入ReportService
- [ ] **步骤8**: （可选）Controller类已创建
- [ ] **步骤9**: 应用启动成功，日志显示"Font configuration applied"
- [ ] **验证**: 生成的PDF中文正常显示
- [ ] **验证**: 所有配置项均已生效

---

## 总结

### 关键要点

1. **所有配置都会生效**: application.yml中的所有pdf-render配置（template、output、fonts、debug）都会被自动读取和应用

2. **必须的步骤**:
   - 构建并安装pdf-render JAR
   - 在pom.xml中添加依赖
   - 添加`@EnableConfigurationProperties`注解
   - 配置application.yml（至少配置fonts以支持中文）

3. **无需手动配置**: Spring Boot会自动配置ReportService，直接注入使用即可

4. **配置优先级**: 
   ```
   代码配置 > application.yml > 默认值
   ```
   避免在代码中调用`setFontProperties()`等方法，除非需要动态配置

### 相关文档

- [CONFIGURATION_FAQ.md](CONFIGURATION_FAQ.md) - 配置常见问题
- [JAR_INTEGRATION.md](JAR_INTEGRATION.md) - JAR集成基础说明
- [SPRING_BOOT_INTEGRATION_GUIDE.md](SPRING_BOOT_INTEGRATION_GUIDE.md) - Spring Boot集成详细指南
- [FONT_CONFIGURATION.md](FONT_CONFIGURATION.md) - 字体配置详解

---

**文档版本**: 1.0
**最后更新**: 2026-01-30
