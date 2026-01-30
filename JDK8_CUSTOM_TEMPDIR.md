# OpenJDK 8 和自定义临时目录配置指南
# OpenJDK 8 and Custom Temp Directory Configuration Guide

## 快速开始 / Quick Start

如果您的容器环境：
- 使用 **OpenJDK 8**
- `/app/data` 目录**可用且可写**

If your container environment:
- Uses **OpenJDK 8**
- `/app/data` directory is **available and writable**

### 1. 使用预配置的 Dockerfile.jdk8

```bash
# 构建镜像
docker build -f Dockerfile.jdk8 -t pdf-service:jdk8 .

# 运行容器
docker run -p 8080:8080 pdf-service:jdk8

# 或使用 docker-compose
docker-compose up pdf-service-jdk8
```

### 2. Kubernetes 部署

```bash
# 应用 JDK 8 配置
kubectl apply -f kubernetes-deployment-jdk8.yaml

# 验证部署
kubectl get pods -l version=jdk8
kubectl logs -l version=jdk8 --tail=50
```

## 配置说明 / Configuration Details

### JDK 版本对比 / JDK Version Comparison

| 特性 Feature | OpenJDK 8 | OpenJDK 11 |
|-------------|-----------|------------|
| 默认配置文件 | `Dockerfile.jdk8` | `Dockerfile` |
| 推荐临时目录 | `/app/data` | `/tmp` |
| Maven构建镜像 | `maven:3.6-jdk-8-slim` | `maven:3.8-openjdk-11-slim` |
| 运行时镜像 | `openjdk:8-jre-slim` | `openjdk:11-jre-slim` |
| 项目支持 | ✅ 兼容 | ✅ 推荐 |

### 临时目录选择 / Temp Directory Selection

#### 选项 1: /app/data (推荐用于您的环境)

**优点 Advantages:**
- ✅ 您的环境已确认可用
- ✅ 应用专用目录，不与系统临时文件混合
- ✅ 便于调试和监控

**配置 Configuration:**
```bash
-Djava.io.tmpdir=/app/data
```

#### 选项 2: /tmp (标准选择)

**优点 Advantages:**
- ✅ Linux标准临时目录
- ✅ 大多数容器环境支持

**配置 Configuration:**
```bash
-Djava.io.tmpdir=/tmp
```

#### 选项 3: 自定义路径

**配置 Configuration:**
```bash
-Djava.io.tmpdir=/your/custom/path
```

**要求 Requirements:**
- 目录必须存在
- 目录必须可写
- 有足够空间（字体文件约10-20MB）

## 完整配置示例 / Complete Configuration Examples

### Docker 命令行

```bash
# OpenJDK 8 with /app/data
docker run -p 8080:8080 \
  -e JAVA_OPTS="-Xmx1g -Djava.io.tmpdir=/app/data" \
  -e PDF_RENDER_FONTS_REGULAR_PATH="classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf" \
  -e PDF_RENDER_FONTS_DEFAULT_FAMILY="HarmonyOS Sans SC, sans-serif" \
  -v $(pwd)/app-data:/app/data \
  pdf-service:jdk8
```

### docker-compose.yml

```yaml
version: '3.8'

services:
  pdf-service-jdk8:
    build:
      context: .
      dockerfile: Dockerfile.jdk8
    ports:
      - "8080:8080"
    environment:
      - JAVA_OPTS=-Xmx1g -Djava.io.tmpdir=/app/data
      - PDF_RENDER_FONTS_REGULAR_PATH=classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
      - PDF_RENDER_FONTS_DEFAULT_FAMILY=HarmonyOS Sans SC, sans-serif
    volumes:
      - ./app-data:/app/data
```

### Kubernetes ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: pdf-service-config
data:
  JAVA_OPTS: "-Xmx1g -Djava.io.tmpdir=/app/data"
  PDF_RENDER_FONTS_REGULAR_PATH: "classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf"
  PDF_RENDER_FONTS_DEFAULT_FAMILY: "HarmonyOS Sans SC, sans-serif"
```

### Kubernetes Deployment (关键部分)

```yaml
spec:
  containers:
  - name: pdf-service
    image: your-registry/pdf-service:jdk8
    env:
    - name: JAVA_OPTS
      value: "-Xmx1g -Xms512m -Djava.io.tmpdir=/app/data"
    volumeMounts:
    - name: app-data
      mountPath: /app/data
  volumes:
  - name: app-data
    emptyDir:
      sizeLimit: 1Gi
```

## 验证配置 / Verify Configuration

### 1. 检查临时目录

```bash
# 进入容器
docker exec -it <container-id> sh

# 或 Kubernetes
kubectl exec -it <pod-name> -- sh

# 检查临时目录
echo $JAVA_OPTS
ls -la /app/data/

# 生成PDF后检查字体文件
ls -la /app/data/pdf-render-font-*
```

### 2. 检查Java系统属性

```bash
# 在容器内
java -XshowSettings:properties -version 2>&1 | grep tmpdir

# 应该显示
# java.io.tmpdir = /app/data
```

### 3. 查看应用日志

```bash
# Docker
docker logs <container-id> | grep -i font

# Kubernetes
kubectl logs <pod-name> | grep -i font

