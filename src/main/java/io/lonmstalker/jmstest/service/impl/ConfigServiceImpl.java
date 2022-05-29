package io.lonmstalker.jmstest.service.impl;

import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import io.lonmstalker.jmstest.jms.CustomContainerFactory;
import io.lonmstalker.jmstest.model.ConnectionConfig;
import io.lonmstalker.jmstest.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import javax.jms.ConnectionFactory;
import java.util.HashMap;
import java.util.Map;

import static javax.jms.DeliveryMode.NON_PERSISTENT;

@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {
    private final MessageConverter messageConverter;
    private final CustomContainerFactory containerFactory;
    private static final Map<String, ConnectionFactory> factoryMap = new HashMap<>();
    private static final Map<String, JmsTemplate> templateMap = new HashMap<>();

    @NonNull
    @Override
    public ConnectionConfig createContainer(@NonNull final ConnectionConfig connectionConfig) {
        var listenerContainer =
                this.containerFactory.createListenerContainer(connectionConfig, getConnectionFactory(connectionConfig));
        if (Boolean.TRUE.equals(connectionConfig.getAutoStartup())) {
            listenerContainer.start();
        }
        return connectionConfig;
    }

    @Override
    public boolean stopListen(String queueName) {
        return this.containerFactory.destroyListener(queueName);
    }

    @Override
    public boolean startListen(String queueName) {
        return this.containerFactory.startListener(queueName);
    }

    @NonNull
    @Override
    public JmsTemplate getJmsTemplate(@NonNull final ConnectionConfig connectionConfig) {
        return templateMap.containsKey(connectionConfig.getQueueName())
                ? templateMap.get(connectionConfig.getQueueName())
                : createJmsTemplate(connectionConfig, this.messageConverter);
    }

    @NonNull
    private static JmsTemplate createJmsTemplate(@NonNull final ConnectionConfig connectionConfig, @NonNull final MessageConverter messageConverter) {
        var jmsTemplate = new JmsTemplate();
        var conFactory = factoryMap.getOrDefault(connectionConfig.getQueueName(), getConnectionFactory(connectionConfig));

        jmsTemplate.setConnectionFactory(conFactory);
        jmsTemplate.setMessageConverter(messageConverter);
        jmsTemplate.setDefaultDestinationName(connectionConfig.getQueueName());
        jmsTemplate.setDeliveryMode(NON_PERSISTENT);
        jmsTemplate.setDeliveryPersistent(false);
        jmsTemplate.setExplicitQosEnabled(true);
        templateMap.put(connectionConfig.getQueueName(), jmsTemplate);

        return jmsTemplate;
    }

    @NonNull
    private static ConnectionFactory getConnectionFactory(@NonNull final ConnectionConfig connectionConfig) {
        return factoryMap.containsKey(connectionConfig.getQueueName())
                ? factoryMap.get(connectionConfig.getQueueName())
                : createCachingFactory(connectionConfig);
    }

    @NonNull
    private static ConnectionFactory createCachingFactory(@NonNull final ConnectionConfig connectionConfig) {
        final var factory = new CachingConnectionFactory();

        factory.setTargetConnectionFactory(createMqFactory(connectionConfig));
        factoryMap.put(connectionConfig.getQueueName(), factory);

        return factory;
    }

    @NonNull
    @SneakyThrows
    private static ConnectionFactory createMqFactory(@NonNull final ConnectionConfig connectionConfig) {
        final var mqFactory = new MQConnectionFactory();

        mqFactory.setQueueManager(connectionConfig.getQueueManager());
        mqFactory.setChannel(connectionConfig.getChannel());
        mqFactory.setConnectionNameList(connectionConfig.getConnName());
        mqFactory.setStringProperty(WMQConstants.USERID, connectionConfig.getUserName());
        mqFactory.setStringProperty(WMQConstants.PASSWORD, connectionConfig.getPassword());
        mqFactory.setStringProperty("XMSC_WMQ_CONNECTION_MODE", "1");

        return mqFactory;
    }

}
