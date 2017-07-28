package com.example.android.clanmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.clanmanager.pojo.Strike;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HistoryDetailActivity extends AppCompatActivity {

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDetailReference;
    private ValueEventListener mEmptyCheckListener;
    private ChildEventListener mChildEventListener;

    private ProgressBar mProgressBar;
    private ListView mListView;
    private ArrayList<Object> mStrikeList;
    private TwoLineAdapter mStrikeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);

        Intent intent = getIntent();
        String name = "";
        String seasonDate = "";
        if (intent.hasExtra("name") && intent.hasExtra("seasonDate")) {
            name = intent.getStringExtra("name");
            seasonDate = intent.getStringExtra("seasonDate");
            setTitle(name);
        } else {
            Toast.makeText(this, "Usuario inválido", Toast.LENGTH_SHORT).show();
            finish();
        }

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDetailReference = mFirebaseDatabase.getReference()
                .child("history").child(seasonDate).child(name).child("strikes");

        mListView = (ListView) findViewById(R.id.strikes_list);
        mStrikeList = new ArrayList<>();
        mStrikeAdapter = new TwoLineAdapter(this, mStrikeList);

        mListView.setAdapter(mStrikeAdapter);

        mProgressBar = (ProgressBar) findViewById(R.id.pb_sanciones_editor);

        TextView nameTextView = (TextView) findViewById(R.id.sancionado_text_view);
        nameTextView.setText(name);
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
        mStrikeAdapter.clear();
        detachDatabaseListener();
    }

    private void attachDatabaseListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Strike strike = dataSnapshot.getValue(Strike.class);
                    mStrikeAdapter.add(strike);
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
            mDetailReference.addChildEventListener(mChildEventListener);
        }

        if (mEmptyCheckListener == null) {
            mEmptyCheckListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        View emptyView = findViewById(R.id.strikes_empty_view);
                        emptyView.setVisibility(View.VISIBLE);
                        mListView.setEmptyView(emptyView);
                        mProgressBar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            mDetailReference.addValueEventListener(mEmptyCheckListener);
        }
    }

    private void detachDatabaseListener() {
        if (mChildEventListener != null) {
            mDetailReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }

        if (mEmptyCheckListener != null) {
            mDetailReference.removeEventListener(mEmptyCheckListener);
            mEmptyCheckListener = null;
        }
    }
}