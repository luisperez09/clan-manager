package com.example.android.clanmanager;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Muestra index de fechas de cierres de temporadas
 */
public class HistoryActivity extends AppCompatActivity {
    /**
     * Instancia de la base de datos de Firebase
     */
    private FirebaseDatabase mFirebaseDatabase;
    /**
     * Referencia de la base de datos que apunta al nodo del index de las temporadas
     */
    private DatabaseReference mHistoryReference;
    /**
     * Listener de los nodos hijos de la referencia de los índices de las temporadas
     */
    private ChildEventListener mChildEventListener;
    /**
     * Listener para chequeo de existencia de datos en la referencia de los índices de las
     * temporadas
     */
    private ValueEventListener mEmptyCheckListener;

    // Objetos para manejo de la UI
    private ListView mListView;
    private List<String> mDates = new ArrayList<>();
    private ArrayAdapter<String> mAdapter;
    private ProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mHistoryReference = mFirebaseDatabase.getReference().child("history_index");

        mListView = (ListView) findViewById(R.id.history_list_view);
        mAdapter = new ArrayAdapter<String>(this, R.layout.history_list_item, mDates);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String dateKey = (String) mListView.getItemAtPosition(position);
                Intent i = new Intent(HistoryActivity.this, SeasonHistoryActivity.class);
                i.putExtra("dateKey", dateKey);
                startActivity(i);
            }
        });

        mProgressBar = (ProgressBar) findViewById(R.id.history_progress_bar);
        mProgressBar.getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
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
        mAdapter.clear();
        detachDatabaseListener();
    }

    /**
     * Crea los Listeners de la base de datos en caso de no existir y los adjunta a la referencia
     * de los índices de las temporadas.
     * <p>
     * {@link #mChildEventListener} agrega la fecha de la temporada a la lista y oculta el
     * ProgressBar
     * <p>
     * {@link #mEmptyCheckListener} chequea si existen datos en la referencia de los índices de
     * las temporadas, oculta el ProgressBar y muestra el EmptyView en caso de no recibir datos
     * <p>
     * Ambos Listeners son adjuntados a la referencia {@link #mHistoryReference}
     *
     * @see #detachDatabaseListener()
     */
    private void attachDatabaseListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    String date = (String) dataSnapshot.getValue();
                    mAdapter.add(date);
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
            mHistoryReference.addChildEventListener(mChildEventListener);
        }
        if (mEmptyCheckListener == null) {
            mEmptyCheckListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        mProgressBar.setVisibility(View.GONE);
                        View emptyView = findViewById(R.id.history_empty_view);
                        emptyView.setVisibility(View.VISIBLE);
                        mListView.setEmptyView(emptyView);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            mHistoryReference.addValueEventListener(mEmptyCheckListener);
        }
    }

    /**
     * Retira los Listeners de la base de datos en caso de existir y los setea a <code>null</code>
     *
     * @see #attachDatabaseListener()
     */
    private void detachDatabaseListener() {
        if (mChildEventListener != null) {
            mHistoryReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
        if (mEmptyCheckListener != null) {
            mHistoryReference.removeEventListener(mEmptyCheckListener);
            mEmptyCheckListener = null;
        }
    }
}
