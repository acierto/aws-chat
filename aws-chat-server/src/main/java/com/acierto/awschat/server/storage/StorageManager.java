package com.acierto.awschat.server.storage;

import com.acierto.awschat.server.storage.data.Message;
import com.acierto.awschat.server.storage.data.User;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;

public class StorageManager {

    private final AmazonDynamoDB amazonDynamoDB;

    public StorageManager(AmazonDynamoDB amazonDynamoDB) {
        this.amazonDynamoDB = amazonDynamoDB;
    }

    public void createTables() {
        createTable(Message.class);
        createTable(User.class);
    }

    private boolean tableExists(Class<?> table) {
        ListTablesResult result = amazonDynamoDB.listTables();
        return result.getTableNames().contains(table.getSimpleName());
    }

    private void createTable(Class<?> table) {
        if(!tableExists(table)) {
            DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB);
            CreateTableRequest tableRequest = dynamoDBMapper.generateCreateTableRequest(table);
            tableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
            amazonDynamoDB.createTable(tableRequest);
        }
    }
}
