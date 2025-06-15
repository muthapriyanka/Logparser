package com.logparser.processor;

import com.logparser.aggregator.LogAggregator;
import com.logparser.model.APMMetric;

public class APMLogProcessor implements LogProcessor {
    @Override
    public void process(String line, LogAggregator aggregator) {
        if (line == null || aggregator == null) return;

        String[] parts = line.split(" ");
        String metric = null;
        double value = 0;

        for (String part : parts) {
            if (part.startsWith("metric=")) {
                metric = part.split("=", 2)[1];
            } else if (part.startsWith("value=")) {
                try {
                    value = Double.parseDouble(part.split("=", 2)[1]);
                } catch (NumberFormatException e) {
                    value = 0;
                }
            }
        }
       
        if (metric != null) {
            APMMetric apmMetric = new APMMetric(metric, value);
            aggregator.collectApm(apmMetric);
        }
    }
}
