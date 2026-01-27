# 中文字体支持实现总结 / Chinese Font Support Implementation Summary

## 问题 (Problem)

用户报告：
> 是否支持指定字体？生成的pdf中包含大量中文，部署到服务器后是否会有编码问题导致文件生成失败或者生成的文件包含无法渲染的框

翻译：
- PDF包含大量中文文本
- 部署到服务器后担心编码问题
- 担心中文字符显示为框框（□）

## 根本原因 (Root Cause)

1. **默认字体不支持中文**：DejaVu Sans、Arial等默认字体不包含中文字形
2. **服务器环境缺少中文字体**：Linux服务器通常不预装中文字体
3. **Flying Saucer需要显式注册字体**：PDF渲染引擎需要手动注册和嵌入字体

## 解决方案 (Solution)

### 1. 字体配置系统

创建了完整的字体配置系统，支持：
- 编程方式配置（`FontConfig`类）
- Spring Boot YAML配置
- 自动字体嵌入到PDF

### 2. 核心实现

#### a) HtmlReportRenderer.java
```java
// 新增方法
public void setFontConfig(FontConfig fontConfig)
private void registerFontsWithRenderer(ITextRenderer renderer)
private String resolveFontPath(String path)
```

功能：
- 接受FontConfig配置
- 将字体注册到Flying Saucer的ITextRenderer
- 支持classpath和文件系统路径
- 优雅降级：字体加载失败时使用默认字体

#### b) FontConfig.java
```java
// 更新方法
public String getFontFamilyCss()
```

功能：
- 生成`@font-face` CSS声明
- 构建font-family列表（自定义字体 + CJK字体 + 后备字体）

#### c) 模板更新
所有6个模板（flexible, report, invoice, certificate, executive-summary, matcher-report-1.0）:
```html
<style th:inline="text">
  /* 自定义字体声明 */
  [(${fontFaceDeclaration})]
  
  body {
    font-family: [(${fontFamily})];
  }
</style>
```

### 3. 使用方式

#### 编程方式
```java
FontConfig fontConfig = new FontConfig();
fontConfig.setRegularFontPath("classpath:/fonts/HarmonyOS_SansSC_Regular.ttf");
fontConfig.setBoldFontPath("classpath:/fonts/HarmonyOS_SansSC_Bold.ttf");

service.getHtmlRenderer().setFontConfig(fontConfig);
```

#### Spring Boot方式
```yaml
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_SansSC_Regular.ttf
    bold-path: classpath:/fonts/HarmonyOS_SansSC_Bold.ttf
    default-family: HarmonyOS Sans SC, DejaVu Sans, sans-serif
```

### 4. 推荐字体

| 字体名称 | 授权 | 下载地址 |
|---------|------|----------|
| HarmonyOS Sans SC | 免费商用 | https://developer.harmonyos.com |
| Noto Sans CJK SC | SIL OFL | https://github.com/googlefonts/noto-cjk |
| 文泉驿 | GPL | http://wenq.org/wqy2/index.cgi |

## 测试

### 测试文件
- `ChineseFontTest.java` - 3个测试用例
  - 默认字体测试（会显示方框）
  - 自定义字体测试（正确显示）
  - Matcher报告测试（正确显示）

### 测试结果
- ✅ 所有25个测试通过
- ✅ 无安全漏洞（CodeQL扫描）
- ✅ 生成的PDF可在test-output目录查看

## 文档

### 新增文档
1. **FONT_CONFIGURATION.md** (8.8KB)
   - 中英文双语
   - 详细配置指南
   - FAQ
   - 技术细节

2. **README.md / README_CN.md**
   - 添加字体配置章节
   - 引用详细文档

3. **application.yml.example**
   - 增强字体配置示例
   - 添加详细注释

## 兼容性

### 向后兼容
- ✅ 不配置字体时，行为与之前完全一致
- ✅ 所有现有API保持不变
- ✅ 现有测试全部通过

### 新功能
- ✅ 可选配置字体
- ✅ 支持中日韩文字
- ✅ Spring Boot集成

## 部署建议

### 用户需要做什么

1. **获取字体文件**
   - 下载免费开源中文字体
   - 或使用已有的商业授权字体

2. **放置字体文件**
   ```
   src/main/resources/fonts/
   ├── HarmonyOS_SansSC_Regular.ttf
   └── HarmonyOS_SansSC_Bold.ttf
   ```

3. **配置路径**
   - 编程方式：调用`setFontConfig()`
   - Spring Boot：在`application.yml`配置

### 验证
生成包含中文的PDF，检查：
- ✅ 中文字符正确显示
- ❌ 不再显示方框（□）

## 技术亮点

1. **优雅降级**：字体加载失败时自动使用默认字体
2. **灵活配置**：支持编程和配置文件两种方式
3. **路径解析**：自动处理classpath和文件系统路径
4. **模板注入**：使用Thymeleaf动态注入字体配置
5. **完整文档**：中英文双语，覆盖所有使用场景

## 后续优化建议

1. **字体子集化**：只嵌入使用到的字符，减小PDF大小
2. **字体缓存**：缓存已加载的字体，提升性能
3. **SLF4J日志**：替换System.err为标准日志框架
4. **字体验证**：启动时验证字体文件有效性

## 结论

此实现完全解决了用户提出的问题：
- ✅ 支持自定义字体
- ✅ 中文正确显示
- ✅ 无编码问题
- ✅ 服务器部署无障碍

用户只需：
1. 下载中文字体
2. 配置路径
3. 生成PDF

就能完美渲染中文内容，不再出现方框。
