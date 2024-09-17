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

## ref

- <https://github.com/dyrnq/cobra-example>
- <https://github.com/nddipiazza/grpc-java-bidirectional-streaming-example>