package com.android.watermark;

import android.os.Environment;

import java.io.File;

/**
 * Created by zhongjihao on 18-2-7.
 */
public class FileUtil {

    /**
     * 判断SD卡是否被挂载
     * @param sdcardPath
     * @return
     */
    public static boolean isMount(String sdcardPath) {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState(new File(sdcardPath)));
    }

    public static String getPicFileName(long timeMillis){
        return String.format("%1$tY-%1$tm-%1$td_%1$tH_%1$tM_%1$tS_%1$tL.jpg", timeMillis);
    }
}
