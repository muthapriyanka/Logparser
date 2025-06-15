package com.logparser.processor;

import com.logparser.aggregator.LogAggregator;

public interface LogProcessor {
    void process(String line, LogAggregator aggregator);
}
