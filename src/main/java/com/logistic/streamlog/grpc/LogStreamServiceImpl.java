package com.logistic.streamlog.grpc;

import com.logistic.streamlog.collector.jenkins.JenkinsLogCollector;
import com.logistic.streamlog.proto.ExistingLogRequest;
import com.logistic.streamlog.proto.LogEvent;
import com.logistic.streamlog.proto.LogStreamServiceGrpc;
import com.logistic.streamlog.proto.TriggerLogRequest;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogStreamServiceImpl extends LogStreamServiceGrpc.LogStreamServiceImplBase {

    private final JenkinsLogCollector jenkinsLogCollector;

    // Stream existing build
    @Override
    public void streamExistingLogs(ExistingLogRequest request, StreamObserver<LogEvent> responseObserver) {

        String jobName = request.getJenkinsJobName();

        int buildNumber = request.getJenkinsBuildNumber();

        jenkinsLogCollector.streamBuildLogs(jobName, buildNumber, responseObserver);
    }

    // Trigger + stream (REAL-TIME)
    @Override
    public void triggerAndStreamLogs(TriggerLogRequest request, StreamObserver<LogEvent> responseObserver) {

        String jobName = request.getJenkinsJobName();

        jenkinsLogCollector.triggerAndStream(jobName, responseObserver);
    }
}
