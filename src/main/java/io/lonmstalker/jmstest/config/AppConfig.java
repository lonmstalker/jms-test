package io.lonmstalker.jmstest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@Configuration(proxyBeanMethods = false)
public class AppConfig {

    @Bean
    public MessageConverter messageConverter(){
        return new SimpleMessageConverter();
    }

}
