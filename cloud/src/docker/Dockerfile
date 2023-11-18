#
# BUILD STAGE
#
FROM maven:3.8.3-eclipse-temurin-17 as build
COPY ../../src /usr/src/app/src
COPY ../../pom.xml /usr/src/app
RUN --mount=type=cache,target=/root/.m2 mvn -f /usr/src/app/pom.xml clean package

#
# PACKAGE STAGE
#
FROM eclipse-temurin:17-jre-ubi9-minimal
COPY --from=build /usr/src/app/target/cloud-node-1.0-SNAPSHOT.one-jar.jar /usr/app/cloud-node.jar
EXPOSE 7788
EXPOSE 7799

CMD ["java","-jar","/usr/app/cloud-node.jar"]
