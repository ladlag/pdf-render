# 问题解答：关于字体配置的说明

## 原始问题

> JAR_INTERGRATION.md中的实例现在设置字体是：
> ```java
> PdfRenderProperties properties = new PdfRenderProperties();
> PdfRenderProperties.FontProperties fonts = new PdfRenderProperties.FontProperties();
> fonts.setRegularPath("classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf");
> fonts.setDefaultFamily("HarmonyOS Sans SC, DejaVu Sans, sans-serif");
> properties.setFonts(fonts);
> 
> service.getHtmlRenderer().setFontProperties(fonts);
> ```
> 
> 那么原来在application.yaml的配置是否还生效？是否还需要配置？是否还需要在springboot项目的Application上添加properties注解？

---

## 答案总结

### 1. application.yaml的配置是否还生效？

**答：不生效。代码配置会覆盖YAML配置。**

当你在代码中调用 `service.getHtmlRenderer().setFontProperties(fonts)` 后：
- ✅ 代码中设置的字体配置会生效
- ❌ application.yaml中的字体配置不再生效（被覆盖）

### 2. 是否还需要配置application.yaml？

**答：取决于你的使用方式。**

#### 推荐方式A：仅使用YAML配置（Spring Boot项目）

**✅ 推荐用于生产环境**

```yaml
# application.yml
pdf-render:
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    default-family: HarmonyOS Sans SC, sans-serif
```

```java
@Service
public class PdfService {
    private final ReportService reportService;
    
    // 自动注入，字体已配置完成
    public PdfService(ReportService reportService) {
        this.reportService = reportService;
    }
    
    public byte[] generatePdf(ReportData data) throws IOException {
        // 直接使用，无需再设置字体
        return reportService.generatePdf(data);
    }
}
```

**优点：**
- 配置集中管理
- 支持多环境配置
- 代码简洁

#### 方式B：仅使用代码配置

如果你选择使用代码配置（如JAR_INTEGRATION.md中的示例），则**不需要**配置application.yaml。

适用场景：
- 普通Java项目（非Spring Boot）
- 需要动态切换字体
- 需要多个不同配置的ReportService实例

### 3. 是否还需要@EnableConfigurationProperties注解？

**答：取决于你的配置方式。**

#### 如果使用YAML配置：必须添加

```java
@SpringBootApplication
@EnableConfigurationProperties(PdfRenderProperties.class)  // ← 必须
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

**为什么需要：**
- Spring Boot需要这个注解来识别PdfRenderProperties配置类
- 没有这个注解，pdf-render配置节点不会被解析
- YAML配置完全不生效

#### 如果仅使用代码配置：不需要

但建议还是添加，保持配置的一致性，方便将来改用YAML配置。

---

## 配置优先级规则

```
代码配置 > YAML配置 > 默认配置
```

如果你同时配置了YAML和代码，最终生效的是**代码配置**。

---

## 推荐的最佳实践

### Spring Boot项目（推荐）

1. 在Application类上添加`@EnableConfigurationProperties(PdfRenderProperties.class)`
2. 在application.yml中配置所有参数
3. 直接注入ReportService使用
4. **不要在代码中调用setFontProperties()**

### 普通Java项目

使用JAR_INTEGRATION.md中展示的代码配置方式。

---

## 详细文档

完整的配置说明请参考以下文档：

1. **[CONFIGURATION_FAQ.md](CONFIGURATION_FAQ.md)** - 配置常见问题详解（强烈推荐）
2. [JAR_INTEGRATION.md](JAR_INTEGRATION.md) - JAR集成和配置优先级说明
3. [SPRING_BOOT_INTEGRATION_GUIDE.md](SPRING_BOOT_INTEGRATION_GUIDE.md) - Spring Boot完整集成指南

---

## 快速决策

```
你在使用Spring Boot吗？
├─ 是
│  ├─ 推荐：仅使用YAML配置
│  │  ├─ 添加@EnableConfigurationProperties注解 ✅
│  │  ├─ 配置application.yml ✅
│  │  └─ 不要在代码中调用setFontProperties() ❌
│  │
│  └─ 需要动态字体？
│     └─ 使用代码配置
│        └─ YAML配置会被覆盖
│
└─ 否（普通Java项目）
   └─ 使用代码配置（参考JAR_INTEGRATION.md）
```

---

**文档更新日期：** 2026-01-30
