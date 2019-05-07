package com.inf8405.phonecannon.Connection;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import com.inf8405.phonecannon.Main.MainActivity;
import com.inf8405.phonecannon.R;
import com.inf8405.phonecannon.Utils.PermissionManager;

import java.util.ArrayList;
import java.util.List;

import static com.inf8405.phonecannon.Utils.MessageType.READY;
import static com.inf8405.phonecannon.Utils.MessageType.START;

public class ConnectionActivity extends AppCompatActivity implements CommunicationListener {

    private final IntentFilter intentFilter = new IntentFilter();
    WifiP2pManager.Channel channel;
    WifiP2pManager manager;
    WiFiDirectBroadcastReceiver receiver;
    ListView peersListView;
    PeersAdapter peersAdapter;
    List<WifiP2pDevice> availablePeersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        setupPeersList();

        PermissionManager.checkAllPermissions(this);

        // Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WiFiDirectBroadcastReceiver.getInstance().disconnect();
    }

    @Override
    public void onResume() {
        super.onResume();
        receiver = WiFiDirectBroadcastReceiver.buildInstance(manager, channel, this);
        registerReceiver(receiver, intentFilter);

        // Automatically start discovering peers
        discoverPeers();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    private void setupPeersList() {
        peersListView = findViewById(R.id.peers_lv);
        peersAdapter = new PeersAdapter(this, availablePeersList);
        peersListView.setAdapter(peersAdapter);

        // Connect to peer on click
        peersListView.setOnItemClickListener((parent, view, position, id) -> {
            WifiP2pDevice device = (WifiP2pDevice) parent.getItemAtPosition(position);
            receiver.connectTo(device);
        });
    }

    private void discoverPeers() {
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // Unused
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.e("ConnectionActivity", "Peer discovery onFailure with code " + reasonCode);
            }
        });
    }

    /**
     * Confirms the connection before starting the main activity
     * The client sends a request.
     * Then, the server gives the signal to start.
     */
    @Override
    public void onConnection() {
        if(!receiver.isOwner) {
            receiver.communicationService.sendMessage(READY, "");
        }
    }

    @Override
    public void onNewMessage(int type, String message) {
        if(type == READY) {
            // Notify the second phone to start the game
            receiver.communicationService.sendMessage(START, "");
            startGame();
        } else if(type == START) {
            startGame();
        }
    }

    private void startGame() {
        Intent myIntent = new Intent(ConnectionActivity.this, MainActivity.class);
        startActivity(myIntent);
    }
}
