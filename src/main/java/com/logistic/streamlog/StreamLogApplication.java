package com.logistic.streamlog;

import com.logistic.streamlog.collector.jenkins.JenkinsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JenkinsProperties.class)
public class StreamLogApplication {
    public static void main(String[] args) {
        SpringApplication.run(StreamLogApplication.class, args);
    }
}
