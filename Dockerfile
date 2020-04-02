#
# Build stage
#
FROM maven:3.6.0-jdk-8-slim AS build

WORKDIR /home/app/jp2g

# Cache dependency layer unless POM changes
COPY pom.xml ./
RUN mvn -f ./pom.xml dependency:resolve

# Copy source and install
COPY src ./src
RUN mvn -f ./pom.xml clean install

ENTRYPOINT ["java", "-jar","/home/app/jp2g/target/java-parse-to-graph-1.0-SNAPSHOT.jar"]

#
# Prod stage
#
FROM maven:3.6.0-jdk-8-slim AS production

WORKDIR /home/app/jp2g

COPY --from=build /home/app/jp2g/target/java-parse-to-graph-1.0-SNAPSHOT.jar jp2g.jar
COPY --from=build /home/app/jp2g/target/libs libs

ENTRYPOINT ["java", "-jar","/home/app/jp2g/target/jp2g.jar"]