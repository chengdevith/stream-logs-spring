package com.logistic.streamlog.collector.jenkins;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jenkins")
public record JenkinsProperties(
        String baseUrl,
        String username,
        String apiToken
) {
}
