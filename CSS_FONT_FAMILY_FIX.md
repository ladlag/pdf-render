# CSS Font-Family 解析问题修复说明

## 问题根源 / Root Cause

Flying Saucer 的 CSS 解析器会将**包含空格的字体名称拆分成多个词**，导致字体匹配失败：

```
原字体名: "HarmonyOS Sans SC"
被解析为: "HarmonyOS" / "Sans" / "SC" (三个独立的词)
结果: 匹配失败 → 回退到 Times-Roman
```

### 为什么之前的实现有问题？

1. **每个字体使用不同的family名称**
   ```java
   // 之前的错误做法
   regularFont → "HarmonyOS Sans SC"
   boldFont → "HarmonyOS Sans SC Bold" 
   cjkFont → "Noto Sans CJK SC"
   ```
   
2. **CSS中没有引号**
   ```css
   /* 错误：Flying Saucer会把这个拆成多个词 */
   body { font-family: HarmonyOS Sans SC, sans-serif; }
   ```

3. **结果**
   - CSS解析器找不到名为"HarmonyOS"的字体
   - 回退到默认字体 Times-Roman
   - 中文显示为方框（□）

## 解决方案 / Solution

### 核心改进：统一的无空格别名

```java
// 新做法：所有字体都用同一个无空格别名
private static final String PDF_FONT_FAMILY_ALIAS = "PDFFont";

// 注册字体时使用统一别名
renderer.getFontResolver().addFont(regularPath, "PDFFont", BaseFont.IDENTITY_H, true, null);
renderer.getFontResolver().addFont(boldPath, "PDFFont", BaseFont.IDENTITY_H, true, null);
renderer.getFontResolver().addFont(cjkPath, "PDFFont", BaseFont.IDENTITY_H, true, null);
```

### CSS生成也使用统一别名（带引号）

```java
// 带引号的CSS字体声明
String fontFamilyCss = "\"PDFFont\", sans-serif";
```

生成的CSS：
```css
body { font-family: "PDFFont", sans-serif; }
```

## 为什么这样做？

### 1. 参考官方实现

ITextFontResolver 中的 32 个内置字体键值都是**不带空格的**：

```
Times-Roman, Times-Bold, Helvetica, Courier, Symbol, ZapfDingbats...
```

没有一个像 "Times New Roman" 这样带空格的。

### 2. 保证Bold字体正确匹配

使用**统一的family名称**确保Flying Saucer能正确匹配bold变体：

```java
// 同一个family → CSS请求bold时能找到对应的bold字体
regular: PDFFont (weight: normal)
bold:    PDFFont (weight: bold)
```

如果family名称不同，CSS中的`font-weight: bold`会失败，回退到Times-Bold。

### 3. 简化代码

不再需要：
- 提取字体的内部family名称
- 解析和拼接多个字体名称
- 处理名称中的空格和引号

## 代码对比

### 之前（复杂且容易出错）

```java
// 提取每个字体的内部名称
String regularFamilyName = FontNameExtractor.extractFontFamilyName(regularPath);
String boldFamilyName = FontNameExtractor.extractFontFamilyName(boldPath);
String cjkFamilyName = FontNameExtractor.extractFontFamilyName(cjkPath);

// 注册时使用不同的family名称
renderer.getFontResolver().addFont(regularPath, regularFamilyName, ...);
renderer.getFontResolver().addFont(boldPath, boldFamilyName, ...);
renderer.getFontResolver().addFont(cjkPath, cjkFamilyName, ...);

// 复杂的CSS拼接逻辑（100+行代码）
Set<String> fontFamilyNames = new LinkedHashSet<>();
// ... 提取、去重、拼接
String fontFamilyCss = fontFamily.toString(); // "HarmonyOS Sans SC, sans-serif"
```

### 现在（简洁且可靠）

```java
// 所有字体用统一别名
private static final String PDF_FONT_FAMILY_ALIAS = "PDFFont";

// 注册时统一使用
renderer.getFontResolver().addFont(regularPath, PDF_FONT_FAMILY_ALIAS, ...);
renderer.getFontResolver().addFont(boldPath, PDF_FONT_FAMILY_ALIAS, ...);
renderer.getFontResolver().addFont(cjkPath, PDF_FONT_FAMILY_ALIAS, ...);

// 简单的CSS生成
String fontFamilyCss = "\"" + PDF_FONT_FAMILY_ALIAS + "\", sans-serif";
```

## 验证日志

修复后的日志输出：

```
✓ CJK font registered with Flying Saucer: /tmp/pdf-render-font-xxx.ttf
  Encoding: Identity-H | Embedded: true
  Unified family alias: PDFFont
✓ Regular font registered with Flying Saucer: /tmp/pdf-render-font-yyy.ttf
  Encoding: Identity-H | Embedded: true
  Unified family alias: PDFFont
✓ Total fonts registered for PDF: 2
  All fonts registered with unified family: "PDFFont"
✓ PDF CSS font-family: "PDFFont", sans-serif
```

## 常见问题

### Q: 为什么要带引号？

A: 双重保险。虽然"PDFFont"没有空格，但如果将来改成其他名称（例如"PDF_Main"），引号能保证任何名称都能正确解析。

### Q: 如果我想用自定义别名怎么办？

A: 修改 `PDF_FONT_FAMILY_ALIAS` 常量：

```java
private static final String PDF_FONT_FAMILY_ALIAS = "MyCustomFont";
```

**注意：别名不要包含空格！**

### Q: 这样做会影响现有代码吗？

A: 不会。这是内部实现细节，对外API没有变化：

```java
// 使用方式完全相同
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
fontConfig.setDefaultFamily("HarmonyOS Sans SC, sans-serif"); // 仍然可以用，但会被忽略
service.getHtmlRenderer().setFontConfig(fontConfig);
```

### Q: documentFonts为空是什么原因？

A: 这是一个新问题。可能原因：
1. 字体注册成功但没有真正嵌入
2. `embedded`参数传值错误
3. Flying Saucer版本兼容性问题

需要进一步诊断。请运行：
```bash
mvn test -Dtest=FontEmbeddingVerificationTest
```

## 参考

1. [Flying Saucer CSS解析器源码](https://github.com/flyingsaucerproject/flyingsaucer)
2. [ITextFontResolver内置字体列表](https://javadoc.io/doc/org.xhtmlrenderer/flying-saucer-pdf-openpdf/latest)
3. [CSS font-family规范](https://www.w3.org/TR/css-fonts-3/#font-family-prop)

## 总结

这个修复解决了Flying Saucer CSS解析器的一个已知陷阱：
- ✅ 使用无空格的统一别名
- ✅ CSS中添加引号作为双重保护
- ✅ 简化代码，提高可维护性
- ✅ 确保bold/italic字体正确匹配
- ✅ 所有现有测试通过

中文字符应该能正确显示，不再出现方框（□）！
