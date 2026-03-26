package com.logistic.streamlog.config;

import com.logistic.streamlog.collector.jenkins.JenkinsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient jenkinsRestClient(JenkinsProperties properties){
        String auth = properties.username() + ":" + properties.apiToken();

        String basicAuth = "Basic " + Base64.getEncoder()
                .encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, basicAuth)
                .build();
    }
}
