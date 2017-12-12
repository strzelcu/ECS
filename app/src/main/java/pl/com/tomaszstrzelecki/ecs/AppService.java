package pl.com.tomaszstrzelecki.ecs;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class AppService extends Service {

    // Global variables

    private final IBinder mBinder = new AppService.LocalBinder();
    public static int sensorType = 0;

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

    public static int sensorSensitivity = 0;
    public static int timeLength = 0;
    public static boolean isMesuringOn = false;

    public static boolean isMesuringOn() {
        return isMesuringOn;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public static int getTimeLength() {
        return timeLength;
    }

    public static void setTimeLength(int timeLength) {
        AppService.timeLength = timeLength;
    }

    class LocalBinder extends Binder {
        AppService getService() {
            return AppService.this;
        }
    }

    // AppService lifecycle

    @Override
    public void onCreate() {
        Log.i("AppService", "AppService is created");
        super.onCreate();
    }

    public void startTest() {
        isMesuringOn = true;
        Log.i("AppService", "AppService is started");
    }


}
