package com.mercury.pdf.render;

import com.mercury.pdf.render.model.ReportData;
import com.mercury.pdf.render.model.ReportDataBuilder;
import com.mercury.pdf.render.model.Section;
import com.mercury.pdf.render.model.TableData;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test demonstrating two-column layouts (e.g., signature and date columns)
 * using customContent feature.
 */
public class TwoColumnLayoutTest {
    
    private static final String TEST_OUTPUT_DIR = System.getProperty("test.output.dir", "test-output");
    
    @Test
    public void testSignatureDateTwoColumnLayout() throws IOException {
        // Create report with two-column signature/date layout at the end
        ReportData report = ReportDataBuilder.create()
            .title("项目验收报告")
            .subtitle("2024年度重点项目")
            .reportDate("2024-12-31")
            .reportNumber("PROJ-2024-001")
            
            // Main content
            .addSection(new Section("项目概况")
                .addParagraph("本项目已按计划完成所有开发任务，通过内部测试和用户验收。"))
            
            .addSection(new Section("验收结论")
                .addParagraph("经验收小组审查，该项目各项指标符合验收标准，同意通过验收。"))
            
            // Two-column signature layout using table (most stable)
            .addSection(new Section("")  // No title for signature section
                .withCustomContent(
                    "<div style='margin-top: 50px; page-break-inside: avoid;'>" +
                    "  <table style='width: 100%; border: none;'>" +
                    "    <tr>" +
                    "      <td style='width: 50%; text-align: center; border: none; padding: 20px;'>" +
                    "        <div style='margin-bottom: 60px; font-size: 11pt;'>项目负责人签字：</div>" +
                    "        <div style='border-top: 2px solid #000; width: 200px; margin: 0 auto;'></div>" +
                    "      </td>" +
                    "      <td style='width: 50%; text-align: center; border: none; padding: 20px;'>" +
                    "        <div style='margin-bottom: 60px; font-size: 11pt;'>日期：</div>" +
                    "        <div style='border-top: 2px solid #000; width: 200px; margin: 0 auto;'></div>" +
                    "      </td>" +
                    "    </tr>" +
                    "  </table>" +
                    "</div>"
                ))
            
            .reportNotice("本报告一式三份，项目组、管理部门、财务部门各持一份。")
            .build();
        
        // Generate PDF
        PdfRenderService service = new PdfRenderService();
        byte[] pdf = service.generatePdf(report, "flexible");
        
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
        
        // Save for inspection
        Files.createDirectories(Paths.get(TEST_OUTPUT_DIR));
        Files.write(Paths.get(TEST_OUTPUT_DIR, "two_column_signature_layout.pdf"), pdf);
        
        System.out.println("✓ Two-column signature/date layout PDF generated: " + 
            TEST_OUTPUT_DIR + "/two_column_signature_layout.pdf");
    }
    
    @Test
    public void testThreeColumnApprovalLayout() throws IOException {
        // Create report with three-column approval layout
        ReportData report = ReportDataBuilder.create()
            .title("财务报销申请")
            .subtitle("差旅费用报销")
            .reportDate("2024-12-31")
            
            .addSection(new Section("报销明细")
                .addTable(createExpenseTable()))
            
            // Three-column approval layout
            .addSection(new Section("审批栏")
                .withCustomContent(
                    "<div style='margin-top: 30px;'>" +
                    "  <div style='display: table; width: 100%; border-collapse: collapse;'>" +
                    "    <div style='display: table-row;'>" +
                    "      <div style='display: table-cell; width: 33%; padding: 15px; border: 1px solid #333; text-align: center;'>" +
                    "        <div style='font-weight: bold; margin-bottom: 10px;'>申请人：</div>" +
                    "        <div style='height: 60px;'></div>" +
                    "        <div style='font-size: 9pt; color: #666; margin-top: 5px;'>日期：_______</div>" +
                    "      </div>" +
                    "      <div style='display: table-cell; width: 33%; padding: 15px; border: 1px solid #333; text-align: center;'>" +
                    "        <div style='font-weight: bold; margin-bottom: 10px;'>部门经理：</div>" +
                    "        <div style='height: 60px;'></div>" +
                    "        <div style='font-size: 9pt; color: #666; margin-top: 5px;'>日期：_______</div>" +
                    "      </div>" +
                    "      <div style='display: table-cell; width: 33%; padding: 15px; border: 1px solid #333; text-align: center;'>" +
                    "        <div style='font-weight: bold; margin-bottom: 10px;'>财务审批：</div>" +
                    "        <div style='height: 60px;'></div>" +
                    "        <div style='font-size: 9pt; color: #666; margin-top: 5px;'>日期：_______</div>" +
                    "      </div>" +
                    "    </div>" +
                    "  </div>" +
                    "</div>"
                ))
            
            .build();
        
        // Generate PDF
        PdfRenderService service = new PdfRenderService();
        byte[] pdf = service.generatePdf(report, "flexible");
        
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
        
        // Save for inspection
        Files.createDirectories(Paths.get(TEST_OUTPUT_DIR));
        Files.write(Paths.get(TEST_OUTPUT_DIR, "three_column_approval_layout.pdf"), pdf);
        
        System.out.println("✓ Three-column approval layout PDF generated: " + 
            TEST_OUTPUT_DIR + "/three_column_approval_layout.pdf");
    }
    
    @Test
    public void testFloatingDivLayout() throws IOException {
        // Test floating div layout for signature columns
        ReportData report = ReportDataBuilder.create()
            .title("会议纪要")
            .reportDate("2024-12-31")
            
            .addSection(new Section("会议内容")
                .addParagraph("会议讨论了2025年工作计划..."))
            
            // Floating div layout
            .addSection(new Section("")
                .withCustomContent(
                    "<div style='width: 100%; overflow: hidden; margin-top: 40px;'>" +
                    "  <div style='float: left; width: 45%; padding: 15px;'>" +
                    "    <p style='margin-bottom: 10px; font-weight: bold;'>记录人签字：</p>" +
                    "    <div style='border-top: 2px solid #333; width: 180px; margin-top: 50px;'></div>" +
                    "    <p style='margin-top: 8px; font-size: 9pt; color: #666;'>签字人</p>" +
                    "  </div>" +
                    "  <div style='float: right; width: 45%; padding: 15px;'>" +
                    "    <p style='margin-bottom: 10px; font-weight: bold;'>日期：</p>" +
                    "    <div style='border-top: 2px solid #333; width: 180px; margin-top: 50px;'></div>" +
                    "    <p style='margin-top: 8px; font-size: 9pt; color: #666;'>____年___月___日</p>" +
                    "  </div>" +
                    "</div>"
                ))
            
            .build();
        
        // Generate PDF
        PdfRenderService service = new PdfRenderService();
        byte[] pdf = service.generatePdf(report, "flexible");
        
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
        
        // Save for inspection
        Files.createDirectories(Paths.get(TEST_OUTPUT_DIR));
        Files.write(Paths.get(TEST_OUTPUT_DIR, "floating_div_signature_layout.pdf"), pdf);
        
        System.out.println("✓ Floating div signature layout PDF generated: " + 
            TEST_OUTPUT_DIR + "/floating_div_signature_layout.pdf");
    }
    
    private TableData createExpenseTable() {
        return new TableData(
            Arrays.asList("项目", "金额", "备注"),
            Arrays.asList(
                Arrays.asList("交通费", "￥500", "高铁票"),
                Arrays.asList("住宿费", "￥800", "酒店2晚"),
                Arrays.asList("餐费", "￥300", "工作餐"),
                Arrays.asList("合计", "￥1,600", "")
            )
        );
    }
}
