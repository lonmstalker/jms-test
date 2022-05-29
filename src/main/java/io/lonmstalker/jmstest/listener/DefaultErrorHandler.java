package io.lonmstalker.jmstest.listener;

import io.lonmstalker.jmstest.jms.CustomContainerFactory;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ErrorHandler;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Builder(toBuilder = true)
public class DefaultErrorHandler implements ErrorHandler {
    private static final int MAX_ERRORS = 3;
    private final String queueName;
    private final CustomContainerFactory containerFactory;
    private final AtomicInteger errorCount = new AtomicInteger(0);

    @Override
    public void handleError(Throwable e) {
        log.error("container of '{}' queue caught error: {}", this.queueName, e.getMessage());
        errorCount.incrementAndGet();
        if (errorCount.get() >= MAX_ERRORS) {
            this.containerFactory.destroyListener(this.queueName);
            log.info("stopped container of '{}' queue", this.queueName);
        }
    }

}
