package com.revolut.kataykin.revolutcurrency;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

class Utils {

    boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm != null ? cm.getActiveNetworkInfo() : null;
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
