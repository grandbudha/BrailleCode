package com.pruthvi.gobraille;

import android.content.Context;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Constants {

    public static final int BUF_SIZE = 65536;
    public static final String DATE_FORMAT = "yyyyMMddHHmmss";
    public static final String TAG = "GoBraille";
    public static final String NULL ="NULL";
    public static final String NOT_NULL = "NOT NULL";


    public static String getDateTime(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return simpleDateFormat.format(new Date());
    }

    public static void showToast(AppCompatActivity activity, String message){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
