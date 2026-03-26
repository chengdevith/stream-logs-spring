package com.logistic.streamlog.collector.jenkins.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JenkinsQueueItemResponse(
        Executable executable,
        Boolean cancelled,
        String why
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Executable(
            Integer number,
            String url
    ) {
    }
}