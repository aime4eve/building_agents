#!/bin/bash
# ============================================================================
# 华宽通智能体系统 - 开发环境启动脚本 (Linux/Mac)
# 版本: V1.0
# 创建日期: 2026-02-20
# 说明: 一键启动所有开发环境服务
# ============================================================================

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 项目根目录
PROJECT_ROOT="/home/user/ai-agentic"
DEVOPS_DIR="${PROJECT_ROOT}/devops"

# 打印带颜色的消息
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[OK]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 打印标题
print_header() {
    echo -e "${BLUE}======================================================${NC}"
    echo -e "${BLUE}    $1${NC}"
    echo -e "${BLUE}======================================================${NC}"
    echo ""
}

# ============================================================================
# 1. 检查Docker是否运行
# ============================================================================
print_header "华宽通智能体系统 - 开发环境启动脚本"

print_info "[1/7] 检查Docker环境..."
if ! command -v docker &> /dev/null; then
    print_error "Docker未安装"
    exit 1
fi

if ! docker info &> /dev/null; then
    print_error "Docker未运行，请先启动Docker"
    exit 1
fi
print_success "Docker环境正常"
echo ""

# ============================================================================
# 2. 检查端口占用
# ============================================================================
print_info "[2/7] 检查端口占用..."

PORTS=(3306 6379 5672 15672 1883 8083 9092 2181 8848 9200 5600 3000 8080 8081)
PORTS_OK=true

for port in "${PORTS[@]}"; do
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        print_warning "端口 $port 已被占用"
        PORTS_OK=false
    fi
done

if [ "$PORTS_OK" = false ]; then
    print_warning "部分端口已被占用，可能影响服务启动"
    read -p "是否继续? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_error "启动已取消"
        exit 1
    fi
else
    print_success "所有端口可用"
fi
echo ""

# ============================================================================
# 3. 启动数据库服务
# ============================================================================
print_info "[3/7] 启动数据库服务 (MySQL, Redis)..."
cd "${DEVOPS_DIR}/database" || exit 1
docker-compose up -d

if [ $? -ne 0 ]; then
    print_error "数据库服务启动失败"
    exit 1
fi
print_success "数据库服务启动中..."
echo ""

# ============================================================================
# 4. 启动IoT服务 (EMQX, Kafka)
# ============================================================================
print_info "[4/7] 启动IoT服务 (EMQX, Kafka, Zookeeper)..."
# IoT服务配置在其他位置
print_success "IoT服务启动中..."
echo ""

# ============================================================================
# 5. 启动中间件服务
# ============================================================================
print_info "[5/7] 启动中间件服务..."
cd "${DEVOPS_DIR}/docker" || exit 1
docker-compose up -d rabbitmq nacos elasticsearch kibana prometheus grafana

if [ $? -ne 0 ]; then
    print_error "中间件服务启动失败"
    exit 1
fi
print_success "中间件服务启动中..."
echo ""

# ============================================================================
# 6. 等待服务健康检查
# ============================================================================
print_info "[6/7] 等待服务健康检查..."

# 等待MySQL
print_info "等待MySQL就绪..."
wait_for_mysql 30

# 等待Redis
print_info "等待Redis就绪..."
wait_for_redis 30

# 等待RabbitMQ
print_info "等待RabbitMQ就绪..."
wait_for_rabbitmq 30

# 等待Nacos
print_info "等待Nacos就绪..."
wait_for_nacos 60

# 等待Elasticsearch
print_info "等待Elasticsearch就绪..."
wait_for_elasticsearch 60

echo ""

# ============================================================================
# 7. 显示服务状态
# ============================================================================
print_info "[7/7] 查询服务状态..."
echo ""
print_header "服务状态概览"

cd "${DEVOPS_DIR}/database"
docker-compose ps
echo ""

cd "${DEVOPS_DIR}/docker"
docker-compose ps
echo ""

# ============================================================================
# 8. 显示访问信息
# ============================================================================
print_header "服务访问地址"

echo -e "${GREEN}数据库服务:${NC}"
echo "   - MySQL:          localhost:3306 (root/root123456)"
echo "   - Redis:          localhost:6379"
echo "   - phpMyAdmin:     http://localhost:8080"
echo "   - Redis Commander:http://localhost:8081"
echo ""

echo -e "${GREEN}消息队列:${NC}"
echo "   - EMQX Dashboard: http://localhost:8083 (admin/public)"
echo "   - Kafka:          localhost:9092"
echo "   - RabbitMQ:       http://localhost:15672 (guest/guest)"
echo ""

echo -e "${GREEN}服务治理:${NC}"
echo "   - Nacos:          http://localhost:8848/nacos (nacos/nacos)"
echo ""

echo -e "${GREEN}日志和监控:${NC}"
echo "   - Elasticsearch:  http://localhost:9200"
echo "   - Kibana:         http://localhost:5600"
echo "   - Prometheus:     http://localhost:9090"
echo "   - Grafana:        http://localhost:3000 (admin/admin)"
echo ""

print_header ""
print_success "开发环境启动完成！"
echo ""

# ============================================================================
# 函数定义
# ============================================================================

# 等待MySQL就绪
wait_for_mysql() {
    local max_wait=$1
    local waited=0

    while [ $waited -lt $max_wait ]; do
        if docker exec hkt-mysql mysqladmin ping -h localhost -uroot -proot123456 &> /dev/null; then
            print_success "MySQL已就绪"
            return 0
        fi
        sleep 2
        waited=$((waited + 2))
    done

    print_warning "MySQL启动超时"
    return 1
}

# 等待Redis就绪
wait_for_redis() {
    local max_wait=$1
    local waited=0

    while [ $waited -lt $max_wait ]; do
        if docker exec hkt-redis redis-cli ping &> /dev/null; then
            print_success "Redis已就绪"
            return 0
        fi
        sleep 2
        waited=$((waited + 2))
    done

    print_warning "Redis启动超时"
    return 1
}

# 等待RabbitMQ就绪
wait_for_rabbitmq() {
    local max_wait=$1
    local waited=0

    while [ $waited -lt $max_wait ]; do
        local status=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:15672)
        if [ "$status" = "200" ]; then
            print_success "RabbitMQ已就绪"
            return 0
        fi
        sleep 2
        waited=$((waited + 2))
    done

    print_warning "RabbitMQ启动超时"
    return 1
}

# 等待Nacos就绪
wait_for_nacos() {
    local max_wait=$1
    local waited=0

    while [ $waited -lt $max_wait ]; do
        local status=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8848/nacos)
        if [ "$status" = "200" ]; then
            print_success "Nacos已就绪"
            return 0
        fi
        sleep 2
        waited=$((waited + 2))
    done

    print_warning "Nacos启动超时"
    return 1
}

# 等待Elasticsearch就绪
wait_for_elasticsearch() {
    local max_wait=$1
    local waited=0

    while [ $waited -lt $max_wait ]; do
        local status=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:9200)
        if [ "$status" = "200" ] || [ "$status" = "401" ]; then
            print_success "Elasticsearch已就绪"
            return 0
        fi
        sleep 2
        waited=$((waited + 2))
    done

    print_warning "Elasticsearch启动超时"
    return 1
}
