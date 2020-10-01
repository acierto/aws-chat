package com.acierto.awschat.server;

import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RestController
public class ChatServerController {

    private final ConcurrentMap<String, String> userToSession = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> sessionToUser = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, List<String>> messages = new ConcurrentHashMap<>();

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

        messages.computeIfAbsent(user, k -> new LinkedList<>()).add(message);
        return "SENT";
    }

    @GetMapping("/show")
    public String showAllMessage() {
        StringBuilder sb = new StringBuilder();
        messages.forEach((user, messages) -> {
            sb.append(String.format("%s=", user)).append(messages).append(";");
        });
        return sb.toString();
    }

    @GetMapping("/disconnect")
    public boolean disconnect(@RequestParam(value = "name") String name) {
        return userToSession.remove(name) != null;
    }
}
