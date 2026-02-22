#!/bin/bash
# ============================================================================
# 华宽通智能体系统 - 数据库连接验证脚本
# 版本: V1.0
# ============================================================================

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

# ============================================================================
# MySQL连接验证
# ============================================================================
test_mysql() {
    log_info "Testing MySQL connection..."

    # 等待MySQL启动
    local max_attempts=30
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        if docker exec huakuantong-mysql mysqladmin ping -h localhost -uroot -proot123456 > /dev/null 2>&1; then
            log_success "MySQL is ready!"

            # 测试数据库连接
            docker exec huakuantong-mysql mysql -uroot -proot123456 -e "SELECT 1 FROM huakuantong_agent.tenant LIMIT 1;" > /dev/null 2>&1

            if [ $? -eq 0 ]; then
                log_success "Database connection test passed!"

                # 显示数据库信息
                echo ""
                echo "=========================================="
                echo "MySQL Connection Info:"
                echo "  Host: localhost:3306"
                echo "  Database: huakuantong_agent"
                echo "  Username: root / huakuantong"
                echo "  Password: root123456 / hkt123456"
                echo "=========================================="
                echo ""

                # 显示表数量
                local table_count=$(docker exec huakuantong-mysql mysql -uroot -proot123456 -e "SELECT COUNT(*) AS count FROM information_schema.tables WHERE table_schema='huakuantong_agent';" -s)
                log_info "Tables created: $table_count"

                return 0
            fi
        fi

        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done

    log_error "MySQL connection test failed!"
    return 1
}

# ============================================================================
# InfluxDB连接验证
# ============================================================================
test_influxdb() {
    log_info "Testing InfluxDB connection..."

    # 等待InfluxDB启动
    local max_attempts=30
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        if curl -sf http://localhost:8086/health > /dev/null 2>&1; then
            log_success "InfluxDB is ready!"

            # 测试API查询
            local response=$(curl -s http://localhost:8086/api/v2/buckets -H "Authorization: Token my-super-secret-auth-token")

            if echo "$response" | grep -q "huakuantong_telemetry"; then
                log_success "InfluxDB connection test passed!"

                echo ""
                echo "=========================================="
                echo "InfluxDB Connection Info:"
                echo "  URL: http://localhost:8086"
                echo "  Org: huakuantong"
                echo "  Bucket: huakuantong_telemetry"
                echo "  Token: my-super-secret-auth-token"
                echo "=========================================="
                echo ""

                return 0
            fi
        fi

        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done

    log_warn "InfluxDB may not be running. Start with: docker-compose -f docker-compose-timeseries.yml up -d influxdb"
    return 1
}

# ============================================================================
# TDengine连接验证
# ============================================================================
test_tdengine() {
    log_info "Testing TDengine connection..."

    # 检查TDengine容器是否存在
    if ! docker ps | grep -q hkt-tdengine; then
        log_warn "TDengine container is not running. Start with: docker-compose -f docker-compose-timeseries.yml --profile tdengine up -d"
        return 1
    fi

    # 等待TDengine启动
    local max_attempts=30
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        if docker exec hkt-tdengine taos -s "show databases;" > /dev/null 2>&1; then
            log_success "TDengine is ready!"

            # 测试数据库查询
            docker exec hkt-tdengine taos -s "USE telemetry; SHOW TABLES;" > /dev/null 2>&1

            if [ $? -eq 0 ]; then
                log_success "TDengine connection test passed!"

                echo ""
                echo "=========================================="
                echo "TDengine Connection Info:"
                echo "  TCP: localhost:6030"
                echo "  REST: http://localhost:6041"
                echo "  Database: telemetry"
                echo "  Username: root"
                echo "  Password: taosdata"
                echo "=========================================="
                echo ""

                return 0
            fi
        fi

        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done

    log_error "TDengine connection test failed!"
    return 1
}

# ============================================================================
# Redis连接验证
# ============================================================================
test_redis() {
    log_info "Testing Redis connection..."

    if ! docker ps | grep -q huakuantong-redis; then
        log_warn "Redis container is not running."
        return 1
    fi

    # 测试Redis连接
    if docker exec huakuantong-redis redis-cli -a redis123 ping > /dev/null 2>&1; then
        log_success "Redis is ready!"

        echo ""
        echo "=========================================="
        echo "Redis Connection Info:"
        echo "  Host: localhost:6379"
        echo "  Password: redis123"
        echo "=========================================="
        echo ""

        return 0
    else
        log_error "Redis connection test failed!"
        return 1
    fi
}

# ============================================================================
# 主函数
# ============================================================================
main() {
    echo ""
    echo "=========================================="
    echo "  华宽通数据库连接验证工具"
    echo "=========================================="
    echo ""

    # 检查Docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed!"
        exit 1
    fi

    # 执行测试
    local mysql_result=0
    local influxdb_result=0
    local tdengine_result=0
    local redis_result=0

    # MySQL测试
    if docker ps | grep -q huakuantong-mysql; then
        test_mysql
        mysql_result=$?
    else
        log_warn "MySQL container is not running. Start with: docker-compose up -d mysql"
    fi

    # InfluxDB测试
    if curl -sf http://localhost:8086/ping > /dev/null 2>&1; then
        test_influxdb
        influxdb_result=$?
    else
        log_warn "InfluxDB is not accessible."
    fi

    # TDengine测试
    if docker ps | grep -q hkt-tdengine; then
        test_tdengine
        tdengine_result=$?
    fi

    # Redis测试
    if docker ps | grep -q huakuantong-redis; then
        test_redis
        redis_result=$?
    fi

    # 汇总结果
    echo ""
    echo "=========================================="
    echo "  测试结果汇总"
    echo "=========================================="
    echo ""

    [ $mysql_result -eq 0 ] && echo "✓ MySQL" || echo "✗ MySQL"
    [ $influxdb_result -eq 0 ] && echo "✓ InfluxDB" || echo "✗ InfluxDB"
    [ $tdengine_result -eq 0 ] && echo "✓ TDengine" || echo "✗ TDengine"
    [ $redis_result -eq 0 ] && echo "✓ Redis" || echo "✗ Redis"
    echo ""
    echo "=========================================="
}

# 执行主函数
main "$@"
