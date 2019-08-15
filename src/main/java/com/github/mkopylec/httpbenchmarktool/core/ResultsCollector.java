package com.github.mkopylec.httpbenchmarktool.core;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.nanoTime;

public class ResultsCollector implements ClientHttpRequestInterceptor {

    private final ConcurrentLinkedQueue<Long> responseTimes;
    private final AtomicInteger numberOf200 = new AtomicInteger(0);
    private final AtomicInteger numberOf204 = new AtomicInteger(0);
    private final AtomicInteger numberOf4xx = new AtomicInteger(0);
    private final AtomicInteger numberOf5xx = new AtomicInteger(0);

    public ResultsCollector(int numberOfRequests) {
        responseTimes = new ConcurrentLinkedQueue<>();
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        long start = nanoTime();
        ClientHttpResponse response = execution.execute(request, body);
        HttpStatus statusCode = response.getStatusCode();
        addResponseTime(nanoTime() - start);
        if (statusCode == HttpStatus.OK) {
            numberOf200.incrementAndGet();
        } else if (statusCode == HttpStatus.NO_CONTENT) {
            numberOf204.incrementAndGet();
        } else if (statusCode.is4xxClientError()) {
            numberOf4xx.incrementAndGet();
        } else {
            numberOf5xx.incrementAndGet();
        }
        return response;
    }

    public DescriptiveStatistics getResponseTimesStatistics() {
        DescriptiveStatistics statistics = new DescriptiveStatistics();
        responseTimes.forEach(v -> statistics.addValue(v / 1000000));
        return statistics;
    }

    public int getNumberOfCollectedResponses() {
        return responseTimes.size();
    }

    public int getNumberOf200() {
        return numberOf200.get();
    }

    public int getNumberOf204() {
        return numberOf204.get();
    }

    public int getNumberOf4xx() {
        return numberOf4xx.get();
    }

    public int getNumberOf5xx() {
        return numberOf5xx.get();
    }

    private void addResponseTime(long responseTime) {
		responseTimes.add(responseTime);
    }
}
