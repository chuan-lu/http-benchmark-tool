package com.github.mkopylec.httpbenchmarktool.core;

import com.github.mkopylec.httpbenchmarktool.config.BenchmarkConfiguration;
import com.github.mkopylec.httpbenchmarktool.config.ProgramArgumentsParser;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
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
    private final HttpEntity<String> requestEntity;

	public BenchmarkRunner(String[] arguments) {
        ProgramArgumentsParser argumentsParser = new ProgramArgumentsParser(arguments);
        configuration = argumentsParser.getBenchmarkConfiguration();
        resultsCollector = new ResultsCollector(configuration.getNumberOfRequests());
        threadPool = newCachedThreadPool();
        restTemplate.setInterceptors(singletonList(resultsCollector));
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setConnection("close");
		requestEntity = new HttpEntity<>(configuration.getBody(), headers);
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
        printLine("Number of 200 responses: " + resultsCollector.getNumberOf200());
        printLine("Number of 204 responses: " + resultsCollector.getNumberOf204());
        printLine("Number of 4xx responses: " + resultsCollector.getNumberOf4xx());
        printLine("Number of 5xx responses: " + resultsCollector.getNumberOf5xx());
        printLine("Number of errors: " + numberOfErrors.get());
        printLine("==================================");
    }

    private void executeRequest() {
        threadPool.execute(() -> {
            try {
                //restTemplate.execute(configuration.getUrl(), configuration.getHttpMethod(), null, null);
                restTemplate.exchange(configuration.getUrl(), configuration.getHttpMethod(), requestEntity, String.class);
            } catch (Exception e) {
                printLine(e);
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
