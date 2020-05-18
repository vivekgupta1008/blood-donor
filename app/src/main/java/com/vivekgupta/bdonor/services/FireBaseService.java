package com.vivekgupta.bdonor.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import com.vivekgupta.bdonor.MessageClass;
import com.vivekgupta.bdonor.R;
import com.vivekgupta.bdonor.activities.MainActivitySaveLife;
import com.vivekgupta.bdonor.utils.SaveLifeUtils;
import com.vivekgupta.bdonor.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by vivek on 07-01-2017.
 */

public class FireBaseService extends Service {
    SaveLifeUtils utils;
    SessionManager sessionManager;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseAuth auth;
    public static final int DONOR_NOTIFICATION_ID = 1000;
    private  boolean flag = false;
    private ArrayList<MessageClass> requestList  = new ArrayList<>();
    private long childCount;

    @Override
    public void onCreate() {
        super.onCreate();
        //Log.e(SaveLifeUtils.TAG, "onCreate of service!");
        //utils = new SaveLifeUtils(getApplicationContext());
        sessionManager = new SessionManager(getApplicationContext());
        listenDonorRequest();
        //sessionManager.getSessionUserDetail(userDetal);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //add listener
        //Log.e(SaveLifeUtils.TAG, "onStartCommand of service!");
        //listenDonorRequest();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public void listenDonorRequest(){
        //Log.e(SaveLifeUtils.TAG, "listenDonorRequest");
       // flag = true;
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            return;
        }
//        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference().child("users_" + utils.getCountryCode())
//                .child(userId)
//                .child("req_msg");
        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference().child("users_" + utils.getCountryCode())
                .child("notifications");
        mFirebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                childCount = dataSnapshot.getChildrenCount();
                //Log.e(SaveLifeUtils.TAG, "dataSnapshot childCount is "+childCount);
                addChildEventListener();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void addChildEventListener() {
        mFirebaseDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //Log.e(SaveLifeUtils.TAG, "onChildAdded child count is "+ childCount);
                if(childCount != 0){
                    childCount--;
                    return;
                }
                if(dataSnapshot.getKey().equals(auth.getCurrentUser().getUid()))
                    return;
                synchronized (this){
                    //Log.e("vivek", "onDataChange self triggred is "+RequestorListAdapter.selfTriggered);
                    //if(flag == false){//called while starting a service so notification not req.
                    Map<String,Object> value = (Map<String, Object>) dataSnapshot.getValue();
                    //Log.e(SaveLifeUtils.TAG, "value is: "+ value);
                    if(value != null) {
                        requestList = sessionManager.getUserNotificationList();
                        if(requestList == null)
                            requestList = new ArrayList<>();
                        //requestList.add(0, value);
                        sessionManager.storeUserNotificationList(requestList);
                        String btype = String.valueOf(value.get("btype"));
                        int num = sessionManager.getRequestNumByBtype(btype);
                        int allNum = sessionManager.getRequestNumByBtype("ALL");
                        sessionManager.setRequestNumByBtype(btype, num+1);
                        sessionManager.setRequestNumByBtype("ALL", allNum+1);
                        buildNotification(allNum+1);
                    }
//                    }
//                    else {
//                        flag = false;
//                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.getKey().equals(auth.getCurrentUser().getUid()))
                    return;
                Map<String,Object> value = (Map<String, Object>) dataSnapshot.getValue();
                //Log.e(SaveLifeUtils.TAG, "value is: "+ value);
                if(value != null) {
                    requestList = sessionManager.getUserNotificationList();
                    if(requestList == null)
                        requestList = new ArrayList<>();
                    //requestList.add(value);
                    sessionManager.storeUserNotificationList(requestList);
                    String btype = String.valueOf(value.get("btype"));
                    int num = sessionManager.getRequestNumByBtype(btype);
                    int allNum = sessionManager.getRequestNumByBtype("ALL");
                    sessionManager.setRequestNumByBtype(btype, num+1);
                    sessionManager.setRequestNumByBtype("ALL", allNum+1);
                    buildNotification(allNum+1);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void buildNotification(int num) {
        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle("Blood Donor")
                        .setContentText(num + " New Request!");
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivitySaveLife.class);
        resultIntent.putExtra("from_notification", true);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        //TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
        //stackBuilder.addParentStack(ResultActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        //stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(DONOR_NOTIFICATION_ID, mBuilder.build());


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Log.e(SaveLifeUtils.TAG, "onDestroy of service");
    }
}
