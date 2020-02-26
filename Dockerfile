#From https://stackoverflow.com/questions/27767264/how-to-dockerize-maven-project-and-how-many-ways-to-accomplish-it

#
# Build stage
#
FROM maven:3.6.0-jdk-8-slim AS build

# Install git
RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y git

WORKDIR /home/app/jp2g

# Cache dependency layer unless POM changes
COPY pom.xml ./
RUN mvn -f ./pom.xml dependency:resolve

# Copy source and install
COPY src ./src
RUN mvn -f ./pom.xml clean install

ENTRYPOINT ["java", "-jar","/home/app/jp2g/target/java-parse-to-graph-1.0-SNAPSHOT.jar"]
CMD []

##
## Package stage
##
#FROM openjdk:11-jre-slim
#COPY --from=build /home/app/jp2g/target/java-parse-to-graph-1.0-SNAPSHOT.jar /usr/local/lib/app.jar
#COPY --from=build /home/app/jp2g/target/lib /usr/local/lib/lib
#ENTRYPOINT ["java","-jar","/usr/local/lib/app.jar"]

#https://codefresh.io/docker-tutorial/java_docker_pipeline/