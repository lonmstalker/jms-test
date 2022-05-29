package io.lonmstalker.jmstest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ConnectionConfig {
    private String queueManager;
    private String channel;
    private String connName;
    private String userName;
    private String password;
    private String queueName;
    private Boolean autoStartup;
}
