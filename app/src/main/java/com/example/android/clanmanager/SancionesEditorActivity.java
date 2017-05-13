package com.example.android.clanmanager;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SancionesEditorActivity extends AppCompatActivity {

    private TextView mSancionadoTextView;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mSancionadoReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sanciones_editor);

        Bundle extras = getIntent().getExtras();
        final String key = extras.getString("key");
        setTitle(extras.getString("name"));


        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mSancionadoReference = mFirebaseDatabase.getReference().child("sancionados");


        mSancionadoTextView = (TextView) findViewById(R.id.sancionado_text_view);
        FloatingActionButton button = (FloatingActionButton) findViewById(R.id.agregar_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SancionesEditorActivity.this, key, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
