# Spring Boot JAR集成问题修复说明 / Spring Boot JAR Integration Fix

[English](#english) | [中文](#chinese)

---

## <a name="chinese"></a>中文说明

### 问题描述

用户在将 pdf-render 项目打包为 JAR 并集成到 JDK8 Spring Boot 项目后，遇到以下问题：

1. **中文显示为方框（□）**: 生成的 PDF 报告中中文字符无法正常显示
2. **cjkPath 配置未加载**: 通过 debug 发现除了 `cjkPath` 外其他 properties 都正常加载
3. **Debug HTML 不生成**: 启用 debug 模式后，没有生成中间态 HTML 文件

### 根本原因

`PdfRenderAutoConfiguration` 类在创建 `ReportService` Bean 时，只配置了模板相关的属性，但**没有应用字体配置和调试配置**到 `HtmlReportRenderer`。

虽然 `PdfRenderProperties` 中定义了 `cjkPath`、`cjkFamily` 和 debug 相关属性，但这些配置从未被使用。

### 解决方案

我们修复了 `PdfRenderAutoConfiguration.reportService()` 方法，现在会正确地：

1. ✅ 从 `PdfRenderProperties.FontProperties` 读取所有字体配置
2. ✅ 创建并配置 `FontConfig` 对象（包括 `cjkPath`）
3. ✅ 将 `FontConfig` 应用到 `HtmlReportRenderer`
4. ✅ 从 `PdfRenderProperties.DebugProperties` 读取调试配置
5. ✅ 将调试配置应用到 `HtmlReportRenderer`

### 使用方法

#### 1. 在 Spring Boot 项目的 `application.yml` 中配置

```yaml
pdf-render:
  # 字体配置 - 必须配置才能正确显示中文
  fonts:
    # 常规字体路径
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    # 粗体路径（可选）
    bold-path: classpath:/fonts/HarmonyOS_Sans_SC_Bold.ttf
    # CJK字体路径（重要！用于中文显示）
    cjk-path: classpath:/fonts/NotoSansCJKsc-Regular.otf
    # 默认字体族
    default-family: HarmonyOS Sans SC, Noto Sans CJK SC, DejaVu Sans, sans-serif
    # CJK字体族
    cjk-family: Noto Sans CJK SC, SimSun, sans-serif
  
  # 调试配置 - 启用后会生成中间态HTML
  debug:
    # 是否启用HTML调试输出
    enabled: true
    # HTML输出目录
    output-directory: debug-html
    # 是否在文件名中包含时间戳
    include-timestamp: true
```

#### 2. 确保字体文件存在

将字体文件放置在项目的 `src/main/resources/fonts/` 目录：

```
src/main/resources/fonts/
├── HarmonyOS_Sans_SC_Regular.ttf
├── HarmonyOS_Sans_SC_Bold.ttf
└── NotoSansCJKsc-Regular.otf
```

#### 3. 字体文件获取

**推荐开源字体：**
- **思源黑体 (Noto Sans CJK)**: https://github.com/googlefonts/noto-cjk
- **鸿蒙字体 (HarmonyOS Sans)**: https://developer.harmonyos.com/cn/design/resource

#### 4. 在 Spring Boot 中使用

```java
@Service
public class MyPdfService {
    
    @Autowired
    private ReportService reportService;  // 自动注入，已配置好字体和调试
    
    public byte[] generateReport() throws IOException {
        ReportData data = ReportDataBuilder.create()
            .title("需求预审报告")  // 中文标题
            .addSection(new Section("第一章：匹配结果")
                .addParagraph("这是中文内容，现在可以正常显示了！"))
            .build();
        
        return reportService.generatePdf(data);
    }
}
```

### 验证修复

运行 `PdfRenderAutoConfigurationTest` 可以验证配置是否正确应用：

```bash
mvn test -Dtest=PdfRenderAutoConfigurationTest
```

测试验证了以下场景：
- ✅ 完整字体配置（regular, bold, cjk）
- ✅ 仅 CJK 字体配置
- ✅ 调试 HTML 配置
- ✅ 无字体配置时的行为
- ✅ 模板配置

### 技术细节

#### 修改的文件

1. **PdfRenderAutoConfiguration.java**
   - 添加字体配置逻辑（regular, bold, cjk, font families）
   - 添加调试配置逻辑（enabled, directory, timestamp）

2. **application.yml.example**
   - 添加 `cjk-path` 配置示例
   - 添加 `cjk-family` 配置示例
   - 完善调试配置说明

3. **PdfRenderAutoConfigurationTest.java**（新增）
   - 验证字体配置正确应用
   - 验证调试配置正确应用
   - 验证各种配置场景

#### 配置优先级

1. 如果配置了 `cjk-path`，将使用该字体处理中文字符
2. 如果只配置了 `regular-path`，也会用于中文（需要字体支持）
3. 如果都未配置，将使用系统默认字体（可能无法显示中文）

### 已知限制

- 字体文件需要支持完整的中文字符集
- 嵌入字体会增加 PDF 文件大小（10-20MB）
- 字体文件的内部名称必须与 CSS `font-family` 匹配

### 更多信息

- 详细字体配置指南：`FONT_CONFIGURATION.md`
- Spring Boot 集成指南：`SPRING_BOOT_INTEGRATION_GUIDE.md`
- 调试 HTML 配置：`DEBUG_HTML_CONFIGURATION.md`

---

## <a name="english"></a>English Documentation

### Problem Description

After packaging the pdf-render project as a JAR and integrating it into a JDK8 Spring Boot project, users encountered the following issues:

1. **Chinese characters displayed as boxes (□)**: Chinese text in generated PDF reports was not displaying correctly
2. **cjkPath property not loaded**: Through debugging, found that all properties were loaded except `cjkPath`
3. **Debug HTML not generated**: After enabling debug mode, intermediate HTML files were not being generated

### Root Cause

The `PdfRenderAutoConfiguration` class, when creating the `ReportService` Bean, only configured template-related properties but **did not apply font configuration and debug configuration** to the `HtmlReportRenderer`.

Although `cjkPath`, `cjkFamily`, and debug-related properties were defined in `PdfRenderProperties`, these configurations were never used.

### Solution

We fixed the `PdfRenderAutoConfiguration.reportService()` method to correctly:

1. ✅ Read all font configurations from `PdfRenderProperties.FontProperties`
2. ✅ Create and configure a `FontConfig` object (including `cjkPath`)
3. ✅ Apply `FontConfig` to `HtmlReportRenderer`
4. ✅ Read debug configuration from `PdfRenderProperties.DebugProperties`
5. ✅ Apply debug configuration to `HtmlReportRenderer`

### Usage

#### 1. Configure in Spring Boot's `application.yml`

```yaml
pdf-render:
  # Font configuration - Required for proper Chinese character display
  fonts:
    # Regular font path
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    # Bold font path (optional)
    bold-path: classpath:/fonts/HarmonyOS_Sans_SC_Bold.ttf
    # CJK font path (Important! Used for Chinese character rendering)
    cjk-path: classpath:/fonts/NotoSansCJKsc-Regular.otf
    # Default font family
    default-family: HarmonyOS Sans SC, Noto Sans CJK SC, DejaVu Sans, sans-serif
    # CJK font family
    cjk-family: Noto Sans CJK SC, SimSun, sans-serif
  
  # Debug configuration - Generates intermediate HTML when enabled
  debug:
    # Enable HTML debug output
    enabled: true
    # HTML output directory
    output-directory: debug-html
    # Include timestamp in filename
    include-timestamp: true
```

#### 2. Ensure Font Files Exist

Place font files in your project's `src/main/resources/fonts/` directory:

```
src/main/resources/fonts/
├── HarmonyOS_Sans_SC_Regular.ttf
├── HarmonyOS_Sans_SC_Bold.ttf
└── NotoSansCJKsc-Regular.otf
```

#### 3. Obtaining Font Files

**Recommended Open Source Fonts:**
- **Noto Sans CJK**: https://github.com/googlefonts/noto-cjk
- **HarmonyOS Sans**: https://developer.harmonyos.com/cn/design/resource

#### 4. Using in Spring Boot

```java
@Service
public class MyPdfService {
    
    @Autowired
    private ReportService reportService;  // Auto-injected, pre-configured with fonts and debug
    
    public byte[] generateReport() throws IOException {
        ReportData data = ReportDataBuilder.create()
            .title("Requirements Review Report")  // Chinese title
            .addSection(new Section("Chapter 1: Matching Results")
                .addParagraph("这是中文内容，现在可以正常显示了！"))
            .build();
        
        return reportService.generatePdf(data);
    }
}
```

### Verifying the Fix

Run `PdfRenderAutoConfigurationTest` to verify configurations are correctly applied:

```bash
mvn test -Dtest=PdfRenderAutoConfigurationTest
```

Tests verify the following scenarios:
- ✅ Complete font configuration (regular, bold, cjk)
- ✅ CJK-only font configuration
- ✅ Debug HTML configuration
- ✅ Behavior with no font configuration
- ✅ Template configuration

### Technical Details

#### Modified Files

1. **PdfRenderAutoConfiguration.java**
   - Added font configuration logic (regular, bold, cjk, font families)
   - Added debug configuration logic (enabled, directory, timestamp)

2. **application.yml.example**
   - Added `cjk-path` configuration example
   - Added `cjk-family` configuration example
   - Enhanced debug configuration documentation

3. **PdfRenderAutoConfigurationTest.java** (New)
   - Verifies font configuration is correctly applied
   - Verifies debug configuration is correctly applied
   - Verifies various configuration scenarios

#### Configuration Priority

1. If `cjk-path` is configured, that font will be used for Chinese characters
2. If only `regular-path` is configured, it will also be used for Chinese (if font supports it)
3. If neither is configured, system default fonts will be used (may not display Chinese)

### Known Limitations

- Font files must support full Chinese character set
- Embedding fonts increases PDF file size (10-20MB)
- Font file's internal name must match CSS `font-family`

### More Information

- Detailed font configuration guide: `FONT_CONFIGURATION.md`
- Spring Boot integration guide: `SPRING_BOOT_INTEGRATION_GUIDE.md`
- Debug HTML configuration: `DEBUG_HTML_CONFIGURATION.md`
