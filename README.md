# aws-chat
Implementation of chat based on AWS stack

# aws-chat-server

## Run application as a spring boot application:
`./gradlew :aws-chat-server:bootRun`

## Build image:
`./gradlew :aws-chat-server:bootBuildImage`

## Run docker image:
`docker run -p 5000:5000 -t acierto/aws-chat-server`

## How to configure GUI for local DynamoDB 

`npm install -g dynamodb-admin`
`export DYNAMO_ENDPOINT=http://localhost:8000`
`dynamodb-admin`

# aws-chat-client

Run application as a spring boot application:
`./gradlew :aws-chat-client:bootRun`
