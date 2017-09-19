package com.example.android.clanmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.android.clanmanager.pojo.Sancionado;
import com.example.android.clanmanager.pojo.Strike;
import com.example.android.clanmanager.utils.AdsUtils;
import com.example.android.clanmanager.utils.MapUtils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.text.WordUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Actividad que gestiona lista de los sancionados de la temporada actual. Ejecuta las siguientes
 * funciones:
 * <p>
 * - Agregar, modificar y eliminar sancionados de la base de datos.
 * <p>
 * - Mostrar gráficamente la cantidad de strikes asignados a cada sancionado.
 * <p>
 * - Crear navegación hacia el {@link SancionesEditorActivity editor}, en el cual se gestionan los
 * strikes.
 * <p>
 * - Compartir los detalles de los strikes de un sancionado en específico, así como también
 * compartir una lista con la cantidad de strikes de todos los sancionados en el clan.
 * <p>
 * - Hacer cierre de temporadas y archivar todas las sanciones en el {@link SeasonHistoryActivity
 * historial} para futuras auditorías.
 */
public class SancionadoListActivity extends AppCompatActivity {

    /**
     * Etiqueta para funciones de logging
     */
    public static final String TAG = SancionadoListActivity.class.getSimpleName();

    /**
     * Instancia de la base de datos de Firebase
     */
    private FirebaseDatabase mFirebaseDatabase;
    /**
     * Referencia de la base de datos que apunta al nodo del {@link Sancionado} en la temporada
     * actual
     */
    private DatabaseReference mSancionadosReference;
    /**
     * Listener de los nodos hijos de la referencia del {@link Sancionado}
     */
    private ChildEventListener mChildEventListener;
    /**
     * Listener para chequeo de existencia de datos en la referencia de {@link Sancionado}
     */
    private ValueEventListener mEmptyCheckListener;
    /**
     * Posición del {@link Sancionado} en el adapter. Variable de referencia para remover el item de
     * la lista cuando es eliminado de la base de datos
     */
    private int mAdapterPosition;
    /**
     * Mapa utilizado para compartir la lista de cantidades de sanciones cometidas.
     * Contiene El nombre del {@link Sancionado} que apunta a la cantidad de strikes
     */
    private Map<String, Integer> mShareListMap;
    /**
     * Elemento del layout de muestra Ads
     */
    private AdView mAdView;

