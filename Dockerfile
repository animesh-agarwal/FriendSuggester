FROM java:8-jdk-alpine
COPY target/FriendSuggester-0.0.1-SNAPSHOT-fat.jar /usr/app/
COPY conf/config.json /usr/app
WORKDIR /usr/app
EXPOSE 8000
ENTRYPOINT [ "java", "-jar", "FriendSuggester-0.0.1-SNAPSHOT-fat.jar" , "-conf" "config.json"]