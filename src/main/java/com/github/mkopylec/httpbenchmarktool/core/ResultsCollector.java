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

import static java.lang.System.currentTimeMillis;

public class ResultsCollector implements ClientHttpRequestInterceptor {

    private final List<Long> responseTimes;
    private final AtomicInteger numberOf2xx = new AtomicInteger(0);
    private final AtomicInteger numberOf3xx = new AtomicInteger(0);
    private final AtomicInteger numberOf4xx = new AtomicInteger(0);
    private final AtomicInteger numberOf5xx = new AtomicInteger(0);
    private final AtomicInteger numberOfErrors = new AtomicInteger(0);

    public ResultsCollector(int numberOfRequests) {
        responseTimes = new ArrayList<>(numberOfRequests);
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        ClientHttpResponse response = null;
        long start = currentTimeMillis();
        try {
            response = execution.execute(request, body);
            HttpStatus statusCode = response.getStatusCode();
            if (statusCode.is2xxSuccessful()) {
                numberOf2xx.incrementAndGet();
            } else if (statusCode.is3xxRedirection()) {
                numberOf3xx.incrementAndGet();
            } else if (statusCode.is4xxClientError()) {
                numberOf4xx.incrementAndGet();
            } else {
                numberOf5xx.incrementAndGet();
            }
        } catch (Exception ex) {
            numberOfErrors.incrementAndGet();
        }
        addResponseTime(currentTimeMillis() - start);
        return response;
    }

    public DescriptiveStatistics getResponseTimesStatistics() {
        DescriptiveStatistics statistics = new DescriptiveStatistics();
        responseTimes.forEach(statistics::addValue);
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

    public int getNumberOfErrors() {
        return numberOfErrors.get();
    }

    private void addResponseTime(long responseTime) {
        synchronized (responseTimes) {
            responseTimes.add(responseTime);
        }
    }
}
