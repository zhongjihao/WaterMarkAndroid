package com.android.watermark;

import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zhongjihao on 18-2-7.
 */
public class FileUtil {
    private final static String TAG = "FileUtil";

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

    public static int copyAssetToFileDir(AssetManager assetManager, final String assetName, final String targetFilePath) {
        File targetFile = new File(targetFilePath);
        byte buf[] = new byte[1024];
        int cnt;

        InputStream src = null;
        FileOutputStream dst = null;
        if (!targetFile.exists()) {
            try {
                targetFile.createNewFile();
                dst = new FileOutputStream(targetFile);
                src = assetManager.open(assetName);

                while ((cnt = src.read(buf)) > 0) {
                    dst.write(buf, 0, cnt);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "copyAssetToFileDir exception " + e.toString());
            } finally {
                try {
                    if (src != null) {
                        src.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    if(dst != null){
                        dst.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return 0;
    }
}
