package com.example.android.clanmanager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.clanmanager.pojo.Option;
import com.example.android.clanmanager.utils.AdsUtils;
import com.example.android.clanmanager.utils.ShareUtils;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Menú principal de la app. Muestra opciones de navegación hacia las diferentes actividades
 * de la app. Requiere inicio de sesión mediante proveedor de Google para poder hacer uso de sus
 * funciones
 */
public class MainActivity extends AppCompatActivity {

    /**
     * RequestCode para el login de usuario
     */
    private static final int RC_SIGN_IN = 1;
    /**
     * RequestCode para el permiso de escribir en almacenamiento externo
     */
    public static final int WRITE_FILE_RC = 2;
    /**
     * Flag que chequea si el usuario logueado acaba de iniciar sesión
     */
    private static boolean primerLogin = true;
    /**
     * Key de la versión de la app en la consola de Firebase
     */
    private static final String APP_VERSION_KEY = "app_version";
    /**
     * Nombre de usuario proveniente del proveedor (Gmail)
     */
    private String mUsername;
    /**
     * Tag para hacer logging
     */
    public static final String TAG = MainActivity.class.getSimpleName();
    // Firebase instance variables
    /**
     * Instancia de autenticación
     */
    private FirebaseAuth mFirebaseAuth;
    /**
     * Listener para manejo de cambios de estado de autenticación
     */
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    /**
     * Instancia de configuración remota
     */
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    /**
     * Elemento del layout de muestra Ads
     */
    private AdView mAdView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AdsUtils.initializeMobileAds(this);

        mAdView = (AdView) findViewById(R.id.main_activity_ad_view);
        AdListener adListener = AdsUtils.getBannerAdListener(mAdView,
                findViewById(R.id.copyright_text_view), null);
        mAdView.setAdListener(adListener);
        AdRequest adRequest = AdsUtils.getNewAdRequest();
        mAdView.loadAd(adRequest);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        final ArrayList<Object> options = new ArrayList<>();
        options.add(new Option(getString(R.string.sancionados_label), getString(R.string.sancionados_summary), this, SancionadoListActivity.class));
        options.add(new Option(getString(R.string.war_order_label), getString(R.string.war_order_summary), this, OrderActivity.class));
        options.add(new Option(getString(R.string.black_list_label), getString(R.string.black_list_summary), this, BlacklistActivity.class));
        options.add(new Option(getString(R.string.history_label), getString(R.string.history_summary), this, HistoryActivity.class));

        TwoLineAdapter adapter = new TwoLineAdapter(this, options);
        ListView listView = (ListView) findViewById(R.id.list_options);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Option currentOption = (Option) options.get(position);
                Intent intent = currentOption.getOptionIntent();
                if (intent != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, R.string.under_development, Toast.LENGTH_SHORT).show();
                }
            }
        });

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
                                    .setProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN);

                }
            }
        };
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);

        Map<String, Object> defaultConfigMap = new HashMap<>();
        defaultConfigMap.put(APP_VERSION_KEY, BuildConfig.VERSION_CODE);
        mFirebaseRemoteConfig.setDefaults(defaultConfigMap);
        fetchConfig();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            // Resultado del inicio de sesión
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
        // Adjunta Listener de autenticación
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        // Reanuda el procesamiento del AdView
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Retira Listener de autenticación
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        // Pausa el procesamiento del AdView
        if (mAdView != null) {
            mAdView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
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
            case R.id.action_share_rules:
                checkFilePermission();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case WRITE_FILE_RC:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    shareRules();
                } else {
                    Toast.makeText(this, "Permiso negado", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * Chequea que el usuario haya cedido permiso para escribir en almacenamiento externo para
     * compartir las reglas. Solicita permiso si no ha sido cedido
     */
    private void checkFilePermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_FILE_RC);
        } else {
            shareRules();
        }
    }

    /**
     * Crea archivos de imágenes de las reglas y requisitos de ascenso y los comparte a través
     * de WhatsApp
     */
    private void shareRules() {
        if (ShareUtils.createImageFiles(this)) {
            startActivity(ShareUtils.getShareRulesIntent());
        } else {
            Toast.makeText(this, "No se pudieron crear los archivos", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Ejecuta cierre de sesión del usuario
     */
    private void signOut() {
        AuthUI.getInstance().signOut(this);
        Toast.makeText(this, "Sesión cerrada.\nGracias por usar "
                + getString(R.string.app_name), Toast.LENGTH_SHORT).show();
    }

    /**
     * Trae información de configuración remota del servidor y ejecuta comparación de los datos
     * locales y los remotos
     *
     * @see #compareAppVersion()
     */
    private void fetchConfig() {
        long cacheExpiration = 3600; // 1 hora
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mFirebaseRemoteConfig.activateFetched();
                        compareAppVersion();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error fetching config", e);
                        compareAppVersion();
                    }
                });
    }

    /**
     * Compara versión actual de la app con la versión más reciente detectada en el servidor
     *
     * @see #fetchConfig()
     */
    private void compareAppVersion() {
        Long app_version = mFirebaseRemoteConfig.getLong(APP_VERSION_KEY);
        int fetchedAppVersion = app_version.intValue();
        if (fetchedAppVersion > BuildConfig.VERSION_CODE) {
            Log.w(TAG, "Update available");
            finish();
            Toast.makeText(this, "Hay una nueva versión disponible", Toast.LENGTH_LONG).show();
        } else {
            Log.w(TAG, "Latest version");
        }
    }
}
