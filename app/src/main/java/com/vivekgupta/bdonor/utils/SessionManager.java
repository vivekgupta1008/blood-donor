package com.vivekgupta.bdonor.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import com.vivekgupta.bdonor.MessageClass;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by vivek on 25-12-2016.
 */

public class SessionManager {
    // LogCat tag
    private static String TAG = SessionManager.class.getSimpleName();

    // Shared Preferences
    SharedPreferences pref;

    SharedPreferences.Editor editor;
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "LifeSaverLogin";

    private static final String KEY_IS_LOGGEDIN = "logged";

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setLogin(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);
        editor.commit();
    }

    public boolean isLoggedIn(){
        //boolean bl = pref.getBoolean(KEY_IS_LOGGEDIN,false);
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }
    public void setSessionUserDetail(Map<String, Object> userQ){
        editor.putString(SaveLifeUtils.TAG_NAME, String.valueOf(userQ.get(SaveLifeUtils.TAG_NAME)));
        editor.putString(SaveLifeUtils.TAG_EMAIL, String.valueOf(userQ.get(SaveLifeUtils.TAG_EMAIL)));
        editor.putString(SaveLifeUtils.TAG_BTYPE, String.valueOf(userQ.get(SaveLifeUtils.TAG_BTYPE)));
        editor.putString(SaveLifeUtils.TAG_NUM, String.valueOf(userQ.get(SaveLifeUtils.TAG_NUM)));
        editor.putString(SaveLifeUtils.TAG_DATE, String.valueOf(userQ.get(SaveLifeUtils.TAG_DATE)));
        editor.commit();
    }
    public void getSessionUserDetail(Map<String, Object> userQ){
        userQ.put(SaveLifeUtils.TAG_NAME, pref.getString(SaveLifeUtils.TAG_NAME, ""));
        userQ.put(SaveLifeUtils.TAG_EMAIL, pref.getString(SaveLifeUtils.TAG_EMAIL, ""));
        userQ.put(SaveLifeUtils.TAG_BTYPE, pref.getString(SaveLifeUtils.TAG_BTYPE, ""));
        userQ.put(SaveLifeUtils.TAG_NUM, pref.getString(SaveLifeUtils.TAG_NUM, ""));
        userQ.put(SaveLifeUtils.TAG_DATE, pref.getString(SaveLifeUtils.TAG_DATE, ""));
    }

    synchronized public void setUserLocation(Location mLastLocation){
        String latitude = String.valueOf(mLastLocation.getLatitude());
        String longitude = String.valueOf(mLastLocation.getLongitude());
        editor.putString(SaveLifeUtils.TAG_LATITUDE, latitude);
        editor.putString(SaveLifeUtils.TAG_LONGITUDE, longitude);
        editor.commit();
    }

    synchronized public Double getUserLatitude(){
        String latitude = pref.getString(SaveLifeUtils.TAG_LATITUDE, "");
        if(!latitude.isEmpty())
            return Double.parseDouble(latitude);
        return null;
    }

    synchronized public Double getUserLongitude(){
        String longitude = pref.getString(SaveLifeUtils.TAG_LONGITUDE, "");
        if(!longitude.isEmpty())
            return Double.parseDouble(longitude);
        return null;
    }

    synchronized public void storeUserNotificationList(ArrayList<MessageClass> requestList){
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<MessageClass>>(){}.getType();
        String json = gson.toJson(requestList, type);
        editor.putString(SaveLifeUtils.TAG_NOTIFICATION_LIST, json);
        editor.commit();
    }
    synchronized public ArrayList<MessageClass> getUserNotificationList(){
        ArrayList<MessageClass> requestList = new ArrayList<>();
        Gson gson= new Gson();
        String json = pref.getString(SaveLifeUtils.TAG_NOTIFICATION_LIST, "");
        if (json.equals(""))
            return requestList;
        Type type = new TypeToken<ArrayList<MessageClass>>(){}.getType();
        requestList = gson.fromJson(json, type);
        return requestList;
    }

    synchronized public void setRequestNumByBtype(String bType, int num){
        editor.putInt(bType, num);
        editor.commit();
    }

    public int getRequestNumByBtype(String bType){
        return pref.getInt(bType, 0);
    }
}
