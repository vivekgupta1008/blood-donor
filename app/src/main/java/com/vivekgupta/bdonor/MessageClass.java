package com.vivekgupta.bdonor;

/**
 * Created by vivek on 07-01-2017.
 */

public class MessageClass {
    public String req_id;
    public String time;
    public String req_name;
    public String req_num;
    public String btype;
    public String pints;
    public String req_msg;
    public String req_address;
    public String country_code;

    public MessageClass(String req_id, String time, String req_name, String requestorNum, String btype, String pints, String req_msg,
                        String address, String country_code) {
        this.req_id = req_id;
        this.time = time;
        this.req_name = req_name;
        this.req_num = requestorNum;
        this.btype = btype;
        this.pints = pints;
        this.req_msg = req_msg;
        this.req_address = address;
        this.country_code = country_code;
    }

    public MessageClass(){

    }
}
