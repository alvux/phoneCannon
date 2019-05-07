package com.inf8405.phonecannon;

import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.BatteryManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.inf8405.phonecannon.MainActivity.MainActivity;

public class DeviceInfo {

    Dialog deviceInfoDialog;
    private MainActivity mainActivity;
    ImageButton infoButton;

    public DeviceInfo(MainActivity mainActivity){
        this.mainActivity = mainActivity;

        this.deviceInfoDialog = new Dialog(mainActivity);
        this.infoButton =  mainActivity.findViewById(R.id.button_info);

        this.infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceInfoDialog.setContentView(R.layout.info);
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom( DeviceInfo.this.deviceInfoDialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;


                DeviceInfo.this.deviceInfoDialog.show();
                DeviceInfo.this.deviceInfoDialog.getWindow().setAttributes(lp);

                ImageView closeInfoImg =  DeviceInfo.this.deviceInfoDialog.findViewById(R.id.closeInfoImg);
                closeInfoImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DeviceInfo.this.deviceInfoDialog.dismiss();
                    }
                });

                DeviceInfo.this.getDeviceInfo();
            }
        });
    }

    private void getDeviceInfo(){
        this.getBatteryInfo();
        this.getDownlink();
        this.getUplink();
    }

    private void getBatteryInfo(){
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus =  this.mainActivity.registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level / (float)scale * 100;


        float intialBatteryPct = DeviceInitialBatteryStatus.getInstance().getInitialBatteryPercentage();

        TextView batteryInfo = this.deviceInfoDialog.findViewById(R.id.batteryPct);

        batteryInfo.setText(Math.round((intialBatteryPct-batteryPct)) + " %  (Remaining: " + Math.round(batteryPct) + " % ) ");
    }

    private void getDownlink() {

        PackageManager manager = mainActivity.getApplicationContext().getPackageManager();
        int uid = -1;
        try{
            uid = manager.getApplicationInfo("com.inf8405.phonecannon",0).uid;
        }catch(PackageManager.NameNotFoundException err){

        }

        long receivedBytes = TrafficStats.getUidRxBytes(uid);

        TextView downlinkInfo = this.deviceInfoDialog.findViewById(R.id.downlinkInfo);
        downlinkInfo.setText(Float.toString(receivedBytes) + " bytes");
    }

    private void getUplink() {

        PackageManager manager = mainActivity.getApplicationContext().getPackageManager();
        int uid = -1;
        try{
            uid = manager.getApplicationInfo("com.inf8405.phonecannon",0).uid;
        }catch(PackageManager.NameNotFoundException err){

        }
        long transferedBytes = TrafficStats.getUidTxBytes(uid);

        TextView uplinkInfo = this.deviceInfoDialog.findViewById(R.id.uplinkInfo);
        uplinkInfo.setText(Float.toString(transferedBytes) + " bytes");
    }
}
