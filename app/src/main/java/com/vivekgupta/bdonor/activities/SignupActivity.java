package com.vivekgupta.bdonor.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.vivekgupta.bdonor.R;
import com.vivekgupta.bdonor.User;
import com.vivekgupta.bdonor.utils.SaveLifeUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by vivek on 24-12-2016.
 */

public class SignupActivity extends AppCompatActivity {
    AutoCompleteTextView autoBloodType;
    private SaveLifeUtils saveLifeUtils;
    private EditText inputEmail, inputPassword, phoneNumber, inputName;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private String strBtype, strEmail, strNum = "", strName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        Button btnSignIn = findViewById(R.id.sign_in_button);
        Button btnSignUp = findViewById(R.id.sign_up_button);
        inputName = findViewById(R.id.user_name);
        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.password);
        autoBloodType = findViewById(R.id.signup_blood_type);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.btype_array, android.R.layout.simple_list_item_1);
        // adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        autoBloodType.setAdapter(adapter);
        autoBloodType.setThreshold(1);

        phoneNumber = findViewById(R.id.user_phone);
        progressBar = findViewById(R.id.progressBar);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeypad();
                strEmail = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                strBtype = autoBloodType.getText().toString().trim();
                strNum = phoneNumber.getText().toString();
                strName = inputName.getText().toString();

                if (TextUtils.isEmpty(strName)) {
                    inputName.setError("Required");
                    return;
                }
                if (TextUtils.isEmpty(strEmail)) {
                    inputEmail.setError("Required");
                    return;
                }
                if (!strEmail.contains("@") || !strEmail.contains(".com")) {
                    inputEmail.setError("invalid");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    inputPassword.setError("Required");
                    return;
                }

                if (password.length() < 5) {
                    Toast.makeText(getApplicationContext(), "Password too short, enter minimum 5 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(strBtype)) {
                    //Toast.makeText(getApplicationContext(), "blood type required!", Toast.LENGTH_SHORT).show();
                    autoBloodType.setError("Required");
                    return;
                }

                List<String> blist = Arrays.asList(getResources().getStringArray(R.array.btype_array));
                if (!blist.contains(strBtype.toUpperCase())) {
                    autoBloodType.setError("Invalid");
                    return;
                }
                if (TextUtils.isEmpty(strNum)) {
                    phoneNumber.setError("Required");
                    return;
                }
                if (TextUtils.getTrimmedLength(strNum) > 0) {
                    if (TextUtils.getTrimmedLength(strNum) != 10) {
                        phoneNumber.setError("enter 10 digits");
                        return;
                    }
                }

                progressBar.setVisibility(View.VISIBLE);
                //create user
                auth.createUserWithEmailAndPassword(strEmail, password)
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                //Toast.makeText(SignupActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Toast.makeText(SignupActivity.this, "Authentication failed." + task.getException(),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    //store to DB
                                    Toast.makeText(SignupActivity.this, "account created!", Toast.LENGTH_SHORT).show();
                                    writeNewUser();
                                }
                            }
                        });

            }
        });

        saveLifeUtils = SaveLifeUtils.getInstance(getApplicationContext());
    }

    private void writeNewUser() {
        //get country of the user which should be first child of database
        String countryISOCode = saveLifeUtils.getCountryCode();
        //get uid of user which is second child of database
        String userId = auth.getCurrentUser().getUid();
        //create user details
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String formatDate = df.format(c.getTime());
        User user = new User(strName, strBtype, strNum, strEmail, 0.0, 0.0, formatDate, true);
        //store
        DatabaseReference mFirebaseDatabase = FirebaseDatabase.getInstance().getReference();
        mFirebaseDatabase.child("users_" + countryISOCode).child(userId).setValue(user);
        mFirebaseDatabase.child("users_" + countryISOCode).child(userId).child("req_msg").setValue("");
        progressBar.setVisibility(View.GONE);
        //startActivity(new Intent(CompleteUserProfileActivity.this, MainActivity.class));
        Intent intent = new Intent(SignupActivity.this, MainActivitySaveLife.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        //intent.putExtra("from_notification", false);
        startActivity(intent);
        finish();

    }

    private void closeKeypad() {
        View view = this.getCurrentFocus();
        if (view != null) {
            saveLifeUtils.hideSoftKeyBoard(view, this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }
}
