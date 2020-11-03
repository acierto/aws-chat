package com.acierto.awschat.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

@Component
public class ScheduledMessageSender {
    private static final Logger log = LoggerFactory.getLogger(ScheduledMessageSender.class);
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    private static final int NUM_OF_NAMES = 1006;
    private static final int NUM_OF_MESSAGES = 531;

    @Value("${aws.server.url}")
    private String serverUrl;
    private final List<String> messages;
    private final List<String> names;
    private final RestTemplate restTemplate;
    private final String userName;
    private boolean isConnected;
    private String token;

    public ScheduledMessageSender() throws IOException {
        messages = readResource("messages.txt");
        names = readResource("names.txt");
        restTemplate = new RestTemplate();
        userName = getRandomUserName();
    }

    @Scheduled(fixedRateString = "${aws.chat.message.scheduled-interval}")
    public void sendMessages() {
        if (!isConnected) {
            repeatableConnect(userName, MAX_RECONNECT_ATTEMPTS);
        }
        sendMessage(token, getRandomMessage());
    }

    @Scheduled(initialDelayString = "${aws.chat.message.stop-after}", fixedDelay = Long.MAX_VALUE)
    public void stopMessaging() {
        scheduledTasks.forEach((k, v) -> {
            if (k instanceof ScheduledMessageSender) {
                disconnect(token);
                v.cancel(false);
                System.exit(0);
            }
        });
    }

    @Bean
    public TaskScheduler poolScheduler() {
        return new CustomTaskScheduler();
    }

    private String getRandomUserName() {
        return pickRandomly(names, 0, NUM_OF_NAMES);
    }

    private String getRandomMessage() {
        return pickRandomly(messages, 0, NUM_OF_MESSAGES);
    }

    private String pickRandomly(List<String> list, int min, int max) {
        int nameOrder = new Random().ints(min, max).findFirst().getAsInt();
        return list.get(nameOrder);
    }

    private List<String> readResource(String fileName) throws IOException {
        byte[] content = Files.readAllBytes(ResourceUtils.getFile(String.format("classpath:%s", fileName)).toPath());
        return Arrays.asList(new String(content).split("\\r?\\n"));
    }

    private void repeatableConnect(String name, int maxAttempts) {
        int attempt = 0;
        ResponseEntity<String> connectResponse;
        while (attempt < maxAttempts) {
            connectResponse = connect(name);
            if (connectResponse.getStatusCode() != HttpStatus.OK) {
                attempt++;
            } else {
                isConnected = true;
                token = connectResponse.getBody();
                return;
            }
        }
        throw new RuntimeException("Couldn't connect");
    }

    private ResponseEntity<String> connect(String name) {
        String url = String.format("%s/connect?name=%s", serverUrl, name);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        log.info("[{}] Received response {}", response.getStatusCode(), response.getBody());
        return response;
    }

    private void sendMessage(String token, String message) {
        String url = String.format("%s/send?token=%s", serverUrl, token);
        ResponseEntity<String> response = restTemplate.postForEntity(url, message, String.class);
        log.info("[{}] Received response {}", response.getStatusCode(), response.getBody());
    }

    private void disconnect(String token) {
        String url = String.format("%s/disconnect?token=%s", serverUrl, token);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        log.info("[{}] Received response {}", response.getStatusCode(), response.getBody());
    }

    private final Map<Object, ScheduledFuture<?>> scheduledTasks = new IdentityHashMap<>();

    private class CustomTaskScheduler extends ThreadPoolTaskScheduler {
        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long period) {
            ScheduledFuture<?> future = super.scheduleAtFixedRate(task, period);

            ScheduledMethodRunnable runnable = (ScheduledMethodRunnable) task;
            scheduledTasks.put(runnable.getTarget(), future);

            return future;
        }
    }
}
