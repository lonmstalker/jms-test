package io.lonmstalker.jmstest.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class ConnectionConfig {
    private String queueManager;
    private String channel;
    private String connName;
    private String userName;
    private String password;
    private String queueName;
}
