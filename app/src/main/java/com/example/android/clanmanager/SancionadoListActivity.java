package com.example.android.clanmanager;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class SancionadoListActivity extends AppCompatActivity {

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mSancionadosReference;
    private ChildEventListener mChildEventListener;
    private SancionadoAdapter mSancionadoAdapter;

    private String mSancionadoInput;
    private ProgressBar mProgressBar;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sancionado_list);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mSancionadosReference = mFirebaseDatabase.getReference().child("sancionados");

        final ArrayList<Sancionado> sancionados = new ArrayList<Sancionado>();
        mSancionadoAdapter = new SancionadoAdapter(this, sancionados);
        mListView = (ListView) findViewById(R.id.sancionados_list);
        mListView.setAdapter(mSancionadoAdapter);
        mProgressBar = (ProgressBar) findViewById(R.id.sancionado_progress_bar);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Sancionado clickedSancionado = mSancionadoAdapter.getItem(position);
                String key = clickedSancionado.getKey();
                Toast.makeText(SancionadoListActivity.this, "Key: " + key, Toast.LENGTH_SHORT).show();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_sancionado_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertDialog();
            }
        });
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sancionar a:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input)
                .setPositiveButton("Sancionar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        String inputText = input.getText().toString().trim();
                        if (!inputText.isEmpty()) {
                            mSancionadoInput = inputText;
                            dialog.dismiss();
                            pushNewSancionado();
                        }
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mProgressBar.setVisibility(View.VISIBLE);
        attachDatabaseListener();
    }


    @Override
    protected void onPause() {
        super.onPause();
        mSancionadoAdapter.clear();
        dettachDatabaseListener();
    }

    private void pushNewSancionado() {
        Sancionado sancionado = new Sancionado(mSancionadoInput);
        mSancionadosReference.push().setValue(sancionado);
        Toast.makeText(this, "Nuevo sancionado: " + mSancionadoInput, Toast.LENGTH_SHORT).show();
    }


    private void attachDatabaseListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Sancionado sancionado = dataSnapshot.getValue(Sancionado.class);
                    String key = dataSnapshot.getKey();
                    sancionado.setKey(key);
                    mSancionadoAdapter.add(sancionado);
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
            mSancionadosReference.addChildEventListener(mChildEventListener);
        }
    }

    private void dettachDatabaseListener() {
        if (mChildEventListener != null) {
            mSancionadosReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }
}












