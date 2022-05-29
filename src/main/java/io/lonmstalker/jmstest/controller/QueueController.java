package io.lonmstalker.jmstest.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lonmstalker.jmstest.model.ConnectionConfig;
import io.lonmstalker.jmstest.service.ConfigService;
import io.lonmstalker.jmstest.utils.ExtensionUtils;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.WebAsyncTask;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/queue")
@ExtensionMethod(value = ExtensionUtils.class)
public class QueueController {
    private final ObjectMapper objectMapper;
    private final ConfigService configService;
    public static final Map<String, ConnectionConfig> configMap = new HashMap<>();

    @GetMapping
    public WebAsyncTask<Boolean> changeStatus(
            @RequestParam String queueName, @RequestParam Boolean status
    ) {
        return new WebAsyncTask<>(() ->
                status ? this.configService.startListen(queueName) : this.configService.stopListen(queueName)
        );
    }

    @PostMapping("/write")
    public WebAsyncTask<Boolean> writeInQueue(
            @RequestParam String queueName, @RequestBody JsonNode body
    ) {
        return new WebAsyncTask<>(
                () -> {
                    var template = this.configService.getJmsTemplate(configMap.get(queueName));
                    template.send(queueName, creator -> creator.createTextMessage(this.objectMapper.writeValueAsStringWithCatch(body)));
                    return true;
                }
        );
    }

    @PostMapping("/create")
    public WebAsyncTask<ConnectionConfig> readQueue(@RequestParam String queueName) {
        return new WebAsyncTask<>(
                () -> this.configService.createContainer(configMap.get(queueName))
        );
    }

    @PostMapping
    public WebAsyncTask<ConnectionConfig> saveConfig(@RequestBody ConnectionConfig connectionConfig) {
        configMap.put(connectionConfig.getQueueName(), connectionConfig);
        return new WebAsyncTask<>(() -> connectionConfig);
    }

}
