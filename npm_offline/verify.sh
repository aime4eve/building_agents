#!/bin/bash

#===============================================================================
# OpenSpec 离线包完整性校验脚本 - Linux 版本
#===============================================================================

set -e

PACKAGE_NAME="fission-ai-openspec-1.2.0.tgz"
EXPECTED_SIZE_KB=200

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

log_info() {
    echo -e "${CYAN}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[OK]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_file_structure() {
    log_info "检查目录结构..."
    
    local required_files=(
        "packages/${PACKAGE_NAME}"
        "install.ps1"
        "install.sh"
        "package.json"
        "README.md"
    )
    
    local all_exist=true
    
    for file in "${required_files[@]}"; do
        if [ -f "$file" ]; then
            log_success "  $file"
        else
            log_error "  [缺失] $file"
            all_exist=false
        fi
    done
    
    $all_exist
}

check_package_integrity() {
    log_info "检查离线包完整性..."
    
    local package_path="packages/${PACKAGE_NAME}"
    
    if [ ! -f "$package_path" ]; then
        log_error "离线包不存在: $package_path"
        return 1
    fi
    
    # 检查文件大小
    local size_bytes=$(stat -f%z "$package_path" 2>/dev/null || stat -c%s "$package_path" 2>/dev/null)
    local size_kb=$((size_bytes / 1024))
    
    if [ $size_kb -gt 100 ] && [ $size_kb -lt 500 ]; then
        log_success "  文件大小: ${size_kb} KB"
    else
        log_warning "  文件大小异常: ${size_kb} KB (预期约 200 KB)"
    fi
    
    # 检查文件类型
    local file_type=$(file -b "$package_path")
    if echo "$file_type" | grep -qi "gzip\|tar"; then
        log_success "  包格式验证通过: $file_type"
        return 0
    else
        log_error "  包格式验证失败: $file_type"
        return 1
    fi
}

check_script_syntax() {
    log_info "检查脚本语法..."
    
    # 检查 Bash 脚本语法
    if bash -n install.sh 2>/dev/null; then
        log_success "  install.sh 语法正确"
    else
        log_error "  install.sh 语法错误"
        return 1
    fi
    
    # 检查 PowerShell 脚本是否存在
    if [ -f "install.ps1" ]; then
        log_success "  install.ps1 存在"
    fi
    
    return 0
}

main() {
    echo ""
    echo -e "${CYAN}========================================${NC}"
    echo -e "${CYAN}  OpenSpec 离线包完整性校验${NC}"
    echo -e "${CYAN}========================================${NC}"
    echo ""
    
    local passed=true
    
    if ! check_file_structure; then
        passed=false
    fi
    
    if ! check_package_integrity; then
        passed=false
    fi
    
    if ! check_script_syntax; then
        passed=false
    fi
    
    echo ""
    if $passed; then
        echo -e "${GREEN}========================================${NC}"
        echo -e "${GREEN}  所有检查通过!${NC}"
        echo -e "${GREEN}========================================${NC}"
        exit 0
    else
        echo -e "${RED}========================================${NC}"
        echo -e "${RED}  存在问题，请检查上述错误${NC}"
        echo -e "${RED}========================================${NC}"
        exit 1
    fi
}

main
