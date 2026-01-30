# 快速配置指南 - OpenJDK 8 + /app/data
# Quick Configuration Guide - OpenJDK 8 + /app/data

## 您的环境配置 / Your Environment Setup

根据您的反馈，您的环境：
- ✅ 使用 **OpenJDK 8**
- ✅ `/app/data` 目录可用

Based on your feedback, your environment:
- ✅ Uses **OpenJDK 8**
- ✅ `/app/data` directory is available

## 🚀 立即使用 / Use Right Now

### 选项 1: 使用预配置的 Dockerfile.jdk8

```bash
# 1. 构建镜像
docker build -f Dockerfile.jdk8 -t pdf-service:jdk8 .

# 2. 运行容器
docker run -p 8080:8080 pdf-service:jdk8

# 完成！字体将提取到 /app/data
```

### 选项 2: 使用 docker-compose

```bash
# 1. 启动服务
docker-compose up pdf-service-jdk8

# 2. 查看日志
docker-compose logs -f pdf-service-jdk8

# 应该看到：
# ✓ Font registered with Flying Saucer: /app/data/pdf-render-font-xxx.ttf
```

### 选项 3: Kubernetes 部署

```bash
# 1. 应用配置
kubectl apply -f kubernetes-deployment-jdk8.yaml

# 2. 检查部署
kubectl get pods -l version=jdk8

# 3. 查看日志
kubectl logs -l version=jdk8 --tail=50
```

## 📋 配置文件对照 / Configuration File Reference

| 用途 Purpose | 文件 File | 说明 Description |
|-------------|----------|------------------|
| Docker构建 | `Dockerfile.jdk8` | OpenJDK 8 + /app/data 配置 |
| 本地测试 | `docker-compose.yml` | 服务名：`pdf-service-jdk8` |
| K8s部署 | `kubernetes-deployment-jdk8.yaml` | 生产就绪配置 |
| 详细文档 | `JDK8_CUSTOM_TEMPDIR.md` | 完整指南和故障排除 |

## ✅ 验证配置 / Verify Configuration

### 1. 检查容器环境

```bash
# 进入容器
docker exec -it <container-name> sh

# 检查 Java 版本
java -version
# 应该显示: openjdk version "1.8.x"

# 检查临时目录配置
echo $JAVA_OPTS
# 应该包含: -Djava.io.tmpdir=/app/data

# 检查目录权限
ls -la /app/data/
# 应该可写
```

### 2. 验证字体提取

```bash
# 生成一个PDF后，检查字体文件
ls -la /app/data/pdf-render-font-*

# 应该看到类似：
# -rw-r--r-- 1 appuser appuser 8253488 Jan 30 08:00 /app/data/pdf-render-font-12345.ttf
```

### 3. 查看应用日志

```bash
# Docker
docker logs <container-name> | grep -i font

# Kubernetes
kubectl logs <pod-name> | grep -i font

# 正确的日志输出：
# ✓ Font extracted for PDF rendering: HarmonyOS_Sans_SC_Regular.ttf
# ✓ Font registered with Flying Saucer: /app/data/pdf-render-font-xxx.ttf
#   Font family name (for CSS): HarmonyOS Sans SC
# ✓ Total fonts registered for PDF: 1
```

## 🔧 自定义配置 / Custom Configuration

### 如果需要更改临时目录 / To Change Temp Directory

```bash
# 使用环境变量覆盖
docker run -p 8080:8080 \
  -e JAVA_OPTS="-Xmx1g -Djava.io.tmpdir=/your/custom/path" \
  pdf-service:jdk8

# 确保目录存在且可写
docker exec <container> mkdir -p /your/custom/path
docker exec <container> chmod 777 /your/custom/path
```

### 如果需要更改 JVM 内存 / To Change JVM Memory

```bash
# 增加内存到 2GB
docker run -p 8080:8080 \
  -e JAVA_OPTS="-Xmx2g -Xms1g -Djava.io.tmpdir=/app/data" \
  pdf-service:jdk8
```

## 🐛 常见问题 / Common Issues

### 问题 1: /app/data 不可写

**错误信息:**
```
java.io.IOException: Permission denied: /app/data/pdf-render-font-xxx.ttf
```

**解决方案:**
```bash
# 在容器内
chmod 777 /app/data

# 或在 Dockerfile 中（已经包含在 Dockerfile.jdk8）
RUN mkdir -p /app/data && chmod 777 /app/data
```

### 问题 2: 字体提取到 /tmp 而不是 /app/data

**原因:** JAVA_OPTS 未正确设置

**解决方案:**
```bash
# 检查环境变量
docker exec <container> env | grep JAVA_OPTS

# 应该显示:
# JAVA_OPTS=-Xmx512m -Xms256m -Djava.io.tmpdir=/app/data

# 如果不正确，使用 -e 参数重新运行
docker run -e JAVA_OPTS="-Djava.io.tmpdir=/app/data" ...
```

### 问题 3: JDK 版本不匹配

**错误信息:**
```
UnsupportedClassVersionError: compiled by a more recent version
```

**解决方案:**
```bash
# 确认使用 Dockerfile.jdk8
docker build -f Dockerfile.jdk8 -t pdf-service:jdk8 .

# 不要使用默认的 Dockerfile（那是 JDK 11）
```

## 📚 更多文档 / More Documentation

- **[JDK8_CUSTOM_TEMPDIR.md](JDK8_CUSTOM_TEMPDIR.md)** - 完整配置指南
- **[CONTAINER_DEPLOYMENT.md](CONTAINER_DEPLOYMENT.md)** - 容器部署完整指南
- **[CONTAINER_FONT_DIAGNOSTIC.md](CONTAINER_FONT_DIAGNOSTIC.md)** - 容器字体诊断
- **[CHINESE_QUICKSTART.md](CHINESE_QUICKSTART.md)** - 中文字体快速开始

## 💡 关键要点 / Key Points

1. ✅ **使用 Dockerfile.jdk8** - 专为您的环境优化
2. ✅ **默认配置已优化** - `/app/data` 作为临时目录
3. ✅ **完全兼容** - 与所有现有功能100%兼容
4. ✅ **生产就绪** - 包含健康检查和资源限制
5. ✅ **零配置迁移** - 直接使用，无需额外设置

## 🎯 下一步 / Next Steps

1. **构建镜像:** `docker build -f Dockerfile.jdk8 -t pdf-service:jdk8 .`
2. **运行容器:** `docker run -p 8080:8080 pdf-service:jdk8`
3. **验证字体:** 查看日志确认 "Font registered" 消息
4. **测试PDF:** 生成包含中文的PDF，确认显示正常

如有任何问题，请参考 [JDK8_CUSTOM_TEMPDIR.md](JDK8_CUSTOM_TEMPDIR.md) 中的故障排除章节。
