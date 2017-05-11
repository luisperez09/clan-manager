package com.example.android.clanmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    /**
     * RequestCode para el login de usuario
     */
    private static final int RC_SIGN_IN = 1;
    /**
     * Flag que chequea si el usuario logueado acaba de iniciar sesión
     */
    private static boolean primerLogin = true;
    /**
     * Nombre de usuario proveniente del proveedor (Gmail)
     */
    private String mUsername;
    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAuth = FirebaseAuth.getInstance();

        final ArrayList<Option> options = new ArrayList<Option>();
        options.add(new Option(getString(R.string.sancionados_label), getString(R.string.sancionados_summary)));
        options.add(new Option(getString(R.string.war_order_label), getString(R.string.war_order_summary)));
        options.add(new Option(getString(R.string.black_list_label), getString(R.string.black_list_summary)));

        OptionAdapter adapter = new OptionAdapter(this, options);
        ListView listView = (ListView) findViewById(R.id.list_options);
        listView.setAdapter(adapter);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    mUsername = user.getDisplayName();
                    // Usuario logueado
                    if (primerLogin) {
                        // Por primera vez
                        Toast.makeText(MainActivity.this, getString(R.string.welcome) + mUsername, Toast.LENGTH_SHORT).show();
                        primerLogin = false;
                    }
                } else {
                    // Usuario no logueado
                    startActivityForResult(AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setProviders(Collections.singletonList(
                                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER)
                                                    .build()))
                                    .build(),
                            RC_SIGN_IN);

                }
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, R.string.logged_in, Toast.LENGTH_SHORT).show();
            }
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, R.string.logged_in_canceled, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
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
                return true;
            case R.id.action_sign_out:
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void signOut() {
        AuthUI.getInstance().signOut(this);
        Toast.makeText(this, "Sesión cerrada.\nGracias por usar "
                + getString(R.string.app_name), Toast.LENGTH_SHORT).show();
    }
}
