package com.mercury.pdf.render;

import com.mercury.pdf.render.config.FontConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify that font loading failures do not prevent application startup
 * when using @PostConstruct or similar initialization methods.
 * 
 * This addresses the requirement: "使用@PostConstruct 不会因为字体加载失败 影响项目启动"
 * (Using @PostConstruct should not affect project startup due to font loading failure)
 */
public class PostConstructSafetyTest {

    @Test
    public void testSetFontConfigWithInvalidPathDoesNotThrow() {
        System.out.println("\n========================================");
        System.out.println("测试：无效字体路径不会抛出异常");
        System.out.println("Test: Invalid font path does not throw exception");
        System.out.println("========================================\n");
        
        ReportService service = new ReportService();
        
        // Simulate @PostConstruct initialization with invalid font path
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:/fonts/nonexistent-font.ttf");
        fontConfig.setDefaultFontFamily("NonExistent Font, DejaVu Sans, sans-serif");
        
        // This should NOT throw an exception
        assertDoesNotThrow(() -> {
            service.getHtmlRenderer().setFontConfig(fontConfig);
        }, "setFontConfig should not throw exception even with invalid font path");
        
        System.out.println("✓ Application can start even with invalid font path");
        System.out.println("  Font loading failure was handled gracefully");
    }
    
    @Test
    public void testSetFontConfigWithNullPathDoesNotThrow() {
        System.out.println("\n========================================");
        System.out.println("测试：空字体路径不会抛出异常");
        System.out.println("Test: Null font path does not throw exception");
        System.out.println("========================================\n");
        
        ReportService service = new ReportService();
        
        // Simulate @PostConstruct initialization with null paths
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath(null);
        fontConfig.setDefaultFontFamily("DejaVu Sans, Arial, sans-serif");
        
        assertDoesNotThrow(() -> {
            service.getHtmlRenderer().setFontConfig(fontConfig);
        }, "setFontConfig should not throw exception with null font path");
        
        System.out.println("✓ Application can start with null font configuration");
    }
    
    @Test
    public void testSetFontConfigWithEmptyStringDoesNotThrow() {
        System.out.println("\n========================================");
        System.out.println("测试：空字符串字体路径不会抛出异常");
        System.out.println("Test: Empty string font path does not throw exception");
        System.out.println("========================================\n");
        
        ReportService service = new ReportService();
        
        // Simulate @PostConstruct initialization with empty string
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("");
        fontConfig.setDefaultFontFamily("DejaVu Sans, Arial, sans-serif");
        
        assertDoesNotThrow(() -> {
            service.getHtmlRenderer().setFontConfig(fontConfig);
        }, "setFontConfig should not throw exception with empty string font path");
        
        System.out.println("✓ Application can start with empty font path");
    }
    
    @Test
    public void testSetFontConfigWithInvalidClasspathPrefix() {
        System.out.println("\n========================================");
        System.out.println("测试：无效classpath前缀不会抛出异常");
        System.out.println("Test: Invalid classpath prefix does not throw exception");
        System.out.println("========================================\n");
        
        ReportService service = new ReportService();
        
        // Simulate @PostConstruct with malformed classpath
        FontConfig fontConfig = new FontConfig();
        fontConfig.setRegularFontPath("classpath:fonts/missing-slash.ttf"); // Missing leading slash
        fontConfig.setDefaultFontFamily("DejaVu Sans, Arial, sans-serif");
        
        assertDoesNotThrow(() -> {
            service.getHtmlRenderer().setFontConfig(fontConfig);
        }, "setFontConfig should not throw exception with invalid classpath format");
        
        System.out.println("✓ Application can start with malformed classpath");
    }
    
    @Test
    public void testMultipleSetFontConfigCallsAreIdempotent() {
        System.out.println("\n========================================");
        System.out.println("测试：多次调用setFontConfig是安全的");
        System.out.println("Test: Multiple setFontConfig calls are safe");
        System.out.println("========================================\n");
        
        ReportService service = new ReportService();
        
        // First call with invalid font
        FontConfig fontConfig1 = new FontConfig();
        fontConfig1.setRegularFontPath("classpath:/fonts/invalid1.ttf");
        fontConfig1.setDefaultFontFamily("Font1, sans-serif");
        
        assertDoesNotThrow(() -> {
            service.getHtmlRenderer().setFontConfig(fontConfig1);
        });
        
        // Second call with different invalid font
        FontConfig fontConfig2 = new FontConfig();
        fontConfig2.setRegularFontPath("classpath:/fonts/invalid2.ttf");
        fontConfig2.setDefaultFontFamily("Font2, sans-serif");
        
        assertDoesNotThrow(() -> {
            service.getHtmlRenderer().setFontConfig(fontConfig2);
        });
        
        // Third call with null
        assertDoesNotThrow(() -> {
            service.getHtmlRenderer().setFontConfig(null);
        });
        
        System.out.println("✓ Multiple setFontConfig calls work without issues");
        System.out.println("  Application remains stable after multiple configuration attempts");
    }
    
    @Test
    public void testPostConstructSimulation() {
        System.out.println("\n========================================");
        System.out.println("测试：模拟Spring @PostConstruct场景");
        System.out.println("Test: Simulate Spring @PostConstruct scenario");
        System.out.println("========================================\n");
        
        // Simulate a Spring Service with @PostConstruct
        class SimulatedSpringService {
            private ReportService reportService;
            
            // Simulate @PostConstruct
            public void init() {
                reportService = new ReportService();
                
                // This should not throw exception even if font loading fails
                FontConfig fontConfig = new FontConfig();
                fontConfig.setRegularFontPath("classpath:/fonts/nonexistent.ttf");
                fontConfig.setBoldFontPath("classpath:/fonts/also-nonexistent.ttf");
                fontConfig.setCjkFontPath("classpath:/fonts/missing-cjk.otf");
                fontConfig.setDefaultFontFamily("Missing Font, DejaVu Sans, sans-serif");
                
                reportService.getHtmlRenderer().setFontConfig(fontConfig);
                
                System.out.println("  @PostConstruct method completed successfully");
            }
            
            public ReportService getReportService() {
                return reportService;
            }
        }
        
        // Simulate Spring container calling @PostConstruct
        SimulatedSpringService service = new SimulatedSpringService();
        
        assertDoesNotThrow(() -> {
            service.init(); // This simulates @PostConstruct being called
        }, "@PostConstruct initialization should not fail due to font loading errors");
        
        assertNotNull(service.getReportService(), "Service should be initialized");
        
        System.out.println("✓ Spring Boot application can start successfully");
        System.out.println("  @PostConstruct completed without throwing exceptions");
        System.out.println("  Application is ready to serve requests");
        System.out.println("\n注意：PDF生成仍然可以工作，只是中文可能显示为方框");
        System.out.println("Note: PDF generation will still work, Chinese may show as boxes");
    }
}
