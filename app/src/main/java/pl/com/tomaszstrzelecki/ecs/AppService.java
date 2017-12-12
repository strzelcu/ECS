package pl.com.tomaszstrzelecki.ecs;

import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

public class AppService extends Service {

    // Global variables

    private static android.app.NotificationManager mNotificationManager;
    public static final String STOP_MEASURING = "STOP_MEASURING";
    private final IBinder mBinder = new AppService.LocalBinder();
    public static int sensorType = 0;
    public static int sensorSensitivity = 0;
    public static int timeLength = 0;
    public static boolean isMesuringOn = false;

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
        Log.i("AppLog", "AppService is created");
        super.onCreate();
    }

    public void startMesuring() {
        isMesuringOn = true;
        showNotificationMeasuring(getApplicationContext());
        Toast.makeText(getApplicationContext(), "Rozpoczęto pomiar zużycia energii", Toast.LENGTH_LONG).show();
        Log.i("AppLog", "Mesuring is started");
    }

    public void stopMesuring() {
        isMesuringOn = false;
        //TODO DODAć ZAPIS DANYCH Z SENSORÓW
        SensorsData sensorsData = new SensorsData();
        String[] testData = {"test", "test"};
        sensorsData.addData(testData);
        sensorsData.saveData("SensorTestowy");
        Toast.makeText(getApplicationContext(), "Przerwano pomiar zużycia energii", Toast.LENGTH_LONG).show();
        Log.i("AppLog", "Mesuring is stopped");
    }


    // Notification handling

    public static void showNotificationMeasuring(Context context) {
        mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent stopMeasuringIntent = new Intent(context, NotificationActionService.class)
                .setAction(STOP_MEASURING);
        PendingIntent stopMeasuringPendingIntent = PendingIntent.getService(context, 0,
                stopMeasuringIntent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Pomiar rozpoczęty.")
                .setContentTitle("Trwa pomiar zużycia energii")
                .setContentText("W dowolnej chwili możesz wyłączyć pomiar zużycia energii.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .addAction(new NotificationCompat.Action(R.mipmap.ic_launcher,
                        "Zatrzymaj pomiar zużycia energii", stopMeasuringPendingIntent));
        ;
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
                appService.stopMesuring();
                NotificationManagerCompat.from(this).cancel(1);
            }
        }
    }
}