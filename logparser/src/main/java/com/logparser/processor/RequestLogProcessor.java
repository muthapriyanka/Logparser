package com.logparser.processor;

import com.logparser.aggregator.LogAggregator;
import com.logparser.model.RequestMetric;

public class RequestLogProcessor implements LogProcessor {
    @Override
    public void process(String line, LogAggregator aggregator) {
        if (line == null || aggregator == null) {
            return;
        }

        String url = null;
        Integer responseTime = null;
        Integer statusCode = null;

        String[] parts = line.split(" ");

        for (String part : parts) {
            if (part.startsWith("request_url=")) {
                url = part.split("=")[1].replaceAll("\"", "");
            } else if (part.startsWith("response_time_ms=")) {
                try {
                    responseTime = Integer.parseInt(part.split("=")[1]);
                } catch (NumberFormatException e) {

                }
            } else if (part.startsWith("response_status=")) {
                try {
                    statusCode = Integer.parseInt(part.split("=")[1]);
                } catch (NumberFormatException e) {

                }
            }
        }

        if (url == null || responseTime == null) {
            return;
        }

        RequestMetric requestMetric = new RequestMetric(url, responseTime, statusCode == null ? 0 : statusCode);
        aggregator.collectRequestLog(requestMetric);
    }
}

