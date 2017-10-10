FROM maven:3.5.0-jdk-8-alpine
CMD [ "mvn -U -DskipTests=true spring-boot:run ${SPRING_PROFILE}" ]
