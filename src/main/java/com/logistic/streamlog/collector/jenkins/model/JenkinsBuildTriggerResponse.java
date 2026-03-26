package com.logistic.streamlog.collector.jenkins.model;

public record JenkinsBuildTriggerResponse(
        String queueUrl,
        Long queueId
) {
}
