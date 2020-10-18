package com.acierto.awschat.server;

import com.acierto.awschat.server.storage.data.Message;
import com.acierto.awschat.server.storage.repositories.MessageRepository;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RestController
public class ChatServerController {

    @Autowired
    private MessageRepository messageRepository;

    private final ConcurrentMap<String, String> userToSession = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> sessionToUser = new ConcurrentHashMap<>();

    public ChatServerController(@Autowired AmazonDynamoDB amazonDynamoDB) {
        DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB);
        CreateTableRequest tableRequest = dynamoDBMapper.generateCreateTableRequest(Message.class);
        tableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
        amazonDynamoDB.createTable(tableRequest);
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

        messageRepository.save(new Message(user, message));
        return "SENT";
    }

    @GetMapping("/show")
    public ResponseEntity<Iterable<Message>> showAllMessage() {
        return ResponseEntity.ok(messageRepository.findAll());
    }

    @GetMapping("/disconnect")
    public boolean disconnect(@RequestParam(value = "name") String name) {
        return userToSession.remove(name) != null;
    }
}
