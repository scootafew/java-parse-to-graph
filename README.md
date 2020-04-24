[ide_config]: img/IDE_Configuration.png "IDE Configuration"
# java-parse-to-graph

## Running the Program
### As jar
Run the following commands from the root of the project:

To build jar:
```shell script
mvn clean install
```
```shell script
java -DMAVEN_HOME="C:/Program Files/apache-maven-3.6.3" \
 -DNEO4J_URI=bolt://localhost:7687 \
 -DNEO4J_USERNAME=neo4j \
 -DNEO4J_PASSWORD=<neo4j-password> \
 -jar target/java-parse-to-graph-1.0-SNAPSHOT.jar \
 "https://github.com/javaparser/javaparser.git"
```

### With Docker
Build and tag with the following command:
```shell script
docker build --tag=jp2g .
```

To run container with Docker:
```shell script
docker run --link neo4j \
 --net <docker-network> \
 --env LOG_LEVEL=INFO \
 --env NEO4J_URI=bolt://neo4j:7687 \
 --env NEO4J_USERNAME=neo4j \
 --env NEO4J_PASSWORD=<neo4j-password> \
 --env MAVEN_HOME="/usr/share/maven" \
 jp2g "https://github.com/javaparser/javaparser.git"
```
#### Notes
* Requires the Docker network to be specified if container is to communicate with Neo4j.
* URI assumes the Neo4J container is named "neo4j" so that Docker internal DNS service can resolve the correct host.
* Also note that the path to MAVEN_HOME is internal within the container.

### In IDE (IntelliJ IDEA)
![IDE Configuration][ide_config]

Neo4J and Maven configuration is done by setting the appropriate values as program arguments or environment variables
```shell script
# As program arguments
-DNEO4J_URI=bolt://localhost:7687
-DNEO4J_USERNAME=neo4j
-DNEO4J_PASSWORD=<neo4j-password>
-DMAVEN_HOME="C:/Program Files/apache-maven-3.6.3"

# As environment variables
NEO4J_URI=bolt://localhost:7687
NEO4J_USERNAME=neo4j
NEO4J_PASSWORD=<neo4j-password>
MAVEN_HOME="C:/Program Files/apache-maven-3.6.3"
```

## Cypher commands (Neo4j)
Get a specific node
```
MATCH (p:Package {fullyQualifiedName:"com.york.sdp518.domain"})
RETURN n
```
Get the first 25 nodes with the label "Package"
```
MATCH (n:Package) RETURN n LIMIT 25
```
Delete all nodes and relationships
```
MATCH (n)
OPTIONAL MATCH (n)-[r]-()
DELETE r,n
```
