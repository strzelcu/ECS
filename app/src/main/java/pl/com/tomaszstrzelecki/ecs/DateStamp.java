package pl.com.tomaszstrzelecki.ecs;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateStamp {

    private static Calendar c;

    public static String getStringDate() {
        return new SimpleDateFormat("dd.MM.yyyy").format(new Date());
    }

    public static String getStringTime() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    public static String getStringDateTime() {
        return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date());
    }
}