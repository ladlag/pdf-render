# 容器环境字体问题诊断 / Container Font Issues Diagnostic

## 问题场景 / Problem Scenario

**现象 Symptoms:**
- ✅ 容器内JAR解压后，fonts目录中有字体文件
- ✅ 看到了字体加载日志
- ❌ PDF中文仍然显示为方框（□）

**This means:**
- Fonts ARE in the JAR
- Font loading code IS executing
- BUT Chinese characters still show as boxes

## 诊断步骤 / Diagnostic Steps

### 步骤1: 检查完整的日志输出

**进入容器 Enter container:**
```bash
# Docker
docker exec -it <container-name> sh

# Kubernetes
kubectl exec -it <pod-name> -- sh
```

**查看应用日志 Check application logs:**
```bash
# Docker
docker logs <container-name> 2>&1 | grep -i font

# Kubernetes
kubectl logs <pod-name> | grep -i font
```

**必须看到这些日志 Must see these logs:**

```
✓ Font extracted for PDF rendering: HarmonyOS_Sans_SC_Regular.ttf
✓ Font registered with Flying Saucer: /tmp/pdf-render-font-xxx.ttf
  Encoding: Identity-H | Embedded: true
  Font family name (for CSS): HarmonyOS Sans SC
✓ Total fonts registered for PDF: 1
```

**关键检查点 Key checkpoints:**

1. ✅ 看到 "Font extracted" → 字体从JAR提取成功
2. ✅ 看到 "Font registered" → 字体注册到Flying Saucer成功
3. ✅ 看到 "Font family name: HarmonyOS Sans SC" → 字体内部名称

### 步骤2: 验证字体配置

**检查环境变量 Check environment variables:**
```bash
# 在容器内
env | grep PDF_RENDER
env | grep FONT

# 应该看到 Should see:
# PDF_RENDER_FONTS_REGULAR_PATH=classpath:/fonts/...
# PDF_RENDER_FONTS_DEFAULT_FAMILY=HarmonyOS Sans SC, sans-serif
```

**检查application.yml Check application.yml:**
```bash
# 如果使用ConfigMap
cat /app/config/application.yml | grep -A 5 "pdf-render"
```

### 步骤3: 检查临时文件

**查看提取的字体文件 Check extracted font files:**
```bash
# 在容器内
ls -lh /tmp/pdf-render-font-*

# 应该看到 Should see:
# -rw-r--r-- 1 appuser appuser 7.9M Jan 30 05:00 /tmp/pdf-render-font-123456.ttf
```

**验证文件完整性 Verify file integrity:**
```bash
# 检查文件大小
ls -lh /tmp/pdf-render-font-* | awk '{print $5}'
# HarmonyOS: 应该约 7-8MB
# Noto Sans CJK: 应该约 15-16MB

# 如果文件很小（<1MB），说明提取失败
```

### 步骤4: 检查JAR中的字体

**解压JAR验证字体 Unpack JAR to verify fonts:**
```bash
# 在容器内
cd /tmp
mkdir jar-check
cd jar-check
jar xf /app/app.jar

# 检查字体文件
ls -lh BOOT-INF/classes/fonts/
# 应该看到字体文件

# 检查文件大小
ls -lh BOOT-INF/classes/fonts/*.ttf
```

## 常见问题及解决方案 / Common Issues and Solutions

### 问题1: 只看到 "Font extracted" 但没有 "Font registered"

**原因 Cause:** 
- `defaultFontFamily` 未配置
- 字体注册失败

**诊断 Diagnosis:**
```bash
# 查看完整日志
docker logs <container> 2>&1 | grep -A 5 "Font extracted"

# 如果看到：
# ✓ Font extracted...
# 但是没有后续的 "Font registered"
# 说明字体提取了但注册失败
```

**解决方案 Solution:**

检查配置，必须设置 `default-family`:

```yaml
# application.yml
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    default-family: HarmonyOS Sans SC, sans-serif  # ← 必须设置！MUST SET!
```

**或者通过环境变量 Or via environment variables:**
```bash
PDF_RENDER_FONTS_DEFAULT_FAMILY="HarmonyOS Sans SC, sans-serif"
```

### 问题2: 看到 "Font registered" 但中文仍是方框

**原因 Cause:**
- `font-family` 名称与字体内部名称不匹配
- CSS无法找到字体

**诊断 Diagnosis:**

查看日志中的字体内部名称：
```
✓ Font registered with Flying Saucer: ...
  Font family name (for CSS): HarmonyOS Sans SC  ← 这是关键！
```

**解决方案 Solution:**

确保 `default-family` 包含日志中显示的准确名称：

```yaml
# ✅ 正确 - 名称匹配
pdf-render:
  fonts:
    default-family: HarmonyOS Sans SC, sans-serif
    # "HarmonyOS Sans SC" 必须与日志中的 "Font family name" 完全一致

# ❌ 错误 - 名称不匹配
pdf-render:
  fonts:
    default-family: HarmonyOS Sans, sans-serif  # 缺少 "SC"
    # 或
    default-family: Arial, sans-serif  # 完全不包含 "HarmonyOS Sans SC"
```

