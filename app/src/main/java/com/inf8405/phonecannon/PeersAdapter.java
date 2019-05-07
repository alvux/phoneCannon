package com.inf8405.phonecannon;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class PeersAdapter extends ArrayAdapter<WifiP2pDevice> {

    public PeersAdapter(Context context, List<WifiP2pDevice> events) {
        super(context, 0, events);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        WifiP2pDevice event = getItem(position);
        convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        ((TextView) convertView).setText(event.deviceName);

        return convertView;
    }

}

