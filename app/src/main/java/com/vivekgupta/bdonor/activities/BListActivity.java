package com.vivekgupta.bdonor.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.vivekgupta.bdonor.BTypeList;
import com.vivekgupta.bdonor.R;
import com.vivekgupta.bdonor.adapter.BtypeListAdapter;
import com.vivekgupta.bdonor.interfaces.ItemClickListener;
import com.vivekgupta.bdonor.utils.SessionManager;

import java.util.ArrayList;

/**
 * Created by vivek on 05-02-2017.
 */

public class BListActivity extends AppCompatActivity implements ItemClickListener {
    private ArrayList<BTypeList> bList = new ArrayList<>();
    private SessionManager sessionManager;
    private static  String [] bArray =
            {"ALL", "A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"};
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_result);
        setTitle(R.string.title_bList_activity);
        sessionManager = new SessionManager(this);
        createListView();
    }

    private void createListView(){
        //get the list
        for(String s: bArray){
            BTypeList b = new BTypeList();
            b.bType = s; b.bTypeNum = sessionManager.getRequestNumByBtype(s);
            bList.add(b);
        }
        BtypeListAdapter mAdapter = new BtypeListAdapter(bList);
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.search_recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        if(mRecyclerView != null){
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.setClickListener(this);
        }
    }

    @Override
    public void onClick(View vew, int position) {
        if(position == 0) {
            for (String aBArray : bArray) {
                sessionManager.setRequestNumByBtype(aBArray, 0);
            }
        }
        else {
            int num = sessionManager.getRequestNumByBtype(bArray[position]);
            int numAll = sessionManager.getRequestNumByBtype(bArray[0]);
            sessionManager.setRequestNumByBtype(bArray[position], 0);
            sessionManager.setRequestNumByBtype(bArray[0], numAll-num);
        }
        Intent intent = new Intent(this, RequestListActivity.class);
        intent.putExtra("selected_btype", bArray[position]);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                setResult(RESULT_OK);
                //NavUtils.navigateUpFromSameTask(this);
                //return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
