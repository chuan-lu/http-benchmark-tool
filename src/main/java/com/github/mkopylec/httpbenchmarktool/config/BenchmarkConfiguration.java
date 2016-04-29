package com.github.mkopylec.httpbenchmarktool.config;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;

import java.util.List;

public class BenchmarkConfiguration {

    private final String url;
    private final HttpMethod httpMethod;
    private final List<Header> headers;
    private final StringEntity body;
    private final int requestsPerSecond;
    private final int warmUpTime;

    BenchmarkConfiguration(String url, int warmUpTime, int requestsPerSecond, StringEntity body, List<Header> headers, HttpMethod httpMethod) {
        this.url = url;
        this.warmUpTime = warmUpTime;
        this.requestsPerSecond = requestsPerSecond;
        this.body = body;
        this.headers = headers;
        this.httpMethod = httpMethod;
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

    public int getRequestsPerSecond() {
        return requestsPerSecond;
    }

    public int getWarmUpTime() {
        return warmUpTime;
    }

    public enum HttpMethod {

        GET, POST, PUT, HEAD, OPTION, DELETE;
    }
}
