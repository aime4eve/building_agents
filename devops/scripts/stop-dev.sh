#!/bin/bash
# ============================================================================
# 华宽通智能体系统 - 开发环境停止脚本 (Linux/Mac)
# 版本: V1.0
# 创建日期: 2026-02-20
# 说明: 停止所有开发环境服务
# ============================================================================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m'

PROJECT_ROOT="/home/user/ai-agentic"
DEVOPS_DIR="${PROJECT_ROOT}/devops"

print_header() {
    echo -e "${BLUE}======================================================${NC}"
    echo -e "${BLUE}    $1${NC}"
    echo -e "${BLUE}======================================================${NC}"
    echo ""
}

print_header "华宽通智能体系统 - 开发环境停止脚本"

echo -e "${YELLOW}请选择停止方式:${NC}"
echo "   1. 停止所有服务 (保留数据)"
echo "   2. 停止并删除容器 (保留数据)"
echo "   3. 完全清理 (包括数据)"
echo ""
read -p "请输入选项 (1-3): " CHOICE

case $CHOICE in
    1)
        echo ""
        echo -e "${YELLOW}停止所有服务...${NC}"
        cd "${DEVOPS_DIR}/database"
        docker-compose stop
        cd "${DEVOPS_DIR}/docker"
        docker-compose stop
        echo -e "${GREEN}√ 所有服务已停止${NC}"
        ;;
    2)
        echo ""
        echo -e "${YELLOW}停止并删除容器...${NC}"
        cd "${DEVOPS_DIR}/database"
        docker-compose down
        cd "${DEVOPS_DIR}/docker"
        docker-compose down
        echo -e "${GREEN}√ 容器已删除，数据保留${NC}"
        ;;
    3)
        echo ""
        echo -e "${RED}警告: 此操作将删除所有数据卷!${NC}"
        read -p "确认执行? (yes/no): " CONFIRM
        if [ "$CONFIRM" != "yes" ]; then
            echo -e "${YELLOW}操作已取消${NC}"
            exit 0
        fi
        echo ""
        echo -e "${YELLOW}完全清理所有服务和数据...${NC}"
        cd "${DEVOPS_DIR}/database"
        docker-compose down -v
        cd "${DEVOPS_DIR}/docker"
        docker-compose down -v
        echo -e "${GREEN}√ 完全清理完成${NC}"
        ;;
    *)
        echo -e "${RED}无效选项${NC}"
        exit 1
        ;;
esac
echo ""
