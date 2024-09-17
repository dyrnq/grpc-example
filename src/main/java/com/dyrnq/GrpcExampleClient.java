package com.dyrnq;

import com.dyrnq.grpc.BiDirectionalExampleService;
import com.dyrnq.grpc.StreamServiceGrpc;
import io.grpc.*;
import io.grpc.stub.StreamObserver;
import picocli.CommandLine;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@CommandLine.Command(name = "client", aliases = {"c"}, description = "client")
public class GrpcExampleClient implements Callable<Integer> {
//    public static void main(String[] args) throws IOException, InterruptedException {

    @CommandLine.Option(names = {"-s", "--server"}, description = "server",defaultValue = "127.0.0.1:50053")
    String server;

    @Override
    public Integer call() throws Exception {
        //ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50053).usePlaintext().build();
        ManagedChannel channel = Grpc.newChannelBuilder(server, InsecureChannelCredentials.create()).build();
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

        return 0;
    }
}
