package com.inf8405.phonecannon.Connection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private static WiFiDirectBroadcastReceiver instance = null;

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private ConnectionActivity mActivity;
    public CommunicationService communicationService;
    public boolean isOwner = false;

    public static WiFiDirectBroadcastReceiver getInstance() {
        return instance;
    }

    public static WiFiDirectBroadcastReceiver buildInstance(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                     ConnectionActivity activity) {
        if(instance != null && instance.communicationService != null) {
            instance.communicationService.disconnect();
        }
        instance = new WiFiDirectBroadcastReceiver(manager, channel, activity);
        return instance;
    }

    private WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       ConnectionActivity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
    }
    public void disconnect() {
        if (mManager != null && mChannel != null) {
            mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {
                    if (group != null && mManager != null && mChannel != null
                            && group.isGroupOwner()) {
                        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                Log.d("Disconnect Message", "removeGroup onSuccess -");
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d("Disconnect message", "removeGroup onFailure -" + reason);
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            // Determine if Wifi P2P mode is enabled or not, alert
            // the Activity.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                if(mActivity != null) Toast.makeText(this.mActivity,"WIFI ON", Toast.LENGTH_LONG).show();
            } else {
                if(mActivity != null) Toast.makeText(this.mActivity,"WIFI OFF", Toast.LENGTH_LONG).show();
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
            if (mManager != null) {
                mManager.requestPeers(mChannel, peerListListener);
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
            NetworkInfo networkInfo = intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                mManager.requestConnectionInfo(mChannel, info -> {
                    // If the group is formed and you are the server
                    if(info.groupFormed && info.isGroupOwner) {
                        isOwner = true;
                        // Disconnect from previous group if it wasn't done before
                        if(communicationService != null) communicationService.disconnect();

                        communicationService = new ServerService();
                        // Register the current activity as a listener to the service
                        if(mActivity != null) communicationService.register(mActivity);

                        // Start the connection
                        communicationService.start();

                    // else if you are the client
                    } else if(info.groupFormed) {
                        isOwner = false;
                        // Disconnect from previous group if it wasn't done before
                        if(communicationService != null) communicationService.disconnect();

                        communicationService = new ClientService(info);
                        // Register the current activity as a listener to the service
                        if(mActivity != null) communicationService.register(mActivity);

                        // Start the connection
                        communicationService.start();
                    }

                });
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }

    public void connectTo(WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // Unused, we get the event in the onReceive function above
            }

            @Override
            public void onFailure(int reason) {
                if(mActivity != null) {
                    Toast.makeText(mActivity, "Could not connect to " + device.deviceName, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if(mActivity != null) {
                mActivity.availablePeersList.clear();
                mActivity.availablePeersList.addAll(peerList.getDeviceList());
                mActivity.peersAdapter.notifyDataSetChanged();
            }
        }
    };



}