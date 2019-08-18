package com.livelocation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    EditText ed_name, ed_num;
    Button button;
    TextView txt_user, loc;

    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;

    private String userId;
    private String TAG="FireBase";

    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    private static final long INTERVAL = 1000 * 2;
    private static final long FASTEST_INTERVAL = 1000 * 1;
    Location mCurrentLocation, lStart, lEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

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
            createLocationRequest();
        }

        FirebaseApp.initializeApp(MainActivity.this);

        button.setOnClickListener(this);

        mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseDatabase = mFirebaseInstance.getReference("users");
        mFirebaseInstance.getReference("app_title").setValue("Realtime Location");
        mFirebaseInstance.getReference("app_title").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e(TAG, "App title updated");

                String appTitle = dataSnapshot.getValue(String.class);

                // update toolbar title
                getSupportActionBar().setTitle(appTitle);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read app title value.", error.toException());
            }
        });

    }

    @Override
    public void onClick(View v) {
        if (v == button){
            String name = ed_name.getText().toString();
            String num = ed_num.getText().toString();

            // Check for already existed userId
            if (TextUtils.isEmpty(userId)) {
                createUser(name, num);
            } else {
                updateUser(name, num);
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
                createLocationRequest();

                button.setVisibility(View.GONE);
                ed_name.setVisibility(View.GONE);
                ed_num.setVisibility(View.GONE);

                Utils.INSTANCE.setLoginPref(MainActivity.this, user.name, user.mobile, userId );

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read user", error.toException());
            }
        });
    }

    private void updateUser(String lat, String lng) {

        mFirebaseDatabase.child(userId).child("lat").setValue(lat);
        mFirebaseDatabase.child(userId).child("lng").setValue(lng);

    }

    @SuppressLint("RestrictedApi")
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(Bundle bundle) {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);
        } catch (SecurityException e) {
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        if (lStart == null) {
            lStart = mCurrentLocation;
            lEnd = mCurrentLocation;
        } else
            lEnd = mCurrentLocation;

        loc.setText(lStart+"   "+lEnd);
        Toast.makeText(MainActivity.this,lStart+"   "+lEnd, Toast.LENGTH_LONG).show();
        updateUser(""+lStart, ""+lEnd);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
