package com.logparser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.logparser.aggregator.LogAggregator;
import com.logparser.processor.LogProcessor;
import com.logparser.processor.LogProcessorFactory;

public class Main {

public static void main(String[] args) {
 System.out.println(">>> [CI/CD DEBUG] Logparser main started...");
    
    if (args.length < 2 || !args[0].equals("--file")) {
        System.out.println("Usage: --file <filename>");
        return;
    }

    String filename = args[1];

    LogAggregator aggregator = new LogAggregator();

    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
        String line;
        StringBuilder multiLineLog = new StringBuilder();

        while ((line = br.readLine()) != null) {

            if (isPartOfMultiLineLog(line)) {
                multiLineLog.append(" ").append(line.trim());
                continue;
            }

            if (multiLineLog.length() > 0) {
                processLine(multiLineLog.toString().trim(), aggregator);
                multiLineLog.setLength(0);
            }

            processLine(line.trim(), aggregator);
        }

        if (multiLineLog.length() > 0) {
            processLine(multiLineLog.toString().trim(), aggregator);
        }
    } catch (IOException e) {
        e.printStackTrace();
    }

    aggregator.exportAll();
}

    private static boolean isPartOfMultiLineLog(String line) {
        return !line.startsWith("timestamp=") && !line.contains("=");
    }

    private static void processLine(String line, LogAggregator aggregator) {
        String type = determineLogType(line);

        if (type != null) {
            LogProcessor handler = LogProcessorFactory.getProcessor(type);
            if (handler != null) {
                handler.process(line, aggregator);
            }
        }
    }

    private static String determineLogType(String line) {
        if (line.contains("metric=")) return "APM";
        if (line.contains("level=")) return "APPLICATION";
        if (line.contains("request_url=")) return "REQUEST";
        return null;
    }
}
