package com.vivekgupta.bdonor.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.vivekgupta.bdonor.R;
import com.vivekgupta.bdonor.fragments.FragmentCreateRequestScreen;
import com.vivekgupta.bdonor.fragments.FragmentMain;
import com.vivekgupta.bdonor.fragments.FragmentProfileEdit;
import com.vivekgupta.bdonor.fragments.FragmentSearchScreen;
import com.vivekgupta.bdonor.utils.SaveLifeUtils;
import com.vivekgupta.bdonor.utils.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vivek on 27-12-2016.
 */

public class MainActivitySaveLife extends AppCompatActivity {
    public static final int ACTIVITY_REQUEST_CODE = 99;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseAuth auth;
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private TextView userName, userEmailId;
    TextView reqNumView;
    private Toolbar toolbar;
    private Map<String, Object> userQ;
    // index to identify current nav menu item
    public static int navItemIndex = 0;

    // tags used to attach the fragments
    private static final String TAG_HOME = "home";
    private static final String TAG_EDIT = "edit";
    private static final String TAG_LOGOUT = "logout";
    private static final String TAG_ABOUT_US = "about us";
    private static final String TAG_RATE_US = "rate us";
    private static final String TAG_DELETE = "delete account";
    private static final String TAG_REQUESTS = "Requests";
    public static String CURRENT_TAG = TAG_HOME;
    // toolbar titles respected to selected nav menu item
    private String[] toolBarTitles;
    private Handler mHandler;
    private FirebaseAuth.AuthStateListener authListener;
    SessionManager sessionManager;
    private SaveLifeUtils saveLifeUtils;
    private FragmentMain homeFragment;
    private boolean fromNotification = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layout);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                //Log.d(SaveLifeUtils.TAG, "Key: " + key + " Value: " + value);
            }
        }
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mHandler = new Handler();

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        // Navigation view header
        View navHeader = navigationView.getHeaderView(0);
        userName = navHeader.findViewById(R.id.nav_header_name);
        userEmailId = navHeader.findViewById(R.id.nav_header_mailId);
        sessionManager = new SessionManager(this);
        //ImageView imgNavHeaderBg = (ImageView) navHeader.findViewById(R.id.img_header_bg);

        // load toolbar titles from string resources
        toolBarTitles = getResources().getStringArray(R.array.nav_item_activity_titles);


        subscribeToTopic();
        // load nav menu header data
        loadNavigationHeader();

        // initializing navigation menu
        setUpNavigationView();
        Bundle bundle = getIntent().getExtras();
        navItemIndex=0;
        //Log.e(SaveLifeUtils.TAG, "bundle is: "+ getIntent().getExtras());

