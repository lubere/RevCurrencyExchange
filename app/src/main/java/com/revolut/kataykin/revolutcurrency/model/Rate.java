package com.revolut.kataykin.revolutcurrency.model;

import android.support.annotation.NonNull;

public class Rate {
    private String keyFrom;
    private String keyTo;
    private double rate;

    public Rate(String keyFrom, String keyTo, double rate) {
        this.keyFrom = keyFrom;
        this.keyTo = keyTo;
        this.rate = rate;
    }

    public String getKeyFrom() {
        return keyFrom;
    }

    public void setKeyFrom(String keyFrom) {
        this.keyFrom = keyFrom;
    }

    public String getKeyTo() {
        return keyTo;
    }

    public void setKeyTo(String keyTo) {
        this.keyTo = keyTo;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    @NonNull
    @Override
    public String toString() {
        return "Rate{" +
                "keyFrom='" + keyFrom + '\'' +
                ", keyTo='" + keyTo + '\'' +
                ", rate=" + rate +
                '}';
    }
}
