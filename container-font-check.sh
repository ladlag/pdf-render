#!/bin/sh
# ============================================
# 容器字体诊断脚本
# Container Font Diagnostic Script
# ============================================
#
# 在容器内运行此脚本来诊断字体配置问题
# Run this script inside container to diagnose font configuration issues
#
# Usage:
#   sh container-font-check.sh
#

echo "╔════════════════════════════════════════════════════════════════════╗"
echo "║  容器字体诊断 Container Font Diagnostic                           ║"
echo "╚════════════════════════════════════════════════════════════════════╝"
echo ""

# Colors for output (if supported)
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if colors are supported
if [ ! -t 1 ]; then
    RED=''
    GREEN=''
    YELLOW=''
    NC=''
fi

# Find JAR file
JAR_PATH=""
if [ -f /app/app.jar ]; then
    JAR_PATH="/app/app.jar"
elif [ -f /app/*.jar ]; then
    JAR_PATH=$(ls /app/*.jar | head -1)
fi

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "1. 基本环境检查 Basic Environment Check"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

echo "Java版本 Java Version:"
java -version 2>&1 | head -3
echo ""

echo "当前用户 Current User:"
whoami
echo ""

echo "工作目录 Working Directory:"
pwd
echo ""

echo "JAR文件路径 JAR File Path:"
if [ -n "$JAR_PATH" ]; then
    echo "${GREEN}✓${NC} Found: $JAR_PATH"
    ls -lh "$JAR_PATH"
else
    echo "${RED}✗${NC} JAR file not found"
fi
echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "2. JAR中的字体文件 Fonts in JAR"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

if [ -n "$JAR_PATH" ]; then
    FONTS_IN_JAR=$(jar tf "$JAR_PATH" | grep "fonts/.*\.ttf\|fonts/.*\.otf" | head -10)
    if [ -n "$FONTS_IN_JAR" ]; then
        echo "${GREEN}✓${NC} 找到字体文件 Found font files:"
        echo "$FONTS_IN_JAR"
        
        # Count fonts
        FONT_COUNT=$(echo "$FONTS_IN_JAR" | wc -l)
        echo ""
        echo "字体数量 Font count: $FONT_COUNT"
    else
        echo "${RED}✗${NC} JAR中未找到字体文件 No font files found in JAR"
        echo "${YELLOW}⚠${NC}  请检查 src/main/resources/fonts/ 目录"
        echo "${YELLOW}⚠${NC}  Check src/main/resources/fonts/ directory"
    fi
else
    echo "${RED}✗${NC} 无法检查 - JAR文件未找到"
fi
echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "3. 提取的临时字体文件 Extracted Temporary Font Files"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

TEMP_FONTS=$(ls -1 /tmp/pdf-render-font-* 2>/dev/null)
if [ -n "$TEMP_FONTS" ]; then
    echo "${GREEN}✓${NC} 找到已提取的字体文件 Found extracted font files:"
    ls -lh /tmp/pdf-render-font-*
    echo ""
    
    # Check file sizes
    for font in /tmp/pdf-render-font-*; do
        SIZE=$(ls -lh "$font" | awk '{print $5}')
        echo "  $font: $SIZE"
        
        # Warn if file is too small
        SIZE_BYTES=$(ls -l "$font" | awk '{print $5}')
        if [ "$SIZE_BYTES" -lt 1000000 ]; then
            echo "  ${YELLOW}⚠${NC}  警告：文件过小（<1MB），可能提取失败"
        fi
    done
else
    echo "${YELLOW}⚠${NC}  未找到临时字体文件 No temporary font files found"
    echo "  这可能意味着 This may mean:"
    echo "  1. 应用还未生成PDF Application hasn't generated PDF yet"
    echo "  2. 字体提取失败 Font extraction failed"
    echo "  3. 使用了不同的临时目录 Using different temp directory"
fi
echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "4. 临时目录权限 Temp Directory Permissions"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

echo "临时目录 /tmp:"
ls -ld /tmp
echo ""

# Try to create a test file
TEST_FILE="/tmp/test-write-$$"
if touch "$TEST_FILE" 2>/dev/null; then
    echo "${GREEN}✓${NC} 临时目录可写 Temp directory is writable"
    rm -f "$TEST_FILE"
else
    echo "${RED}✗${NC} 临时目录不可写 Temp directory is NOT writable"
    echo "${YELLOW}⚠${NC}  这会导致字体提取失败 This will cause font extraction to fail"
fi
echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "5. PDF渲染配置环境变量 PDF Render Config Environment Variables"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

PDF_ENV=$(env | grep "^PDF_RENDER")
if [ -n "$PDF_ENV" ]; then
    echo "${GREEN}✓${NC} 找到PDF渲染配置 Found PDF render configuration:"
    echo "$PDF_ENV"
else
    echo "${YELLOW}⚠${NC}  未找到PDF_RENDER环境变量 No PDF_RENDER environment variables found"
    echo "  如果使用application.yml配置则正常"
    echo "  This is normal if using application.yml configuration"
fi
echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "6. Java系统属性 Java System Properties"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

echo "java.io.tmpdir:"
java -XshowSettings:properties 2>&1 | grep java.io.tmpdir || echo "  (未设置 not set)"
echo ""

echo "file.encoding:"
java -XshowSettings:properties 2>&1 | grep file.encoding || echo "  (未设置 not set)"
echo ""

echo "java.awt.headless:"
java -XshowSettings:properties 2>&1 | grep java.awt.headless || echo "  (未设置 not set)"
echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "7. 字体文件完整性测试 Font File Integrity Test"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

if [ -n "$JAR_PATH" ]; then
    WORK_DIR="/tmp/font-check-$$"
    mkdir -p "$WORK_DIR"
    cd "$WORK_DIR"
    
    echo "提取字体文件进行验证 Extracting fonts for verification..."
    jar xf "$JAR_PATH" BOOT-INF/classes/fonts/ 2>/dev/null
    
    if [ -d "BOOT-INF/classes/fonts" ]; then
        echo "${GREEN}✓${NC} 字体文件成功提取 Fonts extracted successfully:"
        ls -lh BOOT-INF/classes/fonts/
        echo ""
        
        # Check each font file
        for font in BOOT-INF/classes/fonts/*.ttf BOOT-INF/classes/fonts/*.otf; do
            if [ -f "$font" ]; then
                SIZE=$(ls -lh "$font" | awk '{print $5}')
                echo "  $(basename $font): $SIZE"
                
                # Check if file is too small
                SIZE_BYTES=$(ls -l "$font" | awk '{print $5}')
                if [ "$SIZE_BYTES" -lt 100000 ]; then
                    echo "  ${RED}✗${NC} 警告：文件过小 (<100KB)"
                elif [ "$SIZE_BYTES" -lt 1000000 ]; then
                    echo "  ${YELLOW}⚠${NC}  文件较小 (<1MB) - 可能是子集字体"
                else
                    echo "  ${GREEN}✓${NC} 文件大小正常"
                fi
            fi
        done
    else
        echo "${RED}✗${NC} 无法提取字体目录 Cannot extract fonts directory"
    fi
    
    # Cleanup
    cd /tmp
    rm -rf "$WORK_DIR"
else
    echo "${RED}✗${NC} 无法测试 - JAR文件未找到"
fi
echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "8. 诊断摘要 Diagnostic Summary"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

ISSUES_FOUND=0

# Check JAR
if [ -z "$JAR_PATH" ]; then
    echo "${RED}✗${NC} JAR文件未找到"
    ISSUES_FOUND=$((ISSUES_FOUND + 1))
fi

# Check fonts in JAR
if [ -n "$JAR_PATH" ]; then
    FONTS_CHECK=$(jar tf "$JAR_PATH" | grep "fonts/.*\.ttf\|fonts/.*\.otf")
    if [ -z "$FONTS_CHECK" ]; then
        echo "${RED}✗${NC} JAR中没有字体文件"
        ISSUES_FOUND=$((ISSUES_FOUND + 1))
    fi
fi

# Check temp directory
if ! touch /tmp/test-$$ 2>/dev/null; then
    echo "${RED}✗${NC} 临时目录不可写"
    ISSUES_FOUND=$((ISSUES_FOUND + 1))
else
    rm -f /tmp/test-$$
fi

if [ $ISSUES_FOUND -eq 0 ]; then
    echo "${GREEN}✓${NC} 未发现明显问题 No obvious issues found"
    echo ""
    echo "如果中文仍显示为方框，请检查："
    echo "If Chinese still shows as boxes, check:"
    echo "  1. 应用日志中是否有 'Font registered' 消息"
    echo "     Check application logs for 'Font registered' messages"
    echo "  2. defaultFontFamily 配置是否正确"
    echo "     Verify defaultFontFamily configuration"
    echo "  3. 运行完整诊断工具："
    echo "     Run full diagnostic tool:"
    echo "     java -cp /app/app.jar com.mercury.pdf.render.FontFileVerifier"
else
    echo "${RED}✗${NC} 发现 $ISSUES_FOUND 个问题 Found $ISSUES_FOUND issue(s)"
    echo ""
    echo "请参考 CONTAINER_FONT_DIAGNOSTIC.md 获取解决方案"
    echo "Refer to CONTAINER_FONT_DIAGNOSTIC.md for solutions"
fi
echo ""

echo "╔════════════════════════════════════════════════════════════════════╗"
echo "║  诊断完成 Diagnostic Complete                                      ║"
echo "╚════════════════════════════════════════════════════════════════════╝"
