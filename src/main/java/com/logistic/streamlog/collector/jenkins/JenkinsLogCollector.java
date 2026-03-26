package com.logistic.streamlog.collector.jenkins;

import com.logistic.streamlog.collector.jenkins.model.JenkinsBuildTriggerResponse;
import com.logistic.streamlog.collector.jenkins.model.JenkinsProgressiveResponse;
import com.logistic.streamlog.collector.jenkins.model.JenkinsQueueItemResponse;
import com.logistic.streamlog.proto.LogEvent;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class JenkinsLogCollector {

    private final JenkinsClient jenkinsClient;

    public void streamBuildLogs(String jobName, int buildNumber, StreamObserver<LogEvent> responseObserver) {
        long start = 0;

        try {
            while (true) {
                JenkinsProgressiveResponse chunk =
                        jenkinsClient.readProgressiveLog(jobName, buildNumber, start);

                if (!chunk.text().isBlank()) {
                    String[] lines = chunk.text().split("\\R");

                    for (String line : lines) {
                        if (line.isBlank()) continue;

                        LogEvent event = LogEvent.newBuilder()
                                .setSource("jenkins")
                                .setType("log")
                                .setMessage(line)
                                .setTimestamp(Instant.now().toString())
                                .setStage("Pipeline")
                                .setStatus("RUNNING")
                                .setHealth("UNKNOWN")
                                .build();

                        responseObserver.onNext(event);
                    }
                }

                start = chunk.nextStart();

                if (!chunk.moreData()) {
                    break;
                }

                Thread.sleep(500);
            }

            LogEvent doneEvent = LogEvent.newBuilder()
                    .setSource("jenkins")
                    .setType("status")
                    .setMessage("Build log streaming completed")
                    .setTimestamp(Instant.now().toString())
                    .setStage("Pipeline")
                    .setStatus("COMPLETED")
                    .setHealth("UNKNOWN")
                    .build();

            responseObserver.onNext(doneEvent);
            responseObserver.onCompleted();

        } catch (Exception e) {
            LogEvent errorEvent = LogEvent.newBuilder()
                    .setSource("jenkins")
                    .setType("error")
                    .setMessage(e.getMessage() != null ? e.getMessage() : "Unknown Jenkins error")
                    .setTimestamp(Instant.now().toString())
                    .setStage("Pipeline")
                    .setStatus("FAILED")
                    .setHealth("UNKNOWN")
                    .build();

            responseObserver.onNext(errorEvent);
            responseObserver.onError(e);
        }
    }

    public void triggerAndStream(String jobName, StreamObserver<LogEvent> responseObserver) {
        try {
            JenkinsBuildTriggerResponse triggerResponse = jenkinsClient.triggerBuild(jobName);

            if (triggerResponse.queueId() == null) {
                throw new IllegalStateException("Jenkins did not return queue id");
            }

            Integer buildNumber = waitForBuildNumber(triggerResponse.queueId(), responseObserver);

            if (buildNumber == null) {
                throw new IllegalStateException("Build number was not assigned");
            }

            streamBuildLogs(jobName, buildNumber, responseObserver);

        } catch (Exception e) {
            LogEvent errorEvent = LogEvent.newBuilder()
                    .setSource("jenkins")
                    .setType("error")
                    .setMessage(e.getMessage() != null ? e.getMessage() : "Unknown Jenkins error")
                    .setTimestamp(Instant.now().toString())
                    .setStage("Queue")
                    .setStatus("FAILED")
                    .setHealth("UNKNOWN")
                    .build();

            responseObserver.onNext(errorEvent);
            responseObserver.onError(e);
        }
    }

    private Integer waitForBuildNumber(long queueId, StreamObserver<LogEvent> responseObserver) throws InterruptedException {
        while (true) {
            JenkinsQueueItemResponse queueItem = jenkinsClient.getQueueItem(queueId);

            if (queueItem.cancelled()) {
                throw new IllegalStateException("Jenkins queue item was cancelled");
            }

            if (queueItem.executable() != null) {
                LogEvent startedEvent = LogEvent.newBuilder()
                        .setSource("jenkins")
                        .setType("status")
                        .setMessage("Build started: #" + queueItem.executable().number())
                        .setTimestamp(Instant.now().toString())
                        .setStage("Queue")
                        .setStatus("RUNNING")
                        .setHealth("UNKNOWN")
                        .build();

                responseObserver.onNext(startedEvent);
                return queueItem.executable().number();
            }

            LogEvent waitingEvent = LogEvent.newBuilder()
                    .setSource("jenkins")
                    .setType("status")
                    .setMessage(queueItem.why() != null ? queueItem.why() : "Waiting for executor...")
                    .setTimestamp(Instant.now().toString())
                    .setStage("Queue")
                    .setStatus("QUEUED")
                    .setHealth("UNKNOWN")
                    .build();

            responseObserver.onNext(waitingEvent);

            Thread.sleep(1000);
        }
    }
}