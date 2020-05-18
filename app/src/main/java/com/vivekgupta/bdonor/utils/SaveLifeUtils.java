package com.vivekgupta.bdonor.utils;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

/**
 * Created by vivek on 29-12-2016.
 */

public class SaveLifeUtils {
    private static Context context;
    private static SaveLifeUtils saveLifeUtils = null;
    private static String countryISOCode = null;
    public static final String TAG = "BDONOR";
    public static final String TAG_NAME = "name";
    public static final String TAG_BTYPE = "btype";
    public static final String TAG_NUM = "num";
    public static final String TAG_EMAIL = "email";
    public static final String TAG_DATE = "date";
    public static final String TAG_LATITUDE = "latitude";
    public static final String TAG_LONGITUDE = "longitude";
    public static final String TAG_DISTANCE = "donor_distance";
    public static final String TAG_NOTIFICATION_LIST = "notification_list";
    public static final int MY_PERMISSIONS_SEND_SMS = 102;
    public static final String NOTIFICATION_TOPIC = "notifications";
    public static final String KEY_NOTIFICATION = "notify_intent";

    private SaveLifeUtils(Context context){
        this.context = context;
        setCountryCode();
    }

    private void setCountryCode() {
        TelephonyManager teleMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        if (teleMgr != null){
            countryISOCode = teleMgr.getSimCountryIso();
        }
        else{
            Toast.makeText(context, "please insert sim", Toast.LENGTH_SHORT).show();
        }
    }

    public static SaveLifeUtils getInstance(Context context){
        if(saveLifeUtils == null){
            saveLifeUtils = new SaveLifeUtils(context);
        }
        return saveLifeUtils;
    }

    public String getCountryCode(){
        if(countryISOCode == null){
            setCountryCode();
        }
        return countryISOCode;
        //return "in";
    }

    public void hideSoftKeyBoard(View view, Context context){
        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
