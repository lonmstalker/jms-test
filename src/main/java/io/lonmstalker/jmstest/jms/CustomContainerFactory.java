package io.lonmstalker.jmstest.jms;

import io.lonmstalker.jmstest.listener.DefaultErrorHandler;
import io.lonmstalker.jmstest.listener.DefaultListener;
import io.lonmstalker.jmstest.model.ConnectionConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.jms.support.QosSettings;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;

import javax.jms.ConnectionFactory;
import java.util.HashMap;
import java.util.Map;

import static javax.jms.DeliveryMode.NON_PERSISTENT;
import static javax.jms.Session.CLIENT_ACKNOWLEDGE;

// uses instead of JmsListenerEndpointRegistry and DefaultMessageListenerContainer
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomContainerFactory {
    private static final int DEFAULT_RECEIVE_TIMEOUT = 10_000;
    private final TaskExecutor taskExecutor;
    private final MessageConverter messageConverter;
    private static final QosSettings qosSettings;
    private static final Map<String, MessageListenerContainer> containerMap;

    static {
        qosSettings = new QosSettings();
        containerMap = new HashMap<>();
        qosSettings.setDeliveryMode(NON_PERSISTENT);
    }

    @NonNull
    public boolean destroyListener(@NonNull final String queueName) {
        if (containerMap.containsKey(queueName)) {
            containerMap.get(queueName).stop();
            containerMap.remove(queueName);
            log.info("container '{}' stopped ", queueName);
            return true;
        }
        return false;
    }

    @NonNull
    public boolean startListener(@NonNull final String queueName) {
        if (containerMap.containsKey(queueName)){
            var container = containerMap.get(queueName);
            if (container.isRunning())
                return false;
            container.start();
            return true;
        }
        return false;
    }

    @NonNull
    public DefaultMessageListenerContainer createListenerContainer(
            @NonNull final ConnectionConfig connectionConfig, @NonNull final ConnectionFactory connectionFactory
    ) {
        var container = createContainerInstance();

        container.setTaskExecutor(this.taskExecutor);
        container.setCacheLevel(4);
        container.setReceiveTimeout(DEFAULT_RECEIVE_TIMEOUT);
        container.setConcurrency("2");
        container.setAutoStartup(false);
        container.setErrorHandler(this.createErrorHandler(connectionConfig.getQueueName()));
        container.setMessageConverter(this.messageConverter);
        container.setMessageListener(new DefaultListener());
        container.setSessionAcknowledgeMode(CLIENT_ACKNOWLEDGE);
        container.setConnectionFactory(connectionFactory);
        container.setDestinationName(connectionConfig.getQueueName());

        container.afterPropertiesSet();
        containerMap.put(connectionConfig.getQueueName(), container);
        return container;
    }

    @NonNull
    private ErrorHandler createErrorHandler(@NonNull final String queueName){
        return DefaultErrorHandler.builder()
                .queueName(queueName)
                .containerFactory(this)
                .build();
    }

    private static DefaultMessageListenerContainer createContainerInstance() {
        return new DefaultMessageListenerContainer();
    }

}
