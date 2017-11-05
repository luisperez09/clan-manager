package com.example.android.clanmanager.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.example.android.clanmanager.R;
import com.google.firebase.crash.FirebaseCrash;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Contiene métodos para crear archivos de imágenes y compartirlos a través de WhatsApp
 */
public class ShareUtils {
    /**
     * Nombre del archivo de las reglas
     */
    private static final String RULES_FILE_NAME = "reglas.jpeg";
    /**
     * Nombre del archivo de los requisitos de ascenso
     */
    private static final String PROMOTION_FILE_NAME = "ascenso.jpeg";

    /**
     * Crea imágenes JPEG de las reglas del clan y requisitos de ascenso en el almacenamiento
     * externo de dispositivo (tarjeta SD)
     *
     * @param context El contexto de la app
     * @return <code>true</code> si ambos archivos fueron creados, <code>false</code> si no existe
     * la carpeta o no se pudieron crear ambos archivos.
     * @see #getImagesUris()
     */
    public static boolean createImageFiles(Context context) {

        Bitmap rulesBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.rules);
        Bitmap promotionBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.promotion);

        // Ruta donde se crearán los archivos públicos
        File storagePath = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        + File.separator + "ClanManager");

        boolean success = true;
        if (!storagePath.exists()) {
            success = storagePath.mkdirs();
        }

        // Crear archivos solo si el directorio existe
        if (success) {
            File rulesFile = new File(storagePath, RULES_FILE_NAME);
            File promotionsFile = new File(storagePath, PROMOTION_FILE_NAME);
            OutputStream os;
            try {
                os = new FileOutputStream(rulesFile);
                boolean rulesExists = rulesBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                os.flush();
                os = new FileOutputStream(promotionsFile);
                boolean promotionExists = promotionBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                os.flush();
                os.close();
                // Devuelve true solo si ambos archivos fueron creados
                return rulesExists && promotionExists;
            } catch (Exception e) {
                Log.e(context.getClass().getSimpleName(), "No se pudo crear el bitmap", e);
                FirebaseCrash.report(e);
                // Si no se pudo crear el bitmap devuelve false
                return false;
            }
        }
        // Si no existe el directorio devuelve false
        return false;
    }

    /**
     * Devuelve intent explícito que comparte las reglas del clan mediante WhatsApp
     *
     * @return Intent formado con datos a compartir mediante WhatsApp
     * @see #createImageFiles(Context)
     */
    public static Intent getShareRulesIntent() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE)
                .putExtra(Intent.EXTRA_TEXT, "Reglas")
                .setType("text/plain")
                .setType("image/jpeg")
                .setPackage("com.whatsapp")
                .putParcelableArrayListExtra(Intent.EXTRA_STREAM, getImagesUris());
        return intent;
    }

    /**
     * Devuelve los Uri de los archivos creados mediante {@link #createImageFiles(Context)}
     *
     * @return ArrayList de Uri de los archivos
     * @see #createImageFiles(Context)
     * @see #getShareRulesIntent()
     */
    private static ArrayList<Uri> getImagesUris() {
        // Directorio público de imágenes de la app: sdcard/Pictures/ClanManager
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                + File.separator + "ClanManager";

        Uri rulesFileUri = Uri.parse(path + File.separator + RULES_FILE_NAME);
        Uri promotionsFileUri = Uri.parse(path + File.separator + PROMOTION_FILE_NAME);

        ArrayList<Uri> imageUriArray = new ArrayList<>();
        imageUriArray.add(rulesFileUri);
        imageUriArray.add(promotionsFileUri);
        return imageUriArray;
    }
}