package com.github.mkopylec.httpbenchmarktool.config;

import com.github.mkopylec.httpbenchmarktool.config.BenchmarkConfiguration.HttpMethod;
import org.apache.http.Header;
import org.apache.http.entity.StringEntity;

import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.Charset.forName;

class BenchmarkConfigurationBuilder {

    private String url;
    private HttpMethod httpMethod;
    private List<Header> headers = new ArrayList<>();
    private StringEntity body;
    private int runTime;
    private int requestsPerSecond;
    private int warmUpTime;

    private BenchmarkConfigurationBuilder() {
    }

    public static BenchmarkConfigurationBuilder aBenchmarkConfiguration() {
        return new BenchmarkConfigurationBuilder();
    }

    public BenchmarkConfigurationBuilder withHttpUrl(String url) {
        this.url = url;
        return this;
    }

    public BenchmarkConfigurationBuilder withHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    public BenchmarkConfigurationBuilder withHeaders(List<Header> headers) {
        this.headers = headers;
        return this;
    }

    public BenchmarkConfigurationBuilder withBody(String body) {
        if (body != null) {
            this.body = new StringEntity(body, forName("UTF-8"));
        }
        return this;
    }

    public BenchmarkConfigurationBuilder withRunTime(int runTime) {
        this.runTime = runTime;
        return this;
    }

    public BenchmarkConfigurationBuilder withRequestsPerSecond(int requestsPerSecond) {
        this.requestsPerSecond = requestsPerSecond;
        return this;
    }

    public BenchmarkConfigurationBuilder withWarmUpTime(int warmUpTime) {
        this.warmUpTime = warmUpTime;
        return this;
    }

    public BenchmarkConfiguration build() {
        return new BenchmarkConfiguration(url, warmUpTime, requestsPerSecond, body, headers, httpMethod, runTime);
    }
}
