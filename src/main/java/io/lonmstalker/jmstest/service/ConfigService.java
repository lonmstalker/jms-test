package io.lonmstalker.jmstest.service;

import io.lonmstalker.jmstest.model.ConnectionConfig;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.lang.NonNull;

public interface ConfigService {
    ConnectionConfig listenQueue(@NonNull final ConnectionConfig connectionConfig);
    JmsTemplate getJmsTemplate(@NonNull final ConnectionConfig connectionConfig);
}
