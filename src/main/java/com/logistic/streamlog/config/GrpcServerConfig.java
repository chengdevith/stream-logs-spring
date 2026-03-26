package com.logistic.streamlog.config;

import com.logistic.streamlog.grpc.LogStreamServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;

@Configuration
@RequiredArgsConstructor
public class GrpcServerConfig {

    private Server server;
    private final LogStreamServiceImpl logStreamService;

    @PostConstruct
    public void start() throws IOException {

        server = ServerBuilder
                .forPort(9090)
                .addService(logStreamService)
                .addService(ProtoReflectionService.newInstance())
                .build()
                .start();

        Runtime.getRuntime().addShutdownHook(new Thread(() ->{
            if (server != null) {
                server.shutdown();
            }
        }));
        System.out.println("gRPC server started on port 9090");
    }

    @PreDestroy
    public void stop(){
        if (server != null) {
            server.shutdown();
        }
    }

}
