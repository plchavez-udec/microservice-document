FROM openjdk:8
VOLUME /tmp
ADD ./target/microservicedocument-0.0.1-SNAPSHOT.jar microservicedocument.jar
ENTRYPOINT ["java","-jar","microservicedocument.jar"]
