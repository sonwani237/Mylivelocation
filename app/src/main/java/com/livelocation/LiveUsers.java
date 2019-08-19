package com.livelocation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class LiveUsers extends AppCompatActivity implements OnMapReadyCallback {

    ArrayList<UsersList> phoneNumbers;
    RecyclerView recyclerView;
    DatabaseReference ref;
    private MapView upMap;
    private GoogleMap googleMap;
    boolean mark = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_users);
        upMap = findViewById(R.id.upMap);

        phoneNumbers = new ArrayList<>();
        recyclerView = findViewById(R.id.users);

        ref = FirebaseDatabase.getInstance().getReference().child("users");
        ref.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Get map of users in datasnapshot
                        collectPhoneNumbers((Map<String,Object>) dataSnapshot.getValue());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });

        setUpMap(upMap);
    }

    private void setUpMap(MapView map) {
        this.upMap = map;
        upMap.onCreate(null);
        upMap.onResume();
        upMap.getMapAsync(LiveUsers.this);
    }

    @SuppressLint("WrongConstant")
    private void collectPhoneNumbers(Map<String,Object> users) {

        for (Map.Entry<String, Object> entry : users.entrySet()){

            Map singleUser = (Map) entry.getValue();
            UsersList usersList = new UsersList();
            usersList.setEntry(entry.getKey());
            usersList.setName((String) singleUser.get("name"));
            usersList.setMobile((String) singleUser.get("mobile"));
            phoneNumbers.add(usersList);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        userAdapter adapter = new userAdapter(this, phoneNumbers);
        recyclerView.setAdapter(adapter);
    }

    Marker marker;
    BitmapDrawable bitmapdraw;


    public void getToken(String token, final String title){
        mark = true;
        ref.child(token).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                try {
                    if (snapshot.getValue() != null) {
                        try {
                            Map singleUser = (Map) snapshot.getValue();
                            Log.e("TAG", "" + (String) singleUser.get("name")); // your name values you will get here
                            String lat = (String) singleUser.get("lat");
                            String lng = (String) singleUser.get("lng");
                            LatLng coordinate = new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));
                            if (mark){
                                googleMap.clear();
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 15));
                                int height = getResources().getDisplayMetrics().widthPixels / 10;
                                int width = getResources().getDisplayMetrics().widthPixels / 10;
                                bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.drop_marker);
                                Bitmap b = bitmapdraw.getBitmap();
                                Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                                marker = googleMap.addMarker(new MarkerOptions().position(coordinate)
                                        .flat(true)
                                        .title(title)
                                        .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
                                mark = false;
                            }else {
                                marker.setPosition(coordinate);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e("TAG", " it's null.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("onCancelled", " cancelled");
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap Map) {
        this.googleMap = Map;
    }
}
