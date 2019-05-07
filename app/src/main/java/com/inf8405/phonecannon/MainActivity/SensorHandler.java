package com.inf8405.phonecannon.MainActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class SensorHandler implements SensorEventListener {

    private final float GRAVITY = 9.8f;
    private final float THROW_THRESHOLD = 18f;
    private final long TIME_THRESHOLD = 200;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor proximity;
    private MainActivity mActivity;
    private boolean isWaitingForThrow = false;
    private boolean isInFlight = false;
    private boolean isFacingUp = true;
    private long throwTime;
    private long landingTime;
    private float initialAcc;

    public SensorHandler (MainActivity activity) {
        mActivity = activity;
        sensorTest();
    }

    public void registerListeners() {
        if (accelerometer != null)
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        if (proximity != null)
        sensorManager.registerListener(this, proximity, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregisterListeners() {
        sensorManager.unregisterListener(this);
    }

    private void sensorTest(){
        sensorManager = (SensorManager) mActivity.getSystemService(mActivity.SENSOR_SERVICE);

        // Use the accelerometer.
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mActivity.toast("Accelerometer found!");
        } else{
            mActivity.toast("Accelerometer NOT found!\nDevice is not compatible!");
        }
        // Use the proximity.
        if (sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null){
            proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            mActivity.toast("Proximity sensor found!");
        } else{
            mActivity.toast("Proximity sensor NOT found!\nDevice is not compatible!");
        }
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            isFacingUp = event.values[0] >= 1;
        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (isWaitingForThrow) {
                checkForThrowBegin(Math.abs(event.values[0]) + Math.abs(event.values[1]) + Math.abs(event.values[2]));
            }
            if (isInFlight) {
                checkForLanding(Math.abs(event.values[0]) + Math.abs(event.values[1]) + Math.abs(event.values[2]));
            }
        }
    }

    private void checkForThrowBegin(float acc) {
        if (acc > GRAVITY + THROW_THRESHOLD) {
            mActivity.startThrow();
            initialAcc = acc;
            isWaitingForThrow = false;
            isInFlight = true;
            throwTime = System.currentTimeMillis();
        }
    }

    private void checkForLanding(float acc) {
        if (System.currentTimeMillis() - throwTime > TIME_THRESHOLD && acc > GRAVITY + THROW_THRESHOLD) {
            mActivity.makeLanding();
            isInFlight = false;
            landingTime = System.currentTimeMillis();
            mActivity.finishShot(throwTime, landingTime, initialAcc, isFacingUp);
        }
    }

    public void listenForThrow(){
        isWaitingForThrow = true;
    }

    public void cancelThrow(){
        isWaitingForThrow = false;
        isInFlight = false;
    }
}
