package com.example.android.clanmanager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ArrayList<Option> options = new ArrayList<Option>();
        options.add(new Option("Sancionados", "Cantidad de strikes del usuario"));
        options.add(new Option("Orden de guerras", "¿Quién lanza la siguiente?"));
        options.add(new Option("Lista Negra", "Personas no bienvenidas"));

        OptionAdapter adapter = new OptionAdapter(this, options);
        ListView listView = (ListView) findViewById(R.id.list_options);
        listView.setAdapter(adapter);
    }
}
