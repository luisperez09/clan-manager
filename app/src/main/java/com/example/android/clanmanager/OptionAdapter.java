package com.example.android.clanmanager;


import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class OptionAdapter extends ArrayAdapter<Option> {

    public OptionAdapter(Context context, ArrayList<Option> options) {
        super(context, 0, options);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }

        Option currentOption = getItem(position);
        TextView descriptionTextView = (TextView) listItemView.findViewById(R.id.option_text_view);
        descriptionTextView.setText(currentOption.getDescription());

        TextView summaryTextView = (TextView) listItemView.findViewById(R.id.summary_text_view);
        summaryTextView.setText(currentOption.getSummary());

        return listItemView;
    }
}
