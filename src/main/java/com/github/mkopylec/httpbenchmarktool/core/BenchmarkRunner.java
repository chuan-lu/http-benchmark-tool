package com.github.mkopylec.httpbenchmarktool.core;

import com.github.mkopylec.httpbenchmarktool.config.BenchmarkConfiguration;
import com.github.mkopylec.httpbenchmarktool.config.ProgramArgumentsParser;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.mkopylec.httpbenchmarktool.utils.Printer.printLine;
import static com.google.common.util.concurrent.RateLimiter.create;
import static java.util.Collections.singletonList;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.TimeUnit.HOURS;

public class BenchmarkRunner {

    private final BenchmarkConfiguration configuration;
    private final ExecutorService threadPool;
    private final RestTemplate restTemplate = new RestTemplate();
    private final AtomicInteger numberOfErrors = new AtomicInteger(0);
    private final ResultsCollector resultsCollector;

    public BenchmarkRunner(String[] arguments) {
        ProgramArgumentsParser argumentsParser = new ProgramArgumentsParser(arguments);
        configuration = argumentsParser.getBenchmarkConfiguration();
        resultsCollector = new ResultsCollector(configuration.getNumberOfRequests());
        threadPool = newCachedThreadPool();
        restTemplate.setInterceptors(singletonList(resultsCollector));
    }

    public void runBenchmark() {
        RateLimiter rateLimiter = create(configuration.getRequestsPerSecond());
        for (int i = 0; i < configuration.getNumberOfRequests(); i++) {
            rateLimiter.acquire();
            executeRequest();
        }

        waitForResponses();
    }

    public void printResults() {
        printLine("Calculating statistics...");
        DescriptiveStatistics statistics = resultsCollector.getResponseTimesStatistics();
        printLine();
        printLine("======== BENCHMARK RESULTS ========");
        printLine("50th percentile: " + (int) statistics.getPercentile(50) + " ms");
        printLine("90th percentile: " + (int) statistics.getPercentile(90) + " ms");
        printLine("99th percentile: " + (int) statistics.getPercentile(99) + " ms");
        printLine("99.9th percentile: " + (int) statistics.getPercentile(99.9) + " ms");
        printLine("99.99th percentile: " + (int) statistics.getPercentile(99.99) + " ms");
        printLine();
        printLine("Number of received responses: " + resultsCollector.getNumberOfCollectedResponses());
        printLine("Number of 2xx responses: " + resultsCollector.getNumberOf2xx());
        printLine("Number of 3xx responses: " + resultsCollector.getNumberOf3xx());
        printLine("Number of 4xx responses: " + resultsCollector.getNumberOf4xx());
        printLine("Number of 5xx responses: " + resultsCollector.getNumberOf5xx());
        printLine("Number of errors: " + numberOfErrors.get());
        printLine("==================================");
    }

    private void executeRequest() {
        threadPool.execute(() -> {
            try {
                restTemplate.execute(configuration.getUrl(), configuration.getHttpMethod(), null, null);
            } catch (Exception e) {
                numberOfErrors.incrementAndGet();
            }
        });
    }

    private void waitForResponses() {
        printLine("Waiting for remaining responses...");
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(1, HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
