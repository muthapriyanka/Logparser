package com.logparser.processor;

import com.logparser.aggregator.LogAggregator;
import com.logparser.model.ApplicationMetric;

public class ApplicationLogProcessor implements LogProcessor {
    @Override
    public void process(String line, LogAggregator aggregator) {
        
        if (line == null || aggregator == null) {
            return;
        }
        String[] parts = line.split(" ");
        String level = null;

        for (String part : parts) {
            if (part.startsWith("level=")) {
                level = part.split("=")[1];
                break;
            }
        }
        if (level != null) {
            ApplicationMetric applicationMetric = new ApplicationMetric(level);
            aggregator.collectApplicationLog(applicationMetric);
        }
    }
}
