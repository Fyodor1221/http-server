package ru.netology.server;

import org.apache.http.NameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
        if (this.queryParams.size() < 1) {
            System.out.println("There are no QUERY params");
            return null;
        }
        return queryParams;
    }

    public List<NameValuePair> getQueryParam(String name) {
        if (this.queryParams.size() < 1) {
            System.out.println("There are no QUERY params");
            return null;
        }
        var params = new ArrayList<NameValuePair>();
        for (NameValuePair param : queryParams) {
            if (param.getName().equals(name)) {
                params.add(param);
            }
        }
        return params;
    }

    public List<String> getPostParams() {
        if (!this.getHeaders().contains("Content-Type: application/x-www-form-urlencoded")) {
            System.out.println("Content-type must be \"application/x-www-form-urlencoded\"");
            return null;
        }
        return Arrays.asList(this.getBody().split("&"));
    }

    public List<String> getPostParam(String name) throws IOException {
        if (!this.getHeaders().contains("Content-Type: application/x-www-form-urlencoded")) {
            System.out.println("Content-type must be \"application/x-www-form-urlencoded\"");
            return null;
        }
        var postParam = new ArrayList<String>();
        char[] value = new char[name.length()];
        for (String param : this.getPostParams()) {
            if (param.startsWith(name)) {
                param.getChars(name.length() + 1, param.length(), value, 0);
                postParam.add(new String(value).trim());
            }
        }
        return postParam;
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
        return "method: " + method + '\n' +
                "path: " + path + '\n' +
                "protocol: " + protocol + '\n' +
                "queryParams: " + queryParams + '\n' +
                "headers: " + headers + '\n' +
                "body: " + body + '\n' +
                "------------";
    }
}
