package com.github.mkopylec.httpbenchmarktool.core;

import com.github.mkopylec.httpbenchmarktool.config.BenchmarkConfiguration;
import com.github.mkopylec.httpbenchmarktool.config.ProgramArgumentsParser;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.mkopylec.httpbenchmarktool.utils.Printer.printLine;
import static com.google.common.util.concurrent.RateLimiter.create;
import static java.util.Collections.singletonList;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.TimeUnit.HOURS;
import static org.apache.http.impl.nio.client.HttpAsyncClients.createMinimal;
import static org.springframework.http.HttpMethod.POST;

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
    private ResultsCollector resultsCollector;

    public void runBenchmark(String[] arguments) {
        ProgramArgumentsParser argumentsParser = new ProgramArgumentsParser(arguments);
        BenchmarkConfiguration configuration = argumentsParser.getBenchmarkConfiguration();
        HttpRequestBase request = getRequest(configuration);
        RateLimiter rateLimiter = create(configuration.getRequestsPerSecond());
//        threads = new ArrayList<>(configuration.getNumberOfRequests());
//        threadPool = newFixedThreadPool(configuration.getRequestsPerSecond()*10);
        resultsCollector = new ResultsCollector(configuration.getNumberOfRequests());
        threadPool = newCachedThreadPool();
        restTemplate.setInterceptors(singletonList(resultsCollector));
        responseTimes = new ArrayList<>(configuration.getNumberOfRequests());
//        httpClient.start();
        for (int i = 0; i < configuration.getNumberOfRequests(); i++) {
            rateLimiter.acquire();
            executeRequest(request);
        }
        waitForResponses();
    }

    public void printResults() {
        printLine("Calculating statistics...");
//        responseTimes.forEach(statistics::addValue);
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
        printLine("Number of errors: " + resultsCollector.getNumberOfErrors());
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
//            try {
//                RestTemplate restTemplate = new RestTemplate();
//                long start = currentTimeMillis();
//                HttpResponse response = httpClient.execute(request, null).get();
            restTemplate.execute(request.getURI(), POST, null, null);
//                addResponseTime(currentTimeMillis() - start);
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
//            } catch (Exception e) {
//                e.printStackTrace();
//                numberOfErrors.incrementAndGet();
//            }
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
