package com.vivekgupta.bdonor;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by vivek on 24-12-2016.
 */

@IgnoreExtraProperties
public class User {

    public String name;
    public String email;
    public String btype;
    public String num;
    public Double  latitude;
    public Double  longitude;
    public String date;
    public boolean logged;

    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    public User() {
    }

    public User(String name, String btype, String num, String email, Double latitude, Double longitude, String date, boolean logged) {
        this.name = name;
        this.email = email;
        this.btype = btype;
        this.num = num;
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;;
        this.logged = logged;
    }
}
