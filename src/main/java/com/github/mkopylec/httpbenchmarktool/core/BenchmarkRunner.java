package com.github.mkopylec.httpbenchmarktool.core;

import com.github.mkopylec.httpbenchmarktool.config.BenchmarkConfiguration;
import com.github.mkopylec.httpbenchmarktool.config.ProgramArgumentsParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import static java.lang.System.currentTimeMillis;
import static org.apache.http.impl.nio.client.HttpAsyncClients.createMinimal;

public class BenchmarkRunner {



    public void runBenchmark(String[] arguments) {
        ProgramArgumentsParser argumentsParser = new ProgramArgumentsParser(arguments);
        BenchmarkConfiguration configuration = argumentsParser.getBenchmarkConfiguration();
        HttpRequestBase request = getRequest(configuration);

        executeRequest(request);
    }

    private HttpRequestBase getRequest(BenchmarkConfiguration configuration) {
        return null;
    }

    private void executeRequest(HttpRequestBase request) {
        long start = currentTimeMillis();
        createMinimal().execute(request, new FutureCallback<HttpResponse>() {

            @Override
            public void completed(HttpResponse result) {

            }

            @Override
            public void failed(Exception ex) {

            }

            @Override
            public void cancelled() {

            }
        });
    }
}
