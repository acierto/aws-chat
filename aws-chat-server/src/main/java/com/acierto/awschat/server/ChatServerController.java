package com.acierto.awschat.server;

import com.acierto.awschat.server.storage.StorageManager;
import com.acierto.awschat.server.storage.data.Message;
import com.acierto.awschat.server.storage.data.User;
import com.acierto.awschat.server.storage.repositories.MessageRepository;
import com.acierto.awschat.server.storage.repositories.UserRepository;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ChatServerController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    public ChatServerController(@Autowired AmazonDynamoDB amazonDynamoDB) {
        new StorageManager(amazonDynamoDB).createTables();
    }

    @GetMapping("/connect")
    public String connect(@RequestParam(value = "name") String name) {
        if (userRepository.findByName(name) != null) {
            throw new RuntimeException(String.format("User %s is already connected", name));
        }

        User user = new User(name);
        userRepository.save(user);
        return user.getSessionToken();
    }

    @PostMapping("/send")
    public String sendMessage(@RequestParam(value = "token") String token, @RequestBody String message) {
        User user = userRepository.findBySessionToken(token);
        if (user == null) {
            throw new RuntimeException(String.format("User with provided token %s has not found", token));
        }

        messageRepository.save(new Message(user.getName(), message));
        return "SENT";
    }

    @GetMapping("/history")
    public ResponseEntity<Iterable<Message>> history() {
        return ResponseEntity.ok(messageRepository.findAll());
    }

    @GetMapping("/disconnect")
    public String disconnect(@RequestParam(value = "token") String token) {
        User user = userRepository.findBySessionToken(token);
        if(user == null) {
            return "NOT FOUND";
        }
        userRepository.delete(user);
        return "SIGNED OFF";
    }
}
