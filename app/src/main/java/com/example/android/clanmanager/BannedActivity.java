package com.example.android.clanmanager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class BannedActivity extends AppCompatActivity {

    private EditText mBannedEditText;
    private EditText mReasonEditText;
    private Button mSubmitBanButton;

    // Firebase instance variables
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mBannedDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banned);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mBannedDatabaseReference = mFirebaseDatabase.getReference().child("banned");

        mBannedEditText = (EditText) findViewById(R.id.banned_name);
        mReasonEditText = (EditText) findViewById(R.id.banned_reason);
        mSubmitBanButton = (Button) findViewById(R.id.submit_new_banned_button);

        mSubmitBanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Banned banned = new Banned(
                        mBannedEditText.getText().toString(),
                        mReasonEditText.getText().toString());
                mBannedDatabaseReference.push().setValue(banned);
                finish();
                Toast.makeText(BannedActivity.this, R.string.user_has_been_banned, Toast.LENGTH_SHORT).show();
            }
        });

    }
}
