package com.dyrnq;

import com.dyrnq.grpc.BiDirectionalExampleService;
import com.dyrnq.grpc.StreamServiceGrpc;
import io.grpc.stub.StreamObserver;

public class ExampleServiceGrpcImpl extends StreamServiceGrpc.StreamServiceImplBase {
    private int total;

    @Override
    public StreamObserver<BiDirectionalExampleService.Request> channel(StreamObserver<BiDirectionalExampleService.Response> responseObserver) {

        // 在这里启动一个新线程,定期向客户端发送消息
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(2000); // 每2秒发送一次
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                // 构建要发送的响应消息
                BiDirectionalExampleService.Response response = BiDirectionalExampleService.Response.newBuilder()
                        .setTotal(99999999)
                        .build();

                // 发送消息给客户端
                responseObserver.onNext(response);
            }
        }).start();

        StreamObserver<BiDirectionalExampleService.Request> so = new StreamObserver<BiDirectionalExampleService.Request>() {
            @Override
            public void onNext(BiDirectionalExampleService.Request value) {
//                try {
//                    Thread.sleep(1000L);
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                }
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
