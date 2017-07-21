package com.example.android.clanmanager;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.clanmanager.pojo.Banned;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class BannedActivity extends AppCompatActivity {

    private EditText mBannedEditText;
    private EditText mReasonEditText;
    private FloatingActionButton mSubmitBanButton;

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
        mSubmitBanButton = (FloatingActionButton) findViewById(R.id.submit_new_banned_button);

        mSubmitBanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Banned banned = new Banned(
                        mBannedEditText.getText().toString(),
                        mReasonEditText.getText().toString());
                if (isValidBan(banned)) {
                    mBannedDatabaseReference.push().setValue(banned);
                    finish();
                    Toast.makeText(BannedActivity.this, R.string.user_has_been_banned, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BannedActivity.this, R.string.all_fields_required, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    /**
     * Helper method que valida datos a insertar a la DB
     *
     * @param banned objeto a validar
     * @return true si todos los campos están poblados, false si al menos uno está en blanco
     */
    private boolean isValidBan(Banned banned) {
        String bannedName = banned.getBanned().trim();
        String bannedReason = banned.getReason().trim();
        return (!bannedName.isEmpty() && !bannedReason.isEmpty());
    }
}
