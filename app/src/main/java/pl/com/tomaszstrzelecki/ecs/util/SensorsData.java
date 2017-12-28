package pl.com.tomaszstrzelecki.ecs.util;

import android.hardware.Sensor;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class SensorsData {

    private int id = 1;
    private File file;

    public SensorsData(int sensorType) {
        String date = DateStamp.getStringDateTime().toLowerCase().replace(" ", "_");
        String fileName = getSensorName(sensorType) + "_" + date + "_pomiar.csv";

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
                    fileOutputStream.close();
                } catch (IOException e) {
                    Log.e("AppLog", "Something happend while creating *.csv file");
                }
            }
        }
    }

    public void addData(ArrayList<String> inputData){
        StringBuilder record = new StringBuilder();
        for (String input :
                inputData) {
            record.append(input).append(";");
        }
        record.deleteCharAt(record.lastIndexOf(";"));
        record.append("\n");

        if(file.exists()) {
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file, true);
                fileOutputStream.write(record.toString().getBytes());
                fileOutputStream.close();
            } catch (IOException e) {
                Log.e("AppLog", "Something happend while appending record to *.csv file");
            }
        }
    }

    public void closeFile(){
        Log.i("AppLog", "Closing file (Not yet closed?)");
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

