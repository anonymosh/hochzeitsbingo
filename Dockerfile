FROM openjdk:8-jdk-alpine
RUN apk add --no-cache fontconfig ttf-dejavu
COPY target/bingo-0.0.1-SNAPSHOT.jar bingo-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/bingo-0.0.1-SNAPSHOT.jar"]