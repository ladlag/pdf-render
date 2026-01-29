# Spring Boot Integration Examples

This directory contains complete working examples of integrating the pdf-render library into Spring Boot applications.

## Quick Start Example

### Project Structure

```
examples/
└── spring-boot-quickstart/
    ├── src/
    │   ├── main/
    │   │   ├── java/
    │   │   │   └── com/
    │   │   │       └── example/
    │   │   │           └── pdfapp/
    │   │   │               ├── PdfApplication.java
    │   │   │               ├── config/
    │   │   │               │   └── PdfRenderConfig.java
    │   │   │               ├── controller/
    │   │   │               │   └── ReportController.java
    │   │   │               └── service/
    │   │   │                   └── PdfReportService.java
    │   │   └── resources/
    │   │       ├── application.yml
    │   │       ├── templates/
    │   │       │   └── matcher-report-final.html
    │   │       └── fonts/
    │   │           └── HarmonyOS_Sans_SC_Regular.ttf
    │   └── test/
    │       └── java/
    └── pom.xml
```

## Files in This Example

### PdfApplication.java

```java
package com.example.pdfapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import config.com.mercury.pdf.render.PdfRenderProperties;

/**
 * Spring Boot application with PDF rendering capabilities
 */
@SpringBootApplication
@EnableConfigurationProperties(PdfRenderProperties.class)
public class PdfApplication {
    public static void main(String[] args) {
        SpringApplication.run(PdfApplication.class, args);
    }
}
```

### PdfRenderConfig.java

```java
package com.example.pdfapp.config;

import com.mercury.pdf.render.ReportService;
import config.com.mercury.pdf.render.FontConfig;
import config.com.mercury.pdf.render.PdfRenderProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for PDF rendering service
 */
@Configuration
public class PdfRenderConfig {

    private final PdfRenderProperties properties;

    public PdfRenderConfig(PdfRenderProperties properties) {
        this.properties = properties;
    }

    @Bean
    public ReportService reportService() {
        ReportService service = new ReportService();
        service.setUseHtmlPipeline(true);

        // Configure template settings
        service.getHtmlRenderer().setDefaultTemplateName(
                properties.getTemplate().getDefaultName()
        );
        service.getHtmlRenderer().setCacheTemplates(
                properties.getTemplate().isCacheEnabled()
        );

        // Configure fonts for Chinese text
        if (properties.getFonts().getRegularPath() != null) {
            FontConfig fontConfig = new FontConfig();
            fontConfig.setRegularFontPath(properties.getFonts().getRegularPath());
            fontConfig.setDefaultFontFamily(properties.getFonts().getDefaultFamily());
            service.getHtmlRenderer().setFontConfig(fontConfig);
        }

        // Enable debug mode if configured
        if (properties.getDebug().isEnabled()) {
            service.getHtmlRenderer().setDebugHtmlEnabled(true);
            service.getHtmlRenderer().setDebugHtmlOutputDirectory(
                    properties.getDebug().getOutputDirectory()
            );
        }

        return service;
    }
}
```

### PdfReportService.java

See [SPRING_BOOT_INTEGRATION_GUIDE.md](../SPRING_BOOT_INTEGRATION_GUIDE.md#3-pdf报告服务类) for the complete service implementation.

### ReportController.java

See [SPRING_BOOT_INTEGRATION_GUIDE.md](../SPRING_BOOT_INTEGRATION_GUIDE.md#4-rest-controller) for the complete controller implementation.

### application.yml

```yaml
spring:
  application:
    name: pdf-app

pdf-render:
  template:
    location: classpath:/templates/
    default-name: matcher-report-final
    cache-enabled: true
  fonts:
    regular-path: classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf
    default-family: HarmonyOS Sans SC, DejaVu Sans, Arial, sans-serif
  debug:
    enabled: false
    output-directory: debug-html

server:
  port: 8080

logging:
  level:
    com.mercury.pdf.render: INFO
    com.example.pdfapp: INFO
```

### pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>pdf-app</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <name>PDF Application</name>
    <description>Spring Boot application with PDF rendering</description>

    <properties>
        <java.version>1.8</java.version>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <spring-boot.version>2.7.18</spring-boot.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <!-- PDF Rendering Library -->
        <dependency>
            <groupId>com.mercury</groupId>
            <artifactId>pdf-render</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>

        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Boot Configuration Processor (optional, for IDE support) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <version>${spring-boot.version}</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>repackage</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

## How to Run

### Step 1: Build and Install pdf-render Library

```bash
cd /path/to/pdf-render
mvn clean install
```

### Step 2: Copy Required Resources

Copy the following files to your Spring Boot project:

1. **Template file**: Copy `src/main/resources/templates/matcher-report-final.html` to your project's `src/main/resources/templates/` directory
2. **Font file**: Copy `src/main/resources/fonts/HarmonyOS_Sans_SC_Regular.ttf` to your project's `src/main/resources/fonts/` directory

### Step 3: Build the Application

```bash
cd /path/to/your/spring-boot-app
mvn clean package
```

### Step 4: Run the Application

```bash
java -jar target/pdf-app-1.0.0.jar
```

Or use Maven:

```bash
mvn spring-boot:run
```

### Step 5: Test the API

Generate a sample matcher report:

```bash
curl -o sample-report.pdf http://localhost:8080/api/reports/matcher/sample
```

Or open in browser: `http://localhost:8080/api/reports/matcher/sample`

## API Endpoints

- `GET /api/reports/matcher/sample` - Generate sample matcher report
- `POST /api/reports/matcher` - Generate custom matcher report (send ReportData JSON)
- `POST /api/reports/generate?templateName=xxx` - Generate report with custom template

## Configuration Profiles

### Development Profile

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

or

```bash
java -jar target/pdf-app-1.0.0.jar --spring.profiles.active=dev
```

### Production Profile

```bash
java -jar target/pdf-app-1.0.0.jar --spring.profiles.active=prod
```

## Docker Support

### Dockerfile

```dockerfile
FROM openjdk:8-jdk-alpine

# Install font support
RUN apk add --no-cache fontconfig ttf-dejavu

# Copy application
COPY target/pdf-app-1.0.0.jar /app.jar

# Copy fonts
COPY src/main/resources/fonts /fonts

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Build and Run Docker Image

```bash
# Build image
docker build -t pdf-app:1.0.0 .

# Run container
docker run -p 8080:8080 pdf-app:1.0.0
```

## Troubleshooting

### Chinese characters display as boxes (□)

Make sure:
1. Font file is in `src/main/resources/fonts/`
2. Font path is configured correctly in `application.yml`
3. Font file is included in the JAR (check with `jar tf target/pdf-app-1.0.0.jar | grep fonts`)

### Template not found

Make sure:
1. Template file is in `src/main/resources/templates/`
2. Template name in configuration matches the filename (without .html extension)
3. Template file is included in the JAR

### OutOfMemoryError

Increase heap size:

```bash
java -Xmx2G -jar target/pdf-app-1.0.0.jar
```

## Additional Resources

- [Complete Integration Guide](../SPRING_BOOT_INTEGRATION_GUIDE.md)
- [Main README](../README.md)
- [Template Guide](../TEMPLATE_GUIDE.md)
- [Font Configuration](../FONT_CONFIGURATION.md)
