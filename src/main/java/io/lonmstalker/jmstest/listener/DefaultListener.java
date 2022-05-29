package io.lonmstalker.jmstest.listener;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@Slf4j
public class DefaultListener implements MessageListener {

    @Override
    @SneakyThrows
    public void onMessage(Message message) {
        log.info("get message: {}", ((TextMessage) message).getText());
    }
}
