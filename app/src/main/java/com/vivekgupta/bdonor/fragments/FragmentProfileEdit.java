package com.vivekgupta.bdonor.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.vivekgupta.bdonor.R;
import com.vivekgupta.bdonor.User;
import com.vivekgupta.bdonor.utils.SaveLifeSpinners;
import com.vivekgupta.bdonor.utils.SaveLifeUtils;
import com.vivekgupta.bdonor.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by vivek on 27-12-2016.
 */

public class FragmentProfileEdit extends android.support.v4.app.Fragment {
    SaveLifeUtils saveLifeUtils;
    private EditText editName, editNumber;
    Spinner spinnerBType;
    private TextView editDate;
    String strName, strBType, strNumber, strDate, strEmail;
    int mYear, mMonth, mDay;
    SaveLifeUtils saveLife;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View profileView = inflater.inflate(R.layout.fragment_edit_prifile, container, false);
        //get resources
        editName = profileView.findViewById(R.id.edit_user_name);
        //btype spinner
        spinnerBType = profileView.findViewById(R.id.spinner_btype);
        SaveLifeSpinners saveLifeSpinners = new SaveLifeSpinners();
        spinnerBType.setOnItemSelectedListener(saveLifeSpinners);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.btype_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBType.setAdapter(adapter);

        editNumber = profileView.findViewById(R.id.edit_phone);
        editDate = profileView.findViewById(R.id.lastDonated);
        //TextView email = (TextView) profileView.findViewById(R.id.user_email_id);
        Button editSave = profileView.findViewById(R.id.editSave);

        //get saved values from previous state
        Map<String, Object> user = new HashMap<>();
        getUserDetail(user);
        //set text from previous save
        editName.setText(String.valueOf(user.get(SaveLifeUtils.TAG_NAME)));
        spinnerBType.setSelection(adapter.getPosition(String.valueOf(user.get(SaveLifeUtils.TAG_BTYPE))));
        editNumber.setText(String.valueOf(user.get(SaveLifeUtils.TAG_NUM)));
        //email.setText(String.valueOf(user.get(SaveLifeUtils.TAG_EMAIL)));
        strEmail = String.valueOf(user.get(SaveLifeUtils.TAG_EMAIL));
        editDate.setText(String.valueOf(user.get(SaveLifeUtils.TAG_DATE)));
        editDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeypad();
                showDatePickerDialog();
            }
        });
        editSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //close keypad
                closeKeypad();

                strName = editName.getText().toString().trim();
                strNumber = editNumber.getText().toString().trim();
                if(TextUtils.isEmpty(strName)){
                    editName.setError("Required");
                    return;
                }
                if(TextUtils.isEmpty(strNumber)){
                    editNumber.setError("Required");
                }
                if(TextUtils.getTrimmedLength(strNumber) > 0) {
                    if(TextUtils.getTrimmedLength(strNumber) != 10){
                        editNumber.setError("enter 10 digits");
                        return;
                    }
                }
                strDate = editDate.getText().toString();
                if(TextUtils.isEmpty(strDate)){

                }
                strBType = spinnerBType.getSelectedItem().toString();
                if(TextUtils.isEmpty(strBType)){
                    Toast.makeText(getActivity(), "blood type required!", Toast.LENGTH_SHORT).show();
                }
                updateUserProfile();
            }
        });

        saveLifeUtils = SaveLifeUtils.getInstance(getActivity().getApplicationContext());
        return profileView;
    }

    @Override
    public void onStart() {
        super.onStart();

    }
    private void updateUserProfile() {
        //Get Firebase auth instance
        FirebaseAuth auth = FirebaseAuth.getInstance();
        //get country of the user which should be first child of database
        String countryISOCode = saveLifeUtils.getCountryCode();
        //get uid of user which is second child of database
        String userId = auth.getCurrentUser().getUid();
        //create user details
        User user = new User(strName, strBType, strNumber,strEmail,0.0,0.0, strDate, true);
        //store
        DatabaseReference mFirebaseDatabase = FirebaseDatabase.getInstance().getReference();
        mFirebaseDatabase.child("users_"+countryISOCode).child(userId).setValue(user);
        //progressBar.setVisibility(View.GONE);
        //save details in session()
        setUserDetail(strName, strBType, strNumber, strEmail, strDate);
        Toast.makeText(getActivity(), "profile saved", Toast.LENGTH_SHORT).show();
    }

    public void showDatePickerDialog(){
        //dateFragment.show(getActivity().getFragmentManager(), "datePicker");
        Calendar mcurrentDate=Calendar.getInstance();
        mYear=mcurrentDate.get(Calendar.YEAR);
        mMonth=mcurrentDate.get(Calendar.MONTH);
        mDay=mcurrentDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog mDatePicker=new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker datepicker, int year, int month, int day) {
                // TODO Auto-generated method stub
                   editDate.setText(day+"/"+(month+1)+"/"+year);
            }
        },mYear, mMonth, mDay);
        mDatePicker.setTitle("Select date");
        mDatePicker.show();
    }
    private void getUserDetail(Map<String, Object> user) {
        SessionManager sessionManager  = new SessionManager(getActivity());
        sessionManager.getSessionUserDetail(user);
    }

    private void closeKeypad() {
        View view = getActivity().getCurrentFocus();
        if(view != null) {
            //saveLife = new SaveLifeUtils(getContext());
            saveLifeUtils.hideSoftKeyBoard(view, getActivity());
        }
    }
    private void setUserDetail(String name, String btype, String num, String email, String date) {
        Map<String, Object> user = new HashMap<>();
        SessionManager sessionManager = new SessionManager(getActivity());
        user.put(SaveLifeUtils.TAG_NAME, name);
        user.put(SaveLifeUtils.TAG_BTYPE, btype);
        user.put(SaveLifeUtils.TAG_NUM, num);
        user.put(SaveLifeUtils.TAG_EMAIL, email);
        user.put(SaveLifeUtils.TAG_DATE, date);
        sessionManager.setSessionUserDetail(user);
    }
}
