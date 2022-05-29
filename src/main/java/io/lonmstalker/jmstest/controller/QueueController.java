package io.lonmstalker.jmstest.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lonmstalker.jmstest.model.ConnectionConfig;
import io.lonmstalker.jmstest.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.WebAsyncTask;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@EnableAsync
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/queue")
public class QueueController {
    private final ObjectMapper objectMapper;
    private final ConfigService configService;
    public static final Map<String, ConnectionConfig> configMap = new HashMap<>();

    @PostMapping("/write")
    public WebAsyncTask<Boolean> writeInQueue(
            @RequestParam String queueName,
            @RequestBody JsonNode body
    ) {
        return new WebAsyncTask<>(
                () -> {
                    var template = this.configService.getJmsTemplate(configMap.get(queueName));
                    template.send(queueName, creator -> creator.createTextMessage(this.writeNode(body)));
                    return true;
                }
        );
    }

    @PostMapping("/listen")
    public WebAsyncTask<ConnectionConfig> readQueue(@RequestParam String queueName){
        return new WebAsyncTask<>(
                () -> this.configService.listenQueue(configMap.get(queueName))
        );
    }

    @PostMapping
    public WebAsyncTask<ConnectionConfig> saveConfig(@RequestBody ConnectionConfig connectionConfig){
        configMap.put(connectionConfig.getQueueName(), connectionConfig);
        return new WebAsyncTask<>(() -> connectionConfig);
    }

    @NonNull
    private String writeNode(@NonNull final JsonNode body){
        try {
           return this.objectMapper.writeValueAsString(body);
        } catch (Exception ex){
            log.error(ex.getMessage());
            return "";
        }
    }
}
