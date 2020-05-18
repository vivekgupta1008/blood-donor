package com.vivekgupta.bdonor.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import com.vivekgupta.bdonor.adapter.CustomPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vivekgupta.bdonor.R;

/**
 * Created by vivek on 12-04-2017.
 */

public class FragmentMain extends Fragment {
    View view;
    private CustomPagerAdapter pagerAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_main, container, false);
        TabLayout tabLayout = view.findViewById(R.id.sliding_tabs);
        tabLayout.addTab(tabLayout.newTab().setText("Create Request"));
        tabLayout.addTab(tabLayout.newTab().setText("Search Donor"));
        //tabLayout.setTabMode(TabLayout.MODE_FIXED);
       // tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabTextColors(Color.parseColor("#616161"), Color.parseColor("#424242"));

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        final ViewPager viewPager = (ViewPager)view.findViewById(R.id.viewpager);
        pagerAdapter = new CustomPagerAdapter(getChildFragmentManager(), getActivity(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

        });
        return view;
    }

    public CustomPagerAdapter getPagerAdapter(){
        return pagerAdapter;
    }
}
