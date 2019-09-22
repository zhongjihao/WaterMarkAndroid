package com.android.watermark;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeUtil {
    public static final String TIME_FORMAT_WATERMARK_DISPLAY = "yyyy/MM/dd HH:mm:ss";

    public static String getCurrentTime(final String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);

        return "北京时间: "+formatter.format(Calendar.getInstance().getTime());
    }
}
