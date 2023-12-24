package ru.netology.server;

import org.apache.http.NameValuePair;

import java.util.ArrayList;
import java.util.List;

public class Request {
    private String method;
    private String path;
    private String protocol;
    private final List<String> headers = new ArrayList<>();
    private final List<NameValuePair> queryParams = new ArrayList<>();
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

    public List<NameValuePair> getQueryParams() {
        return queryParams;
    }

    public List<NameValuePair> getQueryParam(String name) {
        var params = new ArrayList<NameValuePair>();
        for (NameValuePair param : queryParams) {
            if (param.getName().equals(name)) {
                params.add(param);
            }
        }
        return params;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void addHeader(String header) {
        this.headers.add(header);
    }

    public void addQueryParams(List<NameValuePair> pairList) {
        this.queryParams.addAll(pairList);
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return method + " " + path + " " + protocol + '\n' +
                "queryParams" + queryParams +
                "headers:\n" + headers + '\n' +
                "body:\n" + body + '\n' +
                '}';
    }
}
