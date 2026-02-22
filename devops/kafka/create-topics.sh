#!/bin/bash
## =====================================================================
## Kafka Topics Creation Script
## 华宽通智能体系统 - Kafka Topic创建脚本
## =====================================================================

set -e

## ----------------------------------------------------------------------
## 配置变量
## ----------------------------------------------------------------------
KAFKA_BROKER=${KAFKA_BROKER:-"localhost:9092"}
REPLICATION_FACTOR=${REPLICATION_FACTOR:-3}
PARTITIONS_TELEMETRY=${PARTITIONS_TELEMETRY:-50}
PARTITIONS_EVENT=${PARTITIONS_EVENT:-20}
PARTITIONS_STATUS=${PARTITIONS_STATUS:-20}
PARTITIONS_COMMAND=${PARTITIONS_COMMAND:-30}

## 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

## ----------------------------------------------------------------------
## 函数定义
## ----------------------------------------------------------------------
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

create_topic() {
    local topic_name=$1
    local partitions=$2
    local retention_ms=$3
    local segment_ms=$4

    log_info "Creating topic: $topic_name"

    kafka-topics.sh --create \
        --topic "$topic_name" \
        --partitions "$partitions" \
        --replication-factor "$REPLICATION_FACTOR" \
        --config retention.ms="$retention_ms" \
        --config segment.ms="$segment_ms" \
        --config flush.messages=10000 \
        --config flush.ms=1000 \
        --config cleanup.policy=delete \
        --bootstrap-server "$KAFKA_BROKER" && \
        log_info "Topic $topic_name created successfully" || \
        log_warn "Topic $topic_name may already exist"
}

## ----------------------------------------------------------------------
## 检查Kafka连接
## ----------------------------------------------------------------------
log_info "Checking Kafka connection to $KAFKA_BROKER..."
if ! kafka-broker-api-versions.sh --bootstrap-server "$KAFKA_BROKER" &> /dev/null; then
    log_error "Cannot connect to Kafka broker at $KAFKA_BROKER"
    exit 1
fi
log_info "Kafka connection OK"

## ----------------------------------------------------------------------
## 创建Topics
## ----------------------------------------------------------------------

## 1. 遥测数据Topic
## - 30天保留（2592000000ms）
## - 每天一个segment（86400000ms）
## - 50个分区（按设备ID哈希）
create_topic \
    "device-telemetry" \
    "$PARTITIONS_TELEMETRY" \
    2592000000 \
    86400000

## 2. 设备事件Topic
## - 90天保留（7776000000ms）
## - 每天一个segment
## - 20个分区（按租户ID哈希）
create_topic \
    "device-event" \
    "$PARTITIONS_EVENT" \
    7776000000 \
    86400000

## 3. 设备状态Topic
## - 7天保留（604800000ms）
## - 每小时一个segment（3600000ms）
## - 20个分区（按设备ID哈希）
create_topic \
    "device-status" \
    "$PARTITIONS_STATUS" \
    604800000 \
    3600000

## 4. 命令下发Topic
## - 1天保留（86400000ms）
## - 每小时一个segment
## - 30个分区（按设备ID哈希）
create_topic \
    "device-command" \
    "$PARTITIONS_COMMAND" \
    86400000 \
    3600000

## 5. 告警Topic（新增）
## - 90天保留
## - 每天一个segment
## - 10个分区
create_topic \
    "device-alarm" \
    10 \
    7776000000 \
    86400000

## 6. OTA升级Topic（新增）
## - 30天保留
## - 每天一个segment
## - 5个分区
create_topic \
    "device-ota" \
    5 \
    2592000000 \
    86400000

## ----------------------------------------------------------------------
## 验证Topics
## ----------------------------------------------------------------------
log_info "Verifying created topics..."
kafka-topics.sh --list --bootstrap-server "$KAFKA_BROKER" | grep -E "^device-"

## ----------------------------------------------------------------------
## 显示Topic详情
## ----------------------------------------------------------------------
log_info "Topic details:"
kafka-topics.sh --describe --bootstrap-server "$KAFKA_BROKER" | grep -E "Topic: device-"

log_info "All topics created successfully!"
