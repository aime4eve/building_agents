#!/bin/bash
# ============================================================================
# 华宽通智能体系统 - 时序数据库启动脚本
# 版本: V1.0
# ============================================================================

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查Docker是否安装
check_docker() {
    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed. Please install Docker first."
        exit 1
    fi

    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        log_error "Docker Compose is not installed. Please install Docker Compose first."
        exit 1
    fi

    log_info "Docker environment check passed."
}

# 等待服务就绪
wait_for_service() {
    local service_name=$1
    local health_check_url=$2
    local max_attempts=30
    local attempt=1

    log_info "Waiting for $service_name to be ready..."

    while [ $attempt -le $max_attempts ]; do
        if curl -sf "$health_check_url" > /dev/null 2>&1; then
            log_info "$service_name is ready!"
            return 0
        fi

        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done

    log_error "$service_name failed to start within expected time."
    return 1
}

# 初始化InfluxDB
init_influxdb() {
    log_info "Initializing InfluxDB..."

    # 等待InfluxDB就绪
    wait_for_service "InfluxDB" "http://localhost:8086/health"

    # 执行初始化设置
    docker exec hkt-influxdb influx setup \
        --username admin \
        --password admin123456 \
        --org huakuantong \
        --bucket huakuantong_telemetry \
        --retention 90d \
        --token my-super-secret-auth-token \
        --force

    if [ $? -eq 0 ]; then
        log_info "InfluxDB initialized successfully!"
    else
        log_warn "InfluxDB may already be initialized."
    fi
}

# 初始化TDengine
init_tdengine() {
    log_info "Initializing TDengine..."

    # 等待TDengine就绪
    local max_attempts=30
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        if docker exec hkt-tdengine taos -s "show databases;" > /dev/null 2>&1; then
            log_info "TDengine is ready!"

            # 创建数据库
            docker exec hkt-tdengine taos -s "CREATE DATABASE IF NOT EXISTS telemetry KEEP 90 UPDATE 1;"

            log_info "TDengine initialized successfully!"
            return 0
        fi

        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done

    log_error "TDengine failed to start."
    return 1
}

# 启动InfluxDB方案
start_influxdb() {
    log_info "Starting InfluxDB solution..."

    docker-compose -f docker-compose-timeseries.yml up -d influxdb redis mysql

    init_influxdb

    log_info "InfluxDB solution started successfully!"
    echo ""
    echo "=========================================="
    echo "Service URLs:"
    echo "  InfluxDB Web UI: http://localhost:8086"
    echo "  InfluxDB API:    http://localhost:8086"
    echo "  MySQL:           localhost:3306"
    echo "  Redis:           localhost:6379"
    echo "=========================================="
    echo ""
    echo "Credentials:"
    echo "  Username: admin"
    echo "  Password: admin123456"
    echo "  Org:      huakuantong"
    echo "  Token:    my-super-secret-auth-token"
    echo "=========================================="
}

# 启动TDengine方案
start_tdengine() {
    log_info "Starting TDengine solution..."

    docker-compose -f docker-compose-timeseries.yml --profile tdengine up -d tdengine redis mysql

    init_tdengine

    log_info "TDengine solution started successfully!"
    echo ""
    echo "=========================================="
    echo "Service URLs:"
    echo "  TDengine TCP:    localhost:6030"
    echo "  TDengine REST:   http://localhost:6041"
    echo "  MySQL:           localhost:3306"
    echo "  Redis:           localhost:6379"
    echo "=========================================="
    echo ""
    echo "Credentials:"
    echo "  Username: root"
    echo "  Password: taosdata"
    echo "=========================================="
}

# 启动完整监控栈
start_monitoring() {
    log_info "Starting full monitoring stack..."

    docker-compose -f docker-compose-timeseries.yml --profile monitoring --profile ui up -d

    init_influxdb

    log_info "Full monitoring stack started successfully!"
    echo ""
    echo "=========================================="
    echo "Service URLs:"
    echo "  InfluxDB Web UI:  http://localhost:8086"
    echo "  InfluxDB UI:      http://localhost:8088"
    echo "  Grafana:          http://localhost:3000"
    echo "  MySQL:            localhost:3306"
    echo "  Redis:            localhost:6379"
    echo "=========================================="
    echo ""
    echo "Grafana Credentials:"
    echo "  Username: admin"
    echo "  Password: admin123"
    echo "=========================================="
}

# 停止服务
stop_services() {
    log_info "Stopping all services..."

    docker-compose -f docker-compose-timeseries.yml down

    log_info "All services stopped."
}

# 查看服务状态
show_status() {
    log_info "Service status:"
    echo ""
    docker-compose -f docker-compose-timeseries.yml ps
}

# 查看日志
show_logs() {
    local service=$1

    if [ -z "$service" ]; then
        docker-compose -f docker-compose-timeseries.yml logs -f
    else
        docker-compose -f docker-compose-timeseries.yml logs -f "$service"
    fi
}

# 清理数据
clean_data() {
    log_warn "This will remove all data volumes. Are you sure? (y/N)"
    read -r response

    if [[ "$response" =~ ^[Yy]$ ]]; then
        log_info "Removing data volumes..."
        docker-compose -f docker-compose-timeseries.yml down -v
        log_info "Data volumes removed."
    else
        log_info "Operation cancelled."
    fi
}

# 主菜单
show_menu() {
    echo ""
    echo "=========================================="
    echo "  华宽通时序数据库管理脚本"
    echo "=========================================="
    echo ""
    echo "1) 启动 InfluxDB 方案"
    echo "2) 启动 TDengine 方案"
    echo "3) 启动完整监控栈 (InfluxDB + Grafana)"
    echo "4) 停止所有服务"
    echo "5) 查看服务状态"
    echo "6) 查看日志"
    echo "7) 清理数据"
    echo "0) 退出"
    echo ""
    echo "=========================================="
}

# 主函数
main() {
    check_docker

    if [ $# -gt 0 ]; then
        case "$1" in
            influxdb)
                start_influxdb
                ;;
            tdengine)
                start_tdengine
                ;;
            monitoring)
                start_monitoring
                ;;
            stop)
                stop_services
                ;;
            status)
                show_status
                ;;
            logs)
                show_logs "$2"
                ;;
            clean)
                clean_data
                ;;
            *)
                echo "Usage: $0 {influxdb|tdengine|monitoring|stop|status|logs|clean}"
                exit 1
                ;;
        esac
    else
        while true; do
            show_menu
            read -p "请选择操作 [0-7]: " choice

            case $choice in
                1)
                    start_influxdb
                    ;;
                2)
                    start_tdengine
                    ;;
                3)
                    start_monitoring
                    ;;
                4)
                    stop_services
                    ;;
                5)
                    show_status
                    ;;
                6)
                    read -p "请输入服务名称 (留空查看所有): " service
                    show_logs "$service"
                    ;;
                7)
                    clean_data
                    ;;
                0)
                    log_info "Goodbye!"
                    exit 0
                    ;;
                *)
                    log_error "无效选择，请重新输入。"
                    ;;
            esac
        done
    fi
}

# 执行主函数
main "$@"