//        if(bundle != null || savedInstanceState == null) {
//            /*do not change order of these if statements*/
//            if (savedInstanceState == null) {
//                navItemIndex = 0;
//                CURRENT_TAG = TAG_HOME;
//                Log.e(SaveLifeUtils.TAG, "savedInstanceState is null");
//            }
//            if(bundle != null) {
//                Log.e(SaveLifeUtils.TAG, "bundle is not null");
//                //noti = bundle.getBoolean("from_notification");
//                //if (noti) {
//                    navItemIndex = 1;
//                    CURRENT_TAG = TAG_REQUESTS;
//                //}
//            }
//            loadHomeFragment();
//        }

        if(bundle != null){
            String str = bundle.getString(SaveLifeUtils.KEY_NOTIFICATION);
            if(str.equals("notify_intent")){
                fromNotification = true;
            }
        }
        loadHomeFragment();
        //if(!noti)
            //startFireBaseService();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String str = intent.getStringExtra(SaveLifeUtils.KEY_NOTIFICATION);
        if(str != null){
            if(str.equals("notify_intent")){
                drawer.openDrawer(Gravity.START);
            }
        }
    }

    private void subscribeToTopic() {
        saveLifeUtils = SaveLifeUtils.getInstance(getApplicationContext());
        FirebaseMessaging.getInstance().subscribeToTopic(SaveLifeUtils.NOTIFICATION_TOPIC+"_"+saveLifeUtils.getCountryCode());
    }

    @Override
    protected void onStart() {
        super.onStart();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //Log.e(SaveLifeUtils.TAG, "inside onAuthStateChanged!!");
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    //stop location updates
                    //FragmentSearchScreen fragmentSearchScreen = (FragmentSearchScreen) getSupportFragmentManager().findFragmentById(R.id.home_scroll_view);
                    FragmentSearchScreen fragmentSearchScreen = (FragmentSearchScreen) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.fragment_main + ":"
                    +homeFragment.getPagerAdapter().getItem(0));
                    //fragmentSearchScreen.removeLocationUpdates();
                    //start login activity
                    if(fragmentSearchScreen == null)
                        Log.e(SaveLifeUtils.TAG, "FragmentSearchScreen returns null!!!!!");
                    else
                        fragmentSearchScreen.removeLocationUpdates();
                    startActivity(new Intent(MainActivitySaveLife.this, LoginActivity.class));
                    finish();
                }
            }
        };
        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() == null) {
            startActivity(new Intent(MainActivitySaveLife.this, LoginActivity.class));
            finish();
        }
        auth.addAuthStateListener(authListener);
    }

    protected void onStop(){
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ACTIVITY_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                if (navItemIndex != 0) {
                    navItemIndex = 0;
                    CURRENT_TAG = TAG_HOME;
                    selectNavMenu();
                }
            }
        }
    }

    private void loadNavigationHeader() {
        queryCurrentData();
        int numAll = sessionManager.getRequestNumByBtype("ALL");
        MenuItem menuItem = navigationView.getMenu().getItem(1);
        menuItem.setActionView(R.layout.request_dot);
        View actionView = menuItem.getActionView();
        reqNumView = actionView.findViewById(R.id.action_view_req_num);
        if(numAll != 0){
            reqNumView.setVisibility(View.VISIBLE);
            reqNumView.setText(String.valueOf(numAll));
        }
        else
            reqNumView.setVisibility(View.GONE);
    }

    private void loadHomeFragment() {
        // selecting navigation menu item
        selectNavMenu();
        // set toolbar title
        //setToolbarTitle();
        //close the navigation drawer if user selects current item
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
            drawer.closeDrawers();
            return;
        }

       //call to deterrmine which option selected and function correspondingly
        getSelectedOptionMenu();
        //Closing drawer on item click
        drawer.closeDrawers();

        // refresh toolbar menu
        invalidateOptionsMenu();
    }

    private void getSelectedOptionMenu() {
        switch (navItemIndex) {
            case 0:
                // home
                homeFragment = new FragmentMain();
                startFragment(homeFragment);
                // set toolbar title
                setToolbarTitle();
                if(fromNotification){
                    fromNotification = false;
                    drawer.openDrawer(Gravity.START);
                }
                break;
            case 1://request list
                Intent intent = new Intent(this, BListActivity.class);
                startActivityForResult(intent, ACTIVITY_REQUEST_CODE);
                //startActivity(intent);
                break;
            case 2:
                // edit profile
                FragmentProfileEdit profileFragment = new FragmentProfileEdit();
                startFragment(profileFragment);
                // set toolbar title
                setToolbarTitle();
                break;
            case 3:
                //signOutLifeSaver();
                createAlertDialog("Log Out", "Are you sure?");
                navigationView.getMenu().getItem(0).setChecked(true);

                break;
//            case 4://ABOUT US
//                Toast.makeText(this, "not implemented!", Toast.LENGTH_SHORT).show();
//                break;
            case 4://RATE US
                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }catch(Exception e){
                    Toast.makeText(this, "not implemented!", Toast.LENGTH_SHORT).show();
                }

                break;
            case 5://DELETE
                createDeleteAlertDialog();

                break;
            default:
                startFragment(new FragmentCreateRequestScreen());
        }
    }

    private void startFragment(final Fragment fragment){
        //load the fragment with cross fade effect if it has huge data
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                // update the main content by replacing fragments
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                //fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        //android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
                fragmentTransaction.commitAllowingStateLoss();
               // fragmentTransaction.addToBackStack(null);
                //fragmentTransaction.commit();
            }
        };
        // If mPendingRunnable is not null, then add to the message queue
        mHandler.post(mPendingRunnable);
    }

    private void deleteUserAuthentication(){
        auth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    //stopService(new Intent(MainActivitySaveLife.this, FireBaseService.class));
                    //enable ui
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    // launch login activity
                    startActivity(new Intent(MainActivitySaveLife.this, LoginActivity.class));
                    finish();
                }
                else{
                    //Toast.makeText(this, "sorry unable to service", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void deleteUserDataBase(){
        String userId = auth.getCurrentUser().getUid();

        mFirebaseDatabase.child("users_" + saveLifeUtils.getCountryCode()).child(userId)
                .removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        deleteUserAuthentication();
                    }
                });
        //remove notifications also
        DatabaseReference notifications =
                mFirebaseDatabase.child("notifications").child(userId);
       if(notifications != null)
           notifications.removeValue();
    }

    private void deleteUserAccount(){
        deleteUserDataBase();
    }

    private void signOutLifeSaver() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(SaveLifeUtils.NOTIFICATION_TOPIC+"_"+saveLifeUtils.getCountryCode());
        auth.signOut();
    }

    private void reAutheticateUser(String password){
       // progressBar.setVisibility(View.VISIBLE);
        //disable ui
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        //reauthenticate and call
        Map<String, Object> user = new HashMap<>();
        sessionManager.getSessionUserDetail(user);
        String email = String.valueOf(user.get(SaveLifeUtils.TAG_EMAIL));
        AuthCredential authCredential = EmailAuthProvider.getCredential(email, password);
        auth.getCurrentUser().reauthenticate(authCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                makeToast("deleting account");
                deleteUserAccount();
            }
        });

    }

    private void createDeleteAlertDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Password");
        //set input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        builder.setView(input);
        //set buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = input.getText().toString();
                if(TextUtils.isEmpty(password)) {
                    input.setError("Required");
                    return;
                }
                closeKeypad(input);
                dialog.cancel();
                closeKeypad();
                reAutheticateUser(password);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }
    private void createAlertDialog(String dialog_title, String dialog_msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Add the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                signOutLifeSaver();
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                dialog.cancel();
            }
        });
    // Set other dialog properties
        builder.setMessage(dialog_msg)
                .setTitle(dialog_title);

    // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void queryCurrentData() {
        //Log.e(SaveLifeUtils.TAG, "queryCurrentData");
        auth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference();
        String userId = auth.getCurrentUser().getUid();
        // query user info
        Query queryRef = mFirebaseDatabase.child("users_" + saveLifeUtils.getCountryCode()).child(userId);
        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot != null) {
                    userQ = (Map<String, Object>) dataSnapshot.getValue();
                    //Log.e(SaveLifeUtils.TAG, "user profile:" + userQ);
                    if(userQ != null) {
                        userName.setText(String.valueOf(userQ.get(SaveLifeUtils.TAG_NAME)));
                        userEmailId.setText(String.valueOf(userQ.get(SaveLifeUtils.TAG_EMAIL)));
                        //save data for further use
                        setUserProfile(userQ);
                    }

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                queryCurrentData();
            }
        });
    }

    private void setToolbarTitle() {
        getSupportActionBar().setTitle(toolBarTitles[navItemIndex]);
        //getSupportActionBar().setTitle(Html.fromHtml("<font color='#59380d'>hello </font>"));
    }

    private void selectNavMenu() {
        if(navItemIndex < 3)//not checking for items greater than 3
            navigationView.getMenu().getItem(navItemIndex).setChecked(true);
    }

    private void setUpNavigationView() {
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.nav_home:
                        navItemIndex = 0;
                        CURRENT_TAG = TAG_HOME;
                        break;
                    case R.id.nav_requests:
                        navItemIndex = 1;
                        CURRENT_TAG = TAG_REQUESTS;
                        break;
                    case R.id.nav_edit:
                        navItemIndex = 2;
                        CURRENT_TAG = TAG_EDIT;
                        break;
                    case R.id.nav_logout:
                        navItemIndex = 3;
                        CURRENT_TAG = TAG_LOGOUT;
                        break;
                    case R.id.nav_rate_us:
                        navItemIndex = 4;
                        CURRENT_TAG = TAG_RATE_US;
                        break;
                    case R.id.nav_user_delete:
                        navItemIndex = 5;
                        CURRENT_TAG = TAG_DELETE;
                        break;
                    default:
                        navItemIndex = 0;
                }

                //Checking if the item is in checked state or not, if not make it in checked state
