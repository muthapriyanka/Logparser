package com.logparser;


import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.logparser.aggregator.LogAggregator;
import com.logparser.processor.ApplicationLogProcessor;

class AppLogAggregatorTest {
    @Test
    void process_singleErrorLog_updatesCountCorrectly() {
        LogAggregator logAggregator = new LogAggregator();
        ApplicationLogProcessor processor = new ApplicationLogProcessor();
        String logInput = "level=ERROR";

        processor.process(logInput, logAggregator);

        var severityMap = logAggregator.getApplicationLogStats();
        assertEquals(1, severityMap.get("ERROR"));
    }

    @Test
    void process_multipleLogs_accumulatesEachLevel() {
        LogAggregator logAggregator = new LogAggregator();
        ApplicationLogProcessor processor = new ApplicationLogProcessor();

        processor.process("level=ERROR", logAggregator);
        processor.process("level=INFO", logAggregator);
        processor.process("level=ERROR", logAggregator);

        var severityMap = logAggregator.getApplicationLogStats();
        assertEquals(2, severityMap.get("ERROR"));
        assertEquals(1, severityMap.get("INFO"));
    }

    @Test
    void process_nullInput_doesNotCrash() {
        LogAggregator logAggregator = new LogAggregator();
        ApplicationLogProcessor processor = new ApplicationLogProcessor();

        processor.process(null, logAggregator);

        var severityMap = logAggregator.getApplicationLogStats();
        assertEquals(0, severityMap.size());
    }

    @Test
    void process_invalidFormat_ignoresLine() {
        LogAggregator logAggregator = new LogAggregator();
        ApplicationLogProcessor processor = new ApplicationLogProcessor();

        processor.process("nonsense_log_line", logAggregator);

        var severityMap = logAggregator.getApplicationLogStats();
        assertEquals(0, severityMap.size());
    }
}
