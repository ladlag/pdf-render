# 容器云部署指南 / Container Deployment Guide

## 问题描述 / Problem Description

**中文：** 服务部署到容器云（如Docker、Kubernetes）后，PDF中文显示为方框（□）

**English:** After deploying to container cloud (Docker, Kubernetes), Chinese characters appear as boxes (□) in PDF

## 根本原因 / Root Cause

容器环境与本地开发环境的主要区别：

Container environment differs from local development in several ways:

1. **字体文件必须打包进容器** 
   Font files must be packaged into the container

2. **临时目录可能不可写或路径不同**
   Temp directory may be read-only or have different path

3. **容器镜像可能缺少字体库**
   Container image may lack font packages

4. **资源路径在JAR中可能不同**
   Resource paths may differ when packaged in JAR

## 解决方案 / Solution

### 方案1：使用 classpath 字体（推荐 Recommended）

这是**最可靠的方案**，字体打包在应用JAR中。

This is the **most reliable approach** - fonts are packaged in application JAR.

#### 步骤 1: 确认字体在 resources 中

```
src/main/resources/
└── fonts/
    ├── HarmonyOS_Sans_SC_Regular.ttf
    └── NotoSansCJKsc-Regular.otf
```

#### 步骤 2: 配置使用 classpath 前缀

**application.yml:**
```yaml
pdf-render:
  fonts:
    # ✅ 使用 classpath: - 在容器中自动提取
    # ✅ Use classpath: - auto-extracts in container
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    default-family: HarmonyOS Sans SC, sans-serif
```

#### 步骤 3: 创建 Dockerfile

**Dockerfile (Spring Boot):**
```dockerfile
# 使用官方 OpenJDK 镜像
FROM openjdk:11-jre-slim

# 设置工作目录
WORKDIR /app

# 复制 JAR 文件（字体已打包在 JAR 中）
COPY target/*.jar app.jar

# 设置临时目录环境变量（可选）
ENV JAVA_OPTS="-Djava.io.tmpdir=/tmp"

# 确保临时目录可写
RUN mkdir -p /tmp && chmod 777 /tmp

# 暴露端口
EXPOSE 8080

# 运行应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

#### 步骤 4: 构建和运行

```bash
# 构建 JAR
mvn clean package

# 构建 Docker 镜像
docker build -t pdf-service:latest .

# 运行容器
docker run -p 8080:8080 pdf-service:latest
```

### 方案2：在容器中安装系统字体

如果需要使用系统字体而非打包字体。

If you need to use system fonts instead of packaged fonts.

**Dockerfile (安装字体):**
```dockerfile
FROM openjdk:11-jre-slim

