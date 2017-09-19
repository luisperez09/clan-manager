package com.example.android.clanmanager;

import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Permite agregar, modificar y editar {@link Strike strikes} del
 * {@link com.example.android.clanmanager.pojo.Sancionado sancionado} seleccionado en la
 * {@link SancionadoListActivity lista}.
 */
public class SancionesEditorActivity extends AppCompatActivity {

    /**
     * Límite de caracteres para los motivos de las sanciones
     */
    private final static int EDITTEXT_INPUT_LIMIT = 40;
    /**
     * Instancia de la base de datos de Firebase
     */
    private FirebaseDatabase mFirebaseDatabase;
    /**
     * Referencia de la base de datos que apunta al nodo de {@link Strike} del
     * {@link com.example.android.clanmanager.pojo.Sancionado Sancionado} seleccionado
     */
    private DatabaseReference mUserStrikesReference;
    /**
     * Listener de los nodos hijos de la referencia del {@link Strike}
     */
    private ChildEventListener mChildEventListener;
    /**
     * Listener para chequeo de existencia de datos en la referencia de {@link Strike}
     */
    private ValueEventListener mEmptyCheckListener;
    /**
     * Motivo del {@link Strike} a ser creado o editado
     */
    private String mStrikeReason;
    /**
     * Posición del {@link Strike} en el adapter. Variable de referencia para remover el item de
     * la lista cuando es eliminado de la base de datos
     */
    private int mAdapterPosition;
    /**
     * Elemento del layout de muestra Ads
     */
    private AdView mAdView;

