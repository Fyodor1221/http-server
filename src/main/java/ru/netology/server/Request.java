package ru.netology.server;

import java.util.ArrayList;
import java.util.List;

public class Request {
    private String method;
    private String path;
    private String protocol;
    private final List<String> headers = new ArrayList<>();
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

    public List<String> getHeaders() {
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

    public Request addHeader(String header) {
        this.headers.add(header);
        return this;
    }

    public Request setBody(String body) {
        this.body = body;
        return this;
    }

    @Override
    public String toString() {
        return method + " " + path + " " + protocol + '\n' +
                "headers:\n" + headers + '\n' +
                "body:\n" + body + '\n' +
                '}';
    }
}
