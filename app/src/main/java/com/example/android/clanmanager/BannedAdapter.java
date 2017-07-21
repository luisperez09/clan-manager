package com.example.android.clanmanager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.android.clanmanager.pojo.Banned;

import java.util.ArrayList;


public class BannedAdapter extends ArrayAdapter<Banned> {

    public BannedAdapter(Context context, ArrayList<Banned> banneds){
        super(context, 0, banneds);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        Banned banned = getItem(position);
        TextView bannedTextView = (TextView) listItemView.findViewById(R.id.option_text_view);
        bannedTextView.setText(banned.getBanned());

        TextView reasonTextView = (TextView) listItemView.findViewById(R.id.summary_text_view);
        reasonTextView.setText(banned.getReason());

        return listItemView;
    }
}
