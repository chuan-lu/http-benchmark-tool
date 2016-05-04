package com.github.mkopylec.httpbenchmarktool.config;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import static com.github.mkopylec.httpbenchmarktool.config.BenchmarkConfiguration.HttpMethod.valueOf;
import static com.github.mkopylec.httpbenchmarktool.config.BenchmarkConfigurationBuilder.aBenchmarkConfiguration;
import static java.lang.Integer.parseInt;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.upperCase;

public class ProgramArgumentsParser {

    private static final String HTTP_URL = "u";
    private static final String HTTP_URL_LONG = "url";
    private static final String DEFAULT_HTTP_URL = "http://localhost:8080";

    private static final String HTTP_METHOD = "m";
    private static final String HTTP_METHOD_LONG = "method";
    private static final String DEFAULT_HTTP_METHOD = "GET";

    private static final String HTTP_HEADER = "h";
    private static final String HTTP_HEADER_LONG = "header";

    private static final String HTTP_BODY = "b";
    private static final String HTTP_BODY_LONG = "body";

    private static final String RUN_TIME = "t";
    private static final String RUN_TIME_LONG = "time";
    private static final String DEFAULT_RUN_TIME = "30";

    private static final String REQUESTS_PER_SECOND = "r";
    private static final String REQUESTS_PER_SECOND_LONG = "rps";
    private static final String DEFAULT_REQUESTS_PER_SECOND = "400";

    private static final String WARM_UP_TIME = "w";
    private static final String WARM_UP_TIME_LONG = "warmup";
    private static final String DEFAULT_WARM_UP_TIME = "10";

    private final CommandLine commandLine;

    public ProgramArgumentsParser(String[] arguments) {
        Options options = new Options();
        options.addOption(HTTP_URL, HTTP_URL_LONG, true, "HTTP request URL. Default is " + DEFAULT_HTTP_URL);
        options.addOption(HTTP_METHOD, HTTP_METHOD_LONG, true, "HTTP request method. Default is " + DEFAULT_HTTP_METHOD);
        options.addOption(HTTP_HEADER, HTTP_HEADER_LONG, true, "HTTP request header in name:value format.");
        options.addOption(HTTP_BODY, HTTP_BODY_LONG, true, "HTTP request body. Default is empty");
        options.addOption(RUN_TIME, RUN_TIME_LONG, true, "Benchmark run time. Default is " + DEFAULT_RUN_TIME);
        options.addOption(REQUESTS_PER_SECOND, REQUESTS_PER_SECOND_LONG, true, "Requests per second. Default is " + DEFAULT_REQUESTS_PER_SECOND);
        options.addOption(WARM_UP_TIME, WARM_UP_TIME_LONG, true, "JVM warm up time is seconds. During warm up no metrics are taken. Default is " + DEFAULT_WARM_UP_TIME);
        try {
            commandLine = new DefaultParser().parse(options, arguments);
        } catch (ParseException e) {
            throw new RuntimeException("Error parsing program arguments", e);
        }
    }

    public BenchmarkConfiguration getBenchmarkConfiguration() {
        return aBenchmarkConfiguration()
                .withHttpUrl(getHttpUrl())
                .withHttpMethod(valueOf(getHttpMethod()))
                .withHeaders(emptyList())//TODO Implement reading headers from agrs
                .withBody(getHttpBody())
                .withRunTime(getRunTime())
                .withRequestsPerSecond(getRequestsPerSecond())
                .withWarmUpTime(getWarmUpTime())
                .build();
    }

    private String getHttpUrl() {
        return getArgumentValue(HTTP_URL, HTTP_URL_LONG, DEFAULT_HTTP_URL);
    }

    private String getHttpMethod() {
        return upperCase(getArgumentValue(HTTP_METHOD, HTTP_METHOD_LONG, DEFAULT_HTTP_METHOD));
    }

    private String getHttpBody() {
        return getArgumentValue(HTTP_BODY, HTTP_BODY_LONG, null);
    }

    private int getRunTime() {
        return parseInt(getArgumentValue(RUN_TIME, RUN_TIME_LONG, DEFAULT_RUN_TIME));
    }

    private int getRequestsPerSecond() {
        return parseInt(getArgumentValue(REQUESTS_PER_SECOND, REQUESTS_PER_SECOND_LONG, DEFAULT_REQUESTS_PER_SECOND));
    }

    private int getWarmUpTime() {
        return parseInt(getArgumentValue(WARM_UP_TIME, WARM_UP_TIME_LONG, DEFAULT_WARM_UP_TIME));
    }

    private String getArgumentValue(String name, String longName, String defaultValue) {
        String value = commandLine.getOptionValue(name);
        if (isNotBlank(value)) {
            return value;
        }
        return commandLine.getOptionValue(longName, defaultValue);
    }
}
