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
## Prerequisite to build this project:
1. Java JDK 8 (http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
2. Maven  (https://maven.apache.org/install.html)
3. Redis (https://redis.io/download)

## Instruction to build and run the project locally
1. Start redis-server ( Default at `127.0.0.1:6379`) 
2. `mvn clean package shade:shade`
3. `java -jar FriendSuggester-0.0.1-SNAPSHOT-fat.jar -conf conf/config.json`


## API Endpoints
```
Create user         : POST http://localhost:8000/create
Add friend          : POST http://localhost:8000/add/:userA/:userB
Get friend requests : GET  http://localhost:8000/friendRequests/:userA
Get friends         : GET  http://localhost:8000/friends/:userA
Get suggestions     : GET  http://localhost:8000//suggestions/:userA
```
