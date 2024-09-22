package com.dyrnq;

import com.dyrnq.grpc.BiDirectionalExampleService;
import com.dyrnq.grpc.StreamServiceGrpc;
import io.grpc.stub.StreamObserver;

public class ExampleServiceGrpcImpl extends StreamServiceGrpc.StreamServiceImplBase {
    private int total;

    @Override
    public StreamObserver<BiDirectionalExampleService.Request> channel(StreamObserver<BiDirectionalExampleService.Response> responseObserver) {

        StreamObserver<BiDirectionalExampleService.Request> so = new StreamObserver<BiDirectionalExampleService.Request>() {
            @Override
            public void onNext(BiDirectionalExampleService.Request value) {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                total++;
                System.out.println("Connecting stream observer " + total + ", " + value.getName());
                responseObserver.onNext(BiDirectionalExampleService.Response.getDefaultInstance().toBuilder().setTotal(total).build());
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("on error");
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.out.println("on completed");
            }
        };
        return so;
    }
}