# 安装中文字体包
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    fonts-noto-cjk \
    fonts-wqy-zenhei \
    fontconfig && \
    rm -rf /var/lib/apt/lists/*

# 刷新字体缓存
RUN fc-cache -fv

WORKDIR /app
COPY target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**注意：** 这会增加镜像大小（约 100-200MB）
**Note:** This increases image size by ~100-200MB

### 方案3：挂载字体目录（不推荐）

仅用于开发/测试环境。

Only for development/testing environments.

```bash
docker run -p 8080:8080 \
  -v /path/to/fonts:/app/fonts \
  -e PDF_RENDER_FONTS_REGULAR_PATH=/app/fonts/font.ttf \
  pdf-service:latest
```

## 完整示例 / Complete Example

### Spring Boot 应用 Dockerfile

**Dockerfile:**
```dockerfile
# ============================================
# Stage 1: Build (可选，如果需要在容器中构建)
# ============================================
FROM maven:3.8-openjdk-11 AS builder

WORKDIR /build
COPY pom.xml .
COPY src ./src

# 构建应用（字体文件在 src/main/resources/fonts 中）
RUN mvn clean package -DskipTests

# ============================================
# Stage 2: Runtime
# ============================================
FROM openjdk:11-jre-slim

# 设置环境变量
ENV LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    JAVA_OPTS="-Xmx512m -Djava.io.tmpdir=/tmp"

# 创建应用用户（安全最佳实践）
RUN useradd -m -u 1000 appuser

# 设置工作目录
WORKDIR /app

# 从构建阶段复制 JAR
COPY --from=builder /build/target/*.jar app.jar

# 确保临时目录存在且可写
RUN mkdir -p /tmp && chmod 777 /tmp

# 切换到非 root 用户
USER appuser

# 健康检查（可选）
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 暴露端口
EXPOSE 8080

# 运行应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### application.yml 配置

```yaml
# application.yml
spring:
  application:
    name: pdf-service

server:
  port: 8080

# PDF 渲染配置
pdf-render:
  fonts:
    # ✅ 使用 classpath - 字体在 JAR 中
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    default-family: HarmonyOS Sans SC, DejaVu Sans, sans-serif
    
  debug:
    # 容器中启用调试输出到 /tmp
    enabled: ${DEBUG_HTML:false}
    output-directory: /tmp/debug-html
    
logging:
  level:
    com.mercury.pdf.render: ${LOG_LEVEL:INFO}
```

### docker-compose.yml

```yaml
version: '3.8'

services:
  pdf-service:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - LOG_LEVEL=INFO
      - DEBUG_HTML=false
      # JVM 参数
      - JAVA_OPTS=-Xmx1g -Djava.io.tmpdir=/tmp
    volumes:
      # 可选：挂载调试输出目录
      - ./debug-html:/tmp/debug-html
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
    restart: unless-stopped
```

## Kubernetes 部署

### Deployment YAML

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: pdf-service
  labels:
    app: pdf-service
spec:
  replicas: 2
  selector:
    matchLabels:
      app: pdf-service
  template:
    metadata:
      labels:
        app: pdf-service
    spec:
      containers:
      - name: pdf-service
        image: your-registry/pdf-service:latest
        ports:
        - containerPort: 8080
          name: http
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: JAVA_OPTS
          value: "-Xmx1g -Djava.io.tmpdir=/tmp"
        # PDF 渲染配置
        - name: PDF_RENDER_FONTS_REGULAR_PATH
          value: "classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf"
        - name: PDF_RENDER_FONTS_DEFAULT_FAMILY
          value: "HarmonyOS Sans SC, sans-serif"
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        volumeMounts:
        - name: tmp
          mountPath: /tmp
      volumes:
      - name: tmp
        emptyDir: {}
---
apiVersion: v1
kind: Service
metadata:
  name: pdf-service
spec:
  selector:
    app: pdf-service
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: ClusterIP
```

## 常见问题排查 / Troubleshooting

### 问题1：字体文件未找到

**症状 Symptom:**
```
Font not found in classpath: /fonts/xxx.ttf
```

**检查 Check:**
```bash
# 检查 JAR 中是否包含字体
jar tf target/app.jar | grep fonts

# 应该看到：
# BOOT-INF/classes/fonts/HarmonyOS_Sans_SC_Regular.ttf
```

**解决 Solution:**
确保字体在 `src/main/resources/fonts/` 目录中。

### 问题2：临时目录不可写

**症状 Symptom:**
```
java.io.IOException: Permission denied: /tmp/pdf-render-font-xxx.ttf
```

**解决 Solution:**
```dockerfile
# 在 Dockerfile 中添加
RUN mkdir -p /tmp && chmod 777 /tmp

# 或设置不同的临时目录
ENV JAVA_OPTS="-Djava.io.tmpdir=/app/temp"
RUN mkdir -p /app/temp && chmod 777 /app/temp
```

### 问题3：中文仍显示为方框

**检查清单 Checklist:**

1. ✅ 字体文件在 JAR 中？
   ```bash
   jar tf app.jar | grep fonts
   ```

2. ✅ 配置使用 `classpath:` 前缀？
   ```yaml
   regular-path: classpath:/fonts/xxx.ttf  # ✅
   # 不是 Not: regular-path: /fonts/xxx.ttf  # ❌
   ```

3. ✅ 查看容器日志
   ```bash
   docker logs <container-id> | grep "Font registered"
   
   # 应该看到：
   # ✓ Font registered with Flying Saucer: /tmp/pdf-render-font-xxx.ttf
   ```

4. ✅ 进入容器检查
   ```bash
   docker exec -it <container-id> sh
   ls -la /tmp/pdf-render-font-*
   ```

### 问题4：镜像过大

**优化建议 Optimization:**

1. 使用 slim 镜像
   ```dockerfile
   FROM openjdk:11-jre-slim  # ✅ 小
   # 不是 FROM openjdk:11     # ❌ 大
   ```

2. 使用多阶段构建
   ```dockerfile
   FROM maven:3.8-openjdk-11 AS builder
   # ... 构建 ...
   
   FROM openjdk:11-jre-slim
   COPY --from=builder /build/target/*.jar app.jar
   ```

3. 选择合适的字体文件
   - HarmonyOS Sans SC: ~8 MB
   - Noto Sans CJK SC: ~16 MB

## 验证部署 / Verify Deployment

### 1. 本地 Docker 测试

```bash
# 构建
docker build -t pdf-service:test .

# 运行
docker run -d -p 8080:8080 --name pdf-test pdf-service:test

# 测试 API
curl -X POST http://localhost:8080/api/pdf/generate \
  -H "Content-Type: application/json" \
  -d '{"title":"测试报告","content":"中文内容"}' \
  --output test.pdf

# 检查日志
docker logs pdf-test | grep Font

# 应该看到：
# ✓ Font registered with Flying Saucer: ...
# ✓ Total fonts registered for PDF: 1

# 清理
docker stop pdf-test && docker rm pdf-test
```

### 2. 验证 PDF

```bash
# 在容器中生成测试 PDF
docker exec pdf-test curl -X POST http://localhost:8080/api/pdf/test

# 复制出来检查
docker cp pdf-test:/tmp/test.pdf ./

# 打开 test.pdf 验证中文显示
```

## 最佳实践 / Best Practices

### 1. 字体管理

✅ **推荐做法 Recommended:**
- 使用 `classpath:/fonts/` - 字体打包在 JAR 中
- 选择适当大小的字体文件（HarmonyOS ~8MB）
- 在 Maven/Gradle 中明确包含字体文件

```xml
<!-- pom.xml - 确保字体被打包 -->
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

### 2. 临时目录

✅ **推荐做法 Recommended:**
```dockerfile
# 创建专用临时目录
RUN mkdir -p /app/temp && chmod 777 /app/temp
ENV JAVA_OPTS="-Djava.io.tmpdir=/app/temp"
```

### 3. 监控和日志

✅ **推荐做法 Recommended:**
```yaml
# application.yml
logging:
  level:
    com.mercury.pdf.render: INFO  # 生产环境
    # com.mercury.pdf.render: DEBUG  # 调试时

management:
  endpoints:
    web:
      exposure:
        include: health,metrics
```

### 4. 资源限制

✅ **推荐做法 Recommended:**
```yaml
# Kubernetes 资源限制
resources:
  requests:
    memory: "512Mi"  # 最小内存
    cpu: "250m"      # 最小 CPU
  limits:
    memory: "1Gi"    # 最大内存（PDF 生成可能消耗内存）
    cpu: "1000m"     # 最大 CPU
```

## 环境变量配置 / Environment Variables

支持通过环境变量配置（Spring Boot特性）：

Supports configuration via environment variables (Spring Boot feature):

```bash
# 字体配置
PDF_RENDER_FONTS_REGULAR_PATH=classpath:/fonts/font.ttf
PDF_RENDER_FONTS_DEFAULT_FAMILY=HarmonyOS Sans SC, sans-serif

# 调试配置
PDF_RENDER_DEBUG_ENABLED=false
PDF_RENDER_DEBUG_OUTPUT_DIRECTORY=/tmp/debug-html

# JVM 参数
JAVA_OPTS=-Xmx1g -Djava.io.tmpdir=/tmp
```

## 云平台特定配置 / Cloud Platform Specific

### 阿里云 ACK / Alibaba Cloud

```yaml
# 使用 NAS 存储临时文件（可选）
volumeMounts:
- name: temp-storage
  mountPath: /tmp
volumes:
- name: temp-storage
  persistentVolumeClaim:
    claimName: pdf-temp-pvc
```

### 腾讯云 TKE / Tencent Cloud

```yaml
# 使用 CBS 存储（可选）
volumes:
- name: temp-storage
  persistentVolumeClaim:
    claimName: pdf-temp-pvc
```

### AWS EKS

```yaml
# 使用 EBS 或 emptyDir
volumes:
- name: tmp
  emptyDir:
    sizeLimit: 1Gi
```

## 总结 / Summary

### 关键点 Key Points

1. ✅ **使用 `classpath:` 前缀** - 最可靠
   Use `classpath:` prefix - most reliable

2. ✅ **字体必须在 `src/main/resources/fonts/`**
   Fonts must be in `src/main/resources/fonts/`

3. ✅ **确保临时目录可写**
   Ensure temp directory is writable

4. ✅ **使用 slim 镜像减小大小**
   Use slim images to reduce size

5. ✅ **通过日志验证字体加载**
   Verify font loading via logs

### 快速检查 Quick Check

```bash
# 1. 检查 JAR 中的字体
jar tf target/*.jar | grep fonts

# 2. 查看容器日志
docker logs <container> | grep Font

# 3. 检查临时目录
docker exec <container> ls -la /tmp/pdf-render-font-*

# 4. 测试 PDF 生成
curl http://localhost:8080/api/pdf/test --output test.pdf
```

## 相关文档 / Related Documentation

- [CHINESE_QUICKSTART.md](CHINESE_QUICKSTART.md) - 快速开始
- [WINDOWS_COMPATIBILITY.md](WINDOWS_COMPATIBILITY.md) - Windows 兼容性
- [FONT_CONFIGURATION.md](FONT_CONFIGURATION.md) - 字体配置详解
- [CHINESE_DISPLAY_COMPLETE_SOLUTION.md](CHINESE_DISPLAY_COMPLETE_SOLUTION.md) - 完整解决方案
