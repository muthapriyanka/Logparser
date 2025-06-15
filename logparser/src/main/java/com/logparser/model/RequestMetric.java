package com.logparser.model;

public class RequestMetric {
    private String url;
    private int resTime;
    private int statusCode;

    public RequestMetric(String url, int resTime, int statusCode) {
        this.url = url;
        this.resTime = resTime;
        this.statusCode = statusCode;
    }

    public String getUrl() {
        return url;
    }

    public int getResponseTime() {
        return resTime;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setResponseTime(int responseTime) {
        this.resTime = responseTime;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}