    // Objetos para el manejo de la UI
    private TwoLineAdapter mSancionadoAdapter;
    private ArrayList<Object> mSancionadosList;
    private String mSancionadoInput;
    private ProgressBar mProgressBar;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sancionado_list);

        AdsUtils.initializeMobileAds(this);

        mAdView = (AdView) findViewById(R.id.sancionado_list_activity_ad_view);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdsUtils.TEST_DEVICE_ID)
                .build();
        mAdView.loadAd(adRequest);
        AdListener adListener = AdsUtils.getBannerAdListener(mAdView,
                findViewById(R.id.add_sancionado_button), findViewById(R.id.sancionados_list));
        mAdView.setAdListener(adListener);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mSancionadosReference = mFirebaseDatabase.getReference().child("sancionados");

        mShareListMap = new HashMap<>();

        mSancionadosList = new ArrayList<>();
        mSancionadoAdapter = new TwoLineAdapter(this, mSancionadosList);
        mListView = (ListView) findViewById(R.id.sancionados_list);
        mListView.setAdapter(mSancionadoAdapter);
        mProgressBar = (ProgressBar) findViewById(R.id.sancionado_progress_bar);
        mProgressBar.getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.MULTIPLY);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Sancionado clickedSancionado = (Sancionado) mSancionadoAdapter.getItem(position);
                String key = clickedSancionado.getKey();
                Intent intent = new Intent(SancionadoListActivity.this, SancionesEditorActivity.class);
                intent.putExtra("key", key)
                        .putExtra("name", clickedSancionado.getName());
                startActivity(intent);
            }
        });
        // Registra el ListView para responder con menú contextual
        registerForContextMenu(mListView);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_sancionado_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertDialog();
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.menu_sancionados_contextual, menu);
    }

    /**
     * Ejecuta acciones de menú contextual de cada {@link Sancionado} en la lista.
     * <p>
     * Comparte strikes del sancionado seleccionado en caso de poseerlos.
     * <p>
     * Modifica Nombre del sancionado seleccionado
     * <p>
     * Elimina el sancionado seleccionado, al igual que todos sus strikes
     *
     * @see #getShareIntentForSancionado(Sancionado)
     * @see #showEditAlertDialog(Sancionado)
     * @see #showDeleteAlertDialog(Sancionado)
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        mAdapterPosition = position;
        Sancionado selectedSancionado = (Sancionado) mSancionadoAdapter.getItem(position);
        switch (item.getItemId()) {
            case R.id.action_share:
                if (selectedSancionado.getStrikes() != null) {
                    Intent shareIntent = getShareIntentForSancionado(selectedSancionado);
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.select_action)));
                } else {
                    Toast.makeText(this,
                            getString(R.string.no_strikes_message, selectedSancionado.getName()),
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_edit_sancionado:
                showEditAlertDialog(selectedSancionado);
                return true;
            case R.id.action_delete_sancionado:
                showDeleteAlertDialog(selectedSancionado);
                return true;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sancionados, menu);
        return true;
    }

    /**
     * Ejecuta acciones del menú principal del ActionBar
     * <p>
     * Comparte lista de strikes con las diferentes cantidades de cada sancionado
     * <p>
     * Archiva la temporada y vacía la lista de sancionados para dar inicio a una nueva temporada
     *
     * @see #shareList()
     * @see #archiveList()
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                shareList();
                return true;
            case R.id.action_archive:
                if (mSancionadosList.size() > 0) {
                    showArchiveAlert();
                } else {
                    Toast.makeText(this, "No hay sancionados", Toast.LENGTH_SHORT).show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Ordena {@link #mShareListMap} por cantidad de strikes, crea lista en modo texto y la comparte
     * mediante un {@link Intent#createChooser(Intent, CharSequence) chooser}.
     * <p>
     * Muestra {@link Toast} en caso de no existir ningún {@link Strike}
     */
    private void shareList() {
        if (mShareListMap.size() > 0) {
            Map<String, Integer> sortedMap = MapUtils.sortByValue(mShareListMap);
            int currentValue = 999;
            String shareMessage = "*Lista de Strikes:*\n";
            for (Map.Entry<String, Integer> entry : sortedMap.entrySet()) {
                if (entry.getValue() < currentValue) {
                    currentValue = entry.getValue();
                    shareMessage += "\nCon *" + currentValue + "* strikes:\n";
                }
                shareMessage += "-" + entry.getKey() + "\n";
            }
            Intent shareListIntent = new Intent();
            shareListIntent.setAction(Intent.ACTION_SEND)
                    .putExtra(Intent.EXTRA_TEXT, shareMessage)
                    .setType("text/plain");
            startActivity(Intent.createChooser(shareListIntent, getString(R.string.select_action)));

        } else {
            Toast.makeText(this, "No hay ningún strike", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Genera Intent con fechas y motivos de los strikes del {@link Sancionado} seleccionado para
     * posteriormente ser compartido mediante texto
     *
     * @param sancionado El sancionado cuyos strikes van a ser compartidos
     * @return El Intent configurado con el texto a compartir
     */
    private Intent getShareIntentForSancionado(Sancionado sancionado) {
        String shareMsg = getString(R.string.users_strikes) + " *" + sancionado.getName() + "*:\n";

        for (Map.Entry<String, Strike> entry : sancionado.getStrikes().entrySet()) {
            Strike strike = entry.getValue();
            shareMsg += "\n" + strike.getDate();
            shareMsg += "\n" + strike.getReason() + "\n";
        }
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_TEXT, shareMsg)
                .setType("text/plain");
        return shareIntent;
    }

    /**
     * Muestra ventana de alerta que recibe el nombre del {@link Sancionado} para posteriormente
     * ser agregado a la base de datos
     *
     * @see #pushNewSancionado()
     */
    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sancionar a:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
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
     * Muestra ventana de alerta que recibe el nuevo nombre del {@link Sancionado}, para
     * posteriormente ser modificado en la base de datos
     *
     * @param sancionado el sancionado que va a ser modificado
     * @see #modifySancionado(Sancionado)
     */
    private void showEditAlertDialog(final Sancionado sancionado) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Modificar sancionado");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setText(sancionado.getName());
        input.setSelectAllOnFocus(true);
        builder.setView(input)
                .setPositiveButton("Modificar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        String inputText = input.getText().toString().trim();
                        if (!inputText.isEmpty()) {
                            mSancionadoInput = inputText;
                            dialog.dismiss();
                            modifySancionado(sancionado);
                        }
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
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
     * Muestra ventana de alerta para confirmar la eliminación del {@link Sancionado} seleccionado
     * de la base de datos
     *
     * @param deleted el sancionado que va a ser eliminado
     * @see #deleteSancionado(Sancionado)
     */
    private void showDeleteAlertDialog(final Sancionado deleted) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Eliminar sancionado")
                .setMessage("¿Está seguro? Esta acción no se puede deshacer")
                .setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteSancionado(deleted);
                    }
                }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
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
     * Muestra ventana confirmación del cierre de la temporada
     *
     * @see #archiveList()
     */
    private void showArchiveAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Archivar temporada")
                .setMessage("Advertencia: esta acción cierra la temporada actual y elimina todas las " +
                        "sanciones vigentes. ¿Está seguro?")
                .setPositiveButton("Archivar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        archiveList();
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


    @Override
    protected void onStart() {
        super.onStart();
        // Muestra el ProgressBar y adjunta los Listeners a la referencia de la base de datos
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
        // Limpia la lista y retira los Listeners de la referencia de la base de datos
        mSancionadoAdapter.clear();
        dettachDatabaseListener();
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    /**
     * Agrega nuevo {@link Sancionado} a la base de datos, tomando el nombre introducido en la
     * ventana de alerta
     *
     * @see #showAlertDialog()
     */
    private void pushNewSancionado() {
        Sancionado sancionado = new Sancionado(WordUtils.capitalize(mSancionadoInput));
        mSancionadosReference.push().setValue(sancionado);
        Toast.makeText(this, "Nuevo sancionado: " + mSancionadoInput, Toast.LENGTH_SHORT).show();
    }

    /**
     * Modifica un {@link Sancionado} existente en la base de datos, colocándole el nombre
     * introducido en la ventana de alerta
     *
     * @param sancionado el sancionado que va a ser modificado
     * @see #showEditAlertDialog(Sancionado)
     */
    private void modifySancionado(Sancionado sancionado) {
        DatabaseReference sancionadoReference = mSancionadosReference.child(sancionado.getKey());
        Map<String, Object> childUpdate = new HashMap<>();
        sancionado.setName(WordUtils.capitalize(mSancionadoInput));
        childUpdate.put("name", sancionado.getName());
        sancionadoReference.updateChildren(childUpdate);
        Toast.makeText(this, "Se modificó el sancionado", Toast.LENGTH_SHORT).show();

    }

    /**
     * Elimina un {@link Sancionado} existente en la base de datos, incluyendo todos sus strikes
     *
     * @param deleted el sancionado a ser eliminado de la base de datos
     * @see #showDeleteAlertDialog(Sancionado)
     */
    private void deleteSancionado(Sancionado deleted) {
        DatabaseReference deletedReference = mSancionadosReference.child(deleted.getKey());
        deletedReference.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, DatabaseReference databaseReference) {
                if (error == null) {
                    Toast.makeText(SancionadoListActivity.this,
                            "Se eliminó el sancionado",
                            Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(SancionadoListActivity.this,
                            "Hubo un error. Intente más tarde",
                            Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
    }

    /**
     * Archiva la temporada en el historial y vacía la lista de sancionados de la temporada actual
     * para dar inicio a una nueva temporada
     * <p>
     * Para mayor rendimiento al manipular el {@link HistoryActivity historial} de temporadas, se
     * almacena también un index de fechas de cierre, de manera que el índice del historial consuma
     * menos ancho de banda. Ese index posteriormente será el Key del nodo de la temporada que se
     * desee leer.
     *
     * @see #showArchiveAlert()
     * @see #removeList()
     */
    private void archiveList() {
        int listSize = mSancionadosList.size();

        if (listSize > 0) {
            // Formato de fecha estándar para todos los dispositivos
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
            Date date = new Date(System.currentTimeMillis());
            String readableDate = dateFormat.format(date);

            DatabaseReference seasonRef = mFirebaseDatabase.getReference().child("history").child(readableDate);
            DatabaseReference seasonIndex = mFirebaseDatabase.getReference().child("history_index").child(readableDate);

            Map<String, Object> seasonMap = new HashMap<>();
            for (int i = 0; i < listSize; i++) {
                Sancionado currentSancionado = (Sancionado) mSancionadosList.get(i);
                currentSancionado.setKey(null);
                seasonMap.put(currentSancionado.getName(), currentSancionado);
            }
            seasonRef.setValue(seasonMap);
            seasonIndex.setValue(readableDate);
            Toast.makeText(this, "Se ha archivado la temporada", Toast.LENGTH_SHORT).show();
            removeList();
        }
    }

    /**
     * Elimina todos los sancionados de la temporada actual de la base de datos y retira todos los
     * ítems de la lista
     *
     * @see #archiveList()
     */
    private void removeList() {
        mSancionadosReference.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, DatabaseReference databaseReference) {
                if (error == null) {
                    mSancionadoAdapter.clear();
                    mSancionadoAdapter.notifyDataSetChanged();
                }
            }
        });
    }


    /**
     * Crea los Listeners de la base de datos en caso de no existir y los adjunta a la referencia
     * del {@link Sancionado}
     * <p>
     * {@link #mChildEventListener}: agrega el sancionado a la lista y oculta el
     * ProgressBar cuando detecta una entrada nueva en la base de datos. Elimina el sancionado de la
     * lista cuando detecta que fue eliminado de la base de datos.
     * <p>
     * {@link #mEmptyCheckListener} chequea si existen datos en la referencia del sancionado en la
     * base de datos, oculta el ProgressBar y muestra el EmptyView en caso de no recibir datos
     * <p>
     * Ambos Listeners son adjuntados a la referencia {@link #mSancionadosReference}
     *
     * @see #dettachDatabaseListener()
     */
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
                    if (sancionado.getStrikes() != null) {
                        int totalSanciones = sancionado.getStrikes().size();
                        mShareListMap.put(sancionado.getName(), totalSanciones);
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    mSancionadosList.remove(mAdapterPosition);
                    mSancionadoAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            mSancionadosReference.orderByChild("name").addChildEventListener(mChildEventListener);
        }

        if (mEmptyCheckListener == null) {
            mEmptyCheckListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        // La lista está vacía
                        mProgressBar.setVisibility(View.INVISIBLE);
                        View emptyView = findViewById(R.id.sancionado_empty_view);
                        emptyView.setVisibility(View.VISIBLE);
                        mListView.setEmptyView(emptyView);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            mSancionadosReference.addValueEventListener(mEmptyCheckListener);
        }
    }

    /**
     * Retira los Listeners de la base de datos en caso de existir y los setea a <code>null</code>
     *
     * @see #attachDatabaseListener()
     */
    private void dettachDatabaseListener() {
        if (mChildEventListener != null) {
            mSancionadosReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }

        if (mEmptyCheckListener != null) {
            mSancionadosReference.removeEventListener(mEmptyCheckListener);
            mEmptyCheckListener = null;
        }
    }
}