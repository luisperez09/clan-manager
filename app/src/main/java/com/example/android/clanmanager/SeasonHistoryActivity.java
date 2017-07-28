package com.example.android.clanmanager;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
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
    private String mSeasonDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_season_history);

        mSeasonDate = "";
        if (getIntent().hasExtra("dateKey")) {
            mSeasonDate = getIntent().getStringExtra("dateKey");
            setTitle(mSeasonDate);
        } else {
            Toast.makeText(this, "Temporada inválida", Toast.LENGTH_SHORT).show();
            finish();
        }

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mSeasonReference = mFirebaseDatabase.getReference().child("history").child(mSeasonDate);

        mListView = (ListView) findViewById(R.id.season_history_list);
        mSancionadosList = new ArrayList<>();
        mSeasonAdapter = new TwoLineAdapter(this, mSancionadosList);
        mListView.setAdapter(mSeasonAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Sancionado clickedSancionado = (Sancionado) mSeasonAdapter.getItem(position);
                Intent intent = new Intent(SeasonHistoryActivity.this, HistoryDetailActivity.class);
                intent.putExtra("name", clickedSancionado.getName())
                        .putExtra("seasonDate", mSeasonDate);
                startActivity(intent);
            }
        });

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
