# 中文字体注册官方指南 / Official Guide for Chinese Font Registration

## 官方文档来源 / Official Documentation Sources

本项目的中文字体注册实现遵循以下官方文档和社区最佳实践：

This project's Chinese font registration follows these official documentation sources and community best practices:

### 1. 官方文档 / Official Documentation

- **Flying Saucer User's Guide (官方用户指南)**
  - URL: https://flyingsaucerproject.github.io/flyingsaucer/r8/guide/users-guide-R8.html
  - 详细说明了如何使用 Flying Saucer 进行 PDF 生成和字体管理

- **ITextFontResolver JavaDoc (API 文档)**
  - URL: https://javadoc.io/doc/org.xhtmlrenderer/flying-saucer-pdf-openpdf/latest
  - 提供了 `addFont()` 方法的完整签名和参数说明

- **OpenPDF 官方仓库**
  - URL: https://github.com/LibrePDF/OpenPDF
  - Flying Saucer PDF (OpenPDF) 的后端库，提供底层 PDF 生成功能

### 2. 社区最佳实践 / Community Best Practices

- **中文字体支持 - CSDN**
  - URL: https://blog.csdn.net/zhong_jianyu/article/details/96147949
  - 详细介绍了 Flying Saucer 中文字体配置和问题解决方案

- **Flying Saucer PDF 中文处理 - 51CTO**
  - URL: https://blog.51cto.com/u_16175450/6642806  
  - 提供了完整的中文字体注册示例代码

- **Stack Overflow - Font Embedding**
  - URL: https://stackoverflow.com/questions/7525403/how-to-embed-font-in-pdf-created-from-html-with-itext-and-flying-saucer
  - 国际社区关于 Flying Saucer 字体嵌入的讨论

---

## 官方字体注册方法 / Official Font Registration Method

### 核心原则 / Core Principles

根据官方文档和社区实践，中文字体注册必须遵循以下核心原则：

According to official documentation and community practices, Chinese font registration MUST follow these core principles:

#### 1. 使用正确的 addFont() 方法签名

**5 参数方法 (推荐用于中文字体) / 5-Parameter Method (Recommended for Chinese fonts):**

```java
void addFont(
    String path,                  // 字体文件路径 / Font file path
    String fontFamilyNameOverride, // CSS 字体名称覆盖 / CSS font family name override
    String encoding,              // 编码 / Encoding
    boolean embedded,             // 是否嵌入 / Whether to embed
    String pathToPFB              // Type 1 字体的 PFB 路径 / PFB path for Type 1 fonts
)
```

**实际调用示例 / Example Usage:**

```java
ITextRenderer renderer = new ITextRenderer();
renderer.getFontResolver().addFont(
    "/path/to/font.ttf",      // 字体文件路径
    "SimHei",                 // CSS 中使用的字体名称
    BaseFont.IDENTITY_H,      // ⚠️ 必须使用 IDENTITY_H 编码
    BaseFont.EMBEDDED,        // ⚠️ 必须嵌入字体 (true)
    null                      // TrueType 字体不需要 PFB
);
```

#### 2. BaseFont.IDENTITY_H 编码（关键！）

**这是中文字体支持的核心要求：**

- `BaseFont.IDENTITY_H` 是 **唯一支持** CJK (中文/日文/韩文) 字符的编码
- 这是行业标准，所有官方文档都推荐使用此编码
- 如果不使用此编码，中文字符会显示为方框 (□)

**为什么必须使用 IDENTITY_H？**

- IDENTITY_H 提供完整的 Unicode 支持
- 它是 PDF/A 标准中 CJK 字体的推荐编码
- OpenPDF 和 iText 都将其作为 CJK 字符的标准编码

**This is the CRITICAL requirement for Chinese font support:**

- `BaseFont.IDENTITY_H` is the **ONLY** encoding that supports CJK characters
- This is the industry standard recommended by all official documentation
- Without this encoding, Chinese characters will display as boxes (□)

**Why IDENTITY_H is required:**

- IDENTITY_H provides full Unicode support
- It's the recommended encoding for CJK fonts in PDF/A standards
- Both OpenPDF and iText designate it as the standard encoding for CJK

#### 3. 字体嵌入（强烈推荐）

