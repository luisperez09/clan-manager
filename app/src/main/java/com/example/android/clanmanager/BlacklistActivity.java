package com.example.android.clanmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class BlacklistActivity extends AppCompatActivity {

    private FloatingActionButton mAddBannedFab;

    // Firebase instance variables
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mBannedDatabaseReference;
    private ChildEventListener mChildEventListener;
    private ValueEventListener mEmptyCheckListener;

    private ListView mlistView;
    private BannedAdapter mBannedAdapter;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blacklist);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mBannedDatabaseReference = mFirebaseDatabase.getReference().child("banned");

        mlistView = (ListView) findViewById(R.id.banned_list);
        ArrayList<Banned> banneds = new ArrayList<>();
        mBannedAdapter = new BannedAdapter(this, banneds);
        mlistView.setAdapter(mBannedAdapter);
        mProgressBar = (ProgressBar) findViewById(R.id.blacklist_progress_bar);

        mAddBannedFab = (FloatingActionButton) findViewById(R.id.fab);
        mAddBannedFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(BlacklistActivity.this, BannedActivity.class));
            }
        });
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
        mBannedAdapter.clear();
        dettachDatabaseListener();
    }

    private void attachDatabaseListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Banned banned = dataSnapshot.getValue(Banned.class);
                    mBannedAdapter.add(banned);
                    mProgressBar.setVisibility(View.INVISIBLE);
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
            mBannedDatabaseReference.addChildEventListener(mChildEventListener);
        }
        if (mEmptyCheckListener == null) {
            mEmptyCheckListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        View emptyView = findViewById(R.id.banned_empty_view);
                        emptyView.setVisibility(View.VISIBLE);
                        mlistView.setEmptyView(emptyView);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            mBannedDatabaseReference.addListenerForSingleValueEvent(mEmptyCheckListener);
        }
    }

    private void dettachDatabaseListener() {
        if (mChildEventListener != null) {
            mBannedDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
        if (mEmptyCheckListener != null) {
            mBannedDatabaseReference.removeEventListener(mEmptyCheckListener);
            mEmptyCheckListener = null;
        }
    }
}
