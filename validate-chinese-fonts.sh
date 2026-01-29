#!/bin/bash

# Chinese Font Validation Script
# 中文字体验证脚本

echo "╔═══════════════════════════════════════════════════════════════╗"
echo "║  中文字体配置验证 Chinese Font Configuration Validator      ║"
echo "╚═══════════════════════════════════════════════════════════════╝"
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

ERRORS=0
WARNINGS=0

# Check 1: Font directory exists
echo "检查 1: 字体目录 Check 1: Font Directory"
echo "─────────────────────────────────────────────────────────────"
if [ -d "src/main/resources/fonts" ]; then
    echo -e "${GREEN}✓${NC} 字体目录存在 Font directory exists"
else
    echo -e "${RED}✗${NC} 字体目录不存在 Font directory missing"
    echo "  创建目录 Create: mkdir -p src/main/resources/fonts"
    ERRORS=$((ERRORS + 1))
fi
echo ""

# Check 2: Font files exist
echo "检查 2: 字体文件 Check 2: Font Files"
echo "─────────────────────────────────────────────────────────────"
FONT_COUNT=0

if [ -f "src/main/resources/fonts/HarmonyOS_Sans_SC_Regular.ttf" ]; then
    SIZE=$(du -h "src/main/resources/fonts/HarmonyOS_Sans_SC_Regular.ttf" | cut -f1)
    echo -e "${GREEN}✓${NC} HarmonyOS_Sans_SC_Regular.ttf ($SIZE)"
    FONT_COUNT=$((FONT_COUNT + 1))
else
    echo -e "${YELLOW}⚠${NC} HarmonyOS_Sans_SC_Regular.ttf 不存在 missing"
    WARNINGS=$((WARNINGS + 1))
fi

if [ -f "src/main/resources/fonts/NotoSansCJKsc-Regular.otf" ]; then
    SIZE=$(du -h "src/main/resources/fonts/NotoSansCJKsc-Regular.otf" | cut -f1)
    echo -e "${GREEN}✓${NC} NotoSansCJKsc-Regular.otf ($SIZE)"
    FONT_COUNT=$((FONT_COUNT + 1))
else
    echo -e "${YELLOW}⚠${NC} NotoSansCJKsc-Regular.otf 不存在 missing"
    WARNINGS=$((WARNINGS + 1))
fi

if [ $FONT_COUNT -eq 0 ]; then
    echo -e "${RED}✗${NC} 没有找到任何中文字体文件 No Chinese font files found"
    echo "  请下载并放置字体文件 Please download and place font files"
    echo "  参考 See: CHINESE_TROUBLESHOOTING.md#下载字体"
    ERRORS=$((ERRORS + 1))
fi
echo ""

# Check 3: Java code compilation
echo "检查 3: 代码编译 Check 3: Code Compilation"
echo "─────────────────────────────────────────────────────────────"
if mvn compile -q 2>/dev/null; then
    echo -e "${GREEN}✓${NC} 代码编译成功 Code compiles successfully"
else
    echo -e "${RED}✗${NC} 代码编译失败 Code compilation failed"
    echo "  运行 Run: mvn compile"
    ERRORS=$((ERRORS + 1))
fi
echo ""

# Check 4: Run tests
echo "检查 4: 运行测试 Check 4: Run Tests"
echo "─────────────────────────────────────────────────────────────"
if [ $FONT_COUNT -gt 0 ]; then
    TEST_OUTPUT=$(mktemp)
    if mvn test -Dtest=ChineseFontTest#testChineseTextWithCustomFonts 2>&1 | tee $TEST_OUTPUT | grep -q "BUILD SUCCESS"; then
        echo -e "${GREEN}✓${NC} 中文字体测试通过 Chinese font test passed"
        
        # Check PDF file size
        if [ -f "test-output/chinese_text_custom_fonts.pdf" ]; then
            SIZE=$(stat -f%z "test-output/chinese_text_custom_fonts.pdf" 2>/dev/null || stat -c%s "test-output/chinese_text_custom_fonts.pdf" 2>/dev/null)
            SIZE_KB=$((SIZE / 1024))
            
            if [ $SIZE_KB -gt 200 ]; then
                echo -e "${GREEN}✓${NC} PDF 大小: ${SIZE_KB} KB (字体已嵌入 font embedded)"
            else
                echo -e "${YELLOW}⚠${NC} PDF 大小: ${SIZE_KB} KB (可能字体未嵌入 font may not be embedded)"
                WARNINGS=$((WARNINGS + 1))
            fi
        fi
    else
        echo -e "${RED}✗${NC} 测试失败 Test failed"
        echo "  运行查看详情 Run for details: mvn test -Dtest=ChineseFontTest"
        ERRORS=$((ERRORS + 1))
    fi
    rm -f $TEST_OUTPUT
else
    echo -e "${YELLOW}⚠${NC} 跳过测试（没有字体文件）Skipped (no font files)"
    WARNINGS=$((WARNINGS + 1))
fi
echo ""

# Summary
echo "╔═══════════════════════════════════════════════════════════════╗"
echo "║  检查摘要 Summary                                             ║"
echo "╚═══════════════════════════════════════════════════════════════╝"
echo ""

if [ $ERRORS -eq 0 ] && [ $WARNINGS -eq 0 ]; then
    echo -e "${GREEN}✅ 完美！所有检查通过${NC}"
    echo -e "${GREEN}✅ Perfect! All checks passed${NC}"
    echo ""
    echo "您可以开始使用中文字体生成 PDF"
    echo "You can now generate PDFs with Chinese fonts"
    echo ""
    echo "示例代码 Example code:"
    echo "────────────────────────────────────────"
    echo "FontConfig fontConfig = new FontConfig();"
    echo "fontConfig.setRegularFontPath(\"classpath:/fonts/HarmonyOS_Sans_SC_Regular.ttf\");"
    echo "fontConfig.setDefaultFontFamily(\"HarmonyOS Sans SC, DejaVu Sans, sans-serif\");"
    echo "service.getHtmlRenderer().setFontConfig(fontConfig);"
    
elif [ $ERRORS -eq 0 ]; then
    echo -e "${YELLOW}⚠ 警告 Warnings: $WARNINGS${NC}"
    echo ""
    echo "有一些警告，但应该可以正常工作"
    echo "There are some warnings, but it should work"
    
else
    echo -e "${RED}✗ 错误 Errors: $ERRORS${NC}"
    echo -e "${YELLOW}⚠ 警告 Warnings: $WARNINGS${NC}"
    echo ""
    echo "请解决上述问题后重新运行此脚本"
    echo "Please fix the issues above and run this script again"
    echo ""
    echo "查看详细指南 See detailed guide:"
    echo "  cat CHINESE_TROUBLESHOOTING.md"
fi

echo ""
exit $ERRORS
