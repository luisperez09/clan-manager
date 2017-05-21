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

import java.util.ArrayList;


public class TwoLineAdapter extends ArrayAdapter<Object> {
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
        Object o = getItem(position);
        if (o instanceof Coleader) {
            listItemView = setupColeaderView(listItemView, position);
        } else if (o instanceof Sancionado) {
            setupSancionadosList(listItemView, position);
        } else if (o instanceof Strike) {
            setupStrike(listItemView, position);
        }

        return listItemView;
    }

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

    private View setupSancionadosList(View lv, int position) {
        Sancionado currentSancionado = (Sancionado) getItem(position);
        TextView nameTextView = (TextView) lv.findViewById(R.id.option_text_view);
        nameTextView.setText(currentSancionado.getName());

        if (currentSancionado.getStrikes() != null) {
            TextView strikesAmmountTextView = (TextView) lv
                    .findViewById(R.id.summary_text_view);
            strikesAmmountTextView.setText("");
            strikesAmmountTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
            strikesAmmountTextView.setTypeface(strikesAmmountTextView.getTypeface(), Typeface.BOLD);
            int numStrikes = currentSancionado.getStrikes().size();
            for (int count = 0; count < numStrikes; count++) {
                strikesAmmountTextView.append("X ");
            }
        }
        return lv;
    }

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
}