**设置 `embedded=true` (使用 `BaseFont.EMBEDDED` 常量):**

- 将字体文件完整嵌入到 PDF 中
- 确保 PDF 在任何系统上都能正确显示
- 无需依赖用户系统上安装的字体

**Without embedding:**
- PDF 可能在没有安装该字体的系统上显示不正确
- 字体替换可能导致布局问题

**Set `embedded=true` (using `BaseFont.EMBEDDED` constant):**

- Embeds the complete font file into the PDF
- Ensures the PDF displays correctly on any system
- No dependency on fonts installed on the user's system

#### 4. 字体名称覆盖（Font Family Name Override）

**使用字体的内部名称：**

- 提取字体文件的实际内部名称（例如 "HarmonyOS Sans SC"）
- 在 CSS 中使用相同的名称：`font-family: 'HarmonyOS Sans SC';`
- 确保 CSS 和注册的字体名称精确匹配

**为什么需要名称覆盖？**

- JAR 部署时系统字体不可用
- 避免字体名称大小写或空格导致的匹配问题
- 提供跨平台的一致性

---

## 完整实现示例 / Complete Implementation Example

### Java 代码 / Java Code

```java
import org.xhtmlrenderer.pdf.ITextRenderer;
import com.lowagie.text.pdf.BaseFont;
import java.io.OutputStream;
import java.io.FileOutputStream;

public class ChinesePdfExample {
    
    public static void generatePdfWithChineseFont() throws Exception {
        // 1. 创建 ITextRenderer 实例
        ITextRenderer renderer = new ITextRenderer();
        
        // 2. 注册中文字体（遵循官方指南）
        // ⚠️ 关键：使用 BaseFont.IDENTITY_H 和 BaseFont.EMBEDDED
        renderer.getFontResolver().addFont(
            "/path/to/SimHei.ttf",     // 字体文件路径
            "SimHei",                  // 字体名称（用于 CSS）
            BaseFont.IDENTITY_H,       // ⚠️ 必须！Unicode CJK 编码
            BaseFont.EMBEDDED,         // ⚠️ 必须！嵌入字体
            null                       // TrueType 不需要 PFB
        );
        
        // 3. 设置 HTML 内容
        String html = "<html><head>" +
            "<style>body { font-family: 'SimHei'; }</style>" +
            "</head><body>" +
            "<h1>中文标题 Chinese Title</h1>" +
            "<p>这是中文内容 This is Chinese content</p>" +
            "</body></html>";
        
        // 4. 渲染 PDF
        renderer.setDocumentFromString(html);
        renderer.layout();
        
        // 5. 输出 PDF
        OutputStream os = new FileOutputStream("chinese_output.pdf");
        renderer.createPDF(os);
        os.close();
    }
}
```

### Spring Boot 配置 / Spring Boot Configuration

```yaml
# application.yml
pdf-render:
  fonts:
    # 使用 classpath 路径加载 JAR 中的字体
    regular-path: classpath:/fonts/SimHei.ttf
    
    # CSS 中使用的字体名称（必须与字体内部名称匹配）
    default-family: SimHei, sans-serif
```

```java
@Service
public class PdfService {
    
    private final PdfRenderService pdfRenderService;
    
    // ✅ 构造函数注入 - Spring Boot 会自动配置字体
    public PdfService(PdfRenderService pdfRenderService) {
        this.pdfRenderService = pdfRenderService;
    }
    
    public byte[] generatePdf(ReportData data) throws Exception {
        // 字体已经通过 Spring Boot 自动配置注册
        // 直接生成 PDF 即可
        return pdfRenderService.generatePdf(data);
    }
}
```

---

## 常见问题诊断 / Troubleshooting

### 问题 1: 中文显示为方框 (□)

**原因 / Cause:**
- 未使用 `BaseFont.IDENTITY_H` 编码
- 字体未注册
- CSS font-family 与注册的字体名称不匹配

**解决方案 / Solution:**
1. 确认使用了 `BaseFont.IDENTITY_H` 编码
2. 确认看到字体注册成功的日志：
   ```
   INFO: ✓ Font registered with Flying Saucer: /path/to/font.ttf
   INFO:   Encoding: Identity-H | Embedded: true
   ```
