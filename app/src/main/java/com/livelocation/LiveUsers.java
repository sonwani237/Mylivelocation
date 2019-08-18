package com.livelocation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class LiveUsers extends AppCompatActivity {

    ArrayList<UsersList> phoneNumbers;
    RecyclerView recyclerView;
    DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_users);

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

    public void getToken(String token){
        ref.child(token).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                try {
                    if (snapshot.getValue() != null) {
                        try {
                            Map singleUser = (Map) snapshot.getValue();
//                            userData.setName((String) singleUser.get("name"));
//                            userData.setMobile((String) singleUser.get("mobile"));
                            Log.e("TAG", "" + (String) singleUser.get("name")); // your name values you will get here
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

}
