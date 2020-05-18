package com.vivekgupta.bdonor.activities;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.vivekgupta.bdonor.R;
import com.vivekgupta.bdonor.adapter.SaveLifeAdapter;
import com.vivekgupta.bdonor.utils.SaveLifeUtils;
import com.vivekgupta.bdonor.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.SEND_SMS;

/**
 * Created by vivek on 26-12-2016.
 */

public class SearchResultActivity extends AppCompatActivity {
    private ArrayList<Map<String, Object>> userList= new ArrayList<>();
    private String bloodType;
    SessionManager sessionManager;
    private Map<String,Object> requestorDetail = new HashMap<>();
    private String donorNum;
    private FirebaseUser firebaseUser;
    SaveLifeAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_result);
        //relativeLayout = (RelativeLayout) findViewById(R.id.search_relative_layout);
        //get the passed values
        //getActionBar().setHomeButtonEnabled(true);
        //getActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();
        bloodType = bundle.getString("blood_type");
        String searchRange = bundle.getString("search_range");
        Double latitude = bundle.getDouble("latitude");
        Double longitude = bundle.getDouble("longitude");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //Log.e(SaveLifeUtils.TAG, "blood and range is :" + bloodType + "," + searchRange);
        createUserListView();
        //inititate query
        initiateQuery(bloodType, searchRange, latitude, longitude);
    }

    private void initiateQuery(String btype, final String searchRange, final Double slat, final Double slon) {
        //get firebase
        DatabaseReference mFirebaseDatabase = FirebaseDatabase.getInstance().getReference();
        Query queryRef = mFirebaseDatabase.child("users_" + SaveLifeUtils.getInstance(this).getCountryCode()).orderByChild("btype").equalTo(btype);
        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    Map<String, Object> value = (Map<String, Object>) eventSnapshot.getValue();
                    //Double.valueOf((Double) value.get("latitude"));
                    Double desLat = (Double) value.get("latitude");
                    Double desLon = (Double) value.get("longitude");
                    if (firebaseUser != null)
                        if (eventSnapshot.getKey().equals(firebaseUser.getUid()))
                            continue;
                    int distance = distanceBetween(slat, slon, desLat, desLon);
                    if (distance <= Integer.parseInt(searchRange)) {
                        value.put("donor_distance", distance);
                        value.put("donor_key", eventSnapshot.getKey());
                        userList.add(value);
                        mAdapter.notifyDataSetChanged();
                        //Log.e(SaveLifeUtils.TAG, "userdetail:" + value);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void createUserListView() {
        // specify an adapter
        mAdapter = new SaveLifeAdapter(userList, this);
        //get recycler view
        RecyclerView mRecyclerView = findViewById(R.id.search_recycler_view);
        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        //add divider
        //DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(this,
          //      LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
            //mRecyclerView.addItemDecoration(mDividerItemDecoration);
        mRecyclerView.setAdapter(mAdapter);
    }

//    public void sendDonorRequest(String donorKey, String distance){
//        //Log.e(SaveLifeUtils.TAG,"sending donor request!");
//        //get uid of user
//        FirebaseAuth auth = FirebaseAuth.getInstance();
//        String requestorKey = auth.getCurrentUser().getUid();
//        //create message details
//        sessionManager = new SessionManager(this);
//        sessionManager.getSessionUserDetail(requestorDetail);
//        String requestorName = String.valueOf(requestorDetail.get(SaveLifeUtils.TAG_NAME));
//        String requestorNum = String.valueOf(requestorDetail.get(SaveLifeUtils.TAG_NUM));
//        MessageClass msg = new MessageClass(requestorName, requestorNum, distance);
//        //post the message
//        DatabaseReference mFirebaseDatabase = FirebaseDatabase.getInstance().getReference();
//        mFirebaseDatabase.child("users_"+getCountryCode()).child(donorKey).child("req_msg").child(requestorKey).setValue(msg);
//        Toast.makeText(this, "request sent", Toast.LENGTH_SHORT).show();
//
//    }

    public void sendDonorRequest(String number){
        donorNum = number;
        utilSendSMS();
    }
    private void utilSendSMS(){
        if(!mayRequestSMS())
            return;
        sendDonorMessage();

    }

    private boolean mayRequestSMS(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
//        if (shouldShowRequestPermissionRationale(SEND_SMS)) {
//            Snackbar.make(relativeLayout, R.string.sms_permission_rationale, Snackbar.LENGTH_INDEFINITE)
//                    .setAction(android.R.string.ok, new View.OnClickListener() {
//                        @Override
//                        @TargetApi(Build.VERSION_CODES.M)
//                        public void onClick(View v) {
//                            requestPermissions(new String[]{SEND_SMS}, MY_PERMISSIONS_SEND_SMS);
//                        }
//                    }).show();
//        } else {
            requestPermissions(new String[]{SEND_SMS}, SaveLifeUtils.MY_PERMISSIONS_SEND_SMS);
      //  }
        return false;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case SaveLifeUtils.MY_PERMISSIONS_SEND_SMS:{
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    utilSendSMS();
                } else {
                    Toast.makeText(this, "sms access denied", Toast.LENGTH_SHORT).show();
                    //assert true;
                }
            }
        }
    }

    private void sendDonorMessage(){
        //get requestor detail
        sessionManager = new SessionManager(this);
        sessionManager.getSessionUserDetail(requestorDetail);
        String requestorName = String.valueOf(requestorDetail.get(SaveLifeUtils.TAG_NAME));
        //make message
        String msg = "Hi this is "+requestorName+" from Blood Donor. I need " +bloodType+" blood urgently. Please contact me!";
        //send message
        SmsManager sm = SmsManager.getDefault();
        sm.sendTextMessage(donorNum, null, msg, null, null);
        sendToastMessage("Sending SMS");

    }
    private int distanceBetween(double slat, double slon, double elat, double elon){
        float distance;
        Location currLocation=new Location("currLocation");
        currLocation.setLatitude(slat);
        currLocation.setLongitude(slon);

        Location desLocation=new Location("desLocation");
        desLocation.setLatitude(elat);
        desLocation.setLongitude(elon);

        distance = currLocation.distanceTo(desLocation);
        //Log.e(SaveLifeUtils.TAG, "distance="+distance);
        return Math.round(distance/1000);
    }

    private void sendToastMessage(String str){
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

}
