package com.example.android.clanmanager;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.clanmanager.pojo.Banned;
import com.example.android.clanmanager.pojo.Coleader;
import com.example.android.clanmanager.pojo.Option;
import com.example.android.clanmanager.pojo.Sancionado;
import com.example.android.clanmanager.pojo.Strike;

import java.util.ArrayList;


/**
 * Adapter genérico para ListItemView de dos líneas. Recibe {@link ArrayList} de objetos genéricos
 * y rellena los items de la lista según el tipo de objeto específico de la lista.
 */
public class TwoLineAdapter extends ArrayAdapter<Object> {
    /**
     * Crea un adapter genérico para ítems de dos líneas
     *
     * @param context el contexto de la aplicación
     * @param data    la lista de objetos
     */
    public TwoLineAdapter(Context context, ArrayList<Object> data) {
        super(context, 0, data);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView =
                    LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }
        // Ejecuta control de flujo de acuerdo al tipo de objeto
        Object o = getItem(position);
        if (o instanceof Coleader) {
            listItemView = setupColeaderView(listItemView, position);
        } else if (o instanceof Sancionado) {
            listItemView = setupSancionadosList(listItemView, position);
        } else if (o instanceof Strike) {
            listItemView = setupStrike(listItemView, position);
        } else if (o instanceof Option) {
            listItemView = setupOption(listItemView, position);
        } else if (o instanceof Banned) {
            listItemView = setupBanned(listItemView, position);
        }

        return listItemView;
    }

    /**
     * Rellena los views de los colíderes y resalta el ítem del colíder responsable de la guerra
     *
     * @param lv       el item view a rellenar
     * @param position la posición del {@link Coleader Colider} dentro del adapter
     */
    private View setupColeaderView(View lv, int position) {
        Coleader coleader = (Coleader) getItem(position);
        TextView mainTextView = (TextView) lv.findViewById(R.id.option_text_view);
        mainTextView.setText(coleader.getName());
        LinearLayout container = (LinearLayout) lv.findViewById(R.id.text_container);
        if (coleader.isResponsible()) {
            container.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.responsible_selector));
        } else {
            container.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.touch_selector));
        }
        return lv;
    }

    /**
     * Rellena los views de los sancionados, marcando una "X" por cada strike registrado
     *
     * @param lv       el item view a rellenar
     * @param position la posición del {@link Sancionado} dentro del adapter
     */
    private View setupSancionadosList(View lv, int position) {
        Sancionado currentSancionado = (Sancionado) getItem(position);
        TextView nameTextView = (TextView) lv.findViewById(R.id.option_text_view);
        nameTextView.setText(currentSancionado.getName());

        TextView strikesAmmountTextView = (TextView) lv.findViewById(R.id.summary_text_view);
        strikesAmmountTextView.setText("");

        if (currentSancionado.getStrikes() != null) {
            strikesAmmountTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
            strikesAmmountTextView.setTypeface(strikesAmmountTextView.getTypeface(), Typeface.BOLD);

            int numStrikes = currentSancionado.getStrikes().size();
            for (int count = 0; count < numStrikes; count++) {
                strikesAmmountTextView.append("X ");
            }
        }
        return lv;
    }

    /**
     * Rellena los views de los strikes, mostrando la fecha y el motivo de los mismos
     *
     * @param lv       el item view a rellenar
     * @param position la posición del {@link Strike} dentro del adapter
     */
    private View setupStrike(View lv, int position) {
        Strike currentStrike = (Strike) getItem(position);

        String date = currentStrike.getDate();
        TextView dateTextView = (TextView) lv.findViewById(R.id.summary_text_view);
        dateTextView.setText(date);

        String reason = currentStrike.getReason();
        TextView reasonTextView = (TextView) lv.findViewById(R.id.option_text_view);
        reasonTextView.setText(reason);

        return lv;
    }

    /**
     * Rellena los views de las opciones del menú principal, mostrando una breve descripción de
     * cada una
     *
     * @param lv       el item view a rellenar
     * @param position la posición de la {@link Option Opción} dentro del adapter
     */
    private View setupOption(View lv, int position) {
        Option currentOption = (Option) getItem(position);
        TextView descriptionTextView = (TextView) lv.findViewById(R.id.option_text_view);
        descriptionTextView.setText(currentOption.getDescription());

        TextView summaryTextView = (TextView) lv.findViewById(R.id.summary_text_view);
        summaryTextView.setText(currentOption.getSummary());

        return lv;
    }

    /**
     * Rellena los views de las opciones del menú principal, mostrando una breve descripción de
     * cada una
     *
     * @param lv       el item view a rellenar
     * @param position la posición de la {@link Banned baneado} dentro del adapter
     */
    private View setupBanned(View lv, int position) {
        Banned currentBanned = (Banned) getItem(position);

        TextView bannedTextView = (TextView) lv.findViewById(R.id.option_text_view);
        bannedTextView.setText(currentBanned.getBanned());

        TextView reasonTextView = (TextView) lv.findViewById(R.id.summary_text_view);
        reasonTextView.setText(currentBanned.getReason());

        return lv;
    }
}