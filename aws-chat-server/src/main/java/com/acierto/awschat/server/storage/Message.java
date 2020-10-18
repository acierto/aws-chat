package com.acierto.awschat.server.storage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {
    private final String username;
    private final String messageBody;
    private final String deliveryTime;

    public Message(String username, String messageBody) {
        this.username = username;
        this.messageBody = messageBody;
        this.deliveryTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public Message(String username, String messageBody, String deliveryTime) {
        this.username = username;
        this.messageBody = messageBody;
        this.deliveryTime = deliveryTime;
    }

    public String getUsername() {
        return username;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    @Override
    public String toString() {
        return "Message{" +
                "username='" + username + '\'' +
                ", messageBody='" + messageBody + '\'' +
                ", deliveryTime='" + deliveryTime + '\'' +
                '}';
    }
}
