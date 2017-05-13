package com.example.android.clanmanager;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SancionadoListActivity extends AppCompatActivity {

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mSancionadosReference;

    private String mSancionadoInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sancionado_list);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mSancionadosReference = mFirebaseDatabase.getReference().child("sancionados");

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

    private void pushNewSancionado() {
        Sancionado sancionado = new Sancionado(mSancionadoInput);
        mSancionadosReference.push().setValue(sancionado);
        Toast.makeText(this, "Nuevo sancionado: " + mSancionadoInput, Toast.LENGTH_SHORT).show();
    }
}












