package pl.com.tomaszstrzelecki.ecs;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SensorsData {

    private StringBuffer data;
    private int id = 1;
    private String date;

    SensorsData() {
        data = new StringBuffer();
        date = DateStamp.getStringDateTime().toLowerCase().replace(" ", "_");
    }

    void addData(String[] inputData){
        StringBuilder record = new StringBuilder();
        for (String input :
                inputData) {
            record.append(input).append(",");
        }
        record.append("\n");
        data.append(record);
    }

    void saveData(String sensorName){

        String fileName = sensorName + "_" + date + "_pomiar.csv";
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
}

