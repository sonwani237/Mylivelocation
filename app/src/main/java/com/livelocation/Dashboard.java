package com.livelocation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Dashboard extends AppCompatActivity implements View.OnClickListener {

    Button getLoc, sendLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        getLoc = findViewById(R.id.getLoc);
        sendLoc = findViewById(R.id.sendLoc);

        getLoc.setOnClickListener(this);
        sendLoc.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.getLoc:
                startActivity(new Intent(this, LiveUsers.class));
                break;
            case R.id.sendLoc:

                startActivity(new Intent(this, MainActivity.class));

                break;
        }
    }
}
