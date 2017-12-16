package pl.com.tomaszstrzelecki.ecs.util;

import android.hardware.Sensor;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class SensorsData {

    private StringBuffer data;
    private int id = 1;
    private String date;

    public SensorsData() {
        data = new StringBuffer();
    }

    public void addData(ArrayList<String> inputData){
        StringBuilder record = new StringBuilder();
        for (String input :
                inputData) {
            record.append(input).append(";");
        }
        record.deleteCharAt(record.lastIndexOf(";"));
        record.append("\n");
        data.append(record);
    }

    public void saveData(int sensorNumber){

        date = DateStamp.getStringDateTime().toLowerCase().replace(" ", "_");
        String fileName = getSensorName(sensorNumber) + "_" + date + "_pomiar.csv";
        File file;

        if(isExternalStorageWritable()) {

            File path = new File(Environment.getExternalStorageDirectory() + File.separator + "ECS", "pomiary");
            file = new File(path, fileName);

            if(!path.exists()) {
                path.mkdirs();
            }

            if(!file.exists()) {
                try {
                    file.createNewFile();
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write(data.toString().getBytes());
                    fileOutputStream.close();
                } catch (IOException e) {
                    Log.e("AppLog", "Something happend while saving *.csv file");
                }
            }
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private String getSensorName(int sensorNumber) {
        switch(sensorNumber){
            case Sensor.TYPE_ACCELEROMETER: return "Akcelerometr";
            case Sensor.TYPE_GYROSCOPE: return "Żyroskop";
            case Sensor.TYPE_MAGNETIC_FIELD: return "Magnetometr";
            case Sensor.TYPE_PROXIMITY: return "Czujnik_zbliżeniowy";
        }
        return "Unknown";
    }
}

