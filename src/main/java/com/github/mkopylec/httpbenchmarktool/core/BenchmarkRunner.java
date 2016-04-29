package com.github.mkopylec.httpbenchmarktool.core;

import com.github.mkopylec.httpbenchmarktool.config.BenchmarkConfiguration;
import com.github.mkopylec.httpbenchmarktool.config.ProgramArgumentsParser;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.mkopylec.httpbenchmarktool.utils.Printer.printLine;
import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.sleep;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.HOURS;
import static org.apache.http.impl.nio.client.HttpAsyncClients.createMinimal;

public class BenchmarkRunner {

    private final ExecutorService threadPool = newFixedThreadPool(1000);
    private final CloseableHttpAsyncClient httpClient = createMinimal();

    private final List<Long> responseTimes = new ArrayList<>(1000000);
    private final AtomicInteger numberOf2xx = new AtomicInteger(0);
    private final AtomicInteger numberOf3xx = new AtomicInteger(0);
    private final AtomicInteger numberOf4xx = new AtomicInteger(0);
    private final AtomicInteger numberOf5xx = new AtomicInteger(0);
    private final AtomicInteger numberOfErrors = new AtomicInteger(0);

    public void runBenchmark(String[] arguments) {
        ProgramArgumentsParser argumentsParser = new ProgramArgumentsParser(arguments);
        BenchmarkConfiguration configuration = argumentsParser.getBenchmarkConfiguration();
        HttpRequestBase request = getRequest(configuration);
        httpClient.start();

        for (int i = 0; i < configuration.getRunTime() * configuration.getRequestsPerSecond(); i++) {
            executeRequest(request);
            try {
                sleep(1000 / configuration.getRequestsPerSecond());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            threadPool.awaitTermination(1, HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void printResults() {
        printLine();
        printLine("======== BENCHMARK RESULTS ========");
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
        threadPool.submit(() -> {
            long start = currentTimeMillis();
            try {
                HttpResponse response = httpClient.execute(request, null).get();
                addResponseTime(currentTimeMillis() - start);
                int responseStatus = response.getStatusLine().getStatusCode();
                if (responseStatus < 300) {
                    numberOf2xx.incrementAndGet();
                } else if (responseStatus < 400) {
                    numberOf3xx.incrementAndGet();
                } else if (responseStatus < 500) {
                    numberOf4xx.incrementAndGet();
                } else {
                    numberOf5xx.incrementAndGet();
                }
            } catch (Exception e) {
                e.printStackTrace();
                numberOfErrors.incrementAndGet();
            }
        });
    }

    private void addResponseTime(long responseTime) {
        synchronized (responseTimes) {
            responseTimes.add(responseTime);
        }
    }
}
