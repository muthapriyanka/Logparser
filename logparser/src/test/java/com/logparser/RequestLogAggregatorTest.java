package com.logparser;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.logparser.aggregator.LogAggregator;
import com.logparser.processor.RequestLogProcessor;

class RequestLogAggregatorTest {
   
    @Test
    void shouldTrackSingleRequestLogCorrectly() {
        LogAggregator logAggregator = new LogAggregator();
        RequestLogProcessor logHandler = new RequestLogProcessor();

        String logEntry = "request_url=\"/api/test\" response_time_ms=120 response_status=200";
        logHandler.process(logEntry, logAggregator);

        var requestStats = logAggregator.summarizeRequests();
        assertTrue(requestStats.containsKey("/api/test"));

        var timingStats = (Map<String, Double>) requestStats.get("/api/test").get("response_times");
        assertEquals(120, timingStats.get("min"));
    }

    @Test
    void shouldSummarizeMultipleRequestLogs() {
        LogAggregator logAggregator = new LogAggregator();
        RequestLogProcessor logHandler = new RequestLogProcessor();

        logHandler.process("request_url=\"/api/test\" response_time_ms=100 response_status=200", logAggregator);
        logHandler.process("request_url=\"/api/test\" response_time_ms=200 response_status=404", logAggregator);

        var requestStats = logAggregator.summarizeRequests();
        var timingStats = (Map<String, Double>) requestStats.get("/api/test").get("response_times");

        assertEquals(100, timingStats.get("min"));
        assertEquals(200, timingStats.get("max"));
        assertEquals(150, timingStats.get("50_percentile"));
    }

    @Test
    void shouldIgnoreMalformedRequestLog() {
        LogAggregator logAggregator = new LogAggregator();
        RequestLogProcessor logHandler = new RequestLogProcessor();

        logHandler.process("invalid_format", logAggregator);

        assertTrue(logAggregator.summarizeRequests().isEmpty());
    }

    @Test
    void shouldSkipRequestLogWithMissingFields() {
        LogAggregator logAggregator = new LogAggregator();
        RequestLogProcessor logHandler = new RequestLogProcessor();

        logHandler.process("request_url=\"/api/test\" response_status=200", logAggregator);

        var requestStats = logAggregator.summarizeRequests();
        assertFalse(requestStats.containsKey("/api/test"));
    }
}