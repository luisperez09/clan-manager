package com.example.android.clanmanager;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.android.clanmanager.pojo.Banned;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Muestra lista de los miembros baneados del clan
 */
public class BlacklistActivity extends AppCompatActivity {

    private FloatingActionButton mAddBannedFab;

    // Firebase instance variables
    /**
     * Instancia de la base de datos de Firebase
     */
    private FirebaseDatabase mFirebaseDatabase;
    /**
     * Referencia de la base de datos que apunta al nodo de los baneados
     */
    private DatabaseReference mBannedDatabaseReference;
    /**
     * Listener de los nodos hijos de la referencia de los baneados
     */
    private ChildEventListener mChildEventListener;
    /**
     * Listener para chequeo de existencia de datos en la referencia de los baneados
     */
    private ValueEventListener mEmptyCheckListener;

    // Objetos para manejo de la UI
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
        mProgressBar.getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
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
        // Muestra ProgressBar y adjunta Listeners de la base de datos
        mProgressBar.setVisibility(View.VISIBLE);
        attachDatabaseListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Limpia el ListView y retira los Listeners de la base de datos
        mBannedAdapter.clear();
        dettachDatabaseListener();
    }

    /**
     * Crea los Listeners de la base de datos en caso de no existir y los adjunta a la referencia
     * de los baneados.
     * <p>
     * {@link #mChildEventListener} agrega cada baneado a la lista y oculta el ProgressBar
     * <p>
     * {@link #mEmptyCheckListener} chequea si existen datos en la referencia de los baneados,
     * oculta el ProgressBar y muestra el EmptyView en caso de no recibir datos
     * <p>
     * Ambos Listeners son adjuntados a la referencia {@link #mBannedDatabaseReference}
     *
     * @see #dettachDatabaseListener()
     */
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

    /**
     * Retira los Listeners de la base de datos en caso de existir y los setea a <code>null</code>
     *
     * @see #attachDatabaseListener()
     */
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
