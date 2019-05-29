package com.example.biowaxscanner;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Validations {
    public static boolean hasActiveInternetConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return true;
        } else {
            return false;
        }
        // validation
    }

}
