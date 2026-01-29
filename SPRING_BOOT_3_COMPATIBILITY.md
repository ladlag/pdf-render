# Spring Boot 3.x Compatibility

## Overview

This library now supports both Spring Boot 2.x and Spring Boot 3.x through dual auto-configuration mechanisms.

## Auto-Configuration Support

### Spring Boot 2.x
- Uses `META-INF/spring.factories`
- Registered via `org.springframework.boot.autoconfigure.EnableAutoConfiguration`

### Spring Boot 3.x
- Uses `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
- Follows the new Spring Boot 3.x auto-configuration registration mechanism

## What This Means

When you add this library as a dependency to your Spring Boot application, the `ReportService` bean will be automatically registered and available for injection, regardless of whether you're using Spring Boot 2.x or 3.x.

### Example Usage

```java
@RestController
@RequestMapping("/api/reports")
public class ReportController {
    
    @Autowired
    private ReportService reportService;  // Automatically injected
    
    @PostMapping("/generate")
    public ResponseEntity<byte[]> generateReport(@RequestBody ReportData data) throws IOException {
        byte[] pdfBytes = reportService.generatePdf(data);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "report.pdf");
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(pdfBytes);
    }
}
```

## Configuration

You can configure the `ReportService` through `application.yml` or `application.properties`:

```yaml
pdf-render:
  template:
    default-name: report
    cache-enabled: true
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    cjk-path: classpath:/fonts/NotoSansCJK-Regular.otf
  debug:
    enabled: false
```

## Migration from Manual Bean Configuration

If you were previously creating the `ReportService` bean manually in a `@Configuration` class, you can now remove that configuration. The auto-configuration will handle it for you.

### Before (Manual Configuration)
```java
@Configuration
public class PdfConfig {
    
    @Bean
    public ReportService reportService() {
        return new ReportService();
    }
}
```

### After (Auto-Configuration)
```java
// No manual configuration needed!
// Just inject ReportService where you need it:

@Service
public class MyService {
    
    @Autowired
    private ReportService reportService;
    
    // Use reportService...
}
```

## Troubleshooting

### Bean Not Found Error

If you see an error like:
```
Field reportService required a bean of type 'com.mercury.pdf.render.ReportService' that could not be found.
```

**Possible causes:**
1. The library JAR is not on your classpath
2. Component scanning is disabled or limited
3. You're using a very old version of Spring Boot that doesn't support auto-configuration

**Solutions:**
1. Verify the dependency is in your `pom.xml` or `build.gradle`
2. Make sure your main application class has `@SpringBootApplication` annotation
3. Update to Spring Boot 2.x or 3.x (minimum supported: 2.7.x)

### Custom Configuration

If you need to customize the `ReportService` beyond what properties allow, you can still create your own bean which will override the auto-configured one:

```java
@Configuration
public class CustomPdfConfig {
    
    @Bean
    public ReportService reportService() {
        ReportService service = new ReportService();
        // Custom configuration
        service.getHtmlRenderer().setDebugHtmlEnabled(true);
        return service;
    }
}
```

## Version Compatibility

| Spring Boot Version | Auto-Configuration File | Status |
|---------------------|------------------------|--------|
| 2.0.x - 2.7.x       | `spring.factories`     | ✅ Supported |
| 3.0.x and above     | `AutoConfiguration.imports` | ✅ Supported |
| 1.x                 | N/A                    | ❌ Not Supported |

## Technical Details

Both auto-configuration files point to the same configuration class:
- `com.mercury.pdf.render.config.PdfRenderAutoConfiguration`

This class:
1. Creates a `ReportService` bean
2. Configures it with properties from `application.yml`
3. Sets up fonts, templates, and debug settings
4. Only activates when `ReportService.class` is on the classpath

## Further Reading

- [Spring Boot Auto-Configuration Guide](SPRING_BOOT_INTEGRATION_GUIDE.md)
- [Spring Boot 3 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)
- [Configuration Properties](FONT_CONFIGURATION.md)