3. 检查 CSS 中的 `font-family` 是否与字体内部名称匹配

### 问题 2: 没有看到字体注册日志

**原因 / Cause:**
- 未调用字体注册方法
- Spring Boot 中使用了 `@PostConstruct` 导致配置被覆盖

**解决方案 / Solution:**
- 纯 Java: 确保调用了 `renderer.setFontConfig(fontConfig)`
- Spring Boot: 使用 `application.yml` 配置，不要使用 `@PostConstruct`

### 问题 3: PDF 在某些系统上显示异常

**原因 / Cause:**
- 字体未嵌入 (`embedded=false`)

**解决方案 / Solution:**
- 必须使用 `BaseFont.EMBEDDED` (即 `embedded=true`)
- 检查日志确认: `Embedded: true`

---

## 验证工具 / Verification Tools

### 1. 字体文件验证工具

```bash
mvn compile exec:java -Dexec.mainClass="com.mercury.pdf.render.FontFileVerifier"
```

输出示例：
```
字体 Font: HarmonyOS_Sans_SC_Regular.ttf
✓ 字体文件存在 Font file exists
  文件大小 File size: 7.88 MB
  字体内部名称 Internal name: HarmonyOS Sans SC

推荐配置 Recommended Configuration:
  regularFontPath: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
  defaultFontFamily: HarmonyOS Sans SC, sans-serif
```

### 2. 最小化字体测试

```bash
mvn compile exec:java -Dexec.mainClass="com.mercury.pdf.render.MinimalFontTest"
```

---

## 总结 / Summary

### 关键要点 / Key Points

1. **✅ 使用 5 参数的 `addFont()` 方法**
2. **✅ 必须使用 `BaseFont.IDENTITY_H` 编码** (这是核心!)
3. **✅ 必须设置 `embedded=true`** (使用 `BaseFont.EMBEDDED`)
4. **✅ 字体名称必须与 CSS 中的 font-family 匹配**
5. **✅ TrueType/OpenType 字体的 `pathToPFB` 参数设为 `null`**

### 官方支持的编码 / Officially Supported Encodings

- **BaseFont.IDENTITY_H** - ⭐ 推荐用于 CJK (中文/日文/韩文)
- BaseFont.CP1252 - 仅用于西文字符
- BaseFont.WINANSI - 仅用于 Windows 西文字符

### 不要使用的编码 / Encodings to AVOID for Chinese

- ❌ BaseFont.WINANSI - 不支持中文
- ❌ BaseFont.CP1252 - 不支持中文  
- ❌ null 或空字符串 - 会导致中文显示为方框

---

## 参考资源 / References

### 官方文档 / Official Documentation

1. Flying Saucer User's Guide - PDF Generation
   - https://flyingsaucerproject.github.io/flyingsaucer/r8/guide/users-guide-R8.html

2. ITextFontResolver API Documentation
   - https://javadoc.io/doc/org.xhtmlrenderer/flying-saucer-pdf-openpdf/latest

3. OpenPDF GitHub Repository
   - https://github.com/LibrePDF/OpenPDF

### 社区资源 / Community Resources

1. Flying Saucer 中文字体完整解决方案 (CSDN)
   - https://blog.csdn.net/zhong_jianyu/article/details/96147949

2. Flying Saucer PDF 中文问题处理 (51CTO)
   - https://blog.51cto.com/u_16175450/6642806

3. Font Embedding with iText and Flying Saucer (Stack Overflow)
   - https://stackoverflow.com/questions/7525403/how-to-embed-font-in-pdf-created-from-html-with-itext-and-flying-saucer

4. Flying Saucer Users Google Group
   - https://groups.google.com/g/flying-saucer-users

---

## 版本信息 / Version Information

本指南基于以下版本：

This guide is based on the following versions:

- Flying Saucer PDF (OpenPDF): 9.1.22
- OpenPDF: 1.3.11 (后端 / backend)
- Java: 1.8+

---

**注意 / Note:** 本文档遵循 OpenPDF 和 Flying Saucer 官方社区的推荐做法。如有任何疑问，请参考上述官方文档链接。

**Note:** This document follows the recommended practices from the OpenPDF and Flying Saucer official communities. For any questions, please refer to the official documentation links above.
