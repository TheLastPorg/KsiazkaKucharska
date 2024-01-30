package com.example.kucharska;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

public class SensorService extends Service implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private int threshold = 11000;
    private static int textColor = Color.BLACK;
    private static int backgroundColor = Color.WHITE;
    private int lux = 0;

    private static SensorDataListener sensorDataListener;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("SensorService", "onCreate");

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onDestroy() {
        Log.d("SensorService", "onDestroy");
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d("SensorService", "onAccuracyChanged");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            lux = (int)event.values[0];

            if (lux < threshold) {
                textColor = Color.WHITE;
                backgroundColor = Color.DKGRAY;
            } else {
                textColor = Color.BLACK;
                backgroundColor = Color.WHITE;
            }

            if (sensorDataListener != null){
                sensorDataListener.onColorsChanged(textColor, backgroundColor);
            }
        }
    }


    public static void setSensorDataListener(SensorDataListener listener) {
        Log.d("SensorService", "setSensorDataListener");
        sensorDataListener = listener;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("SensorService", "onBind");
        return null;
    }

    public static int getTextColor() {
        return textColor;
    }

    public static int getBackgroundColor() {
        return backgroundColor;
    }
}