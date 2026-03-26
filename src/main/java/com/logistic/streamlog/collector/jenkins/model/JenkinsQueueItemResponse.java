package com.logistic.streamlog.collector.jenkins.model;

public record JenkinsQueueItemResponse(
        Executable executable,
        boolean cancelled,
        String why
) {
    public record Executable(
            int number,
            String url
    ) {
    }
}
