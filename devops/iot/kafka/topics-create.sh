#!/bin/bash
# =============================================================================
# Kafka Topic 创建脚本 - 设备接入层
# =============================================================================
# 说明: 根据设备接入层架构设计说明书创建所需Topic
# =============================================================================

# Kafka Broker地址
KAFKA_BROKERS="kafka1:9092,kafka2:9093,kafka3:9094"

# 等待Kafka就绪
echo "等待Kafka集群就绪..."
while ! kafka-broker-api-versions.sh --bootstrap-server $KAFKA_BROKERS 2>/dev/null; do
  echo "Kafka集群尚未就绪，等待中..."
  sleep 5
done
echo "Kafka集群已就绪"

# ---------------------------------------------------------------------------
# 创建设备遥测数据Topic
# ---------------------------------------------------------------------------
echo "创建 device-telemetry Topic..."
kafka-topics.sh --create \
  --topic device-telemetry \
  --partitions 50 \
  --replication-factor 3 \
  --config retention.ms=2592000000 \
  --config segment.ms=86400000 \
  --config cleanup.policy=delete \
  --config compression.type=snappy \
  --config max.message.bytes=1048576 \
  --bootstrap-server $KAFKA_BROKERS

# ---------------------------------------------------------------------------
# 创建设备事件Topic
# ---------------------------------------------------------------------------
echo "创建 device-event Topic..."
kafka-topics.sh --create \
  --topic device-event \
  --partitions 20 \
  --replication-factor 3 \
  --config retention.ms=7776000000 \
  --config segment.ms=86400000 \
  --config cleanup.policy=delete \
  --config compression.type=snappy \
  --config max.message.bytes=1048576 \
  --bootstrap-server $KAFKA_BROKERS

# ---------------------------------------------------------------------------
# 创建设备状态Topic
# ---------------------------------------------------------------------------
echo "创建 device-status Topic..."
kafka-topics.sh --create \
  --topic device-status \
  --partitions 30 \
  --replication-factor 3 \
  --config retention.ms=604800000 \
  --config segment.ms=3600000 \
  --config cleanup.policy=delete \
  --config compression.type=snappy \
  --config max.message.bytes=524288 \
  --bootstrap-server $KAFKA_BROKERS

# ---------------------------------------------------------------------------
# 创建设备命令Topic
# ---------------------------------------------------------------------------
echo "创建 device-command Topic..."
kafka-topics.sh --create \
  --topic device-command \
  --partitions 30 \
  --replication-factor 3 \
  --config retention.ms=86400000 \
  --config segment.ms=3600000 \
  --config cleanup.policy=delete \
  --config compression.type=snappy \
  --config max.message.bytes=524288 \
  --bootstrap-server $KAFKA_BROKERS

# ---------------------------------------------------------------------------
# 创建设备心跳Topic
# ---------------------------------------------------------------------------
echo "创建 device-heartbeat Topic..."
kafka-topics.sh --create \
  --topic device-heartbeat \
  --partitions 20 \
  --replication-factor 3 \
  --config retention.ms=604800000 \
  --config segment.ms=3600000 \
  --config cleanup.policy=delete \
  --config compression.type=snappy \
  --config max.message.bytes=262144 \
  --bootstrap-server $KAFKA_BROKERS

# ---------------------------------------------------------------------------
# 创建设备OTA Topic
# ---------------------------------------------------------------------------
echo "创建 device-ota Topic..."
kafka-topics.sh --create \
  --topic device-ota \
  --partitions 10 \
  --replication-factor 3 \
  --config retention.ms=2592000000 \
  --config segment.ms=86400000 \
  --config cleanup.policy=delete \
  --config compression.type=snappy \
  --config max.message.bytes=10485760 \
  --bootstrap-server $KAFKA_BROKERS

# ---------------------------------------------------------------------------
# 创建设备认证Topic（用于设备认证日志）
# ---------------------------------------------------------------------------
echo "创建 device-auth Topic..."
kafka-topics.sh --create \
  --topic device-auth \
  --partitions 10 \
  --replication-factor 3 \
  --config retention.ms=2592000000 \
  --config segment.ms=86400000 \
  --config cleanup.policy=delete \
  --config compression.type=snappy \
  --config max.message.bytes=262144 \
  --bootstrap-server $KAFKA_BROKERS

# ---------------------------------------------------------------------------
# 验证Topic创建
# ---------------------------------------------------------------------------
echo ""
echo "========================================="
echo "Topic创建完成，验证结果："
echo "========================================="
kafka-topics.sh --list --bootstrap-server $KAFKA_BROKERS

echo ""
echo "========================================="
echo "Topic详细信息："
echo "========================================="
kafka-topics.sh --describe --bootstrap-server $KAFKA_BROKERS

echo ""
echo "========================================="
echo "消费者组信息："
echo "========================================="
kafka-consumer-groups.sh --bootstrap-server $KAFKA_BROKERS --list

echo ""
echo "Topic创建脚本执行完成！"
