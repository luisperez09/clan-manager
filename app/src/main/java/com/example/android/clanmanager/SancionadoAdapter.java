package com.example.android.clanmanager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class SancionadoAdapter extends ArrayAdapter<Sancionado> {

    public SancionadoAdapter(Context context, ArrayList<Sancionado> data) {
        super(context, 0, data);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.sancionado_item_list,
                    parent, false);
        }
        Sancionado currentSancionado = getItem(position);
        TextView nameTextView = (TextView) listItemView.findViewById(R.id.sancionado_name_text_view);
        nameTextView.setText(currentSancionado.getName());

        if (currentSancionado.getStrikes() != null) {
            TextView sancionesAmmountTextView = (TextView) listItemView
                    .findViewById(R.id.sanciones_ammount_text_view);
            sancionesAmmountTextView.setText("");
            int numStrikes = currentSancionado.getStrikes().size();
            for (int count = 0; count < numStrikes; count++) {
                sancionesAmmountTextView.append("X ");
            }
        }

        return listItemView;
    }
}
