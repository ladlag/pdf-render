# ⚠️ Spring Boot 集成重要更新 Important Spring Boot Integration Update

## Bean 冲突警告 Bean Conflict Warning

**如果您遇到以下错误：**

```
The bean 'reportService' could not be registered. A bean with that name 
has already been defined in class path resource 
[com/mercury/pdf/render/config/PdfRenderAutoConfiguration.class]
```

**这意味着您不应该手动创建 `ReportService` bean！**

---

## ✅ 正确的使用方式 Correct Usage

### pdf-render 库提供自动配置！

pdf-render library provides auto-configuration!

**只需要配置 YAML，不需要创建配置类！**

**Just configure YAML, no configuration class needed!**

### 步骤 Step

1. **配置 application.yml**

```yaml
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    default-family: HarmonyOS Sans SC, DejaVu Sans, sans-serif
```

2. **直接注入使用 Direct Injection**

```java
@Service
public class MyService {
    @Autowired
    private ReportService reportService;  // ✅ 自动配置
    
    public byte[] generate() throws IOException {
        return reportService.generatePdf(data);
    }
}
```

---

## 📖 详细文档 Detailed Documentation

请查看：**[SPRING_BOOT_AUTO_CONFIGURATION.md](SPRING_BOOT_AUTO_CONFIGURATION.md)**

包含：
- Bean 冲突解决方案
- 自动配置说明
- 完整示例代码
- 常见问题解答

Contains:
- Bean conflict solutions
- Auto-configuration explanation
- Complete example code
- FAQ

---

## ❌ 不要这样做 Don't Do This

```java
// ❌ 错误：会导致 bean 冲突
@Configuration
public class PdfRenderConfig {
    @Bean
    public ReportService reportService() {
        return new ReportService();
    }
}
```

---

**最后更新 Last Updated**: 2026-01-29
