package com.parking.app.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;

public class AppUtils {
    public static boolean isInternetAvailable(@NonNull Context context) {
        return isConnected(context);
    }

    public static boolean isConnected(@NonNull Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        } else {
            return false;
        }
    }

    //This makes a real connection to an url and checks if you can connect to this url, this needs to be wrapped in a background thread
    private static boolean isAbleToConnect() {
        new Thread(() -> {
            someFuncToRun((value) -> {
                return value;
            });
        }).start();
        return false;
    }

    // a thread sometime is to delegate those heavy lifting elsewhere
    public static boolean someFuncToRun(Callback<Boolean> p) {
        try {

            URL myUrl = new URL("http://www.google.com");
            URLConnection connection = myUrl.openConnection();
            connection.setConnectTimeout(1000);
            connection.connect();
            return p.cb(true);
        } catch (Exception e) {
            Log.i("exception", Objects.requireNonNull(e.getMessage()));
            return p.cb(false);
        }
    }

    // The key is here, this allow you to pass a lambda callback to your method
// update: use generic to allow passing different type of data
// you could event make it <T,S> so input one type return another type
    interface Callback<T> {
        public T cb(T a);
    }

    public static void ToastLocal(int msg, Context c) {
        Toast.makeText(c, c.getString(msg), Toast.LENGTH_SHORT).show();
    }
}
