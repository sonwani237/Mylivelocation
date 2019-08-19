package com.livelocation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    EditText ed_name, ed_num;
    Button button;
    TextView txt_user, loc;

    private static DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;

    private static String userId;
    private String TAG="FireBase";
    private static final int REQUEST_PERMISSIONS = 1;
    private static String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

    private Location mLastKnownLocation;
    FusedLocationProviderClient mFusedLocationProviderClient;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        ReadCurrentLocationPermission();
        button = findViewById(R.id.button);
        ed_name = findViewById(R.id.ed_name);
        ed_num = findViewById(R.id.ed_num);
        txt_user = findViewById(R.id.txt_user);
        loc = findViewById(R.id.loc);

        if (Utils.INSTANCE.getSession(this)!=null){
            userId = Utils.INSTANCE.getSession(this);
            txt_user.setText(Utils.INSTANCE.getName(this)+" "+Utils.INSTANCE.getNum(this));
            button.setVisibility(View.GONE);
            ed_name.setVisibility(View.GONE);
            ed_num.setVisibility(View.GONE);

            handler = new Handler(this.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    getDeviceLocation();
                    handler.postDelayed(this, 5000);
                }
            }, 1000);

        }

        FirebaseApp.initializeApp(MainActivity.this);

        button.setOnClickListener(this);

        mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseDatabase = mFirebaseInstance.getReference("users");
        mFirebaseInstance.getReference("app_title").setValue("Realtime Location");
        mFirebaseInstance.getReference("app_title").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String appTitle = dataSnapshot.getValue(String.class);
                getSupportActionBar().setTitle(appTitle);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read app title value.", error.toException());
            }
        });

    }

    @SuppressLint("MissingPermission")
    public void getDeviceLocation() {
        try {
            Task locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if (task.isSuccessful()) {
                        try {
                            mLastKnownLocation = (Location) task.getResult();
                            LatLng coordinate = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                            Log.e(TAG, "App coordinate updated"+coordinate);
                            loc.setText(""+coordinate);
                            updateUser(""+mLastKnownLocation.getLatitude(), ""+mLastKnownLocation.getLongitude());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (SecurityException e) {
        }
    }


    @Override
    public void onClick(View v) {
        if (v == button){
            String name = ed_name.getText().toString();
            String num = ed_num.getText().toString();

            // Check for already existed userId
            if (TextUtils.isEmpty(userId)) {
                createUser(name, num);
            }
        }
    }

    private void createUser(String name, String mobile) {
        if (TextUtils.isEmpty(userId)) {
            userId = mFirebaseDatabase.push().getKey();
        }
        User user = new User(name, mobile);
        mFirebaseDatabase.child(userId).setValue(user);

        addUserChangeListener();
    }

    private void addUserChangeListener() {
        // User data change listener
        mFirebaseDatabase.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                if (user == null) {
                    return;
                }
                txt_user.setText(user.name + ", " + user.mobile);
                ed_num.setText("");
                ed_name.setText("");

                button.setVisibility(View.GONE);
                ed_name.setVisibility(View.GONE);
                ed_num.setVisibility(View.GONE);

                Utils.INSTANCE.setLoginPref(MainActivity.this, user.name, user.mobile, userId );
                handler = new Handler(MainActivity.this.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        getDeviceLocation();
                        handler.postDelayed(this, 5000);
                    }
                }, 1000);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read user", error.toException());
            }
        });
    }

    public void ReadCurrentLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) ) {

            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSIONS);
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {

        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length == 3 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                        (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
        }
    }

    public void updateUser(String lat, String lng) {

        mFirebaseDatabase.child(userId).child("lat").setValue(lat);
        mFirebaseDatabase.child(userId).child("lng").setValue(lng);

    }
}
