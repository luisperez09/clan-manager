package com.example.android.clanmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
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

import com.example.android.clanmanager.utils.MapUtils;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SancionadoListActivity extends AppCompatActivity {

    public static final String TAG = SancionadoListActivity.class.getSimpleName();

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mSancionadosReference;
    private ChildEventListener mChildEventListener;
    private SancionadoAdapter mSancionadoAdapter;
    private ValueEventListener mEmptyCheckListener;

    private String mSancionadoInput;
    private ProgressBar mProgressBar;
    private ListView mListView;
    private Map<String, Integer> mShareListMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sancionado_list);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mSancionadosReference = mFirebaseDatabase.getReference().child("sancionados");

        mShareListMap = new HashMap<>();

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
                Intent intent = new Intent(SancionadoListActivity.this, SancionesEditorActivity.class);
                intent.putExtra("key", key)
                        .putExtra("name", clickedSancionado.getName());
                startActivity(intent);
            }
        });

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

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        Sancionado selectedSancionado = mSancionadoAdapter.getItem(position);
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
                break;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sancionados, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                shareList();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

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

    @Override
    protected void onStart() {
        super.onStart();
        mProgressBar.setVisibility(View.VISIBLE);
        attachDatabaseListener();
    }


    @Override
    protected void onStop() {
        super.onStop();
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
                    if (sancionado.getStrikes() != null) {
                        int totalSanciones = sancionado.getStrikes().size();
                        Log.w("MainActivity", sancionado.getName() + " está sancionado con: "
                                + totalSanciones + " sanciones");
                        mShareListMap.put(sancionado.getName(), totalSanciones);
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
            mSancionadosReference.addChildEventListener(mChildEventListener);
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
            mSancionadosReference.addListenerForSingleValueEvent(mEmptyCheckListener);
        }
    }

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











