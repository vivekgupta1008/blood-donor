package com.vivekgupta.bdonor.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.vivekgupta.bdonor.MessageClass;
import com.vivekgupta.bdonor.R;
import com.vivekgupta.bdonor.utils.SaveLifeUtils;
import com.vivekgupta.bdonor.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vivek on 27-12-2016.
 */

public class FragmentCreateRequestScreen extends android.support.v4.app.Fragment implements View.OnClickListener{
    View homeView;
    private AutoCompleteTextView bloodType;
    private EditText pintsNeeded, msgBox, addrBox;
    private static final String[] blood_list = {"A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB"};
    SaveLifeUtils saveLifeUtils;
    DatabaseReference mFirebaseDatabase;
    String countryISOCode;
    SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        homeView = inflater.inflate(R.layout.fragment_create_request, container, false);
        bloodType = homeView.findViewById(R.id.user_blood_type);
        // Create the adapter and set it to the AutoCompleteTextView
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, blood_list);

        bloodType.setAdapter(adapter);
        bloodType.setThreshold(1);

        pintsNeeded = homeView.findViewById(R.id.pints_needed);
        msgBox = homeView.findViewById(R.id.message_box);
        addrBox = homeView.findViewById(R.id.address_box);
        Button btnNotifyAll = homeView.findViewById(R.id.btn_send_to_all);
        btnNotifyAll.setOnClickListener(this);

        sessionManager = new SessionManager(getActivity());
        saveLifeUtils = SaveLifeUtils.getInstance(getActivity().getApplicationContext());
        countryISOCode = saveLifeUtils.getCountryCode();
        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference();
        return homeView;
    }

    @Override
    public void onClick(View v) {
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

        String pints = pintsNeeded.getText().toString().trim();
        if (TextUtils.isEmpty(pints)) {
            pintsNeeded.setError("Required");
            return;
        }

        String reqMsg = msgBox.getText().toString().trim();
        if (TextUtils.isEmpty(reqMsg)) {
            msgBox.setError("Required");
            return;
        }
        String requestorAddress = addrBox.getText().toString().trim();
        if(TextUtils.isEmpty(requestorAddress)){
            addrBox.setError("Required");
            return;
        }
        //get the requestor detail
        Map<String, Object> user = new HashMap<>();
        sessionManager.getSessionUserDetail(user);
        String requestorName = String.valueOf(user.get(SaveLifeUtils.TAG_NAME));
        String requestorNumber = String.valueOf(user.get(SaveLifeUtils.TAG_NUM));
        //requestor key
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        //create requestor message node
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        MessageClass msg = new MessageClass(userId, date, requestorName, requestorNumber, btype, pints, reqMsg,
               requestorAddress,countryISOCode);
        //notify all users
        if (mFirebaseDatabase == null) {
            mFirebaseDatabase = FirebaseDatabase.getInstance().getReference();
        }
        //mFirebaseDatabase.child("notifications").child(userId).setValue(msg);
        mFirebaseDatabase.child("notifications").child(userId).setValue(msg);
        Toast.makeText(getActivity(), "notification sent to all users", Toast.LENGTH_SHORT).show();
        closeKeypad();

    }

//    private void mayRequestLocation(){
//        if (ActivityCompat.checkSelfPermission(getActivity(), ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED) {
//            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
//                    mGoogleApiClient);
//
//            if(mLastLocation == null) {
//                Toast.makeText(getActivity(), "failed to get user location!", Toast.LENGTH_SHORT).show();
//            }
//            else
//                updateUser();//update database with user location
//            return;
//        }
//        if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
//            final Snackbar snackBar = Snackbar.make(homeView, R.string.location_permission_rationale, Snackbar.LENGTH_INDEFINITE);
//            snackBar.setAction(android.R.string.ok, new View.OnClickListener() {
//                @Override
//                @TargetApi(Build.VERSION_CODES.M)
//                public void onClick(View v) {
//                    requestPermissions(new String[]{ACCESS_FINE_LOCATION}, MY_PERMISSIONS_ACCESS_FINE_LOCATION);
//                    snackBar.dismiss();
//                }
//
//            });
//            snackBar.setCallback(new Snackbar.Callback() {
//                @Override
//                public void onDismissed(Snackbar snackbar, int event) {
//                    //super.onDismissed(snackbar, event);
//                    if(event == DISMISS_EVENT_SWIPE)
//                        bLocationDenied = true;
//                }
//            });
//            snackBar.show();
//        } else {
//            requestPermissions(new String[]{ACCESS_FINE_LOCATION}, MY_PERMISSIONS_ACCESS_FINE_LOCATION);
//        }
//    }

    private void closeKeypad() {
        View view = getActivity().getCurrentFocus();
        if(view != null) {
            saveLifeUtils.hideSoftKeyBoard(view, getActivity());
        }
    }
}
