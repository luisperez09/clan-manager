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
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.android.clanmanager.pojo.Coleader;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Muestra lista de colíderes en el orden de responsabilidad de las guerras y resalta al
 * responsable actual. Permite delegar responsabilidad entre colíderes
 */
public class OrderActivity extends AppCompatActivity {

    private final static int EDITTEXT_INPUT_LIMIT = 40;

    /**
     * Instancia de la base de datos de Firebase
     */
    private FirebaseDatabase mFirebaseDatabase;
    /**
     * Referencia de la base de datos que apunta al nodo del {@link Coleader Colíder}
     */
    private DatabaseReference mColeadersReference;
    /**
     * Listener de los nodos hijos de la referencia del {@link Coleader Colíder}
     */
    private ChildEventListener mChildEventListener;
    /**
     * Listener para chequeo de existencia de datos en la referencia de {@link Coleader Colíder}
     */
    private ValueEventListener mEmptyCheckListener;
    /**
     * Key del {@link Coleader colíder} encargado de lanzar la guerra
     */
    private String mCurrentResponsible;
    /**
     * Posición del {@link Coleader colíder} responsable dentro del adapter
     */
    private int mCurrentResponsiblePosition;
    // Objetos para el manejo de la UI
    private ProgressBar mProgressBar;
    private ListView mListView;
    private ArrayList<Object> mData;
    private TwoLineAdapter mTwoLineAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mColeadersReference = mFirebaseDatabase.getReference().child("coleaders");

        mData = new ArrayList<>();
        mTwoLineAdapter = new TwoLineAdapter(this, mData);
        mListView = (ListView) findViewById(R.id.coleaders_list);
        mListView.setAdapter(mTwoLineAdapter);
        mProgressBar = (ProgressBar) findViewById(R.id.coleaders_pb);
        mProgressBar.getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_coleader_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddColeaderDialog();
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Coleader currentColeader = (Coleader) mTwoLineAdapter.getItem(position);
                String newResponsibleKey = currentColeader.getKey();
                showAlertDialog(newResponsibleKey, position);
                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Muestra ProgressBar y adjunta los Listeners a la referencia de la base de datos
        mProgressBar.setVisibility(View.VISIBLE);
        attachDatabaseListeners();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Limpia la lista y retira los Listeners de la referencia de la base de datos
        mTwoLineAdapter.clear();
        detachDatabaseListeners();
    }

    /**
     * Muestra ventana de alerta que recibe el nombre del {@link Coleader colíder} para posteriormente
     * agregarlo a la base de datos
     *
     * @see #addNewColeader(String)
     */
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

    /**
     * Muestra ventana de alerta para confirmación de delagación de responsabilidad
     *
     * @param key      key del nuevo responsable en la base de datos
     * @param position posición del nuevo responsable en el adapter
     * @see #delegateResponsibility(String, int)
     */
    private void showAlertDialog(final String key, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¿Delegar responsabilidad?")
                .setPositiveButton("Delegar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        delegateResponsibility(key, position);
                        Toast.makeText(OrderActivity.this, "Se delegó la responsabilidad", Toast.LENGTH_SHORT).show();
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

    /**
     * Delega la responsabilidad de la guerra a otro {@link Coleader colider}.
     * <p>
     * Se actualiza en la base de datos el nuevo responsable según el key recibido y el anterior
     * según el key almacenado en {@link #mCurrentResponsible}
     * <p>
     * Se actualiza el adapter para ajustar el <code>selector</code> según el nuevo responsable
     *
     * @param position la posición del nuevo responsable en el adapter
     * @param key      key del nuevo responsable en la base de datos
     * @see #showAlertDialog(String, int)
     */
    private void delegateResponsibility(String key, int position) {
        /////////// Queries a la base de datos ///////////
        Map<String, Object> noLongerResponsible = new HashMap<>();
        noLongerResponsible.put("responsible", false);
        mColeadersReference.child(mCurrentResponsible).updateChildren(noLongerResponsible);

        Map<String, Object> newResponsible = new HashMap<>();
        newResponsible.put("responsible", true);
        mColeadersReference.child(key).updateChildren(newResponsible);

        ////////// Actualización del adapter y la UI ////////////
        mCurrentResponsible = key;

        Coleader previous = (Coleader) mData.get(mCurrentResponsiblePosition);
        previous.setResponsible(false);
        mData.set(mCurrentResponsiblePosition, previous);

        Coleader current = (Coleader) mData.get(position);
        current.setResponsible(true);
        mData.set(position, current);

        // Responsable adopta nueva posición en el adapter
        mCurrentResponsiblePosition = position;
        mTwoLineAdapter.notifyDataSetChanged();
    }

    /**
     * Agrega nuevo {@link Coleader colíder} a la base de datos, con el nombre recibido de la
     * ventana de alerta
     *
     * @param coleaderName nombre del nuevo colíder
     */
    private void addNewColeader(String coleaderName) {
        Coleader coleader = new Coleader(coleaderName, false);
        mColeadersReference.push().setValue(coleader);
        Toast.makeText(this, "Se agregó el colíder", Toast.LENGTH_SHORT).show();
    }

    /**
     * Crea los Listeners de la base de datos en caso de no existir y los adjunta a la referencia
     * del {@link Coleader Colíder}
     * <p>
     * {@link #mChildEventListener}: agrega el colíder a la lista y oculta el
     * ProgressBar cuando detecta una entrada nueva en la base de datos. Asigna el
     * {@link #mCurrentResponsible key} y la {@link #mCurrentResponsiblePosition posición} del
     * responsable detectado en la base de datos
     * <p>
     * {@link #mEmptyCheckListener} chequea si existen datos en la referencia del colíder en la
     * base de datos, oculta el ProgressBar y muestra el EmptyView en caso de no recibir datos
     * <p>
     * Ambos Listeners son adjuntados a la referencia {@link #mColeadersReference}
     *
     * @see #detachDatabaseListeners()
     */
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
                    coleader.setKey(dataSnapshot.getKey());
                    mTwoLineAdapter.add(coleader);
                    if (coleader.isResponsible()) {
                        mCurrentResponsible = dataSnapshot.getKey();
                        mCurrentResponsiblePosition = mData.size() - 1;
                    }

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

    /**
     * Retira los Listeners de la base de datos en caso de existir y los setea a <code>null</code>
     *
     * @see #attachDatabaseListeners()
     */
    public void detachDatabaseListeners() {
        if (mChildEventListener != null) {
            mColeadersReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
        if (mEmptyCheckListener != null) {
            mColeadersReference.removeEventListener(mEmptyCheckListener);
            mEmptyCheckListener = null;
        }
    }
}