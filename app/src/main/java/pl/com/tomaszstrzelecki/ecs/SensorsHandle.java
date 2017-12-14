package pl.com.tomaszstrzelecki.ecs;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;

public class SensorsHandle {

    private int sensorType = 0;
    private int sensorSensitivity = 0;
    private SensorManager sensorManager;

    public ArrayList<Float> getSensorValues() {
        return sensorValues;
    }

    private ArrayList<Float> sensorValues = new ArrayList<>();

    public SensorsHandle(int sensorType, int sensorSensitivity, SensorManager sensorManager){
        this.sensorType = sensorType;
        this.sensorSensitivity = sensorSensitivity;
        this.sensorManager = sensorManager;
    }

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            ArrayList<Float> measurementValues = new ArrayList<>();
            Log.i("MeasurementValues", "###");
            for (float value :
                    sensorEvent.values) {
                measurementValues.add(value);
                Log.i("MeasurementValues", "" + value);
            }
            sensorValues = measurementValues;
            Log.i("MeasurementValues", "###");
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    public void startListeningSensor() {
        Sensor sensor = sensorManager.getDefaultSensor(sensorType);
        sensorManager.registerListener(sensorEventListener, sensor, sensorSensitivity);
    }

    public void stopListeningSensor() {
        sensorManager.unregisterListener(sensorEventListener);
    }
}
