package com.github.mkopylec.httpbenchmarktool.config;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.springframework.http.HttpMethod;

import java.util.List;

public class BenchmarkConfiguration {

    private final String url;
    private final HttpMethod httpMethod;
    private final List<Header> headers;
    private final StringEntity body;
    private final int runTime;
    private final int requestsPerSecond;
    private final int warmUpTime;

    BenchmarkConfiguration(String url, int warmUpTime, int requestsPerSecond, StringEntity body, List<Header> headers, HttpMethod httpMethod, int runTime) {
        this.url = url;
        this.warmUpTime = warmUpTime;
        this.requestsPerSecond = requestsPerSecond;
        this.body = body;
        this.headers = headers;
        this.httpMethod = httpMethod;
        this.runTime = runTime;
    }

    public String getUrl() {
        return url;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public StringEntity getBody() {
        return body;
    }

    public int getRunTime() {
        return runTime;
    }

    public int getRequestsPerSecond() {
        return requestsPerSecond;
    }

    public int getWarmUpTime() {
        return warmUpTime;
    }

    public int getNumberOfRequests() {
        return runTime * requestsPerSecond;
    }
}
