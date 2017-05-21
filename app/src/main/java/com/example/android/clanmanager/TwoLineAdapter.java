package com.example.android.clanmanager;

import android.content.Context;
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
}
