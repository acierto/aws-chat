package com.acierto.awschat.server.storage;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class MessageStorage {

    private static final Logger log = LoggerFactory.getLogger(MessageStorage.class);

    private final static Long READ_CAPACITY_UNITS = 10L;
    private final static Long WRITE_CAPACITY_UNITS = 10L;
    private final static String TABLE_NAME = "messages";

    private final static String PRIMARY_KEY = "id";
    private final static String USERNAME_FIELD = "username";
    private final static String DELIVERY_TIME_FIELD = "deliveryTime";
    private final static String MESSAGE_FIELD = "messageBody";

    private final DynamoDB dynamoDB;

    public MessageStorage(String serviceEndpoint, String signingRegion) {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(serviceEndpoint, signingRegion))
                .build();

        dynamoDB = new DynamoDB(client);
    }

    public void createTable() {
        try {
            log.info("Attempting to create table; please wait...");
            if (!isTableExist()) {
                Table table = dynamoDB.createTable(TABLE_NAME,
                        Arrays.asList(
                                new KeySchemaElement(USERNAME_FIELD, KeyType.HASH),
                                new KeySchemaElement(DELIVERY_TIME_FIELD, KeyType.RANGE)
                        ),
                        Arrays.asList(
                                new AttributeDefinition(USERNAME_FIELD, ScalarAttributeType.S),
                                new AttributeDefinition(DELIVERY_TIME_FIELD, ScalarAttributeType.S)
                        ),
                        new ProvisionedThroughput(READ_CAPACITY_UNITS, WRITE_CAPACITY_UNITS));
                table.waitForActive();
                log.info("Success.  Table status: " + table.getDescription().getTableStatus());
            }
        } catch (Exception e) {
            log.error("Unable to create table: {} {}", e.getMessage(), e.getCause());
        }
    }

    private boolean isTableExist() {
        try {
            dynamoDB.getTable(TABLE_NAME).describe();
            return true;
        } catch (com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException ignored) {
        }
        return false;
    }

    public void addMessage(Message message) {
        Table table = dynamoDB.getTable(TABLE_NAME);
        PrimaryKey primaryKey = new PrimaryKey();
        primaryKey.addComponent(PRIMARY_KEY, UUID.randomUUID().toString());
        primaryKey.addComponent(DELIVERY_TIME_FIELD, message.getDeliveryTime());

        Item item = new Item()
                .withPrimaryKey(primaryKey)
                .withString(USERNAME_FIELD, message.getUsername())
                .withString(DELIVERY_TIME_FIELD, message.getDeliveryTime())
                .withString(MESSAGE_FIELD, message.getMessageBody());

        try {
            PutItemSpec putItemSpec = new PutItemSpec()
                    .withItem(item)
                    .withConditionExpression("attribute_not_exists(deliveryTime)");
            PutItemOutcome outcome = table.putItem(putItemSpec);

            log.info("PutItem succeeded:\n" + outcome.getPutItemResult());
        } catch (Exception e) {
            log.error("Unable to add item: {} {}", message, e.getMessage());
        }
    }

    public List<Message> readMessages() {
        List<Message> messages = new LinkedList<>();
        Table table = dynamoDB.getTable(TABLE_NAME);

        ScanSpec scanSpec = new ScanSpec().withProjectionExpression(
                String.join(",", USERNAME_FIELD, DELIVERY_TIME_FIELD, MESSAGE_FIELD)
        );

        try {
            ItemCollection<ScanOutcome> items = table.scan(scanSpec);

            for (Item item : items) {
                messages.add(new Message(
                        item.getString(USERNAME_FIELD),
                        item.getString(DELIVERY_TIME_FIELD),
                        item.getString(MESSAGE_FIELD))
                );
            }

        } catch (Exception e) {
            log.error("Unable to scan the table: {} ", e.getMessage());
        }
        return messages;
    }

}
