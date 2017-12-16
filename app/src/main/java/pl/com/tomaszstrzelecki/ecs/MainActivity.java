package pl.com.tomaszstrzelecki.ecs;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import static pl.com.tomaszstrzelecki.ecs.AppService.*;

public class MainActivity extends AppCompatActivity {

    // Global variables

    static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 1;

    // Graphics variables

    protected Button monitoring;
    protected EditText editTextTimeLength;
    protected Spinner sensorsSpiner;
    protected Spinner sensitivitySpinner;

    // App Service
    private AppService appService;
    private boolean isAppServiceConnect = false;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            AppService.LocalBinder binder = (AppService.LocalBinder) service;
            appService = binder.getService();
            isAppServiceConnect = true;
            Log.i("AppLog", "AppService is binded to MainActivity");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isAppServiceConnect = false;
            Log.i("AppLog", "AppService is unbinded from MainActivity");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        checkWriteExternalStoragePermissions();

        // Graphics initialization

        setContentView(R.layout.activity_main);
        editTextTimeLength = findViewById(R.id.timeLength);

        // Sensor menu

        sensorsSpiner = findViewById(R.id.sensors_spinner);
        final String[] sensorsElements = {"Akcelerometr", "Żyroskop", "Magnetometr", "Czujnik zbliżeniowy"};
        ArrayAdapter<String> sensorsAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, sensorsElements);
        sensorsSpiner.setAdapter(sensorsAdapter);

        sensorsSpiner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int id, long position) {

                switch ((int) position) {
                    case 0:
                        setSensorType(Sensor.TYPE_ACCELEROMETER);
                        break;
                    case 1:
                        setSensorType(Sensor.TYPE_GYROSCOPE);
                        break;
                    case 2:
                        setSensorType(Sensor.TYPE_MAGNETIC_FIELD);
                        break;
                    case 3:
                        setSensorType(Sensor.TYPE_PROXIMITY);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        // Sensitivity menu

        sensitivitySpinner = findViewById(R.id.sensitivity_spinner);
        final String[] sensitivityElements = {"Niska", "Średnia", "Wysoka"};
        ArrayAdapter<String> sensitivityAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, sensitivityElements);
        sensitivitySpinner.setAdapter(sensitivityAdapter);

        sensitivitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int id, long position) {

                switch ((int) position) {
                    case 0:
                        setSensorSensitivity(SensorManager.SENSOR_STATUS_ACCURACY_LOW);
                        break;
                    case 1:
                        setSensorSensitivity(SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM);
                        break;
                    case 2:
                        setSensorSensitivity(SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
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
                if (!isMesuringOn()) {
                    try {
                        setTimeLength((Integer.valueOf(editTextTimeLength.getText().toString())));
                        monitoring.setText("Trwa pomiar");
                        monitoring.setEnabled(false);
                        sensorsSpiner.setEnabled(false);
                        sensitivitySpinner.setEnabled(false);
                        editTextTimeLength.setEnabled(false);
                        appService.startMesuring();
                        finish();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Wpisz czas w pole tekstowe!", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    // Check permissions

    private static class PermissionManager {
        //A method that can be called from any Activity, to check for specific permission
        private static void check(Activity activity, String permission, int requestCode){
            //If requested permission isn't Granted yet
            if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                //Request permission from user
                ActivityCompat.requestPermissions(activity,new String[]{permission},requestCode);
            }
        }
    }

    public void checkWriteExternalStoragePermissions() {
        PermissionManager.check(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Brak uprawnień zapisu w pamięci wewnętrznej", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onStart() {
        Intent appServiceIntent = new Intent(this, AppService.class);
        bindService(appServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
        startService(appServiceIntent);
        if(isMesuringOn()){
            monitoring.setEnabled(false);
            sensorsSpiner.setEnabled(false);
            sensitivitySpinner.setEnabled(false);
            editTextTimeLength.setEnabled(false);
            //editTextTimeLength.setText(getTimeLength());
            //monitoring.setText("Trwa pomiar");
        }
        super.onStart();
        Log.i("AppLog", "Main activity started");
    }

    @Override
    public void onStop() {
        if (isAppServiceConnect) {
            unbindService(mConnection);
            isAppServiceConnect = false;
        }
        if(isMesuringOn()){
            monitoring.setEnabled(true);
            sensorsSpiner.setEnabled(true);
            sensitivitySpinner.setEnabled(true);
            editTextTimeLength.setEnabled(true);
        }
        super.onStop();
        Log.i("AppLog", "Main activity stopped");
    }
}