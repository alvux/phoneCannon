package com.inf8405.phonecannon;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public final class DeviceInitialBatteryStatus {

    private static DeviceInitialBatteryStatus INSTANCE = null;
    public float initialBatteryPct = 0;
    private DeviceInitialBatteryStatus(){

    };

    public static DeviceInitialBatteryStatus getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new DeviceInitialBatteryStatus();
        }

        return INSTANCE;
    }

    public float getInitialBatteryPercentage() {
        return this.initialBatteryPct;
    }

    public void setInitialBatteryPct(float initialBatteryPct) {
        this.initialBatteryPct =  initialBatteryPct;
    }


}
