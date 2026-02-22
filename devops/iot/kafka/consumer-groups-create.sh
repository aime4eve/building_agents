#!/bin/bash
# =============================================================================
# Kafka 消费者组创建脚本 - 设备接入层
# =============================================================================
# 说明: 创建设备数据处理所需消费者组
# =============================================================================

KAFKA_BROKERS="kafka1:9092,kafka2:9093,kafka3:9094"

echo "创建Kafka消费者组..."

# ---------------------------------------------------------------------------
# 遥测数据消费者组
# ---------------------------------------------------------------------------
echo "创建 telemetry-processor-group 消费者组..."
# Kafka消费者组会在首次消费时自动创建
# 这里使用kafka-console-consumer触发消费者组创建
timeout 5 kafka-console-consumer.sh \
  --bootstrap-server $KAFKA_BROKERS \
  --topic device-telemetry \
  --group telemetry-processor-group \
  --from-beginning \
  --max-messages 0 2>/dev/null || true

# ---------------------------------------------------------------------------
# 设备事件消费者组
# ---------------------------------------------------------------------------
echo "创建 event-processor-group 消费者组..."
timeout 5 kafka-console-consumer.sh \
  --bootstrap-server $KAFKA_BROKERS \
  --topic device-event \
  --group event-processor-group \
  --from-beginning \
  --max-messages 0 2>/dev/null || true

# ---------------------------------------------------------------------------
# 设备状态消费者组
# ---------------------------------------------------------------------------
echo "创建 status-processor-group 消费者组..."
timeout 5 kafka-console-consumer.sh \
  --bootstrap-server $KAFKA_BROKERS \
  --topic device-status \
  --group status-processor-group \
  --from-beginning \
  --max-messages 0 2>/dev/null || true

# ---------------------------------------------------------------------------
# 设备命令消费者组（EMQX桥接）
# ---------------------------------------------------------------------------
echo "创建 command-bridge-group 消费者组..."
timeout 5 kafka-console-consumer.sh \
  --bootstrap-server $KAFKA_BROKERS \
  --topic device-command \
  --group command-bridge-group \
  --from-beginning \
  --max-messages 0 2>/dev/null || true

# ---------------------------------------------------------------------------
# 设备心跳消费者组
# ---------------------------------------------------------------------------
echo "创建 heartbeat-processor-group 消费者组..."
timeout 5 kafka-console-consumer.sh \
  --bootstrap-server $KAFKA_BROKERS \
  --topic device-heartbeat \
  --group heartbeat-processor-group \
  --from-beginning \
  --max-messages 0 2>/dev/null || true

# ---------------------------------------------------------------------------
# OTA升级消费者组
# ---------------------------------------------------------------------------
echo "创建 ota-processor-group 消费者组..."
timeout 5 kafka-console-consumer.sh \
  --bootstrap-server $KAFKA_BROKERS \
  --topic device-ota \
  --group ota-processor-group \
  --from-beginning \
  --max-messages 0 2>/dev/null || true

# ---------------------------------------------------------------------------
# 验证消费者组
# ---------------------------------------------------------------------------
echo ""
echo "========================================="
echo "消费者组创建完成，验证结果："
echo "========================================="
kafka-consumer-groups.sh --bootstrap-server $KAFKA_BROKERS --list

echo ""
echo "消费者组详细信息："
kafka-consumer-groups.sh --bootstrap-server $KAFKA_BROKERS --describe --all-groups

echo ""
echo "消费者组创建脚本执行完成！"
