package com.example.driverlog;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Switch;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class SharedPreferencesHelper {
    public static final String SHARED_PREF_NAME = "DRIVER_LOG_DATA_4";
    public static final String ET_ID = "ID";
    public static final String ET_IP = "IP";
    public static final String FIO = "FIO";
    public static final String BUTTON_DB = "BUTTON_DB";
    public static final String ADDRESS_KEY = "ADDRESS_KEY";
    public static final String SWITCH_KEY = "SWITCH_KEY";
    public static final Type SWITCH_TYPE = new TypeToken<HashMap<Integer, Boolean>>() {}.getType();


    private SharedPreferences mSharedPreferences;
    private Gson mGson = new Gson();

    public SharedPreferencesHelper(Context context) {
        // параметры - название файла и режим
        mSharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    public String getId(){
        return mSharedPreferences.getString(ET_ID, "");
    }

    public void addId(String id){
        mSharedPreferences.edit().putString(ET_ID, id).apply();
    }

    public String getIP(){
        return mSharedPreferences.getString(ET_IP, "");
    }

    public void addIP(String ip){
        mSharedPreferences.edit().putString(ET_IP, ip).apply();
    }

    public String getFIO(){
        return mSharedPreferences.getString(FIO, "");
    }

    public void addFIO(String fio){
        mSharedPreferences.edit().putString(FIO, fio).apply();
    }

    public int getBDB(){
        return mSharedPreferences.getInt(BUTTON_DB, 1);
    }

    public void addBDB(int isEnable){
        mSharedPreferences.edit().putInt(BUTTON_DB, isEnable).apply();
    }

    public String getAddress(){
        return mSharedPreferences.getString(ADDRESS_KEY, "");
    }

    public void addAddress(String address){
        mSharedPreferences.edit().putString(ADDRESS_KEY, address).apply();
    }

    public HashMap<Integer, Boolean> getHashMapSwitch(){
        HashMap<Integer, Boolean> switches = mGson.fromJson(mSharedPreferences.getString(SWITCH_KEY, ""), SWITCH_TYPE);
        return switches == null ? new HashMap<Integer, Boolean>() : switches;
    }

    public void addHashMapSwitch(HashMap<Integer, Boolean> switches){
         mSharedPreferences.edit().putString(SWITCH_KEY, mGson.toJson(switches, SWITCH_TYPE)).apply();
    }
}