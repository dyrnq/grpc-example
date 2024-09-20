package com.dyrnq;

import com.dyrnq.grpc.BiDirectionalExampleService;
import com.dyrnq.grpc.StreamServiceGrpc;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
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
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@CommandLine.Command(name = "client", aliases = {"c"}, description = "client")
public class GrpcExampleClient implements Callable<Integer> {
//    public static void main(String[] args) throws IOException, InterruptedException {

    @CommandLine.Option(names = {"-s", "--server"}, description = "server",defaultValue = "127.0.0.1:50053")
    String server;

    @CommandLine.Option(names = {"--ca"}, description = "ca")
    String ca;

    @CommandLine.Option(names = {"--cert"}, description = "cert")
    String cert;
    @CommandLine.Option(names = {"--key"}, description = "key")
    String key;

    @Override
    public Integer call() throws Exception {
        //ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50053).usePlaintext().build();

        ManagedChannel channel;
        // 创建 gRPC 通道
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
            channel = NettyChannelBuilder.forTarget(server)
                    .negotiationType(NegotiationType.TLS)
                    .sslContext(sslContext)
                    .build();
        } else {
            channel = Grpc.newChannelBuilder(server, InsecureChannelCredentials.create()).build();
        }


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
