package pl.com.tomaszstrzelecki.ecs;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // Graphics variables

    protected Button monitoring;
    protected EditText editTextTimeLength;
    protected Spinner sensorsSpiner;
    protected Spinner sensitivitySpinner;

    // App Service
    private AppService appService;
    private boolean isAppServiceConnect = false;
    private Intent appServiceIntent;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            AppService.LocalBinder binder = (AppService.LocalBinder) service;
            appService = binder.getService();
            isAppServiceConnect = true;
            Log.i("MainActivity", "AppService is binded to MainActivity");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isAppServiceConnect = false;
            Log.i("MainActivity", "AppService is unbinded from MainActivity");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Graphics initialization

        setContentView(R.layout.activity_main);
        editTextTimeLength = (EditText) findViewById(R.id.timeLength);

        // Sensor menu

        sensorsSpiner = (Spinner) findViewById(R.id.sensors_spinner);
        final String[] sensorsElements = {"Akcelerometr", "Żyroskop", "Magnetometr", "Czujnik zbliżeniowy"};
        ArrayAdapter<String> sensorsAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, sensorsElements);
        sensorsSpiner.setAdapter(sensorsAdapter);

        sensorsSpiner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int id, long position) {

                if (AppService.getSensorType() != 0) {

                }

                switch ((int) position) {
                    case 0:
                        AppService.setSensorType(Sensor.TYPE_ACCELEROMETER);
                        break;
                    case 1:
                        AppService.setSensorType(Sensor.TYPE_GYROSCOPE);
                        break;
                    case 2:
                        AppService.setSensorType(Sensor.TYPE_MAGNETIC_FIELD);
                        break;
                    case 3:
                        AppService.setSensorType(Sensor.TYPE_PROXIMITY);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        // Sensitivity menu

        sensitivitySpinner = (Spinner) findViewById(R.id.sensitivity_spinner);
        final String[] sensitivityElements = {"Niska", "Średnia", "Wysoka"};
        ArrayAdapter<String> sensitivityAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, sensitivityElements);
        sensitivitySpinner.setAdapter(sensitivityAdapter);

        sensitivitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int id, long position) {

                if (AppService.getSensorSensitivity() != 0) {

                }

                switch ((int) position) {
                    case 0:
                        AppService.setSensorSensitivity(SensorManager.SENSOR_STATUS_ACCURACY_LOW);
                        break;
                    case 1:
                        AppService.setSensorSensitivity(SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM);
                        break;
                    case 2:
                        AppService.setSensorSensitivity(SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });


        // Monitor button

        monitoring = (Button) findViewById(R.id.monitoring);

        monitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!AppService.isMesuringOn()) {
                    monitoring.setText("Trwa pomiar");
                    monitoring.setEnabled(false);
                    sensorsSpiner.setEnabled(false);
                    sensitivitySpinner.setEnabled(false);
                    editTextTimeLength.setEnabled(false);
                    AppService.setTimeLength((Integer.valueOf(editTextTimeLength.getText().toString())));
                    appService.startTest();
                    finish();
                }
            }
        });
    }

    @Override
    public void onStart() {
        appServiceIntent = new Intent(this, AppService.class);
        bindService(appServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
        startService(appServiceIntent);
        if(AppService.isMesuringOn()){
            monitoring.setEnabled(false);
            sensorsSpiner.setEnabled(false);
            sensitivitySpinner.setEnabled(false);
            editTextTimeLength.setEnabled(false);
            editTextTimeLength.setText("" + AppService.getTimeLength());
            monitoring.setText("Trwa pomiar");
        }
        super.onStart();
        Log.i("MainActivity", "Main activity started");
    }

    @Override
    public void onStop() {
        if (isAppServiceConnect) {
            unbindService(mConnection);
            isAppServiceConnect = false;
        }
        if(AppService.isMesuringOn()){
            monitoring.setEnabled(true);
            sensorsSpiner.setEnabled(true);
            sensitivitySpinner.setEnabled(true);
            editTextTimeLength.setEnabled(true);
        }
        super.onStop();
        Log.i("MainActivity", "Main activity stopped");
    }
}