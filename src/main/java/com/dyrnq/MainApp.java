package com.dyrnq;

import picocli.CommandLine;

@CommandLine.Command(
        subcommands = {
                GrpcExampleClient.class,

        },
        version = "Sample 1.0",
        mixinStandardHelpOptions = true
)
class MainApp implements Runnable {


//
//    @CommandLine.Option(names = {"-v", "--verbose"}, description = "explain what is being done")
//    boolean verbose;
//    @CommandLine.Option(names = {"-i", "--accessKeyId"}, description = "accessKeyId")
//    String accessKeyId;
//
//    @CommandLine.Option(names = {"-s", "--accessKeySecret"}, description = "accessKeySecret")
//    String accessKeySecret;

    public static void main(String[] args) {
        MainApp app = new MainApp();
        int code = new CommandLine(app).execute(args);
        System.exit(code);
    }

    @Override
    public void run() {

    }

    // ...
}
