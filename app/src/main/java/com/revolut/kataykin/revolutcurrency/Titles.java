package com.revolut.kataykin.revolutcurrency;

import java.text.DecimalFormat;

class Titles {
    static boolean logEnabled = true;

    private DecimalFormat df;

    public DecimalFormat getDecimalFormat() {
        df = df != null ? df : new DecimalFormat("0.######");
        return df;
    }
}
