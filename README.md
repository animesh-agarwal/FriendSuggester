# FriendSuggester
A basic friend management module which allows users to send friend requests to other users, see pending requests, accept them & get suggested friends.

## Configuration
Project configurations can be changed in https://github.com/animesh-agarwal/FriendSuggester/blob/master/conf/config.json. The port and host for the HTTP Server and the peristence layer can be configured.
```json                
{
    "http.port": 8000,
    "http.host": "localhost",
    "service": "redis",
    "service.host": "127.0.0.1",
    "service.port": 6380
  }
```

## Instruction to build and run the project
1. `mvn clean package shade:shade`
2. `java -jar FriendSuggester-0.0.1-SNAPSHOT-fat.jar -conf conf/config.json`

Prerequisite to build this project:
1. Java JDK 8 (http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
2. Maven  (https://maven.apache.org/install.html)
