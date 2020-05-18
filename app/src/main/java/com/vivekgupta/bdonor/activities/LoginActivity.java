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
import com.vivekgupta.bdonor.utils.SaveLifeUtils;
import com.vivekgupta.bdonor.utils.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vivek on 24-12-2016.
 */

public class LoginActivity extends AppCompatActivity {
    private EditText inputPassword;
    private AutoCompleteTextView inputEmail;
    private FirebaseAuth auth;
    private ProgressBar progressBar;
    private SaveLifeUtils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();
        utils = SaveLifeUtils.getInstance(getApplicationContext());

        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivitySaveLife.class));
            finish();
        }
        // set the view now
        setContentView(R.layout.activity_login);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        inputEmail = findViewById(R.id.email);
        addEmailsToAutoComplete();
        inputPassword = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);
        Button btnSignup = findViewById(R.id.btn_signup);
        Button btnLogin = findViewById(R.id.btn_login);
        Button btnReset = findViewById(R.id.btn_reset_password);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeypad();
                String email = inputEmail.getText().toString();
                final String password = inputPassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //authenticate user
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                progressBar.setVisibility(View.GONE);
                                if (!task.isSuccessful()) {
                                    // there was an error
                                    //if (password.length() < 6) {
                                      //  inputPassword.setError(getString(R.string.minimum_password));
                                    //} else {
                                        Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                                    //}
                                } else {
                                    Intent intent = new Intent(LoginActivity.this, MainActivitySaveLife.class);
                                    //intent.putExtra("from_notification", false);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
            }
        });
    }
    private void closeKeypad() {
        View view = this.getCurrentFocus();
        if(view != null) {
            utils.hideSoftKeyBoard(view, this);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
//        if(utils == null)
//            utils = new SaveLifeUtils(getApplicationContext());
    }
    private void addEmailsToAutoComplete() {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        List<String> emailAddressCollection = new ArrayList<>();
        Map<String, Object> userDetail = new HashMap<>();
        SessionManager sessionManager = new SessionManager(this);
        sessionManager.getSessionUserDetail(userDetail);
        String email = String.valueOf(userDetail.get(SaveLifeUtils.TAG_EMAIL));
        if(!TextUtils.isEmpty(email)) {
            emailAddressCollection.add(email);
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<>(LoginActivity.this,
                            android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

            inputEmail.setAdapter(adapter);
        }
    }
}
