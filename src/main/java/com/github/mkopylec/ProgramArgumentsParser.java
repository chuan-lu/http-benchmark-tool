package com.github.mkopylec;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class ProgramArgumentsParser {

    private static final String HTTP_METHOD = "m";
    private static final String HTTP_METHOD_LONG = "method";

    private static final String HTTP_HEADER = "h";
    private static final String HTTP_HEADER_LONG = "header";

    private static final String HTTP_BODY = "b";
    private static final String HTTP_BODY_LONG = "body";

    private final CommandLine commandLine;

    public ProgramArgumentsParser(String[] arguments) throws ParseException {
        Options options = new Options();
        options.addOption(HTTP_METHOD, HTTP_METHOD_LONG, true, "HTTP request method");
        options.addOption(HTTP_HEADER, HTTP_HEADER_LONG, true, "HTTP request header");
        options.addOption(HTTP_BODY, HTTP_BODY_LONG, true, "HTTP request body");
        commandLine = new DefaultParser().parse(options, arguments);
    }

    public String getHttpMethod() {
        return getArgumentValue(HTTP_METHOD, HTTP_METHOD_LONG);
    }

    private String getArgumentValue(String name, String longName) {
        String value = commandLine.getOptionValue(name);
        if (value != null) {
            return value;
        }
        return commandLine.getOptionValue(longName);
    }
}
