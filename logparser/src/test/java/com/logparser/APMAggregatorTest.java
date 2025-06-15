package com.logparser;

import java.util.Map;

import org.junit.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;

import com.logparser.aggregator.LogAggregator;
import com.logparser.processor.APMLogProcessor;

import static junit.framework.Assert.assertEquals;

public class APMAggregatorTest {

    private LogAggregator logAggregator;
    private APMLogProcessor apmProcessor;

    @Before
    public void init() {
        logAggregator = new LogAggregator();
        apmProcessor = new APMLogProcessor();
    }

    @Test
    public void shouldHandleValidSingleAPMEntry() {
        String logEntry = "metric=cpu_usage_percent value=75.5";
        apmProcessor.process(logEntry, logAggregator);

        var metricSummary = logAggregator.summarizeApm();
        assertTrue(metricSummary.containsKey("cpu_usage_percent"));
        assertEquals(75.5, metricSummary.get("cpu_usage_percent").get("average"), 0.01);
    }

    @Test
   public void shouldAggregateMultipleApmMetricsCorrectly() {
        apmProcessor.process("metric=cpu_usage_percent value=75.5", logAggregator);
        apmProcessor.process("metric=cpu_usage_percent value=85.0", logAggregator);
        apmProcessor.process("metric=memory_usage_percent value=60.0", logAggregator);

        var metricSummary = logAggregator.summarizeApm();
        Map<String, Double> cpuUsage = metricSummary.get("cpu_usage_percent");
        Map<String, Double> memoryUsage = metricSummary.get("memory_usage_percent");

        assertNotNull(cpuUsage);
        assertNotNull(memoryUsage);
        assertEquals(80.25, cpuUsage.get("average"), 0.01);
        assertEquals(60.0, memoryUsage.get("average"), 0.01);
    }

    @Test
   public void shouldIgnoreMalformedApmLine() {
        String malformedInput = "invalid_format";
        apmProcessor.process(malformedInput, logAggregator);

        assertTrue(logAggregator.summarizeApm().isEmpty());
    }

    @Test
    public void shouldIgnoreNullApmLine() {
        apmProcessor.process(null, logAggregator);

        assertTrue(logAggregator.summarizeApm().isEmpty());
    }

    @Test
   public void shouldCaptureExtremesForMetricValues() {
        apmProcessor.process("metric=cpu_usage_percent value=0.0", logAggregator);
        apmProcessor.process("metric=cpu_usage_percent value=100.0", logAggregator);

        var metricSummary = logAggregator.summarizeApm();
        Map<String, Double> cpuUsage = metricSummary.get("cpu_usage_percent");

        assertEquals(0.0, cpuUsage.get("minimum"));
        assertEquals(100.0, cpuUsage.get("max"));
    }
}
