package com.example.android.clanmanager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mHistoryReference;
    private ChildEventListener mChildEventListener;

    private ListView mListView;
    private List<String> mDates = new ArrayList<>();
    private ArrayAdapter<String> mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mHistoryReference = mFirebaseDatabase.getReference().child("history_index");

        mListView = (ListView) findViewById(R.id.history_list_view);
        mAdapter = new ArrayAdapter<String>(this, R.layout.history_list_item, mDates);
        mListView.setAdapter(mAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachDatabaseListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        detachDatabaseListener();
    }

    private void attachDatabaseListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    String date = (String) dataSnapshot.getValue();
                    mAdapter.add(date);
                    mAdapter.notifyDataSetChanged();
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
            mHistoryReference.addChildEventListener(mChildEventListener);
        }
    }

    private void detachDatabaseListener() {
        if (mChildEventListener != null) {
            mHistoryReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }
}
