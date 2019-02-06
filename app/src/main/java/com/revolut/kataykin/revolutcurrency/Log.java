package com.revolut.kataykin.revolutcurrency;

public class Log {
    public static void i(Class<?> cl, String text) {
        if (Titles.logEnabled)
            android.util.Log.i(cl.getName(), cl.getSimpleName() + " -> " + text);
    }

    public static void e(Class<?> cl, String text) {
        if (Titles.logEnabled)
            android.util.Log.e(cl.getName(), cl.getSimpleName() + " -> " + text);
    }

    public static void d(Class<?> cl, String text) {
        if (Titles.logEnabled)
            android.util.Log.d(cl.getName(), cl.getSimpleName() + " -> " + text);
    }
}
