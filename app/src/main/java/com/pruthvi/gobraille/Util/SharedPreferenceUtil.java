package com.pruthvi.gobraille.Util;

import android.content.Context;
import android.content.SharedPreferences;

import com.pruthvi.gobraille.Constants;

public class SharedPreferenceUtil {

    private final String SHARED_PREFERENCE_UTIL = "go_braille_sp";



    public static final String INIT = "go_braille_init";

    private SharedPreferences sharedPreferences;
    private Context context;


    public SharedPreferenceUtil(Context context){
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCE_UTIL, Context.MODE_PRIVATE);
    }

    public void add(String key, String value){
        this.sharedPreferences.edit().putString(key, value).apply();
    }

    public String get(String key){
        return this.sharedPreferences.getString(key, Constants.NULL);
    }

}