### 问题3: 临时目录权限问题

**症状 Symptom:**
```
java.io.IOException: Permission denied: /tmp/pdf-render-font-xxx.ttf
```

**解决方案 Solution:**

**Dockerfile中添加 Add to Dockerfile:**
```dockerfile
# 创建并设置临时目录权限
RUN mkdir -p /tmp && chmod 777 /tmp

# 或使用专用临时目录
RUN mkdir -p /app/temp && chmod 777 /app/temp
ENV JAVA_OPTS="-Djava.io.tmpdir=/app/temp"
```

**Kubernetes中使用emptyDir In Kubernetes use emptyDir:**
```yaml
volumeMounts:
- name: tmp
  mountPath: /tmp
volumes:
- name: tmp
  emptyDir:
    sizeLimit: 1Gi
```

### 问题4: 字体文件未打包到JAR

**症状 Symptom:**
```
Font not found in classpath: /fonts/xxx.ttf
```

**诊断 Diagnosis:**
```bash
# 检查JAR内容
jar tf /app/app.jar | grep fonts

# 应该看到 Should see:
# BOOT-INF/classes/fonts/HarmonyOS_Sans_SC_Regular.ttf
```

**解决方案 Solution:**

确保字体在正确位置：
```
项目结构 Project structure:
src/main/resources/
└── fonts/
    └── HarmonyOS_Sans_SC_Regular.ttf
```

**pom.xml确认资源配置 Verify pom.xml:**
```xml
<build>
  <resources>
    <resource>
      <directory>src/main/resources</directory>
      <includes>
        <include>**/*</include>
      </includes>
    </resource>
  </resources>
</build>
```

### 问题5: JVM字体系统配置问题

**症状 Symptom:**
- 日志正常但PDF仍显示方框
- 容器环境特有问题

**解决方案 Solution:**

**添加JVM参数 Add JVM options:**
```dockerfile
ENV JAVA_OPTS="-Djava.awt.headless=true \
               -Djava.io.tmpdir=/tmp \
               -Dfile.encoding=UTF-8"
```

**或在启动命令中 Or in startup command:**
```bash
java -Djava.awt.headless=true \
     -Djava.io.tmpdir=/tmp \
     -Dfile.encoding=UTF-8 \
     -jar app.jar
```

### 问题6: 容器镜像缺少fontconfig

**症状 Symptom:**
- Flying Saucer无法加载字体
- 特别是使用Alpine镜像时

**解决方案 Solution:**

**使用Debian/Ubuntu基础镜像 Use Debian/Ubuntu base:**
```dockerfile
FROM openjdk:11-jre-slim  # ✅ 推荐 Recommended

# 不要使用 Don't use:
# FROM openjdk:11-jre-alpine  # ❌ 可能缺少fontconfig
```

**或在Alpine中安装fontconfig Or install fontconfig in Alpine:**
```dockerfile
FROM openjdk:11-jre-alpine

RUN apk add --no-cache fontconfig ttf-dejavu
```

## 完整诊断脚本 / Complete Diagnostic Script

创建这个脚本在容器内运行完整诊断：

**container-font-check.sh:**
```bash
#!/bin/sh
# 容器内字体诊断脚本
# Container font diagnostic script

echo "=========================================="
echo "容器字体诊断 Container Font Diagnostic"
echo "=========================================="
echo ""

echo "1. 检查JAR中的字体 Check fonts in JAR"
echo "----------------------------------------"
if [ -f /app/app.jar ]; then
    jar tf /app/app.jar | grep fonts/
    echo ""
else
    echo "❌ JAR file not found at /app/app.jar"
    echo ""
fi

echo "2. 检查临时目录中的字体 Check fonts in temp"
echo "----------------------------------------"
ls -lh /tmp/pdf-render-font-* 2>/dev/null || echo "No extracted fonts found in /tmp"
echo ""

echo "3. 检查环境变量 Check environment variables"
echo "----------------------------------------"
env | grep PDF_RENDER || echo "No PDF_RENDER environment variables"
echo ""

echo "4. 检查临时目录权限 Check temp directory permissions"
echo "----------------------------------------"
ls -ld /tmp
echo ""

echo "5. 测试字体提取 Test font extraction"
echo "----------------------------------------"
cd /tmp
mkdir -p font-test
cd font-test
jar xf /app/app.jar BOOT-INF/classes/fonts/ 2>/dev/null
if [ -d BOOT-INF/classes/fonts ]; then
    echo "✓ 字体文件提取成功 Fonts extracted successfully:"
    ls -lh BOOT-INF/classes/fonts/
else
    echo "❌ 无法提取字体 Cannot extract fonts"
fi
cd /tmp
rm -rf font-test
echo ""

echo "6. 检查Java版本和属性 Check Java version and properties"
echo "----------------------------------------"
java -version 2>&1 | head -3
echo ""
echo "java.io.tmpdir:"
java -XshowSettings:properties 2>&1 | grep java.io.tmpdir
echo ""

echo "=========================================="
echo "诊断完成 Diagnostic complete"
echo "=========================================="
```

