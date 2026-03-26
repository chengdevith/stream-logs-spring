package com.logistic.streamlog.collector.jenkins;

import com.logistic.streamlog.collector.jenkins.model.JenkinsBuildTriggerResponse;
import com.logistic.streamlog.collector.jenkins.model.JenkinsProgressiveResponse;
import com.logistic.streamlog.collector.jenkins.model.JenkinsQueueItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class JenkinsClient {

    private final RestClient restClient;
    private final JenkinsProperties properties;
    private final ObjectMapper objectMapper;

    public JenkinsProgressiveResponse readProgressiveLog(String jobName, int buildNumber, long start) {
        String path = "/job/%s/%d/logText/progressiveText?start=%d"
                .formatted(jobName, buildNumber, start);

        ResponseEntity<String> response = restClient.get()
                .uri(path)
                .retrieve()
                .toEntity(String.class);

        String body = response.getBody() == null ? "" : response.getBody();
        String textSizeHeader = response.getHeaders().getFirst("X-Text-Size");
        String moreDataHeader = response.getHeaders().getFirst("X-More-Data");

        long nextStart = textSizeHeader != null ? Long.parseLong(textSizeHeader) : start;
        boolean moreData = "true".equalsIgnoreCase(moreDataHeader);

        return new JenkinsProgressiveResponse(body, nextStart, moreData);
    }

    public JenkinsBuildTriggerResponse triggerBuild(String jobName) {
        String path = "/job/%s/build".formatted(jobName);

        ResponseEntity<Void> response = restClient.post()
                .uri(path)
                .retrieve()
                .toBodilessEntity();

        String location = response.getHeaders().getFirst("Location");
        Long queueId = extractQueueId(location);

        return new JenkinsBuildTriggerResponse(location, queueId);
    }

    public JenkinsQueueItemResponse getQueueItem(long queueId) {
        try {
            String path = "/queue/item/%d/api/json".formatted(queueId);

            String rawJson = restClient.get()
                    .uri(path)
                    .retrieve()
                    .body(String.class);

            System.out.println("Jenkins queue raw JSON: " + rawJson);

            if (rawJson == null || rawJson.isBlank()) {
                throw new IllegalStateException("Empty Jenkins queue response");
            }

            return objectMapper.readValue(rawJson, JenkinsQueueItemResponse.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse Jenkins queue item response", e);
        }
    }

    private Long extractQueueId(String location) {
        if (location == null || location.isBlank()) {
            return null;
        }

        String cleaned = location.endsWith("/")
                ? location.substring(0, location.length() - 1)
                : location;

        String id = cleaned.substring(cleaned.lastIndexOf('/') + 1);
        return Long.parseLong(id);
    }
}