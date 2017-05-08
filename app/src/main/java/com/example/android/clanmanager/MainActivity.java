package com.example.android.clanmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ArrayList<Option> options = new ArrayList<Option>();
        options.add(new Option(getString(R.string.sancionados_label), getString(R.string.sancionados_summary)));
        options.add(new Option(getString(R.string.war_order_label), getString(R.string.war_order_summary)));
        options.add(new Option(getString(R.string.black_list_label), getString(R.string.black_list_summary)));

        OptionAdapter adapter = new OptionAdapter(this, options);
        ListView listView = (ListView) findViewById(R.id.list_options);
        listView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
