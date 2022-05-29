package io.lonmstalker.jmstest.service.impl;

import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import io.lonmstalker.jmstest.listener.DefaultListener;
import io.lonmstalker.jmstest.model.ConnectionConfig;
import io.lonmstalker.jmstest.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpoint;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.QosSettings;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import javax.jms.ConnectionFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static javax.jms.DeliveryMode.NON_PERSISTENT;
import static javax.jms.Session.CLIENT_ACKNOWLEDGE;

@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {
    private final JmsListenerEndpointRegistry registry;
    private final TaskExecutor taskExecutor;
    private static final Map<String, ConnectionFactory> factoryMap = new HashMap<>();
    private static final Map<String, JmsTemplate> templateMap = new HashMap<>();

    @NonNull
    @Override
    public ConnectionConfig listenQueue(@NonNull final ConnectionConfig connectionConfig) {
        final var containerFactory = getContainerFactory(getConnectionFactory(connectionConfig), this.taskExecutor);
        this.registry.registerListenerContainer(getJmsListenerEndpoint(connectionConfig.getQueueName()), containerFactory, true);
        return connectionConfig;
    }

    @NonNull
    @Override
    public JmsTemplate getJmsTemplate(@NonNull final ConnectionConfig connectionConfig) {
        return templateMap.containsKey(connectionConfig.getQueueName())
                ? templateMap.get(connectionConfig.getQueueName())
                : createJmsTemplate(connectionConfig);
    }

    @NonNull
    private static JmsTemplate createJmsTemplate(@NonNull final ConnectionConfig connectionConfig){
        var jmsTemplate = new JmsTemplate();
        var conFactory = factoryMap.getOrDefault(connectionConfig.getQueueName(), getConnectionFactory(connectionConfig));

        jmsTemplate.setConnectionFactory(conFactory);
        jmsTemplate.setMessageConverter(new SimpleMessageConverter());
        jmsTemplate.setDefaultDestinationName(connectionConfig.getQueueName());
        jmsTemplate.setDeliveryMode(NON_PERSISTENT);
        jmsTemplate.setDeliveryPersistent(false);
        jmsTemplate.setExplicitQosEnabled(true);
        templateMap.put(connectionConfig.getQueueName(), jmsTemplate);

        return jmsTemplate;
    }

    @NonNull
    private static DefaultJmsListenerContainerFactory getContainerFactory(
            @NonNull final ConnectionFactory connectionFactory, @NonNull final TaskExecutor taskExecutor
    ) {
        final var factory = new DefaultJmsListenerContainerFactory();
        final var qosSettings = new QosSettings();

        qosSettings.setDeliveryMode(NON_PERSISTENT);
        factory.setTaskExecutor(taskExecutor);
        factory.setConnectionFactory(connectionFactory);
        factory.setSessionAcknowledgeMode(CLIENT_ACKNOWLEDGE);
        factory.setReplyQosSettings(qosSettings);
        factory.setAutoStartup(true);
        factory.setConcurrency("2");

        return factory;
    }

    @NonNull
    private static JmsListenerEndpoint getJmsListenerEndpoint(@NonNull final String queueName) {
        final var endpoint = new SimpleJmsListenerEndpoint();
        endpoint.setId("myJmsEndpoint-" + UUID.randomUUID());
        endpoint.setDestination(queueName);
        endpoint.setMessageListener(new DefaultListener());
        return endpoint;
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
