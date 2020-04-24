package com.york.sdp518;

import com.york.sdp518.service.MavenInvoker;
import com.york.sdp518.util.Utils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.File;

@Configuration
@Profile("prod")
public class ServiceConfiguration {

    private static final String MAVEN_HOME_ENV_VAR = "MAVEN_HOME";
    private static final File MAVEN_HOME = new File(Utils.getPropertyOrEnv(MAVEN_HOME_ENV_VAR, true));

    @Bean
    public MavenInvoker getMavenInvoker() {
        MavenInvoker invoker = new MavenInvoker();
        invoker.setMavenHome(MAVEN_HOME);
        return invoker;
    }

    @Bean
    public Neo4jSessionFactory getNeo4jSessionFactory(org.neo4j.ogm.config.Configuration configuration) {
        return new Neo4jSessionFactory(configuration);
    }

    @Bean
    public org.neo4j.ogm.config.Configuration getNeo4jConfiguration() {
        String uri = Utils.getPropertyOrEnv("NEO4J_URI", true);
        String username = Utils.getPropertyOrEnv("NEO4J_USERNAME", true);
        String password = Utils.getPropertyOrEnv("NEO4J_PASSWORD", true);

        return new org.neo4j.ogm.config.Configuration.Builder()
                .uri(uri)
                .credentials(username, password)
                .verifyConnection(true)
                .autoIndex("update") // TODO Handle index creation at DB init time
                .build();
    }

}
