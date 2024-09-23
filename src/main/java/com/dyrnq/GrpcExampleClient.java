package com.dyrnq;

import com.dyrnq.grpc.BiDirectionalExampleService;
import com.dyrnq.grpc.StreamServiceGrpc;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.ClientAuth;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

@CommandLine.Command(name = "client", aliases = {"c"}, description = "client")
public class GrpcExampleClient implements Callable<Integer> {
//    public static void main(String[] args) throws IOException, InterruptedException {
    private static final int MAX_RETRIES = 5; // 最大重试次数
    private static final long RETRY_DELAY_MS = 3000; // 重试延迟（毫秒
    @CommandLine.Option(names = {"-s", "--server"}, description = "server",defaultValue = "127.0.0.1:50053")
    String server;

    @CommandLine.Option(names = {"--ca"}, description = "ca")
    String ca;

    @CommandLine.Option(names = {"--cert"}, description = "cert")
    String cert;
    @CommandLine.Option(names = {"--key"}, description = "key")
    String key;

//    @Override
//    public Integer call() throws Exception {
//        //ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50053).usePlaintext().build();
//
//        ManagedChannel channel;
//        // 创建 gRPC 通道
//        if (StringUtils.isNoneBlank(ca) && StringUtils.isNoneBlank(cert) && StringUtils.isNoneBlank(key)) {
//            // 客户端证书和私钥
//            File clientCert = new File(cert);
//            File clientKey = new File(key);
//            // CA 证书
//            File trustCertificate = new File(ca);
//            // 创建 SSL 上下文
//            SslContext sslContext = GrpcSslContexts
//                    .forClient()
//                    .clientAuth(ClientAuth.REQUIRE)
//                    .trustManager(trustCertificate) // 设置信任管理器
//                    .keyManager(clientCert, clientKey)
//                    .build();
//            channel = NettyChannelBuilder.forTarget(server)
//                    .negotiationType(NegotiationType.TLS)
//                    .sslContext(sslContext)
//                    .build();
//        } else {
//            channel = Grpc.newChannelBuilder(server, InsecureChannelCredentials.create()).build();
//        }
//
//
//        StreamServiceGrpc.StreamServiceStub service = StreamServiceGrpc.newStub(channel);
//        AtomicReference<StreamObserver<BiDirectionalExampleService.Request>> requestObserverRef = new AtomicReference<>();
//
//
//        CountDownLatch finishedLatch = new CountDownLatch(1);
//        StreamObserver<com.dyrnq.grpc.BiDirectionalExampleService.Request> observer = service.channel(new StreamObserver<BiDirectionalExampleService.Response>() {
//            @Override
//            public void onNext(BiDirectionalExampleService.Response value) {
//                System.out.println("onNext from client" + value.getTotal());
//                try {
//                    Thread.sleep(1000L);
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                }
//                BiDirectionalExampleService.Request rq = BiDirectionalExampleService.Request.newBuilder().setId("1").setName(UUID.randomUUID().toString()).build();
//                //requestObserverRef.get().onNext(BiDirectionalExampleService.Request.getDefaultInstance());
//                requestObserverRef.get().onNext(rq);
//
//            }
//
//            @Override
//            public void onError(Throwable t) {
//                System.out.println("on error");
//                t.printStackTrace();
//            }
//
//            @Override
//            public void onCompleted() {
//                System.out.println("on completed");
//                finishedLatch.countDown();
//            }
//        });
//        requestObserverRef.set(observer);
//        observer.onNext(BiDirectionalExampleService.Request.getDefaultInstance());
//        finishedLatch.await();
//        observer.onCompleted();
//
//        return 0;
//    }

    @Override
    public Integer call() throws Exception {
        int retries = 0;
        while (true) {
            try {
                ManagedChannel channel = createChannel();
                return executeWithChannel(channel);
            } catch (StatusRuntimeException e) {
                if (retries < MAX_RETRIES) {
                    retries++;
                    System.out.println("Failed to connect to the server. Retrying in " + RETRY_DELAY_MS + "ms...");
                    Thread.sleep(RETRY_DELAY_MS);
                } else {
                    throw e;
                }
            }
        }
    }

    private void reconnect() {
        ManagedChannel channel = null;
        try {
            channel = createChannel();
            // 使用新通道重新执行操作
            executeWithChannel(channel);
        } catch (Exception e) {
            e.printStackTrace();
            // 重连失败，等待一段时间后重试
            try {
                Thread.sleep(RETRY_DELAY_MS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            reconnect();
        } finally {
            if (channel != null) {
                channel.shutdownNow();
            }
        }
    }

    private ManagedChannel createChannel() throws Exception {
        if (StringUtils.isNoneBlank(ca) && StringUtils.isNoneBlank(cert) && StringUtils.isNoneBlank(key)) {
            // 客户端证书和私钥
            File clientCert = new File(cert);
            File clientKey = new File(key);
            // CA 证书
            File trustCertificate = new File(ca);
            // 创建 SSL 上下文
            SslContext sslContext = GrpcSslContexts
                    .forClient()
                    .clientAuth(ClientAuth.REQUIRE)
                    .trustManager(trustCertificate) // 设置信任管理器
                    .keyManager(clientCert, clientKey)
                    .build();
            return NettyChannelBuilder.forTarget(server)
                    .negotiationType(NegotiationType.TLS)
                    .sslContext(sslContext)
                    .build();
        } else {
            return Grpc.newChannelBuilder(server, InsecureChannelCredentials.create()).build();
        }
    }

    private Integer executeWithChannel(ManagedChannel channel) throws Exception {
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
                // 尝试重连
                reconnect();
            }

            @Override
            public void onCompleted() {
                System.out.println("on completed");
                finishedLatch.countDown();
            }
        });
        requestObserverRef.set(observer);
//        observer.onNext(BiDirectionalExampleService.Request.getDefaultInstance());

        // 启动一个定时任务来检查连接状态
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            if (!channel.isShutdown() && !channel.isTerminated()) {
                try {
                    // 尝试发送一个空请求来检查连接
                    observer.onNext(BiDirectionalExampleService.Request.getDefaultInstance());
                } catch (Exception e) {
                    // 如果发送失败，可能是连接断开了，尝试重连
                    System.out.println("Connection lost. Reconnecting...");
                    reconnect();
                }
            }
        }, 0, 5, TimeUnit.SECONDS); // 每5秒检查一次连接状态

        finishedLatch.await();
        return 0;
    }


}
