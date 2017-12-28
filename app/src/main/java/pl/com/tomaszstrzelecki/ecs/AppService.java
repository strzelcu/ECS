package pl.com.tomaszstrzelecki.ecs;

import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import pl.com.tomaszstrzelecki.ecs.util.DateStamp;
import pl.com.tomaszstrzelecki.ecs.util.SensorsData;

public class AppService extends Service {

    // Global variables

    private static android.app.NotificationManager mNotificationManager;
    public static final String STOP_MEASURING = "STOP_MEASURING";
    private final IBinder mBinder = new AppService.LocalBinder();
    public static int sensorType = 0;
    public static int sensorSensitivity = 0;
    public static int timeLength = 0;
    public static boolean isMesuringOn = false;
    private int batteryPercentage = 0;
    private int batteryVoltage = 0;
    private SensorsHandle sensorHandle;
    private Thread savingDataThread;
    private SensorsData sensorsData;

    //Getters and setters

    public static int getSensorType() {
        return sensorType;
    }

    public static void setSensorType(int sensorType) {
        AppService.sensorType = sensorType;
    }

    public static int getSensorSensitivity() {
        return sensorSensitivity;
    }

    public static void setSensorSensitivity(int sensorSensitivity) {
        AppService.sensorSensitivity = sensorSensitivity;
    }

    public static boolean isMesuringOn() {
        return isMesuringOn;
    }

    public static void setTimeLength(int timeLength) {
        AppService.timeLength = timeLength;
    }

    public static int getTimeLength() {
        return timeLength;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    class LocalBinder extends Binder {
        AppService getService() {
            return AppService.this;
        }
    }

    // AppService lifecycle

    @Override
    public void onCreate() {
        registerBatteryLevelReceiver();
        Log.i("AppLog", "AppService is created");
        super.onCreate();
    }

    public void startMesuring() {
        isMesuringOn = true;
        showNotificationMeasuring(getApplicationContext());
        sensorHandle = new SensorsHandle(sensorType, sensorSensitivity, (SensorManager) getSystemService(Context.SENSOR_SERVICE));
        sensorHandle.startListeningSensor();
        startSavingDataThread();
        Toast.makeText(getApplicationContext(), "Rozpoczęto pomiar zużycia energii", Toast.LENGTH_LONG).show();
        Log.i("AppLog", "Mesuring is started");
    }

    public void stopMesuring() {
        isMesuringOn = false;
        NotificationManagerCompat.from(this).cancelAll();
        stopSavingDataThread();
        sensorHandle.stopListeningSensor();
        Log.i("AppLog", "Mesuring is stopped");
    }


    // Notification handling

    public static void showNotificationMeasuring(Context context) {
        mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent stopMeasuringIntent = new Intent(context, NotificationActionService.class)
                .setAction(STOP_MEASURING);
        PendingIntent stopMeasuringPendingIntent = PendingIntent.getService(context, 0,
                stopMeasuringIntent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Pomiar rozpoczęty.")
                .setContentTitle("Trwa pomiar zużycia energii")
                .setContentText("W dowolnej chwili możesz wyłączyć pomiar zużycia energii.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .addAction(new NotificationCompat.Action(R.mipmap.ic_launcher,
                        "Zatrzymaj pomiar zużycia energii", stopMeasuringPendingIntent));

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        int id = 1;
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        mNotificationManager.notify(id, mBuilder.build());
    }

    public static class NotificationActionService extends IntentService {

        private AppService appService;
        private Intent appServiceIntent;
        private ServiceConnection mConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className,
                                           IBinder service) {
                AppService.LocalBinder binder = (AppService.LocalBinder) service;
                appService = binder.getService();
                Log.i("AppLog", "AppService is binded to NotificationActionService");
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                Log.i("AppLog", "AppService is unbinded from NotificationActionService");
            }
        };

        @Override
        public void onCreate() {
            super.onCreate();
            appServiceIntent = new Intent(this, AppService.class);
            bindService(appServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
        }

        public NotificationActionService() {
            super(NotificationActionService.class.getSimpleName());
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            String action = intent.getAction();
            if (STOP_MEASURING.equals(action)) {
                try {
                    appService.stopMesuring();
                    NotificationManagerCompat.from(this).cancel(1);
                    android.os.Process.killProcess(android.os.Process.myPid());
                } catch (Exception e) {
                    NotificationManagerCompat.from(this).cancel(1);
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            }
        }
    }

    // SavingDataThread

    private void startSavingDataThread() {
        sensorsData = new SensorsData(sensorType);

        savingDataThread = new Thread() {

            int timeInSeconds = timeLength * 60;
            int timeStamp = 1;
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        ArrayList<String> values = new ArrayList<>();
                        values.add(DateStamp.getStringDateTime());
                        values.add(String.valueOf(System.currentTimeMillis()));
                        values.add(String.valueOf(timeStamp));
                        for (Float value :
                                sensorHandle.getSensorValues()) {
                            values.add(String.valueOf(value));
                        }
                        values.add(String.valueOf(batteryPercentage));
                        values.add(String.valueOf(batteryVoltage));
                        sensorsData.addData(values);
                        timeStamp++;
                        timeInSeconds--;
                        if (timeInSeconds < 0) {
                            sensorsData.closeFile();
                            stopMesuring();
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    }
                } catch (InterruptedException ignored) {
                    sensorsData.closeFile();
                    isMesuringOn = false;
                }
            }
        };
        Log.i("AppLog", "SavingDataThread started");
        savingDataThread.start();
    }

    private void stopSavingDataThread() {
        savingDataThread.interrupt();
        Log.i("AppLog", "SavingDataThread stoped");
    }

    // Battery details handle

    private void registerBatteryLevelReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(battery_receiver, filter);
    }

    private BroadcastReceiver battery_receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isPresent = intent.getBooleanExtra("present", false);
            String technology = intent.getStringExtra("technology");
            int scale = intent.getIntExtra("scale", -1);
            int rawlevel = intent.getIntExtra("level", -1);
            int voltage = intent.getIntExtra("voltage", 0);
            int level = 0;

            Bundle bundle = intent.getExtras();

            Log.i("AppLog", "BatteryLevel" + bundle.toString());

            if (isPresent) {
                if (rawlevel >= 0 && scale > 0) {
                    level = (rawlevel * 100) / scale;
                }

                String info = ("Technology: " + technology + "\n");
                batteryPercentage = level;
                info += "Battery Level: " + level + "%\n";
                batteryVoltage = voltage;
                info += ("Voltage: " + voltage + "\n");

                Log.i("BatterInfo", info);
            }
        }
    };

    @Override
    protected void finalize() throws Throwable {
        sensorsData.closeFile();
        super.finalize();
    }
}