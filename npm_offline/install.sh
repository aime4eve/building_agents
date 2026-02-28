#!/bin/bash

#===============================================================================
# OpenSpec 离线安装脚本 - Linux 版本
#
# 描述:
#   此脚本用于在无法访问外网的 Linux 环境中安装 OpenSpec。
#   需要 Node.js 20.19.0 或更高版本。
#
# 用法:
#   ./install.sh           # 本地安装
#   ./install.sh --global  # 全局安装
#   ./install.sh -h        # 显示帮助
#
# 作者: OpenSpec Offline Installer
# 版本: 1.0.0
#===============================================================================

set -e

# 配置
REQUIRED_NODE_VERSION="20.19.0"
PACKAGE_NAME="fission-ai-openspec-1.2.0.tgz"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
INSTALL_PATH="${SCRIPT_DIR}"
GLOBAL_INSTALL=false

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${CYAN}[$(date '+%Y-%m-%d %H:%M:%S')] [INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[$(date '+%Y-%m-%d %H:%M:%S')] [SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')] [WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[$(date '+%Y-%m-%d %H:%M:%S')] [ERROR]${NC} $1"
}

# 显示帮助
show_help() {
    echo ""
    echo -e "${CYAN}========================================${NC}"
    echo -e "${CYAN}  OpenSpec 离线安装程序 v1.0.0${NC}"
    echo -e "${CYAN}  Linux 版本${NC}"
    echo -e "${CYAN}========================================${NC}"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  -g, --global    全局安装"
    echo "  -p, --path      指定安装路径 (默认: 当前目录)"
    echo "  -h, --help      显示此帮助信息"
    echo ""
    echo "示例:"
    echo "  $0                    # 本地安装"
    echo "  $0 --global           # 全局安装"
    echo "  $0 --path /opt/tools  # 指定安装路径"
    echo ""
}

# 解析参数
parse_args() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            -g|--global)
                GLOBAL_INSTALL=true
                shift
                ;;
            -p|--path)
                INSTALL_PATH="$2"
                shift 2
                ;;
            -h|--help)
                show_help
                exit 0
                ;;
            *)
                log_error "未知参数: $1"
                show_help
                exit 1
                ;;
        esac
    done
}

# 检查 Node.js 版本
check_node_version() {
    log_info "检查 Node.js 版本..."
    
    if ! command -v node &> /dev/null; then
        log_error "未检测到 Node.js"
        log_info "请安装 Node.js $REQUIRED_NODE_VERSION 或更高版本"
        log_info "推荐使用 nvm 安装:"
        log_info "  curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash"
        log_info "  nvm install $REQUIRED_NODE_VERSION"
        log_info "  nvm use $REQUIRED_NODE_VERSION"
        return 1
    fi
    
    NODE_VERSION=$(node --version | sed 's/v//')
    
    # 版本比较
    if [ "$(printf '%s\n' "$REQUIRED_NODE_VERSION" "$NODE_VERSION" | sort -V | head -n1)" != "$REQUIRED_NODE_VERSION" ]; then
        log_error "Node.js 版本过低: v$NODE_VERSION"
        log_error "需要版本: v$REQUIRED_NODE_VERSION 或更高"
        return 1
    fi
    
    log_success "Node.js 版本检查通过: v$NODE_VERSION"
    return 0
}

# 检查离线包是否存在
check_package_exists() {
    log_info "检查离线包..."
    
    PACKAGE_PATH="${INSTALL_PATH}/packages/${PACKAGE_NAME}"
    
    if [ ! -f "$PACKAGE_PATH" ]; then
        log_error "离线包不存在: $PACKAGE_PATH"
        log_info "请确保以下文件存在:"
        log_info "  packages/${PACKAGE_NAME}"
        return 1
    fi
    
    log_success "离线包检查通过: $PACKAGE_PATH"
    return 0
}

# 安装 OpenSpec
install_openspec() {
    log_info "开始安装 OpenSpec..."
    
    PACKAGE_PATH="${INSTALL_PATH}/packages/${PACKAGE_NAME}"
    
    if [ "$GLOBAL_INSTALL" = true ]; then
        log_info "安装模式: 全局安装"
        npm install -g "$PACKAGE_PATH"
    else
        log_info "安装模式: 本地安装"
        npm install "$PACKAGE_PATH" --save-dev
    fi
    
    if [ $? -ne 0 ]; then
        log_error "安装失败"
        return 1
    fi
    
    return 0
}

# 验证安装
verify_installation() {
    log_info "验证安装..."
    
    if command -v openspec &> /dev/null; then
        VERSION=$(openspec --version 2>/dev/null || true)
        if [ -n "$VERSION" ]; then
            log_success "OpenSpec 安装成功! 版本: $VERSION"
            return 0
        fi
    fi
    
    if npx openspec --version &> /dev/null; then
        VERSION=$(npx openspec --version 2>/dev/null || true)
        if [ -n "$VERSION" ]; then
            log_success "OpenSpec 安装成功! 版本: $VERSION"
            return 0
        fi
    fi
    
    log_error "安装验证失败"
    return 1
}

# 显示使用指南
show_usage() {
    echo ""
    echo -e "${CYAN}========================================${NC}"
    echo -e "${CYAN}  OpenSpec 使用指南${NC}"
    echo -e "${CYAN}========================================${NC}"
    echo ""
    echo -e "${YELLOW}初始化项目:${NC}"
    echo "  cd your-project"
    echo "  npx openspec init"
    echo ""
    echo -e "${YELLOW}创建变更提案:${NC}"
    echo "  /opsx:propose \"your feature description\""
    echo ""
    echo -e "${YELLOW}查看帮助:${NC}"
    echo "  openspec --help"
    echo ""
    echo -e "${YELLOW}禁用遥测:${NC}"
    echo "  export OPENSPEC_TELEMETRY=0"
    echo ""
}

# 主函数
main() {
    echo ""
    echo -e "${CYAN}========================================${NC}"
    echo -e "${CYAN}  OpenSpec 离线安装程序 v1.0.0${NC}"
    echo -e "${CYAN}  Linux 版本${NC}"
    echo -e "${CYAN}========================================${NC}"
    echo ""
    
    parse_args "$@"
    
    log_info "安装路径: $INSTALL_PATH"
    log_info "安装模式: $( [ "$GLOBAL_INSTALL" = true ] && echo '全局' || echo '本地' )"
    
    if ! check_node_version; then
        exit 1
    fi
    
    if ! check_package_exists; then
        exit 1
    fi
    
    if ! install_openspec; then
        exit 1
    fi
    
    if ! verify_installation; then
        exit 1
    fi
    
    show_usage
    
    log_success "安装完成!"
}

main "$@"
