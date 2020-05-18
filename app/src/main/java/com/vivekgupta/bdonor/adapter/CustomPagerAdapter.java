package com.vivekgupta.bdonor.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.vivekgupta.bdonor.fragments.FragmentSearchScreen;
import com.vivekgupta.bdonor.fragments.FragmentCreateRequestScreen;

/**
 * Created by vivek on 12-04-2017.
 */

public class CustomPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {
    int numTabs;
    Context context;
    public CustomPagerAdapter(FragmentManager fm, Context context, int numTabs) {
        super(fm);
        this.numTabs = numTabs;
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new FragmentCreateRequestScreen();
            case 1:
                return new FragmentSearchScreen();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numTabs;
    }
}