# 应该看到
# ✓ Font extracted for PDF rendering: HarmonyOS_Sans_SC_Regular.ttf
# ✓ Font registered with Flying Saucer: /app/data/pdf-render-font-xxx.ttf
```

### 4. 测试PDF生成

```bash
# 调用API生成PDF（根据您的端点）
curl -X POST http://localhost:8080/api/pdf/generate \
  -H "Content-Type: application/json" \
  -d '{"title":"测试报告","content":"中文内容测试"}' \
  --output test.pdf

# 检查PDF文件
file test.pdf
# 应该显示: PDF document

# 打开PDF验证中文显示正确
```

## 故障排除 / Troubleshooting

### 问题1: /app/data 不存在或不可写

**症状 Symptom:**
```
java.io.IOException: Permission denied: /app/data/pdf-render-font-xxx.ttf
```

**解决方案 Solution:**

**在 Dockerfile 中:**
```dockerfile
RUN mkdir -p /app/data && \
    chmod 777 /app/data && \
    chown appuser:appuser /app/data
```

**在 Kubernetes 中:**
```yaml
volumeMounts:
- name: app-data
  mountPath: /app/data
volumes:
- name: app-data
  emptyDir: {}
```

### 问题2: JDK 8 特定错误

**症状 Symptom:**
```
UnsupportedClassVersionError: ... has been compiled by a more recent version
```

**解决方案 Solution:**
- 确认使用 `Dockerfile.jdk8`
- 检查 pom.xml 中的 Java 版本:
```xml
<properties>
    <maven.compiler.source>8</maven.compiler.source>
    <maven.compiler.target>8</maven.compiler.target>
</properties>
```

### 问题3: 字体提取到错误的目录

**症状 Symptom:**
字体提取到 `/tmp` 而不是 `/app/data`

**解决方案 Solution:**

确认 JAVA_OPTS 正确设置:
```bash
# 检查环境变量
docker exec <container> env | grep JAVA_OPTS

# 应该显示
# JAVA_OPTS=-Xmx1g -Djava.io.tmpdir=/app/data

# 如果不正确，更新配置
docker run -e JAVA_OPTS="-Djava.io.tmpdir=/app/data" ...
```

### 问题4: 空间不足

**症状 Symptom:**
```
java.io.IOException: No space left on device
```

**解决方案 Solution:**

增加存储限制:
```yaml
# Kubernetes
volumes:
- name: app-data
  emptyDir:
    sizeLimit: 2Gi  # 增加到 2GB
```

## 性能优化 / Performance Optimization

### JVM 参数调优 (OpenJDK 8)

```bash
# 基本配置 (512MB)
JAVA_OPTS="-Xmx512m -Xms256m -Djava.io.tmpdir=/app/data"

# 中等负载 (1GB)
JAVA_OPTS="-Xmx1g -Xms512m -Djava.io.tmpdir=/app/data"

# 高负载 (2GB)
JAVA_OPTS="-Xmx2g -Xms1g -Djava.io.tmpdir=/app/data -XX:+UseG1GC"

# 添加 GC 日志 (调试用)
JAVA_OPTS="-Xmx1g -Djava.io.tmpdir=/app/data -XX:+PrintGCDetails -XX:+PrintGCTimeStamps"
```

### 资源限制建议

| 场景 Scenario | CPU | Memory | 磁盘 Disk |
|--------------|-----|--------|----------|
| 轻量使用 Light | 0.25 | 512Mi | 500Mi |
| 中等使用 Medium | 0.5 | 1Gi | 1Gi |
| 高负载 Heavy | 1.0 | 2Gi | 2Gi |

## 迁移指南 / Migration Guide

### 从 JDK 11 迁移到 JDK 8

1. **更新构建配置**
```bash
# 使用 Dockerfile.jdk8
docker build -f Dockerfile.jdk8 -t your-app:jdk8 .
```

2. **更新部署配置**
```yaml
# 更新镜像引用
image: your-registry/pdf-service:jdk8
```

3. **测试验证**
```bash
# 运行测试
docker run your-app:jdk8 mvn test

# 验证PDF生成
curl http://localhost:8080/api/pdf/test
```

### 从 /tmp 迁移到 /app/data

1. **更新环境变量**
```bash
# 旧配置
-Djava.io.tmpdir=/tmp

# 新配置
-Djava.io.tmpdir=/app/data
```

2. **确保目录存在**
```dockerfile
RUN mkdir -p /app/data && chmod 777 /app/data
```

3. **验证迁移**
```bash
# 检查字体提取位置
docker exec <container> ls -la /app/data/pdf-render-font-*
```

## 相关文档 / Related Documentation

- [CONTAINER_DEPLOYMENT.md](CONTAINER_DEPLOYMENT.md) - 完整容器部署指南
- [CONTAINER_FONT_DIAGNOSTIC.md](CONTAINER_FONT_DIAGNOSTIC.md) - 容器字体诊断
- [Dockerfile.jdk8](Dockerfile.jdk8) - OpenJDK 8 配置
- [kubernetes-deployment-jdk8.yaml](kubernetes-deployment-jdk8.yaml) - K8s JDK8 配置

## 总结 / Summary

✅ **OpenJDK 8 支持** - 使用 `Dockerfile.jdk8`
✅ **/app/data 支持** - 配置 `-Djava.io.tmpdir=/app/data`
✅ **完全兼容** - 与现有功能100%兼容
✅ **生产就绪** - 包含健康检查和资源限制
✅ **易于迁移** - 最小化配置变更

如有问题，请参考故障排除章节或运行容器诊断脚本。
