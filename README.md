# java-parse-to-graph

### run jar
java -DGIT_USERNAME=scootafew -DGIT_PASSWORD=XXX -DMAVEN_HOME="C:/Program Files/apache-maven-3.6.3" -DNEO4J_URI=bolt://neo4j:neo4j@localhost:7687 -jar java-parse-to-graph-1.0-SNAPSHOT.jar "https://github.com/scootafew/ast.git"

### neo4j commands
MATCH (p:Package {fullyQualifiedName:"com.york.sdp518.domain"})
OPTIONAL MATCH (p)-[r]-()
DELETE r,p

MATCH (p:Package)
OPTIONAL MATCH (p)-[r]-()
DELETE r,p

MATCH (n) RETURN n

### Docker commands
docker build --tag=jp2g .

docker run --link neo4j --net repo-miner_default --env LOG_LEVEL=INFO --env GIT_USERNAME=scootafew --env GIT_PASSWORD=XXX --env NEO4J_URI=bolt://neo4j:neo4j@neo4j:7687 --env M2_HOME="/usr/share/maven" jp2g "https://github.com/javaparser/javaparser.git"
