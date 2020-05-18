package com.vivekgupta.bdonor.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.vivekgupta.bdonor.utils.SaveLifeUtils;
import com.vivekgupta.bdonor.utils.SessionManager;
import com.google.android.gms.location.LocationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_PROCESS_UPDATES =
            "com.vivekgupta.bdonor.broadcastreceiver.action";
                    //+ ".PROCESS_UPDATES";
    private DatabaseReference mFirebaseDatabase;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                LocationResult result = LocationResult.extractResult(intent);
                if(result != null) {
                    updateUser(result.getLastLocation(), context);
                }
            }
        }
    }

    private void updateUser(Location location, Context context) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        if(userId != null) {
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();

            Log.i(SaveLifeUtils.TAG, "latitude:" + latitude + " longitude:" + longitude);
            //Toast.makeText(context, "latitude: " + latitude + "logitude: " + longitude, Toast.LENGTH_SHORT).show();

            //store to device
            SessionManager mSessionManager = new SessionManager(context);
            mSessionManager.setUserLocation(location);

            //store to firebase
            if (mFirebaseDatabase == null) {
                mFirebaseDatabase = FirebaseDatabase.getInstance().getReference();
            }

            SaveLifeUtils saveLifeUtils = SaveLifeUtils.getInstance(context);
            String countryISOCode = saveLifeUtils.getCountryCode();

            //for debugging
            //String sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm").format(new Date());

            mFirebaseDatabase.child("users_" + countryISOCode).child(userId).child("latitude").setValue(latitude);
            mFirebaseDatabase.child("users_" + countryISOCode).child(userId).child("longitude").setValue(longitude);
        }
    }
}
