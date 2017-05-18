package com.example.android.clanmanager;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SancionesEditorActivity extends AppCompatActivity {

    private final static int EDITTEXT_INPUT_LIMIT = 40;

    private TextView mSancionadoTextView;
    private ProgressBar mProgressBar;
    private ListView mListView;
    private StrikeAdapter mStrikeAdapter;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserStrikesReference;
    private ChildEventListener mChildEventListener;
    private ValueEventListener mEmptyCheckListener;
    private String mStrikeReason;

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
        mStrikeAdapter = new StrikeAdapter(this, strikes);
        mListView = (ListView) findViewById(R.id.strikes_list);
        mListView.setAdapter(mStrikeAdapter);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_sanciones_editor);

        mSancionadoTextView = (TextView) findViewById(R.id.sancionado_text_view);
        mSancionadoTextView.setText(username);
        FloatingActionButton button = (FloatingActionButton) findViewById(R.id.agregar_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertDialog();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        attachDatabaseListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mStrikeAdapter.clear();
        detachDatabaseListener();
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(EDITTEXT_INPUT_LIMIT)});
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setTitle("Motivo del strike")
                .setPositiveButton("Agregar strike", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String inputText = input.getText().toString().trim();
                        if (!inputText.isEmpty()) {
                            mStrikeReason = inputText;
                            dialogInterface.dismiss();
                            pushNewStrike();
                        }
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        builder.show();
    }

    private void pushNewStrike() {
        String date = DateFormat.getDateInstance().format(new Date());
        Strike strike = new Strike(date, mStrikeReason);
        mUserStrikesReference.push().setValue(strike);
        Toast.makeText(this, "Se agregó el strike", Toast.LENGTH_SHORT).show();
    }

    private void attachDatabaseListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Strike strike = dataSnapshot.getValue(Strike.class);
                    mStrikeAdapter.add(strike);
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
            mUserStrikesReference.addChildEventListener(mChildEventListener);
        }
        if (mEmptyCheckListener == null) {
            mEmptyCheckListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        View emptyView = findViewById(R.id.strikes_empty_view);
                        emptyView.setVisibility(View.VISIBLE);
                        mListView.setEmptyView(emptyView);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            mUserStrikesReference.addListenerForSingleValueEvent(mEmptyCheckListener);
        }
    }

    private void detachDatabaseListener() {
        if (mChildEventListener != null) {
            mUserStrikesReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
        if (mEmptyCheckListener != null) {
            mUserStrikesReference.removeEventListener(mEmptyCheckListener);
            mEmptyCheckListener = null;
        }
    }
}