    private TextView mSancionadoTextView;
    private ProgressBar mProgressBar;
    private ListView mListView;
    private TwoLineAdapter mStrikeAdapter;
    private ArrayList<Object> mStrikesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sanciones_editor);

        Bundle extras = getIntent().getExtras();
        final String key = extras.getString("key");
        String username = extras.getString("name");
        setTitle(username);

        AdsUtils.initializeMobileAds(this);
        mAdView = (AdView) findViewById(R.id.sancionado_detail_activity_ad_view);
        AdRequest adRequest = AdsUtils.getNewAdRequest();
        mAdView.loadAd(adRequest);
        AdListener adListener = AdsUtils.getBannerAdListener(mAdView,
                findViewById(R.id.agregar_button), findViewById(R.id.strikes_list));
        mAdView.setAdListener(adListener);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUserStrikesReference = mFirebaseDatabase.getReference().child("sancionados").child(key).child("strikes");

        mStrikesList = new ArrayList<>();
        mStrikeAdapter = new TwoLineAdapter(this, mStrikesList);
        mListView = (ListView) findViewById(R.id.strikes_list);
        mListView.setAdapter(mStrikeAdapter);
        registerForContextMenu(mListView);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_sanciones_editor);
        mProgressBar.getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.MULTIPLY);

        mSancionadoTextView = (TextView) findViewById(R.id.sancionado_text_view);
        mSancionadoTextView.setText(username);
        FloatingActionButton button = (FloatingActionButton) findViewById(R.id.agregar_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddAlertDialog();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Muestra ProgressBar y adjunta los Listeners a la referencia de la base de datos
        mProgressBar.setVisibility(View.VISIBLE);
        attachDatabaseListener();
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
        // Limpia la lista y retira los Listeners de la referencia de la base de datos
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle(R.string.select_action);
        getMenuInflater().inflate(R.menu.menu_editor_contextual, menu);
    }

    /**
     * Ejecuta acciones de menú contextual de cada {@link Strike} en la lista.
     * <p>
     * Modifica el motivo del strike seleccionado.
     * <p>
     * Elimina el strike seleccionado
     *
     * @see #showEditAlertDialog(Strike)
     * @see #showDeleteDialog(Strike)
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        mAdapterPosition = info.position;
        Strike selectedStrike = (Strike) mStrikeAdapter.getItem(mAdapterPosition);
        switch (item.getItemId()) {
            case R.id.action_edit:
                showEditAlertDialog(selectedStrike);
                return true;
            case R.id.action_delete:
                showDeleteDialog(selectedStrike);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    /**
     * Muestra ventana de alerta que recibe el motivo del {@link Strike} para posteriormente
     * ser agregado a la base de datos
     *
     * @see #pushNewStrike()
     */
    private void showAddAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(EDITTEXT_INPUT_LIMIT)});
        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
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

    /**
     * Muestra ventana de alerta que recibe el nuevo motivo del {@link Strike}, para
     * posteriormente ser modificado en la base de datos
     *
     * @param strike el strike que va a ser modificado
     * @see #updateStrike(Strike)
     */
    private void showEditAlertDialog(final Strike strike) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(EDITTEXT_INPUT_LIMIT)});
        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setText(strike.getReason());
        input.setSelectAllOnFocus(true);
        builder.setView(input);
        builder.setTitle("Editar motivo")
                .setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String inputText = input.getText().toString().trim();
                        if (!inputText.isEmpty()) {
                            mStrikeReason = inputText;
                            updateStrike(strike);
                            dialogInterface.dismiss();
                        }
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
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

    /**
     * Muestra ventana de alerta para confirmar la eliminación del {@link Strike} seleccionado
     * de la base de datos
     *
     * @param strike el strike que va a ser eliminado
     * @see #deleteStrike(Strike)
     */
    private void showDeleteDialog(final Strike strike) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿Eliminar este strike?")
                .setMessage("Esta acción no se puede deshacer")
                .setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteStrike(strike);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });
        builder.show();
    }

    /**
     * Agrega nuevo {@link Strike} a la base de datos, tomando el motivo introducido en la
     * ventana de alerta
     *
     * @see #showAddAlertDialog()
     */
    private void pushNewStrike() {
        // Formato de fecha estándar para todos los dispositivos
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
        String date = dateFormat.format(new Date(System.currentTimeMillis()));
        Strike strike = new Strike(date, mStrikeReason);
        mUserStrikesReference.push().setValue(strike);
        Toast.makeText(this, "Se agregó el strike", Toast.LENGTH_SHORT).show();
    }

    /**
     * Modifica un {@link Strike} existente en la base de datos, colocándole el motivo
     * introducido en la ventana de alerta
     *
     * @param strike el strike que va a ser modificado
     * @see #showEditAlertDialog(Strike)
     */
    private void updateStrike(Strike strike) {
        DatabaseReference strikeReference = mUserStrikesReference.child(strike.getKey());
        Map<String, Object> childUpdate = new HashMap<>();
        strike.setReason(mStrikeReason);
        childUpdate.put("reason", strike.getReason());
        strikeReference.updateChildren(childUpdate);
        Toast.makeText(this, "Se actualizó el strike", Toast.LENGTH_SHORT).show();
    }

    /**
     * Elimina un {@link Strike} existente en la base de datos
     *
     * @param strike el strike a ser eliminado de la base de datos
     * @see #showDeleteDialog(Strike)
     */
    private void deleteStrike(Strike strike) {
        DatabaseReference strikeReference = mUserStrikesReference.child(strike.getKey());
        strikeReference.removeValue();
        Toast.makeText(this, "Se eliminó el strike", Toast.LENGTH_SHORT).show();
    }

    /**
     * Crea los Listeners de la base de datos en caso de no existir y los adjunta a la referencia
     * del {@link Strike}
     * <p>
     * {@link #mChildEventListener}: agrega el strike a la lista y oculta el
     * ProgressBar cuando detecta una entrada nueva en la base de datos. Elimina el strike de la
     * lista cuando detecta que fue eliminado de la base de datos.
     * <p>
     * {@link #mEmptyCheckListener} chequea si existen datos en la referencia del strike en la
     * base de datos, oculta el ProgressBar y muestra el EmptyView en caso de no recibir datos
     * <p>
     * Ambos Listeners son adjuntados a la referencia {@link #mUserStrikesReference}
     *
     * @see #detachDatabaseListener()
     */
    private void attachDatabaseListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Strike strike = dataSnapshot.getValue(Strike.class);
                    String key = dataSnapshot.getKey();
                    strike.setKey(key);
                    mStrikeAdapter.add(strike);
                    mProgressBar.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    mStrikesList.remove(mAdapterPosition);
                    mStrikeAdapter.notifyDataSetChanged();
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
            mUserStrikesReference.addValueEventListener(mEmptyCheckListener);
        }
    }

    /**
     * Retira los Listeners de la base de datos en caso de existir y los setea a <code>null</code>
     *
     * @see #attachDatabaseListener()
     */
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