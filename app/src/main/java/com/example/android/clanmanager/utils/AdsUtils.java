package com.example.android.clanmanager.utils;


import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.android.clanmanager.BuildConfig;
import com.example.android.clanmanager.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

/**
 * Clase que contiene métodos que gestionan los ads de AdMob dentro de la app, tanto su
 * confguración como su interacción con otros elementos de la UI
 */
public final class AdsUtils {

    /**
     * Inicializa la API de AdMob con su respectivo app ID
     *
     * @param context Contexto de la app
     */
    public static void initializeMobileAds(Context context) {
        if (context == null) {
            throw new NullPointerException("El contexto no puede ser nulo.");
        }
        MobileAds.initialize(context, context.getString(R.string.admob_app_id));
    }

    /**
     * Crea AdListener con callbacks comunes para todos los banners dentro de la app. Cuando el
     * ad se carga con éxito ajusta los parámetros del layout para evitar que el ad se solape
     * con otros elementos en la parte inferior de la pantalla. Cuando el ad no se puede cargar
     * registra el motivo en el Log en la versión debug.
     *
     * @param adView     el AdView en el cual se mostrará el ad
     * @param bottomView el view que se encuentra en la parte inferior de la pantalla
     * @param listView   el ListView del Activity
     */
    public static AdListener getBannerAdListener(@NonNull final AdView adView,
                                                 final View bottomView, final View listView) {
        return new AdListener() {
            @Override
            public void onAdLoaded() {
                int adViewId = adView.getId();
                RelativeLayout.LayoutParams params;
                if (bottomView != null) {
                    params = (RelativeLayout.LayoutParams) bottomView.getLayoutParams();
                    params.addRule(RelativeLayout.ABOVE, adViewId);
                    params.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    bottomView.setLayoutParams(params);
                }
                if (listView != null) {
                    params = (RelativeLayout.LayoutParams) listView.getLayoutParams();
                    params.addRule(RelativeLayout.ABOVE, adViewId);
                    params.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    listView.setLayoutParams(params);
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Si la app está en producción no registrar el evento
                if (!BuildConfig.DEBUG) {
                    return;
                }

                String reason;
                switch (errorCode) {
                    case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                        reason = "Error interno del servidor.";
                        break;
                    case AdRequest.ERROR_CODE_INVALID_REQUEST:
                        reason = "Solicitud inválida. Es posible que el ad unit ID sea incorrecto.";
                        break;
                    case AdRequest.ERROR_CODE_NETWORK_ERROR:
                        reason = "La solicitud no tuvo éxito debido a una falla de conexión de red.";
                        break;
                    case AdRequest.ERROR_CODE_NO_FILL:
                        reason = "La solicitud tuvo éxito, pero el servidor no disponía de ads.";
                        break;
                    default:
                        reason = "Se desconoce la causa de la falla";
                        break;
                }
                Log.i("Ads", "No se pudo cargar el Ad. " + reason);

            }
        };
    }
}
