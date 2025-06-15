package com.logparser.aggregator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.logparser.model.APMMetric;
import com.logparser.model.ApplicationMetric;
import com.logparser.model.RequestMetric;

public class LogAggregator {
    private final Map<String, List<Double>> apmMetrics = new HashMap<>();
    private final Map<String, Integer> appLogLevels = new HashMap<>();
    private final Map<String, List<Integer>> requestData = new HashMap<>();
    private final Map<String, Map<String, Integer>> apiStatusGroups = new HashMap<>();

    public Map<String, Integer> getApplicationLogStats() {
        return appLogLevels;
    }

    public void collectApm(APMMetric metric) {
        if (metric == null) return;

        String metricName = metric.getMetric();
        double value = metric.getValue();

        apmMetrics.computeIfAbsent(metricName, k -> new ArrayList<>()).add(value);
    }

    public void collectApplicationLog(ApplicationMetric metric) {
        if (metric == null) return;

        String level = metric.getLevel();
        appLogLevels.put(level, appLogLevels.getOrDefault(level, 0) + 1);
    }

    public void collectRequestLog(RequestMetric metric) {
        if (metric == null || metric.getResponseTime() <= 0 || metric.getUrl() == null) {
            return;
        }

        String url = metric.getUrl();
        int responseTime = metric.getResponseTime();
        int statusCode = metric.getStatusCode();

        requestData.computeIfAbsent(url, k -> new ArrayList<>()).add(responseTime);

        String codeCategory = mapStatusCode(statusCode);
        apiStatusGroups.computeIfAbsent(url, k -> new HashMap<>())
                .put(codeCategory, apiStatusGroups.get(url).getOrDefault(codeCategory, 0) + 1);
    }

    private String mapStatusCode(int statusCode) {
        if (statusCode >= 200 && statusCode < 300) return "2XX";
        if (statusCode >= 400 && statusCode < 500) return "4XX";
        if (statusCode >= 500 && statusCode < 600) return "5XX";
        return "UNKNOWN";
    }

    public void exportAll() {
        writeToFile("apm.json", summarizeApm());
        writeToFile("application.json", appLogLevels);
        writeToFile("request.json", summarizeRequests());
    }

    public Map<String, Map<String, Double>> summarizeApm() {
        Map<String, Map<String, Double>> aggregatedData = new HashMap<>();
        for (Map.Entry<String, List<Double>> entry : apmMetrics.entrySet()) {
            String metric = entry.getKey();
            List<Double> values = entry.getValue();

            if (values.isEmpty()) continue;

            Collections.sort(values);

            Map<String, Double> stats = new HashMap<>();
            stats.put("minimum", roundToTwoDecimalPlaces(values.get(0)));
            stats.put("median", calcMedian(values));
            stats.put("average", roundToTwoDecimalPlaces(values.stream().mapToDouble(d -> d).average().orElse(0.0)));
            stats.put("max", roundToTwoDecimalPlaces(values.get(values.size() - 1)));

            aggregatedData.put(metric, stats);
        }
        return aggregatedData;
    }

    private double calcMedian(List<Double> values) {
        int size = values.size();
        if (size == 0) return 0.0;

        if (size % 2 == 1) {
            return roundToTwoDecimalPlaces(values.get(size / 2));
        } else {
            double middle1 = values.get(size / 2 - 1);
            double middle2 = values.get(size / 2);
            return roundToTwoDecimalPlaces((middle1 + middle2) / 2.0);
        }
    }

    public Map<String, Map<String, Object>> summarizeRequests() {
        Map<String, Map<String, Object>> aggregatedData = new HashMap<>();

        for (String url : requestData.keySet()) {
            List<Integer> responseTimes = requestData.get(url);
            Collections.sort(responseTimes);

            Map<String, Object> urlStats = new HashMap<>();
            urlStats.put("response_times", calcPercentiles(responseTimes));

            urlStats.put("status_codes", fillMissingCodes(url));

            aggregatedData.put(url, urlStats);
        }

        return aggregatedData;
    }

    private Map<String, Double> calcPercentiles(List<Integer> values) {
        Map<String, Double> percentiles = new LinkedHashMap<>(); // Use LinkedHashMap for order

        percentiles.put("50_percentile", roundToTwoDecimalPlaces(getPercentile(values, 50))); // 50th Percentile
        percentiles.put("90_percentile", roundToTwoDecimalPlaces(getPercentile(values, 90))); // 90th Percentile
        percentiles.put("95_percentile", roundToTwoDecimalPlaces(getPercentile(values, 95))); // 95th Percentile
        percentiles.put("99_percentile", roundToTwoDecimalPlaces(getPercentile(values, 99))); // 99th Percentile
        percentiles.put("min", roundToTwoDecimalPlaces((double) values.get(0)));             // Minimum
        percentiles.put("max", roundToTwoDecimalPlaces((double) values.get(values.size() - 1))); // Maximum

        return percentiles;
    }

    private double getPercentile(List<Integer> values, int percentile) {
        if (values.isEmpty()) {
            return 0.0;
        }

        Collections.sort(values);

        double rank = (percentile / 100.0) * (values.size() - 1);
        int lowerIndex = (int) Math.floor(rank);
        int upperIndex = (int) Math.ceil(rank);

        if (lowerIndex == upperIndex) {
            return values.get(lowerIndex);
        }

        double lowerValue = values.get(lowerIndex);
        double upperValue = values.get(upperIndex);
        double fraction = rank - lowerIndex;

        return roundToTwoDecimalPlaces(lowerValue + fraction * (upperValue - lowerValue));
    }

    private Map<String, Integer> fillMissingCodes(String url) {
        Map<String, Integer> codeCounts = apiStatusGroups.getOrDefault(url, new HashMap<>());

        codeCounts.putIfAbsent("2XX", 0);
        codeCounts.putIfAbsent("4XX", 0);
        codeCounts.putIfAbsent("5XX", 0);

        return codeCounts;
    }

    private void writeToFile(String filename, Map<String, ?> data) {
        try (FileWriter file = new FileWriter(System.getProperty("user.dir") + "/" + filename)) {
            JSONObject jsonObject = new JSONObject(data);
            String formattedJson = jsonObject.toString(4);
            file.write(formattedJson);
            System.out.println("Successfully written to " + filename);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + filename);
            e.printStackTrace();
        }
    }

    private double roundToTwoDecimalPlaces(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

