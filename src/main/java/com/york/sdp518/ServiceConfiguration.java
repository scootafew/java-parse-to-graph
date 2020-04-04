package com.york.sdp518;

import com.york.sdp518.service.MavenInvoker;
import com.york.sdp518.util.Utils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class ServiceConfiguration {

    private static final String MAVEN_HOME_ENV_VAR = "MAVEN_HOME";
    private static final File MAVEN_HOME = new File(Utils.getPropertyOrEnv(MAVEN_HOME_ENV_VAR, true));

    @Bean
    public MavenInvoker getMavenInvoker() {
        MavenInvoker invoker = new MavenInvoker();
        invoker.setMavenHome(MAVEN_HOME);
        return invoker;
    }

}
