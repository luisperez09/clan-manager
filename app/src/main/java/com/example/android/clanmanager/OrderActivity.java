package com.example.android.clanmanager;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class OrderActivity extends AppCompatActivity {

    private final static int EDITTEXT_INPUT_LIMIT = 40;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mColeadersReference;
    private ChildEventListener mChildEventListener;
    private ValueEventListener mEmptyCheckListener;

    private ProgressBar mProgressBar;
    private ListView mListView;
    private TwoLineAdapter mTwoLineAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mColeadersReference = mFirebaseDatabase.getReference().child("coleaders");

        ArrayList<Object> data = new ArrayList<>();
        mTwoLineAdapter = new TwoLineAdapter(this, data);
        mListView = (ListView) findViewById(R.id.coleaders_list);
        mListView.setAdapter(mTwoLineAdapter);
        mProgressBar = (ProgressBar) findViewById(R.id.coleaders_pb);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_coleader_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddColeaderDialog();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mProgressBar.setVisibility(View.VISIBLE);
        attachDatabaseListeners();
    }

    private void showAddColeaderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(EDITTEXT_INPUT_LIMIT)});
        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        builder.setTitle("Agregar Colíder");
        builder.setView(input)
                .setPositiveButton("Agregar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String inputText = input.getText().toString().trim();
                        if (!inputText.isEmpty()) {
                            addNewColeader(inputText);
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        final AlertDialog dialog = builder.create();
        input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
        dialog.show();
    }

    private void addNewColeader(String coleaderName) {
        Coleader coleader = new Coleader(coleaderName, false);
        mColeadersReference.push().setValue(coleader);
        Toast.makeText(this, "Se agregó el colíder", Toast.LENGTH_SHORT).show();
    }

    private void attachDatabaseListeners() {
        if (mEmptyCheckListener == null) {
            mEmptyCheckListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        View emptyView = findViewById(R.id.coleaders_empty_view);
                        emptyView.setVisibility(View.VISIBLE);
                        mListView.setEmptyView(emptyView);

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            mColeadersReference.addListenerForSingleValueEvent(mEmptyCheckListener);
        }
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Log.w("OrderActivity", "Child added");
                    mProgressBar.setVisibility(View.INVISIBLE);
                    Coleader coleader = dataSnapshot.getValue(Coleader.class);
                    mTwoLineAdapter.add(coleader);

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
            mColeadersReference.addChildEventListener(mChildEventListener);
        }
    }
}