**在容器中运行 Run in container:**
```bash
# 复制脚本到容器
docker cp container-font-check.sh <container-name>:/tmp/

# 运行诊断
docker exec <container-name> sh /tmp/container-font-check.sh
```

## 快速修复命令 / Quick Fix Commands

### 修复1: 重新部署并检查日志

```bash
# 1. 停止当前容器
docker stop <container-name>

# 2. 删除容器
docker rm <container-name>

# 3. 重新运行，启用详细日志
docker run -d \
  -p 8080:8080 \
  -e LOGGING_LEVEL_COM_MERCURY_PDF_RENDER=DEBUG \
  -e PDF_RENDER_FONTS_REGULAR_PATH=classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf \
  -e PDF_RENDER_FONTS_DEFAULT_FAMILY="HarmonyOS Sans SC, sans-serif" \
  --name pdf-service \
  pdf-service:latest

# 4. 实时查看日志
docker logs -f pdf-service | grep -i font
```

### 修复2: 验证配置

**创建测试端点 Create test endpoint:**

在应用中添加测试端点检查配置：

```java
@RestController
@RequestMapping("/api/debug")
public class DebugController {
    
    @Autowired
    private ReportService reportService;
    
    @GetMapping("/font-config")
    public Map<String, Object> getFontConfig() {
        Map<String, Object> config = new HashMap<>();
        
        HtmlReportRenderer renderer = reportService.getHtmlRenderer();
        PdfRenderProperties.FontProperties fontProps = renderer.getFontProperties();
        
        if (fontProps != null) {
            config.put("regularPath", fontProps.getRegularPath());
            config.put("defaultFamily", fontProps.getDefaultFamily());
            config.put("configured", true);
        } else {
            config.put("configured", false);
        }
        
        return config;
    }
}
```

**调用检查 Call to check:**
```bash
curl http://localhost:8080/api/debug/font-config

# 应该返回 Should return:
# {
#   "regularPath": "classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf",
#   "defaultFamily": "HarmonyOS Sans SC, sans-serif",
#   "configured": true
# }
```

### 修复3: 运行诊断工具

**在容器内运行诊断工具 Run diagnostic in container:**
```bash
# Kubernetes
kubectl exec -it <pod-name> -- java -cp /app/app.jar \
  com.mercury.pdf.render.FontFileVerifier

# Docker
docker exec <container-name> java -cp /app/app.jar \
  com.mercury.pdf.render.FontFileVerifier
```

## 生产环境检查清单 / Production Checklist

在部署到生产前，确认以下所有项：

Before deploying to production, verify all items:

- [ ] **字体在JAR中** Fonts in JAR
  ```bash
  jar tf app.jar | grep fonts/
  ```

- [ ] **配置正确** Configuration correct
  ```yaml
  pdf-render:
    fonts:
      regular-path: classpath:/fonts/xxx.ttf
      default-family: HarmonyOS Sans SC, sans-serif  # ← 必须！
  ```

- [ ] **临时目录可写** Temp directory writable
  ```dockerfile
  RUN mkdir -p /tmp && chmod 777 /tmp
  ```

- [ ] **JVM参数正确** JVM options correct
  ```
  -Djava.awt.headless=true
  -Djava.io.tmpdir=/tmp
  -Dfile.encoding=UTF-8
  ```

- [ ] **日志显示字体注册** Logs show font registration
  ```
  ✓ Font registered with Flying Saucer
  ```

- [ ] **健康检查通过** Health checks pass
  ```bash
  curl http://localhost:8080/actuator/health
  ```

- [ ] **测试PDF生成** Test PDF generation
  ```bash
  curl -X POST http://localhost:8080/api/pdf/test
  ```

- [ ] **验证中文显示** Verify Chinese display
  打开生成的PDF，检查中文字符

## 仍然无法解决？ / Still Not Working?

如果按照以上步骤仍无法解决，请提供以下信息：

If still not working after above steps, provide:

1. **完整日志输出 Complete log output:**
   ```bash
   docker logs <container> > container.log 2>&1
   # 将 container.log 内容发送给支持团队
   ```

2. **容器环境信息 Container environment info:**
   ```bash
   docker exec <container> env | grep -E "PDF|JAVA|PATH"
   ```

3. **JAR内容检查 JAR content check:**
   ```bash
   docker exec <container> jar tf /app/app.jar | grep fonts
   ```

4. **PDF示例文件 Sample PDF file:**
   生成一个有问题的PDF文件用于分析

5. **Dockerfile内容 Dockerfile content:**
   完整的Dockerfile配置

## 相关文档 / Related Documentation

- [CONTAINER_DEPLOYMENT.md](CONTAINER_DEPLOYMENT.md) - 容器部署完整指南
- [CHINESE_QUICKSTART.md](CHINESE_QUICKSTART.md) - 快速开始
- [FONT_CONFIGURATION.md](FONT_CONFIGURATION.md) - 字体配置详解
- [TROUBLESHOOTING.md](TROUBLESHOOTING.md) - 通用故障排除