//                if(navItemIndex == 0 || navItemIndex == 2) {
//                    if (menuItem.isChecked()) {
//                        menuItem.setChecked(false);
//                    } else {
//                        menuItem.setChecked(true);
//                    }
//                    menuItem.setChecked(true);
//                }
//                if(navItemIndex == 1){
//                    menuItem.setChecked(false);
//                }
                //if(navItemIndex < 4)
                    //menuItem.setChecked(true);
                loadHomeFragment();
                return true;
            }
        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                int numAll = sessionManager.getRequestNumByBtype("ALL");
                if(numAll != 0){
                    reqNumView.setVisibility(View.VISIBLE);
                    reqNumView.setText(String.valueOf(numAll));
                }
                else{
                    reqNumView.setVisibility(View.GONE);
                }

                closeKeypad();
            }
        };
        //Setting the actionbarToggle to drawer layout
        drawer.setDrawerListener(actionBarDrawerToggle);
        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
            return;
        }

        // checking if user is on other navigation menu
        // rather than home
        if (navItemIndex != 0) {
            navItemIndex = 0;
            CURRENT_TAG = TAG_HOME;
            loadHomeFragment();
            return;
        }

        super.onBackPressed();
    }

    private void closeKeypad(){
        closeKeypad(this.getCurrentFocus());
    }

    private void closeKeypad(View view) {
        if(view != null) {
            saveLifeUtils.hideSoftKeyBoard(view, this);
        }
    }
    public void setUserProfile(Map<String, Object> userQ){
        sessionManager.setSessionUserDetail(userQ);

    }
    @Override
    protected void onResume() {
        super.onResume();
        if(mFirebaseDatabase == null)
            mFirebaseDatabase = FirebaseDatabase.getInstance().getReference();
        if(auth == null)
            auth = FirebaseAuth.getInstance();
    }

    private void makeToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
