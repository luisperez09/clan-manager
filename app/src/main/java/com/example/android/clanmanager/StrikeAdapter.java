package com.example.android.clanmanager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class StrikeAdapter extends ArrayAdapter<Strike> {

    public StrikeAdapter(Context context, ArrayList<Strike> data) {
        super(context, 0, data);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item,
                    parent, false);
        }

        Strike currentStrike = getItem(position);

        String date = currentStrike.getDate();
        TextView dateTextView = (TextView) listItemView.findViewById(R.id.summary_text_view);
        dateTextView.setText(date);

        String reason = currentStrike.getReason();
        TextView reasonTextView = (TextView) listItemView.findViewById(R.id.option_text_view);
        reasonTextView.setText(reason);

        return listItemView;
    }
}
