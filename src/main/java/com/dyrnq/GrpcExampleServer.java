package com.dyrnq;

import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import picocli.CommandLine;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

@CommandLine.Command(name = "server", aliases = {"s"}, description = "server")
public class GrpcExampleServer implements Callable<Integer> {


    private static Executor executor;
    @CommandLine.Option(names = {"--ca"}, description = "ca")
    String ca;
    @CommandLine.Option(names = {"--cert"}, description = "cert")
    String cert;
    @CommandLine.Option(names = {"--key"}, description = "key")
    String key;
    @CommandLine.Option(names = {"--address"}, description = "address", defaultValue = "0.0.0.0")
    String address;
    @CommandLine.Option(names = {"--port"}, description = "port", defaultValue = "50053")
    Integer port;

    @Override
    public Integer call() throws Exception {
        ServerBuilder builder = null;

        // Set up credentials
        if (ca != null && cert != null && key != null) {
            File caCert = new File(ca);
            File serverCert = new File(cert);
            File serverKey = new File(key);

            SslContext sslContext = GrpcSslContexts
                    .forServer(serverCert, serverKey)
                    .trustManager(caCert) // 设置信任管理器
                    .build();

            builder = NettyServerBuilder.forAddress(new InetSocketAddress(address, port)).sslContext(sslContext);
        } else {
            builder = NettyServerBuilder.forAddress(new InetSocketAddress(address, port));
        }


        executor = MoreExecutors.directExecutor();
        builder.executor(executor);
        Server server = builder.addService(new ExampleServiceGrpcImpl()).build();

        server.start();

        System.out.println("Server has started");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.shutdown();
        }));

        server.awaitTermination();
        return 0;
    }
}