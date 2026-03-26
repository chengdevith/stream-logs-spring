package com.logistic.streamlog.collector.jenkins.model;

public record JenkinsProgressiveResponse(
        String text,
        long nextStart,
        boolean moreData
) {
}
