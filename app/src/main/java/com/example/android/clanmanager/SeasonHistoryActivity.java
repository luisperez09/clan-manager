package com.example.android.clanmanager;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.android.clanmanager.pojo.Sancionado;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class SeasonHistoryActivity extends AppCompatActivity {

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mSeasonReference;
    private ChildEventListener mChildEventListener;

    private ListView mListView;
    private TwoLineAdapter mSeasonAdapter;
    private ArrayList<Object> mSancionadosList;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_season_history);

        String key = "";
        if (getIntent().hasExtra("dateKey")) {
            key = getIntent().getStringExtra("dateKey");
            setTitle(key);
        } else {
            Toast.makeText(this, "Temporada inválida", Toast.LENGTH_SHORT).show();
            finish();
        }

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mSeasonReference = mFirebaseDatabase.getReference().child("history").child(key);

        mListView = (ListView) findViewById(R.id.season_history_list);
        mSancionadosList = new ArrayList<>();
        mSeasonAdapter = new TwoLineAdapter(this, mSancionadosList);
        mListView.setAdapter(mSeasonAdapter);

        mProgressBar = (ProgressBar) findViewById(R.id.season_history_progress_bar);
        mProgressBar.getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mProgressBar.setVisibility(View.VISIBLE);
        attachDatabaseListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSeasonAdapter.clear();
        detachDatabaseListener();
    }

    private void attachDatabaseListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Sancionado sancionado = dataSnapshot.getValue(Sancionado.class);
                    mSeasonAdapter.add(sancionado);
                    mProgressBar.setVisibility(View.GONE);

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            mSeasonReference.addChildEventListener(mChildEventListener);
        }
    }

    private void detachDatabaseListener() {
        if (mChildEventListener != null) {
            mSeasonReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }
}
