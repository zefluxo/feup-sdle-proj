#
# BUILD STAGE
#
FROM maven:3.8.3-eclipse-temurin-17 as build
COPY ../../src /usr/src/app/src
COPY ../../pom.xml /usr/src/app
RUN mvn -f /usr/src/app/pom.xml clean package

#
# PACKAGE STAGE
#
FROM eclipse-temurin:17-jre-ubi9-minimal
COPY --from=build /usr/src/app/target/client-1.0-SNAPSHOT.one-jar.jar /usr/app/client.jar
EXPOSE 5555
CMD ["java","-jar","/usr/app/client.jar"]
