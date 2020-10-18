package com.acierto.awschat.server;

import com.acierto.awschat.server.storage.Message;
import com.acierto.awschat.server.storage.MessageStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RestController
public class ChatServerController {

    private final MessageStorage messageStorage;

    private final ConcurrentMap<String, String> userToSession = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> sessionToUser = new ConcurrentHashMap<>();

    public ChatServerController(
            @Value("${aws.server.dynamodb.endpoint}") String dynamoDbEndpoint,
            @Value("${aws.server.signingRegion}") String signingRegion) {
        messageStorage = new MessageStorage(dynamoDbEndpoint, signingRegion);
        messageStorage.createTable();
    }

    @GetMapping("/connect")
    public String connect(@RequestParam(value = "name") String name) {
        String token = UUID.randomUUID().toString();
        userToSession.putIfAbsent(name, token);
        if (!token.equals(userToSession.get(name))) {
            throw new RuntimeException(String.format("User %s is already connected", name));
        }
        sessionToUser.putIfAbsent(token, name);
        return token;
    }

    @PostMapping("/send")
    public String sendMessage(@RequestParam(value = "token") String token, @RequestBody String message) {
        String user = sessionToUser.get(token);
        if (user == null) {
            throw new RuntimeException(String.format("User with provided token %s has not found", token));
        }

        messageStorage.addMessage(new Message(user, message));
        return "SENT";
    }

    @GetMapping("/show")
    public ResponseEntity<List<Message>> showAllMessage() {
        return ResponseEntity.ok(messageStorage.readMessages());
    }

    @GetMapping("/disconnect")
    public boolean disconnect(@RequestParam(value = "name") String name) {
        return userToSession.remove(name) != null;
    }
}
