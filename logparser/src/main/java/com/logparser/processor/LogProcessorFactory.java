package com.logparser.processor;

public class LogProcessorFactory {
    public static LogProcessor getProcessor(String line) {
        switch (line) {
            case "APM":
                return new APMLogProcessor();
            case "APPLICATION":
                return new ApplicationLogProcessor();
            case "REQUEST":
                return new RequestLogProcessor();
            default:
                return null;
        }
    }
}
