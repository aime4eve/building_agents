package com.hkt.iot.notification.infrastructure.channel;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 邮件消息渠道
 *
 * @author HKT IoT Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailMessageChannel implements MessageChannel {

    private final JavaMailSender mailSender;

    @Value("${notification.email.from:noreply@hkt-iot.com}")
    private String fromEmail;

    @Value("${notification.email.enabled:true}")
    private boolean enabled;

    @Override
    public String send(Map<String, Object> payload) throws Exception {
        String to = (String) payload.get("receiverAddress");
        String title = (String) payload.get("title");
        String content = (String) payload.get("content");

        log.info("发送邮件: to={}, title={}", to, title);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(title);
        helper.setText(content, true);

        mailSender.send(message);

        log.info("邮件发送成功: {}", to);
        return "SUCCESS";
    }

    @Override
    public String getChannelType() {
        return "EMAIL";
    }

    @Override
    public boolean isAvailable() {
        return enabled;
    }
}
