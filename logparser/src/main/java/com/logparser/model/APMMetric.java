package com.logparser.model;

public class APMMetric {
    private String metric;
    private double val;

    public APMMetric(String metric, double val) {
        this.metric = metric;
        this.val = val;
    }

    public String getMetric() {
        return metric;
    }

    public double getValue() {
        return val;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public void setValue(double value) {
        this.val = value;
    }
}
