package io.lonmstalker.jmstest.service;

import io.lonmstalker.jmstest.model.ConnectionConfig;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.lang.NonNull;

public interface ConfigService {
    ConnectionConfig createContainer(@NonNull final ConnectionConfig connectionConfig);
    boolean stopListen(@NonNull final String queueName);
    boolean startListen(@NonNull final String queueName);
    JmsTemplate getJmsTemplate(@NonNull final ConnectionConfig connectionConfig);
}
