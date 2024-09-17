# grpc-example

```bash
mvn \
archetype:generate \
-DgroupId=com.dyrnq \
-DartifactId=grpc-example \
-DarchetypeArtifactId=maven-archetype-quickstart \
-DinteractiveMode=false
```

```bash
## 名字比较奇怪,linux下也是以.exe结尾的
## wget https://repo1.maven.org/maven2/io/grpc/protoc-gen-grpc-java/1.66.0/protoc-gen-grpc-java-1.66.0-linux-x86_64.exe

curl -fSL -# -o /usr/local/bin/protoc-gen-grpc-java https://repo1.maven.org/maven2/io/grpc/protoc-gen-grpc-java/1.66.0/protoc-gen-grpc-java-1.66.0-linux-x86_64.exe
chmod +x /usr/local/bin/protoc-gen-grpc-java
```

```bash
protoc \
--java_out=src/main/java \
--grpc-java_out=src/main/java \
--plugin=protoc-gen-grpc-java=/usr/local/bin/protoc-gen-grpc-java \
src/main/proto/stream.proto
```


## build

```bash
mvn clean package
```


```bash
java -jar target/grpc-example-1.0-SNAPSHOT.jar client --server 127.0.0.1:50053
```
> client.log
```bash
Sep 17, 2024 7:34:39 PM com.google.protobuf.RuntimeVersion validateProtobufGencodeVersionImpl
WARNING:  Protobuf gencode version 4.28.0 is older than the runtime version 4.28.1 at com.dyrnq.grpc.BiDirectionalExampleService$Request. Please avoid checked-in Protobuf gencode that can be obsolete.
Sep 17, 2024 7:34:39 PM com.google.protobuf.RuntimeVersion validateProtobufGencodeVersionImpl
WARNING:  Protobuf gencode version 4.28.0 is older than the runtime version 4.28.1 at com.dyrnq.grpc.BiDirectionalExampleService$Response. Please avoid checked-in Protobuf gencode that can be obsolete.
onNext from client0
onNext from client1
onNext from client2
onNext from client3
```
> server.log
```bash
2024/09/17 19:34:28 server listening at [::]:50053
2024/09/17 19:34:39 
2024/09/17 19:34:40 id:"1" name:"338e659b-c90f-40ef-bd5f-e47aa21b4568"
2024/09/17 19:34:41 id:"1" name:"57ab56f5-2863-4eb4-9811-5ea50206ca2c"
2024/09/17 19:34:42 id:"1" name:"3b20f039-08fb-467e-812a-0b22b7a6d3fc"
```

## ref

- <https://github.com/dyrnq/cobra-example>
- <https://github.com/nddipiazza/grpc-java-bidirectional-streaming-example>