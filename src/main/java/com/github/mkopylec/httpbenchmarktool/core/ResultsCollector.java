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
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.nanoTime;

public class ResultsCollector implements ClientHttpRequestInterceptor {

    private final List<Long> responseTimes;
    private final AtomicInteger numberOf2xx = new AtomicInteger(0);
    private final AtomicInteger numberOf3xx = new AtomicInteger(0);
    private final AtomicInteger numberOf4xx = new AtomicInteger(0);
    private final AtomicInteger numberOf5xx = new AtomicInteger(0);

    public ResultsCollector(int numberOfRequests) {
        responseTimes = new ArrayList<>(numberOfRequests);
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        ClientHttpResponse response = execution.execute(request, body);
        long start = nanoTime();
        HttpStatus statusCode = response.getStatusCode();
        addResponseTime(nanoTime() - start);
        if (statusCode.is2xxSuccessful()) {
            numberOf2xx.incrementAndGet();
        } else if (statusCode.is3xxRedirection()) {
            numberOf3xx.incrementAndGet();
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

    public int getNumberOf2xx() {
        return numberOf2xx.get();
    }

    public int getNumberOf3xx() {
        return numberOf3xx.get();
    }

    public int getNumberOf4xx() {
        return numberOf4xx.get();
    }

    public int getNumberOf5xx() {
        return numberOf5xx.get();
    }

    private void addResponseTime(long responseTime) {
        synchronized (responseTimes) {
            responseTimes.add(responseTime);
        }
    }
}
