package com.vivekgupta.bdonor.utils;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;

/**
 * Created by vivek10.gupta on 12/30/2016.
 */

public class SaveLifeSpinners extends Activity implements AdapterView.OnItemSelectedListener {
    private String itemString;
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        itemString = parent.getItemAtPosition(pos).toString();
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
}
