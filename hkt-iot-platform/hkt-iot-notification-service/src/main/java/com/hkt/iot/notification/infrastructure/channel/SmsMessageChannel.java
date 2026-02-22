package com.hkt.iot.notification.infrastructure.channel;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 短信消息渠道（阿里云SMS）
 *
 * @author HKT IoT Team
 */
@Slf4j
@Component
public class SmsMessageChannel implements MessageChannel {

    @Value("${notification.sms.access-key-id:}")
    private String accessKeyId;

    @Value("${notification.sms.access-key-secret:}")
    private String accessKeySecret;

    @Value("${notification.sms.sign-name:}")
    private String signName;

    @Value("${notification.sms.enabled:true}")
    private boolean enabled;

    private Client client;

    @Override
    public String send(Map<String, Object> payload) throws Exception {
        String phoneNumber = (String) payload.get("receiverAddress");
        String content = (String) payload.get("content");

        log.info("发送短信: to={}", phoneNumber);

        if (client == null) {
            initClient();
        }

        SendSmsRequest request = new SendSmsRequest()
                .setPhoneNumbers(phoneNumber)
                .setSignName(signName)
                .setTemplateCode(content)  // 实际使用时应该使用模板编码
                .setTemplateParam("{}");   // 模板参数

        SendSmsResponse response = client.sendSms(request);

        if ("OK".equals(response.getBody().getCode())) {
            log.info("短信发送成功: {}", phoneNumber);
            return response.getBody().getBizId();
        } else {
            log.error("短信发送失败: {}", response.getBody().getMessage());
            throw new RuntimeException("短信发送失败: " + response.getBody().getMessage());
        }
    }

    @Override
    public String getChannelType() {
        return "SMS";
    }

    @Override
    public boolean isAvailable() {
        return enabled && accessKeyId != null && !accessKeyId.isEmpty();
    }

    /**
     * 初始化阿里云客户端
     */
    private void initClient() throws Exception {
        Config config = new Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret)
                .setEndpoint("dysmsapi.aliyuncs.com");

        client = new Client(config);
    }
}
