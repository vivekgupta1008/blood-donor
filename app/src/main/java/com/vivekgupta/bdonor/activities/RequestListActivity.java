package com.vivekgupta.bdonor.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.vivekgupta.bdonor.MessageClass;
import com.vivekgupta.bdonor.R;
import com.vivekgupta.bdonor.adapter.RequestorListAdapter;
import com.vivekgupta.bdonor.utils.SaveLifeUtils;
import com.vivekgupta.bdonor.utils.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.CALL_PHONE;

/**
 * Created by vivek10.gupta on 2/6/2017.
 *
 */

public class RequestListActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_CALL = 1;
    SaveLifeUtils utils;
    private Map<String, Object> userDetail = new HashMap<>();
    SessionManager sessionManager;
    RelativeLayout relativeLayout;
    String bType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_result);
        relativeLayout = (RelativeLayout) findViewById(R.id.id_search_layout);
        Bundle bundle = getIntent().getExtras();
        bType = bundle.getString("selected_btype");
        setTitle(bType + " Requests");
        sessionManager = new SessionManager(this);
        queryRequestorListData();
    }

    private void queryRequestorListData() {
        utils = SaveLifeUtils.getInstance(getApplicationContext());
        sessionManager.getSessionUserDetail(userDetail);
        //get request list by btype
        ArrayList<MessageClass> requestList;
        ArrayList<MessageClass> tempRequestList = new ArrayList<>();
        requestList = sessionManager.getUserNotificationList();
        if (!bType.equals("ALL")) {
            for (MessageClass m : requestList) {
                if (String.valueOf(m.btype).equals(bType))
                    tempRequestList.add(m);
            }
            createRequestorListView(tempRequestList);
        } else
            createRequestorListView(requestList);
    }

    private void createRequestorListView(ArrayList<MessageClass> requestList) {
        if (requestList.size() == 0) {
            Toast.makeText(this, "you don't have any requestor!", Toast.LENGTH_SHORT).show();
            //finish();
        } else {
            // specify an adapter
            RecyclerView.Adapter mAdapter = new RequestorListAdapter(requestList, this);
            //get recycler view
            RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.search_recycler_view);
            // use a linear layout manager
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
            //add divider
            //DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(this,
            //      LinearLayoutManager.VERTICAL);
            if (mRecyclerView != null) {
                mRecyclerView.setLayoutManager(mLayoutManager);
                // mRecyclerView.addItemDecoration(mDividerItemDecoration);
                mRecyclerView.setAdapter(mAdapter);
            }
        }
    }

    public Double getLatitude() {
        return sessionManager.getUserLatitude();
    }

    public Double getLongitude() {
        return sessionManager.getUserLongitude();
    }

    public void callDonorIntent(String num) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + num));
        if (ActivityCompat.checkSelfPermission(this, CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(callIntent);
            return;
        }
        if (shouldShowRequestPermissionRationale(CALL_PHONE)) {
            final Snackbar snackBar = Snackbar.make(relativeLayout, R.string.location_permission_rationale, Snackbar.LENGTH_INDEFINITE);
            snackBar.setAction(android.R.string.ok, new View.OnClickListener() {
                @Override
                @TargetApi(Build.VERSION_CODES.M)
                public void onClick(View v) {
                    requestPermissions(new String[]{CALL_PHONE}, MY_PERMISSIONS_CALL);
                    snackBar.dismiss();
                }
            });
            snackBar.show();
        } else {
            requestPermissions(new String[]{CALL_PHONE}, MY_PERMISSIONS_CALL);
        }

    }

    synchronized public void updateUserNotificationList(ArrayList<MessageClass> requestList) {
        ArrayList<MessageClass> list = new ArrayList<>(requestList);
        sessionManager.storeUserNotificationList(list);
    }
}
