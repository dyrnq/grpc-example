package com.dyrnq;

import com.dyrnq.grpc.BiDirectionalExampleService;
import com.dyrnq.grpc.StreamServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class GrpcExampleClient {
    public static void main(String[] args) throws IOException, InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50053).usePlaintext().build();
        StreamServiceGrpc.StreamServiceStub service = StreamServiceGrpc.newStub(channel);
        AtomicReference<StreamObserver<BiDirectionalExampleService.Request>> requestObserverRef = new AtomicReference<>();


        CountDownLatch finishedLatch = new CountDownLatch(1);
        StreamObserver<com.dyrnq.grpc.BiDirectionalExampleService.Request> observer = service.channel(new StreamObserver<BiDirectionalExampleService.Response>() {
            @Override
            public void onNext(BiDirectionalExampleService.Response value) {
                System.out.println("onNext from client" + value.getTotal());
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                BiDirectionalExampleService.Request rq = BiDirectionalExampleService.Request.newBuilder().setId("1").setName(UUID.randomUUID().toString()).build();
                //requestObserverRef.get().onNext(BiDirectionalExampleService.Request.getDefaultInstance());
                requestObserverRef.get().onNext(rq);

            }

            @Override
            public void onError(Throwable t) {
                System.out.println("on error");
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.out.println("on completed");
                finishedLatch.countDown();
            }
        });
        requestObserverRef.set(observer);
        observer.onNext(BiDirectionalExampleService.Request.getDefaultInstance());
        finishedLatch.await();
        observer.onCompleted();
    }
}
