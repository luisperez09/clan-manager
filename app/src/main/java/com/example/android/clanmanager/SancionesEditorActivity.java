package com.example.android.clanmanager;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SancionesEditorActivity extends AppCompatActivity {

    private TextView mSancionadoTextView;
    private StrikeAdapter mStrikeAdapter;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserStrikesReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sanciones_editor);

        Bundle extras = getIntent().getExtras();
        final String key = extras.getString("key");
        String username = extras.getString("name");
        setTitle(username);


        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUserStrikesReference = mFirebaseDatabase.getReference().child("sancionados").child(key).child("strikes");

        ArrayList<Strike> strikes = new ArrayList<>();
        strikes.add(new Strike("23/323/424", "Se salto una reserva"));
        strikes.add(new Strike("23/323/523", "Se salto otra reserva"));
        mStrikeAdapter = new StrikeAdapter(this, strikes);
        ListView listView = (ListView) findViewById(R.id.strikes_list);
        listView.setAdapter(mStrikeAdapter);

        mSancionadoTextView = (TextView) findViewById(R.id.sancionado_text_view);
        mSancionadoTextView.setText(username);
        FloatingActionButton button = (FloatingActionButton) findViewById(R.id.agregar_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pushNewStrike();
            }
        });
    }

    private void pushNewStrike() {
        String date = DateFormat.getDateInstance().format(new Date());
        Strike strike = new Strike(date, "Ataco tarde");
        mUserStrikesReference.push().setValue(strike);
        Toast.makeText(this, "Se agregó el strike", Toast.LENGTH_SHORT).show();
    }
}