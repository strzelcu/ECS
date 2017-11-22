package pl.com.tomaszstrzelecki.ecs;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // Zmienne globalne

    public static int sensorType = 0;
    public static int sensorSensitivity = 0;
    public static int timeLength = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Spinner sensorsSpiner = (Spinner)findViewById(R.id.sensors_spinner);
        final String[] sensorsElements = {"Akcelerometr", "Żyroskop", "Magnetometr", "Czujnik zbliżeniowy"};
        ArrayAdapter<String> sensorsAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, sensorsElements);
        sensorsSpiner.setAdapter(sensorsAdapter);

        sensorsSpiner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int id, long position) {

                if(sensorType != 0) {
                    Toast.makeText(MainActivity.this, "Wybrano "
                            + sensorsElements[(int) position].toLowerCase(), Toast.LENGTH_SHORT).show();
                }

                switch((int)position)
                {
                    case 0:
                        sensorType = Sensor.TYPE_ACCELEROMETER;
                        break;
                    case 1:
                        sensorType = Sensor.TYPE_GYROSCOPE;
                        break;
                    case 2:
                        sensorType = Sensor.TYPE_MAGNETIC_FIELD;
                        break;
                    case 3:
                        sensorType = Sensor.TYPE_PROXIMITY;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        final Spinner sensitivitySpinner = (Spinner)findViewById(R.id.sensitivity_spinner);
        final String[] sensitivityElements = {"Niska", "Średnia", "Wysoka"};
        ArrayAdapter<String> sensitivityAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, sensitivityElements);
        sensitivitySpinner.setAdapter(sensitivityAdapter);

        sensitivitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int id, long position) {

                if(sensorSensitivity != 0) {
                    Toast.makeText(MainActivity.this, "Czułość "
                            + sensitivityElements[(int) position].toLowerCase(), Toast.LENGTH_SHORT).show();
                }

                switch((int)position)
                {
                    case 0:
                        sensorSensitivity = SensorManager.SENSOR_STATUS_ACCURACY_LOW;
                        break;
                    case 1:
                        sensorSensitivity = SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM;
                        break;
                    case 2:
                        sensorSensitivity = SensorManager.SENSOR_STATUS_ACCURACY_HIGH;
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
    }
}