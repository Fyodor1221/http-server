package ru.netology.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {
    private String method;
    private String path;
    private String protocol;
    private Map<String, String> headers = new HashMap<>();
    private String body = null;

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getProtocol() {
        return protocol;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public Request setMethod(String method) {
        this.method = method;
        return this;
    }

    public Request setPath(String path) {
        this.path = path;
        return this;
    }

    public Request setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public Request addHeader(String headerType, String headerValue) {
        this.headers.put(headerType, headerValue);
        return this;
    }

    public Request setBody(String body) {
        this.body = body;
        return this;
    }

    @Override
    public String toString() {
        List<String> headerList = null;
        for (String headerType : headers.keySet()) {
            headerList.add(headerType + ": " + headers.get(headerType) + '\n');
        }
        return "Request{\n" +
                method + " " + path + " " + protocol + '\n' +
                "headers\n" + headerList + '\n' +
                "body" + body +
                '}';
    }
}
