# 中文字体显示问题修复说明

## 问题描述
在 fonts 目录下放置字体后，测试用例中的中文还是没显示出来，都是空白。

## 根本原因
Flying Saucer PDF 渲染引擎在处理中文字符时需要特殊配置：
1. 必须使用 **Identity-H 编码**（Unicode 字符映射）
2. CSS 中的 `font-family` 必须与字体文件的**内部名称**完全匹配
3. 不能使用 `@font-face` 的 `src: url('classpath:...')` 语法

之前的代码没有使用 Identity-H 编码，导致中文字符在 PDF 生成过程中被完全过滤掉。

## 解决方案

### 1. 准备中文字体
已在仓库中包含了 Noto Sans CJK SC 字体（Google 开源字体）：
```
src/main/resources/fonts/NotoSansCJKsc-Regular.otf
```

### 2. 正确的配置方式

```java
import com.mercury.pdf.render.ReportService;
import com.mercury.pdf.render.config.FontConfig;

ReportService service = new ReportService();
service.

setUseHtmlPipeline(true);

// 配置字体
FontConfig fontConfig = new FontConfig();
fontConfig.

setRegularFontPath("classpath:/fonts/NotoSansCJKsc-Regular.otf");
// 重要：必须设置为字体的内部名称
fontConfig.

setDefaultFontFamily("Noto Sans CJK SC, DejaVu Sans, Arial, sans-serif");

service.

getHtmlRenderer().

setFontConfig(fontConfig);

// 生成 PDF
byte[] pdf = service.generatePdf(reportData);
```

### 3. 关键要点

**✅ 必须做：**
- 使用 `setDefaultFontFamily()` 设置字体的内部名称
- Noto Sans CJK SC 的内部名称是 `"Noto Sans CJK SC"`
- HarmonyOS Sans SC 的内部名称是 `"HarmonyOS Sans SC"`

**❌ 常见错误：**
- 使用文件名作为 font-family（错误：`"NotoSansCJKsc-Regular"`）
- 忘记设置 defaultFontFamily（会使用默认的 DejaVu Sans）
- 使用不支持中文的字体

## 验证方法

运行测试查看效果：
```bash
mvn test -Dtest=ChineseFontTest
```

生成的 PDF 文件位于：
- `test-output/chinese_text_custom_fonts.pdf` - 使用中文字体
- `test-output/chinese_text_default_fonts.pdf` - 未配置字体（对比用）

## 技术细节

### Identity-H 编码
- Adobe 定义的 Unicode CMap 编码
- 允许 Unicode 字符直接映射到字体字形
- CJK 字体的标准编码方式
- **没有此编码，中文字符会被过滤**

### 实现原理
```java
// HtmlReportRenderer.java
renderer.getFontResolver().addFont(fontPath, "Identity-H", true);
//                                           ^^^^^^^^^^^
//                                           关键：Unicode 编码
```

### 字体内部名称
字体文件包含内部元数据，定义了字体的正式名称。CSS 必须使用这个名称才能引用字体。

常见字体的内部名称：
- Noto Sans CJK SC → `"Noto Sans CJK SC"`
- HarmonyOS Sans SC → `"HarmonyOS Sans SC"`  
- Source Han Sans CN → `"Source Han Sans SC"`
- 思源黑体 → `"Source Han Sans SC"`

## 推荐字体

| 字体 | 内部名称 | 许可 | 下载地址 |
|------|---------|------|----------|
| Noto Sans CJK SC | Noto Sans CJK SC | SIL OFL | https://github.com/googlefonts/noto-cjk |
| HarmonyOS Sans | HarmonyOS Sans SC | 免费商用 | https://developer.harmonyos.com |
| 思源黑体 | Source Han Sans SC | SIL OFL | https://github.com/adobe-fonts/source-han-sans |

## 示例代码

完整的使用示例参见：
- `src/test/java/com/finos/matcher/report/ChineseFontTest.java`

详细的配置文档：
- `FONT_CONFIGURATION.md`（中英文双语）

## 效果对比

### 修复前
PDF 中只显示：
```
Page 1
Report Date: 2024-12-31
Page 2
```
中文内容完全消失 ❌

### 修复后  
PDF 中正确显示：
```
Page 1
中文测试报告
测试中文字体显示
Report Date: 2024-12-31
Page 2
第一章
这是中文段落测试。
```
中文内容完整显示 ✅

## 总结

修复包含三个关键步骤：
1. 使用 Identity-H 编码注册字体
2. 设置正确的字体内部名称
3. 提供中文字体文件

现在仓库中已经包含了示例字体文件和正确的配置方式，所有测试都通过，中文字符可以正常显示！
