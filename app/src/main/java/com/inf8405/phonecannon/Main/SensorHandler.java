package com.inf8405.phonecannon.Main;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.Timer;
import java.util.TimerTask;

public class SensorHandler implements SensorEventListener {

    private final float GRAVITY = 9.8f;
    private final float THROW_THRESHOLD = 15f;
    private final long TIME_THRESHOLD = 1000;
    private final long TIME_DELAY = 1500;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor proximity;
    private MainActivity mActivity;
    private boolean isWaitingForThrow = false;
    private boolean isInFlight = false;
    private boolean isFacingUp = true;
    private long throwTime;
    private long landingTime;
    private float maxAcc;

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
            maxAcc = acc;
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
            waitFinalOrientation();
        } else if (acc > maxAcc){
            maxAcc = acc;
        }
    }

    private void waitFinalOrientation() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                mActivity.finishShot(throwTime, landingTime, maxAcc, isFacingUp);
            }
        }, TIME_DELAY);
    }

    public void listenForThrow(){
        isWaitingForThrow = true;
    }

    public void cancelThrow(){
        isWaitingForThrow = false;
        isInFlight = false;
    }
}
