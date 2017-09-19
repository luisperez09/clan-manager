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
import com.example.android.clanmanager.utils.AdsUtils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * Muestra lista de sancionados y sus strikes según la temporada seleccionada en el index del
 * historial. Permite navegación hacia {@link HistoryDetailActivity} para ver los detalles de los strikes
 */
public class SeasonHistoryActivity extends AppCompatActivity {

    /**
     * Instancia de la base de datos de Firebase
     */
    private FirebaseDatabase mFirebaseDatabase;
    /**
     * Referencia de la base de datos que apunta al nodo de la temporada seleccionada en el historial
     */
    private DatabaseReference mSeasonReference;
    /**
     * Listener de los nodos hijos de la referencia de la temporada seleccionada
     */
    private ChildEventListener mChildEventListener;
    /**
     * Fecha de cierre de la temporada seleccionada. Variable de referencia para utilizar como key
     * para ver los detalles de los strikes del sancionado seleccionado, así como también usarla
     * como título de la actividad
     */
    private String mSeasonDate;
    /**
     * Elemento del layout de muestra Ads
     */
    private AdView mAdView;

    // Objetos para el manejo de la UI
    private ListView mListView;
    private TwoLineAdapter mSeasonAdapter;
    private ArrayList<Object> mSancionadosList;
    private ProgressBar mProgressBar;

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

        AdsUtils.initializeMobileAds(this);

        mAdView = (AdView) findViewById(R.id.season_history_activity_ad_view);
        AdListener adListener = AdsUtils.getBannerAdListener(mAdView, null,
                findViewById(R.id.season_history_list));
        mAdView.setAdListener(adListener);
        AdRequest adRequest = AdsUtils.getNewAdRequest();
        mAdView.loadAd(adRequest);

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
        // Muestra ProgressBar y adjunta Listeners a la referencia de la base de datos
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
        // Limpia la lista y retira Listeners de la referencia de la base de datos
        mSeasonAdapter.clear();
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
     * Crea el Listener de la base de datos en caso de no existir y lo adjunta a la referencia
     * de la temporada
     * <p>
     * {@link #mChildEventListener}: agrega el sancionado a la lista y oculta el
     * ProgressBar cuando detecta una entrada nueva en la base de datos.
     * <p>
     * El Listener es adjuntado a la referencia {@link #mSeasonReference}
     *
     * @see #detachDatabaseListener()
     */
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

    /**
     * Retira el Listener de la base de datos en caso de existir y lo setea a <code>null</code>
     *
     * @see #attachDatabaseListener()
     */
    private void detachDatabaseListener() {
        if (mChildEventListener != null) {
            mSeasonReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }
}
