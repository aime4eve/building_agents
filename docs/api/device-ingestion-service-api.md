# 设备接入服务 API 设计

## 基础信息

| 属性 | 值 |
|------|-----|
| 服务名称 | device-ingestion-service |
| 服务端口 | 8084 |
| 上下文路径 | /api/v1/device-ingestion |
| 协议 | HTTPS |

---

## 1. 设备注册 API

### 1.1 设备注册

**接口说明：** 设备首次接入时注册，获取设备ID和证书

**请求：**
```http
POST /api/v1/device-ingestion/register
Content-Type: application/json
```

**请求体：**
```json
{
  "deviceSn": "SN202401001",
  "deviceType": "TEMPERATURE_SENSOR",
  "deviceModel": {
    "manufacturer": "Huakuantong",
    "model": "HK-T100",
    "firmwareVersion": "1.0.0"
  },
  "tenantId": "tenant_001",
  "spaceId": "space_001"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "设备注册成功",
  "data": {
    "deviceId": "dev_123456",
    "deviceSn": "SN202401001",
    "deviceType": "TEMPERATURE_SENSOR",
    "deviceCertificate": {
      "clientCert": "-----BEGIN CERTIFICATE-----...",
      "clientKey": "-----BEGIN PRIVATE KEY-----...",
      "caCert": "-----BEGIN CERTIFICATE-----..."
    },
    "mqttConfig": {
      "broker": "mqtt.hkt.com",
      "port": 8883,
      "protocol": "mqtts",
      "topics": {
        "telemetry": "device/tenant_001/temperature_sensor/dev_123456/telemetry",
        "event": "device/tenant_001/temperature_sensor/dev_123456/event",
        "status": "device/tenant_001/temperature_sensor/dev_123456/status",
        "heartbeat": "device/tenant_001/temperature_sensor/dev_123456/heartbeat",
        "command": "device/tenant_001/temperature_sensor/dev_123456/command"
      }
    },
    "jwtToken": {
      "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
      "refreshToken": "refresh_token_xxx",
      "expiresAt": 1735689600000
    },
    "createdAt": 1708416000000
  }
}
```

### 1.2 证书续期

**接口说明：** 设备证书即将过期时续期

**请求：**
```http
POST /api/v1/device-ingestion/renew-cert
Content-Type: application/json
Authorization: Bearer {jwt_token}
```

**请求体：**
```json
{
  "deviceId": "dev_123456",
  "oldCertSn": "old_cert_sn"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "证书续期成功",
  "data": {
    "deviceCertificate": {
      "clientCert": "-----BEGIN CERTIFICATE-----...",
      "clientKey": "-----BEGIN PRIVATE KEY-----...",
      "caCert": "-----BEGIN CERTIFICATE-----..."
    },
    "expiresAt": 1767225600000
  }
}
```

### 1.3 Token刷新

**接口说明：** JWT Token即将过期时刷新

**请求：**
```http
POST /api/v1/device-ingestion/refresh-token
Content-Type: application/json
```

**请求体：**
```json
{
  "refreshToken": "refresh_token_xxx"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "Token刷新成功",
  "data": {
    "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "new_refresh_token_xxx",
    "expiresAt": 1735689600000
  }
}
```

---

## 2. 设备状态 API

### 2.1 设备上线通知

**接口说明：** EMQX钩子调用，通知设备上线

**请求：**
```http
POST /api/v1/device-ingestion/status/online
Content-Type: application/json
X-API-Key: {api_key}
```

**请求体：**
```json
{
  "deviceId": "dev_123456",
  "deviceType": "TEMPERATURE_SENSOR",
  "tenantId": "tenant_001",
  "clientIp": "192.168.1.100",
  "connectedAt": 1708416000000,
  "connectionInfo": {
    "protocol": "MQTT",
    "protocolVersion": "3.1.1",
    "clientVersion": "1.0.0"
  }
}
```

**响应：**
```json
{
  "code": 200,
  "message": "设备上线已记录"
}
```

### 2.2 设备离线通知

**接口说明：** EMQX钩子调用，通知设备离线

**请求：**
```http
POST /api/v1/device-ingestion/status/offline
Content-Type: application/json
X-API-Key: {api_key}
```

**请求体：**
```json
{
  "deviceId": "dev_123456",
  "tenantId": "tenant_001",
  "disconnectedAt": 1708416300000,
  "reason": "timeout",
  "lastCommunicatedAt": 1708416100000
}
```

**响应：**
```json
{
  "code": 200,
  "message": "设备离线已记录"
}
```

### 2.3 批量设备状态查询

**接口说明：** 查询多个设备的在线状态

**请求：**
```http
GET /api/v1/device-ingestion/status/batch?deviceIds=dev_001,dev_002,dev_003
Authorization: Bearer {jwt_token}
```

**响应：**
```json
{
  "code": 200,
  "data": [
    {
      "deviceId": "dev_001",
      "status": "ONLINE",
      "lastCommunicatedAt": 1708416100000
    },
    {
      "deviceId": "dev_002",
      "status": "OFFLINE",
      "lastCommunicatedAt": 1708410000000
    },
    {
      "deviceId": "dev_003",
      "status": "FAULT",
      "lastCommunicatedAt": 1708415000000,
      "faultReason": "传感器异常"
    }
  ]
}
```

---

## 3. 设备配置 API

### 3.1 获取设备配置

**接口说明：** 设备获取最新的配置信息

**请求：**
```http
GET /api/v1/device-ingestion/config/{deviceId}
Authorization: Bearer {jwt_token}
```

**响应：**
```json
{
  "code": 200,
  "data": {
    "deviceId": "dev_123456",
    "reportInterval": 60,
    "heartbeatInterval": 60,
    "telemetryConfig": {
      "enabled": true,
      "properties": ["temperature", "humidity", "battery"]
    },
    "eventConfig": {
      "enabled": true,
      "events": ["alarm", "fault"]
    },
    "otaConfig": {
      "autoUpgrade": false,
      "checkInterval": 86400
    },
    "updatedAt": 1708416000000
  }
}
```

---

## 4. 错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 权限不足 |
| 404 | 设备不存在 |
| 409 | 设备已注册 |
| 429 | 请求过于频繁 |
| 500 | 服务器内部错误 |
| 503 | 服务不可用 |

---

## 5. 数据模型

### 5.1 DeviceRegisterRequest

```java
public class DeviceRegisterRequest {
    private String deviceSn;
    private DeviceType deviceType;
    private DeviceModelRequest deviceModel;
    private String tenantId;
    private String spaceId;
}
```

### 5.2 DeviceRegisterResponse

```java
public class DeviceRegisterResponse {
    private String deviceId;
    private String deviceSn;
    private DeviceType deviceType;
    private DeviceCertificateResponse deviceCertificate;
    private MqttConfigResponse mqttConfig;
    private JwtTokenResponse jwtToken;
    private Long createdAt;
}
```

### 5.3 DeviceOnlineRequest

```java
public class DeviceOnlineRequest {
    private String deviceId;
    private DeviceType deviceType;
    private String tenantId;
    private String clientIp;
    private Long connectedAt;
    private ConnectionInfo connectionInfo;
}
```

### 5.4 DeviceOfflineRequest

```java
public class DeviceOfflineRequest {
    private String deviceId;
    private String tenantId;
    private Long disconnectedAt;
    private String reason;
    private Long lastCommunicatedAt;
}
```
