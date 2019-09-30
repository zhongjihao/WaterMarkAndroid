package com.android.watermark;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.common.cache.Cache;


/**
 * Created by zhongjihao100@163.com on 19-9-25.
 * weixin: 18626455927
 */
public class WaterMarkTask implements Runnable{
    private static final String TAG = "WaterMarkTask";
    private static final int DOWNLOAD_THREAD_PRIORITY = Process.THREAD_PRIORITY_URGENT_AUDIO;


    private Camera.Size size;
    private byte[] data;
    private WaterMark.WaterMarkInfo mWaterMarkInfo;
    private String mAddress;
    private Cache<Long, VideoGather.Frame> mDataCaches;
    private YuvToBitmap nv21ToBitmap;
    private SurfaceView mWaterMarkView;


    public WaterMarkTask(Camera.Size size,byte[] data,WaterMark.WaterMarkInfo waterMarkInfo, String address, Cache<Long, VideoGather.Frame> dataCaches, YuvToBitmap nv21ToBitmap, SurfaceView mWaterMarkView) {
        this.size = size;
        this.data = data;
        this.mWaterMarkInfo = waterMarkInfo;
        this.mAddress = address;
        this.mDataCaches = dataCaches;
        this.nv21ToBitmap = nv21ToBitmap;
        this.mWaterMarkView = mWaterMarkView;
    }

    @Override
    public void run() {
        Process.setThreadPriority(DOWNLOAD_THREAD_PRIORITY);
        Log.d(TAG,"E: run");
        if (mWaterMarkInfo != null) {
            try {
                Bitmap srcBitmap = null;
                mWaterMarkInfo.onTimeChanged(TimeUtil.getCurrentTime(TimeUtil.TIME_FORMAT_WATERMARK_DISPLAY));
                mWaterMarkInfo.onLocationChanged(mAddress);

                Log.d(TAG,"width: "+size.width+"  height: "+size.height);
                byte[] yuv = new byte[size.width * size.height *3/2];
                int[] outWidth = new int[1];
                int[] outHeight = new int[1];
                WaterMarkWrap.newInstance().Nv21ClockWiseRotate90(data, size.width, size.height, yuv, outWidth, outHeight);
                mWaterMarkInfo.drawWaterMark(yuv, outWidth[0], outHeight[0]);
                Log.d(TAG, "waterMark--->outWidth: " + outWidth[0] + " ,outHeight: " + outHeight[0]);
                mDataCaches.put(SystemClock.elapsedRealtime(),new VideoGather.Frame(yuv,outWidth[0],outHeight[0]));

                srcBitmap = nv21ToBitmap.nv21ToBitmap(yuv,outWidth[0],outHeight[0]);
                if (srcBitmap != null) {
                    SurfaceHolder waterMarkSurfaceHolder = mWaterMarkView.getHolder();
                    // 获取到画布
                    Canvas canvas = waterMarkSurfaceHolder.lockCanvas();
                    // 将加了水印的图片绘制到预览窗口
                    canvas.drawBitmap(srcBitmap, 0, 0, null);
                    waterMarkSurfaceHolder.unlockCanvasAndPost(canvas);
                } else {
                    Log.e(TAG, "nv21 to bitmap failed!");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.e(TAG, "draw watermark exception", ex);
            }
        }
        Log.d(TAG,"X: run");
    }
}
