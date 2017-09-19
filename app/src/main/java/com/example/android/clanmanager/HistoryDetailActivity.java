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
import com.example.android.clanmanager.utils.AdsUtils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Muestra detalles de los strikes del historial del sancionado seleccionado
 */
public class HistoryDetailActivity extends AppCompatActivity {

    /**
     * Instancia de la base de datos de Firebase
     */
    private FirebaseDatabase mFirebaseDatabase;
    /**
     * Referencia de la base de datos que apunta al nodo del detalle del {@link Strike} dentro del
     * historial
     */
    private DatabaseReference mDetailReference;
    /**
     * Listener para chequeo de existencia de datos en la referencia de {@link Strike} dentro del
     * historial
     */
    private ValueEventListener mEmptyCheckListener;
    /**
     * Listener de los nodos hijos de la referencia del {@link Strike}
     */
    private ChildEventListener mChildEventListener;
    /**
     * Elemento del layout de muestra Ads
     */
    private AdView mAdView;

    // Objetos para manejo de la UI
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

        AdsUtils.initializeMobileAds(this);

        mAdView = (AdView) findViewById(R.id.history_detail_activity_ad_view);
        AdListener adListener = AdsUtils.getBannerAdListener(mAdView, null,
                findViewById(R.id.strikes_list));
        mAdView.setAdListener(adListener);
        AdRequest adRequest = AdsUtils.getNewAdRequest();
        mAdView.loadAd(adRequest);

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
        // Muestra ProgressBar y adjunta Listeners a la base de datos
        mProgressBar.setVisibility(View.VISIBLE);
        attachDatabaseListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    protected void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Limpia la lista y retira los Listeners de la base de datos
        mStrikeAdapter.clear();
        detachDatabaseListener();
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    /**
     * Crea los Listeners de la base de datos en caso de no existir y los adjunta a la referencia
     * del detalle del {@link Strike}.
     * <p>
     * {@link #mChildEventListener} agrega el {@link Strike} a la lista y oculta el
     * ProgressBar
     * <p>
     * {@link #mEmptyCheckListener} chequea si existen datos en la referencia del detalle del
     * {@link Strike} en el historial, oculta el ProgressBar y muestra el EmptyView en caso de no
     * recibir datos
     * <p>
     * Ambos Listeners son adjuntados a la referencia {@link #mDetailReference}
     *
     * @see #detachDatabaseListener()
     */
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

    /**
     * Retira los Listeners de la base de datos en caso de existir y los setea a <code>null</code>
     *
     * @see #attachDatabaseListener()
     */
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