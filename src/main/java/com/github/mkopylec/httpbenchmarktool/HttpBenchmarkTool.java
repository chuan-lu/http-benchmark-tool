package com.github.mkopylec.httpbenchmarktool;

import com.github.mkopylec.httpbenchmarktool.core.BenchmarkRunner;

import static com.github.mkopylec.httpbenchmarktool.utils.Printer.printLine;
import static java.lang.Runtime.getRuntime;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.exit;

public class HttpBenchmarkTool {

    public static void main(String[] args) {
        long start = currentTimeMillis();
        printLine("Starting HTTP benchmark...");
        BenchmarkRunner runner = new BenchmarkRunner();
        getRuntime().addShutdownHook(new Thread(runner::printResults));
        runner.runBenchmark(args);
        printLine("HTTP benchmark ended after " + (currentTimeMillis() - start) + " ms.");
        exit(0);
    }
}
