package com.vivekgupta.bdonor.fragments;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.vivekgupta.bdonor.R;
import com.vivekgupta.bdonor.activities.SearchResultActivity;
import com.vivekgupta.bdonor.broadcastreceiver.LocationUpdatesBroadcastReceiver;
import com.vivekgupta.bdonor.utils.SaveLifeUtils;
import com.vivekgupta.bdonor.utils.SessionManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

/**
 * Created by vivek on 12-04-2017.
 */

public class FragmentSearchScreen extends Fragment implements View.OnClickListener{
    private static final int MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 100;
    View view;
    private AutoCompleteTextView bloodType;
    private static final String[] blood_list = {"A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB"};
    private EditText SearchRange;
    private boolean bLocationDenied = false;
    SessionManager sessionManager;
    SaveLifeUtils saveLifeUtils;
    DatabaseReference mFirebaseDatabase;
    String countryISOCode;
    private LocationRequest mLocationRequest;
    private static final long UPDATE_INTERVAL = 60 * 1000;
    private static final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2;
    private static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 1;
    private FusedLocationProviderClient mFusedLocationClient;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_search, container, false);
        bloodType = view.findViewById(R.id.user_blood_type);
        // Create the adapter and set it to the AutoCompleteTextView
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, blood_list);

        bloodType.setAdapter(adapter);
        bloodType.setThreshold(1);
        SearchRange = view.findViewById(R.id.search_range);
        Button searchButton = view.findViewById(R.id.btn_search);
        searchButton.setOnClickListener(this);

        sessionManager = new SessionManager(getActivity());
        saveLifeUtils = SaveLifeUtils.getInstance(getActivity().getApplicationContext());
        countryISOCode = saveLifeUtils.getCountryCode();
        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        createLocationRequest();
        fetchUserLocation();
        return view;
    }

    @Override
    public void onClick(View v) {
        //check if location permission denied
        if (bLocationDenied) {
            fetchUserLocation();
            return;
        }
        SessionManager sessionManager = new SessionManager(getActivity());
        Double latitude = sessionManager.getUserLatitude();
        Double longitude = sessionManager.getUserLongitude();
        if (latitude == null && longitude == null) {
            Toast.makeText(getActivity(), "failed to get user location!", Toast.LENGTH_SHORT).show();
            fetchUserLocation();
            return;
        }
        //check if blood type empty
        String btype = bloodType.getText().toString().trim();
        if (TextUtils.isEmpty(btype)) {
            bloodType.setError("Required");
            return;
        }
        //check valid blood type
        List<String> bList = Arrays.asList(blood_list);
        if (!bList.contains(btype.toUpperCase())) {
            bloodType.setError("Invalid");
            return;
        }
        //check if range empty
        String range = SearchRange.getText().toString().trim();
        if (TextUtils.isEmpty(range)) {
            SearchRange.setError("Required");
            return;
        }
        //check if range 0
        if (TextUtils.equals(range, "0")) {
            SearchRange.setError("enter more than 0");
            return;
        }
        //start search activity and initiate query
        Intent intent = new Intent(getActivity(), SearchResultActivity.class);
        intent.putExtra("blood_type", btype);
        intent.putExtra("search_range", range);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        startActivity(intent);
    }
    private void fetchUserLocation(){
        if (ActivityCompat.checkSelfPermission(getActivity(), ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
//            LocationManager locationManager = (LocationManager) getActivity().
//                    getSystemService(Context.LOCATION_SERVICE);
//            if((mLastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER))
//                    == null)
//                if((mLastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER))
//                        == null) {
//                    Toast.makeText(getActivity(), "failed to get user location!", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//            updateUser();//update database with user location
            requestLocationUpdates();
            return;
        }
        if (shouldShowRequestPermissionRationale(ACCESS_COARSE_LOCATION)) {
            final Snackbar snackBar = Snackbar.make(view, R.string.location_permission_rationale, Snackbar.LENGTH_INDEFINITE);
            snackBar.setAction(android.R.string.ok, new View.OnClickListener() {
                @Override
                @TargetApi(Build.VERSION_CODES.M)
                public void onClick(View v) {
                    requestPermissions(new String[]{ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_ACCESS_COARSE_LOCATION);
                    snackBar.dismiss();
                }

            });
            snackBar.setCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    //super.onDismissed(snackbar, event);
                    if(event == DISMISS_EVENT_SWIPE)
                        bLocationDenied = true;
                }
            });
            snackBar.show();
        } else {
            requestPermissions(new String[]{ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_ACCESS_COARSE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case MY_PERMISSIONS_ACCESS_COARSE_LOCATION:{
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchUserLocation();
                    bLocationDenied = false;
                } else {
                    bLocationDenied = true;
                    Toast.makeText(getActivity(), "location access denied", Toast.LENGTH_SHORT).show();
                    //assert true;
                }
            }
        }
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(getActivity(), LocationUpdatesBroadcastReceiver.class);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void requestLocationUpdates() {
        try {
            //Log.i(TAG, "Starting location updates");
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, getPendingIntent());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void removeLocationUpdates() {
        try {
            //Log.i(TAG, "Removing location updates");
            mFusedLocationClient.removeLocationUpdates(getPendingIntent());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setMaxWaitTime(MAX_WAIT_TIME);
    }

}