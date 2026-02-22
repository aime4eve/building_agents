#!/bin/bash
# ============================================================================
# 华宽通智能体系统 - 服务健康检查脚本
# 版本: V1.0
# 创建日期: 2026-02-20
# 说明: 检查所有开发环境服务的健康状态
# ============================================================================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 服务定义
declare -A SERVICES=(
    ["MySQL"]="localhost:3306"
    ["Redis"]="localhost:6379"
    ["RabbitMQ"]="localhost:15672"
    ["EMQX"]="localhost:8083"
    ["Kafka"]="localhost:9092"
    ["Nacos"]="localhost:8848"
    ["Elasticsearch"]="localhost:9200"
    ["Kibana"]="localhost:5600"
    ["Prometheus"]="localhost:9090"
    ["Grafana"]="localhost:3000"
    ["phpMyAdmin"]="localhost:8080"
    ["Redis Commander"]="localhost:8081"
)

# 健康检查函数
check_service() {
    local name=$1
    local address=$2
    local host=${address%:*}
    local port=${address#*:}

    # 检查端口是否开放
    if timeout 3 bash -c "cat < /dev/null > /dev/tcp/$host/$port" 2>/dev/null; then
        echo -e "${GREEN}[√]${NC} $name ($address)"
        return 0
    else
        echo -e "${RED}[×]${NC} $name ($address)"
        return 1
    fi
}

# HTTP服务检查
check_http_service() {
    local name=$1
    local url=$2
    local expected_code=${3:-200}

    local status=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 3 "$url" 2>/dev/null)
    if [ "$status" = "$expected_code" ] || [ "$status" = "401" ]; then
        echo -e "${GREEN}[√]${NC} $name ($url)"
        return 0
    else
        echo -e "${RED}[×]${NC} $name ($url) - HTTP $status"
        return 1
    fi
}

# Docker容器检查
check_docker_container() {
    local container=$1
    if docker ps --format '{{.Names}}' | grep -q "^${container}$"; then
        local status=$(docker inspect --format='{{.State.Health.Status}}' "$container" 2>/dev/null)
        if [ "$status" = "healthy" ] || [ -z "$status" ]; then
            echo -e "${GREEN}[√]${NC} 容器 $container 运行中"
            return 0
        else
            echo -e "${YELLOW}[!]${NC} 容器 $container 状态: $status"
            return 1
        fi
    else
        echo -e "${RED}[×]${NC} 容器 $container 未运行"
        return 1
    fi
}

# 打印标题
print_header() {
    echo ""
    echo -e "${BLUE}======================================================${NC}"
    echo -e "${BLUE}    $1${NC}"
    echo -e "${BLUE}======================================================${NC}"
    echo ""
}

# ============================================================================
# 主程序
# ============================================================================
print_header "华宽通智能体系统 - 服务健康检查"

# 1. 检查Docker环境
echo -e "${BLUE}[1/4] 检查Docker环境...${NC}"
if docker info &> /dev/null; then
    echo -e "${GREEN}[√]${NC} Docker运行中"
else
    echo -e "${RED}[×]${NC} Docker未运行"
    exit 1
fi
echo ""

# 2. 检查端口连通性
echo -e "${BLUE}[2/4] 检查端口连通性...${NC}"
for service in "${!SERVICES[@]}"; do
    check_service "$service" "${SERVICES[$service]}"
done
echo ""

# 3. 检查HTTP服务
echo -e "${BLUE}[3/4] 检查HTTP服务...${NC}"
check_http_service "RabbitMQ Management" "http://localhost:15672" "200"
check_http_service "EMQX Dashboard" "http://localhost:8083" "200"
check_http_service "Nacos Console" "http://localhost:8848/nacos" "200"
check_http_service "Elasticsearch" "http://localhost:9200" "200"
check_http_service "Kibana" "http://localhost:5600" "200"
check_http_service "Prometheus" "http://localhost:9090" "200"
check_http_service "Grafana" "http://localhost:3000" "200"
check_http_service "phpMyAdmin" "http://localhost:8080" "200"
check_http_service "Redis Commander" "http://localhost:8081" "200"
echo ""

# 4. 检查Docker容器状态
echo -e "${BLUE}[4/4] 检查Docker容器状态...${NC}"
check_docker_container "hkt-mysql"
check_docker_container "hkt-redis"
check_docker_container "hkt-rabbitmq"
check_docker_container "hkt-nacos"
check_docker_container "hkt-elasticsearch"
check_docker_container "hkt-prometheus"
check_docker_container "hkt-grafana"
echo ""

# 5. 检查数据库连接
echo -e "${BLUE}[5/5] 检查数据库连接...${NC}"

# MySQL检查
if docker exec hkt-mysql mysqladmin ping -h localhost -uroot -proot123456 &> /dev/null; then
    echo -e "${GREEN}[√]${NC} MySQL连接成功"
    # 显示数据库列表
    echo "   可用数据库:"
    docker exec hkt-mysql mysql -uroot -proot123456 -e "SHOW DATABASES;" 2>/dev/null | grep -v "Database" | grep -v "information_schema" | grep -v "mysql" | grep -v "performance_schema" | grep -v "sys" | while read db; do
        echo "     - $db"
    done
else
    echo -e "${RED}[×]${NC} MySQL连接失败"
fi

# Redis检查
if docker exec hkt-redis redis-cli ping &> /dev/null; then
    echo -e "${GREEN}[√]${NC} Redis连接成功"
    # 显示Redis信息
    local redis_info=$(docker exec hkt-redis redis-cli INFO server 2>/dev/null | grep "redis_version" | cut -d: -f2 | tr -d '\r')
    echo "   Redis版本: $redis_info"
else
    echo -e "${RED}[×]${NC} Redis连接失败"
fi

echo ""
print_header "健康检查完成"
