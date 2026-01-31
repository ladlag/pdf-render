# Spring Boot 图表中文显示问题排查指南
# Spring Boot Chart Chinese Display Troubleshooting Guide

## 问题描述 / Problem Description

用户反馈：
- 使用 Spring Boot 的 application.yml 配置
- 使用 matcher-report-final 模板
- 已设置 regularFontPath
- **正文中文显示正常** ✓
- **图表中文显示为方框** ✗

User reported:
- Using Spring Boot application.yml configuration
- Using matcher-report-final template
- regularFontPath is set
- **Body text Chinese displays correctly** ✓
- **Chart Chinese shows as boxes** ✗

---

## 测试结果 / Test Results

我们创建了完整的测试来复现您的场景，测试**全部通过** ✅

We created comprehensive tests to replicate your scenario, and **all tests pass** ✅

```
SpringBootChartTest:
  ✓ Chart font loaded successfully
  ✓ CSS font-family has proper quotes
  ✓ PDF generated with embedded fonts (63 KB)
```

这说明代码本身是正确的，问题可能在于具体配置。

This indicates the code itself is correct; the issue might be in the specific configuration.

---

## 排查步骤 / Troubleshooting Steps

### 步骤 1: 启用调试模式 / Step 1: Enable Debug Mode

在 `application.yml` 中启用调试：

Enable debug in `application.yml`:

```yaml
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    default-family: HarmonyOS Sans SC, sans-serif
  debug:
    enabled: true                    # 启用调试 / Enable debug
    output-directory: debug-output   # 调试输出目录 / Debug output directory
```

生成 PDF 后，检查 `debug-output/` 目录下的 HTML 文件。

After generating PDF, check HTML files in `debug-output/` directory.

---

### 步骤 2: 检查日志输出 / Step 2: Check Log Output

生成 PDF 时，应该看到以下日志：

When generating PDF, you should see these logs:

```
✓ Configuring chart font from regular font: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
✓ Chart font loaded successfully: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
  Chart font family: HarmonyOS Sans SC
  Chart font name: HarmonyOS Sans SC
```

**如果看不到这些日志：**
- 检查 regularPath 是否正确配置
- 检查字体文件是否存在于 classpath 中

**If you don't see these logs:**
- Check if regularPath is correctly configured
- Check if font file exists in classpath

---

### 步骤 3: 验证字体文件 / Step 3: Verify Font File

确保字体文件在正确位置：

Ensure font file is in the correct location:

```
src/main/resources/fonts/HarmonyOS_Sans_SC_Regular.ttf
```

在 Maven 项目中，这会被打包到 JAR 的 classpath 中。

In Maven projects, this will be packaged into the JAR's classpath.

**验证方法 / Verification:**

```bash
# 在 IDE 中运行或使用 Maven
# Run in IDE or with Maven
mvn compile exec:java -Dexec.mainClass="com.mercury.pdf.render.ChartFontDiagnostic"
```

这个诊断工具会显示：
- 字体文件是否找到
- 图表字体是否成功加载
- CSS 是否有正确的引号

This diagnostic tool will show:
- Whether font file is found
- Whether chart font loaded successfully
- Whether CSS has proper quotes

---

### 步骤 4: 检查完整配置 / Step 4: Check Complete Configuration

**推荐的完整 application.yml 配置：**

**Recommended complete application.yml configuration:**

```yaml
pdf-render:
  # 模板配置 / Template configuration
  template:
    default-name: matcher-report-final   # 或其他模板 / or other template
    cache-enabled: true
  
  # 字体配置 / Font configuration
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    default-family: HarmonyOS Sans SC, sans-serif
    # 注意: default-family 可以不设置，会使用默认的 "PDFFont" 别名
    # Note: default-family can be omitted, will use default "PDFFont" alias
  
  # 调试配置 / Debug configuration
  debug:
    enabled: true
    output-directory: debug-output
    include-timestamp: false
```

---

### 步骤 5: 常见问题 / Step 5: Common Issues

#### 问题 1: 字体文件路径错误

**错误示例：**
```yaml
regular-path: /fonts/HarmonyOS_Sans_SC_Regular.ttf  # ❌ 缺少 classpath:
regular-path: fonts/HarmonyOS_Sans_SC_Regular.ttf   # ❌ 缺少前导斜杠
```

**正确示例：**
```yaml
regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf  # ✓
```

#### 问题 2: 只设置了 defaultFamily，没设置 regularPath

**错误配置：**
```yaml
fonts:
  default-family: HarmonyOS Sans SC, sans-serif  # ❌ 只有这个不够
```

**原因：** 图表需要实际的字体文件（regularPath），仅设置 defaultFamily 只影响 CSS。

**Reason:** Charts need the actual font file (regularPath). Setting only defaultFamily affects CSS only.

**正确配置：**
```yaml
fonts:
  regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf  # ✓ 必须设置
  default-family: HarmonyOS Sans SC, sans-serif
```

#### 问题 3: 字体文件不在 JAR 中

**检查方法：**
```bash
# 构建 JAR 后检查
jar tf target/your-app.jar | grep fonts
```

应该看到：
```
fonts/HarmonyOS_Sans_SC_Regular.ttf
```

如果看不到，确保字体文件在 `src/main/resources/fonts/` 目录下。

If you don't see it, ensure font file is in `src/main/resources/fonts/` directory.

---

## 如何获取帮助 / How to Get Help

如果按照上述步骤仍然无法解决，请提供以下信息：

If issue persists after following above steps, please provide:

1. **完整的 application.yml 配置**
   Complete application.yml configuration

2. **运行诊断工具的完整输出**
   Complete output from diagnostic tool:
   ```bash
   mvn compile exec:java -Dexec.mainClass="com.mercury.pdf.render.ChartFontDiagnostic"
   ```

3. **生成 PDF 时的完整日志**
   Complete logs when generating PDF

4. **Debug HTML 文件内容**（特别是 CSS 部分）
   Debug HTML file content (especially CSS section)

5. **项目的目录结构**（fonts 文件夹位置）
   Project directory structure (fonts folder location)

---

## 测试验证 / Test Verification

我们的测试确认以下场景工作正常：

Our tests confirm these scenarios work correctly:

✅ Spring Boot + application.yml 配置
✅ matcher-report-final 模板
✅ regularFontPath 设置
✅ 图表中文显示
✅ CSS 字体引号正确
✅ 字体嵌入 PDF

测试代码：`src/test/java/com/mercury/pdf/render/SpringBootChartTest.java`

Test code: `src/test/java/com/mercury/pdf/render/SpringBootChartTest.java`

运行测试：
```bash
mvn test -Dtest=SpringBootChartTest
```

---

## 总结 / Summary

**关键要点：**

1. ✅ regularPath **必须**设置（图表需要）
2. ✅ defaultFamily 可选（有智能默认值）
3. ✅ 字体文件必须在 classpath 中
4. ✅ 启用 debug 模式可以看到详细信息
5. ✅ 查看日志确认字体是否加载成功

**Key points:**

1. ✅ regularPath **must** be set (charts need it)
2. ✅ defaultFamily is optional (has smart default)
3. ✅ Font file must be in classpath
4. ✅ Enable debug mode to see details
5. ✅ Check logs to confirm font loading success

如果您按照上述步骤操作，图表中文应该能正常显示。

If you follow the above steps, chart Chinese should display correctly.
