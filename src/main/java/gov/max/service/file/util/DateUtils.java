package gov.max.service.file.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    private static String DATE_FORMAT = "EEE, d MMM yy HH:mm";

    public static boolean isTimeStampValid(String inputString) {
        SimpleDateFormat format = new java.text.SimpleDateFormat(DATE_FORMAT);
        try {
            long dtValue = Long.parseLong(inputString);
            Date dt = new Date(dtValue);
            format.format(dt);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String timeStampValue(String inputString) {
        SimpleDateFormat format = new java.text.SimpleDateFormat(DATE_FORMAT);
        try {
            long dtValue = Long.parseLong(inputString);
            Date dt = new Date(dtValue);
            return format.format(dt).toString();
        } catch (Exception e) {
            return "";
        }
    }
}
