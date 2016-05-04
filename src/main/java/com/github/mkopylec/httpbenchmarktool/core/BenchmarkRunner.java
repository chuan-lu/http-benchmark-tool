package com.github.mkopylec.httpbenchmarktool.core;

import com.github.mkopylec.httpbenchmarktool.config.BenchmarkConfiguration;
import com.github.mkopylec.httpbenchmarktool.config.ProgramArgumentsParser;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.http.Header;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.mkopylec.httpbenchmarktool.utils.Printer.printLine;
import static com.google.common.util.concurrent.RateLimiter.create;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.TimeUnit.HOURS;
import static org.apache.http.impl.nio.client.HttpAsyncClients.createMinimal;

public class BenchmarkRunner {

    private ExecutorService threadPool;
    private final CloseableHttpAsyncClient httpClient = createMinimal();
    private final RestTemplate restTemplate = new RestTemplate();
    private List<Thread> threads;

    private List<Long> responseTimes;
    private final AtomicInteger numberOf2xx = new AtomicInteger(0);
    private final AtomicInteger numberOf3xx = new AtomicInteger(0);
    private final AtomicInteger numberOf4xx = new AtomicInteger(0);
    private final AtomicInteger numberOf5xx = new AtomicInteger(0);
    private final AtomicInteger numberOfErrors = new AtomicInteger(0);
    private final DescriptiveStatistics statistics = new DescriptiveStatistics();

    public void runBenchmark(String[] arguments) {
        ProgramArgumentsParser argumentsParser = new ProgramArgumentsParser(arguments);
        BenchmarkConfiguration configuration = argumentsParser.getBenchmarkConfiguration();
        HttpRequestBase request = getRequest(configuration);
        RateLimiter rateLimiter = create(configuration.getRequestsPerSecond());
//        threads = new ArrayList<>(configuration.getNumberOfRequests());
//        threadPool = newFixedThreadPool(configuration.getRequestsPerSecond()*10);
        threadPool = newCachedThreadPool();
        responseTimes = new ArrayList<>(configuration.getNumberOfRequests());
        httpClient.start();
        for (int i = 0; i < configuration.getNumberOfRequests(); i++) {
            rateLimiter.acquire();
            executeRequest(request);
        }
        waitForResponses();
    }

    public void printResults() {
        responseTimes.forEach(statistics::addValue);
        printLine();
        printLine("======== BENCHMARK RESULTS ========");
        printLine("50th percentile: " + (int) statistics.getPercentile(50) + " ms");
        printLine("90th percentile: " + (int) statistics.getPercentile(90) + " ms");
        printLine("99th percentile: " + (int) statistics.getPercentile(99) + " ms");
        printLine("99.9th percentile: " + (int) statistics.getPercentile(99.9) + " ms");
        printLine("99.99th percentile: " + (int) statistics.getPercentile(99.99) + " ms");
        printLine();
        printLine("Number of requests sent: " + responseTimes.size());
        printLine("Number of 2xx responses: " + numberOf2xx.get());
        printLine("Number of 3xx responses: " + numberOf3xx.get());
        printLine("Number of 4xx responses: " + numberOf4xx.get());
        printLine("Number of 5xx responses: " + numberOf5xx.get());
        printLine("Number of errors: " + numberOfErrors.get());
        printLine("==================================");
    }

    private HttpRequestBase getRequest(BenchmarkConfiguration configuration) {
        HttpRequestBase request;

        String url = configuration.getUrl();
        StringEntity body = configuration.getBody();

        switch (configuration.getHttpMethod()) {
            case GET:
                request = new HttpGet(url);
                break;
            case POST:
                HttpPost post = new HttpPost(url);
                post.setEntity(body);
                request = post;
                break;
            case PUT:
                HttpPut put = new HttpPut(url);
                put.setEntity(body);
                request = put;
                break;
            case PATCH:
                HttpPatch patch = new HttpPatch(url);
                patch.setEntity(body);
                request = patch;
                break;
            case HEAD:
                request = new HttpHead(url);
                break;
            case OPTIONS:
                request = new HttpOptions(url);
                break;
            case DELETE:
                request = new HttpDelete(url);
                break;
            default:
                throw new RuntimeException("Invalid HTTP method");
        }
        request.setHeaders(configuration.getHeaders().toArray(new Header[configuration.getHeaders().size()]));

        return request;
    }

    private void executeRequest(HttpRequestBase request) {
        threadPool.execute(() -> {
//            printLine("exec");
            try {
//                RestTemplate restTemplate = new RestTemplate();
                long start = currentTimeMillis();
//                HttpResponse response = httpClient.execute(request, null).get();
                restTemplate.getForEntity(request.getURI(), String.class);
                addResponseTime(currentTimeMillis() - start);
//                printLine("end");
//                int responseStatus = response.getStatusLine().getStatusCode();
//                if (responseStatus < 300) {
//                    numberOf2xx.incrementAndGet();
//                } else if (responseStatus < 400) {
//                    numberOf3xx.incrementAndGet();
//                } else if (responseStatus < 500) {
//                    numberOf4xx.incrementAndGet();
//                } else {
//                    numberOf5xx.incrementAndGet();
//                }
            } catch (Exception e) {
                e.printStackTrace();
                numberOfErrors.incrementAndGet();
            }
        });
//        thread.start();
//        threads.add(thread);
    }

    private void addResponseTime(long responseTime) {
        synchronized (responseTimes) {
            responseTimes.add(responseTime);
        }
    }

    private void waitForResponses() {
        printLine("Waiting for remaining responses...");
//        threads.forEach(thread -> {
//            try {
//                thread.join();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        });
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(1, HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
