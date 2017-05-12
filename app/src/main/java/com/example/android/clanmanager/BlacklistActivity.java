package com.example.android.clanmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class BlacklistActivity extends AppCompatActivity {

    private FloatingActionButton mAddBannedFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blacklist);

        mAddBannedFab = (FloatingActionButton) findViewById(R.id.fab);
        mAddBannedFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(BlacklistActivity.this, BannedActivity.class));
            }
        });
    }
}
