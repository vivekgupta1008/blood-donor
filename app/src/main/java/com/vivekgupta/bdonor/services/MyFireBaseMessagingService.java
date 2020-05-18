package com.vivekgupta.bdonor.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.vivekgupta.bdonor.MessageClass;
import com.vivekgupta.bdonor.R;
import com.vivekgupta.bdonor.activities.MainActivitySaveLife;
import com.vivekgupta.bdonor.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.vivekgupta.bdonor.utils.SaveLifeUtils;

import java.util.ArrayList;

public class MyFireBaseMessagingService extends FirebaseMessagingService {
    //private ArrayList<Map<String, String>> requestList  = new ArrayList<>();
    private ArrayList<MessageClass> requestList  = new ArrayList<>();
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //super.onMessageReceived(remoteMessage);
        //Log.d(SaveLifeUtils.TAG, "notification received");

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            //Log.d(SaveLifeUtils.TAG, "Message data payload: " + remoteMessage.getData());
            FirebaseAuth auth = FirebaseAuth.getInstance();
            String userId = auth.getCurrentUser().getUid();
            String req_id = remoteMessage.getData().get("req_id");
            //Log.d(SaveLifeUtils.TAG, "req_id is: " + req_id);
            if(req_id.equals(userId)){
                return;
            }
            MessageClass value = new MessageClass();
            value.country_code = remoteMessage.getData().get("country_code");
            value.req_name = remoteMessage.getData().get("req_name");
            value.btype = remoteMessage.getData().get("btype");
            value.req_num = remoteMessage.getData().get("req_num");
            value.pints = remoteMessage.getData().get("pints");
            value.req_msg = remoteMessage.getData().get("req_msg");
            value.req_address = remoteMessage.getData().get("req_address");
            value.time = remoteMessage.getData().get("time");
            //Log.e(SaveLifeUtils.TAG, "value is: "+ value);

            SessionManager sessionManager = new SessionManager(getApplicationContext());
            requestList = sessionManager.getUserNotificationList();
            if (requestList == null)
                requestList = new ArrayList<>();
            if(requestList.size() >= 100){
                requestList.remove(requestList.size()-1);
            }
            requestList.add(0, value);
            sessionManager.storeUserNotificationList(requestList);//store all notifications requests
            String btype = String.valueOf(value.btype);
            int num = sessionManager.getRequestNumByBtype(btype);
            int allNum = sessionManager.getRequestNumByBtype("ALL");
            sessionManager.setRequestNumByBtype(btype, num + 1);//store for only showing notifications
            sessionManager.setRequestNumByBtype("ALL", allNum + 1);
            sendNotification("");
        }
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivitySaveLife.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(SaveLifeUtils.KEY_NOTIFICATION, "notify_intent");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_noti_black_24dp)
                        .setContentTitle("Blood Donor")
                        .setContentText("New Request")
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
