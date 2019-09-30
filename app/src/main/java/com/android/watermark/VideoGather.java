package com.android.watermark;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.hardware.Camera.Parameters.PREVIEW_FPS_MAX_INDEX;
import static android.hardware.Camera.Parameters.PREVIEW_FPS_MIN_INDEX;

/**
 * Created by zhongjihao100@163.com on 18-2-7.
 * weixin: 18626455927
 */


public class VideoGather implements LocationManager.ILocationListen{
    private static final String TAG = "VideoGather";
    private int preWidth;
    private int preHeight;
    private int frameRate;
    private static VideoGather mCameraWrapper;

    // 定义系统所用的照相机
    private Camera mCamera;
    //预览尺寸
    private Camera.Size previewSize;
    private Camera.Parameters mCameraParamters;
    private boolean mIsPreviewing = false;
    private CameraPreviewCallback mCameraPreviewCallback;

    private CameraOperateCallback cameraCb;
    private Context mContext;
    private AtomicBoolean mIsCaptrue = new AtomicBoolean(false);


    private Cache<Long, Frame> mDataCaches = CacheBuilder.newBuilder().concurrencyLevel(2)
            .maximumSize(1000)
            .expireAfterWrite(10000, TimeUnit.MILLISECONDS)
            .removalListener(new RemovalListener<Long, Frame>() {
                @Override
                public void onRemoval(RemovalNotification<Long, Frame> item) {
                }
            }).build();

    private WaterMark.WaterMarkInfo mWaterMarkInfo = null;
//    private HandlerThread mHandlerThread;
//    private Handler mHandler;
    private String mAddress = null;
    private final static int MSG_WATER_MAKR = 1;
    private SurfaceView mWaterMarkView;
    private YuvToBitmap nv21ToBitmap;
    private ExecutorService mSyncEs = Executors.newSingleThreadExecutor(new NameThreadFactory("waterMarkTask"));



    private VideoGather() {
        Log.d(TAG,"VideoGather()");
    }

    public interface CameraOperateCallback {
        public void cameraHasOpened();
    }

    public static VideoGather getInstance() {
        if (mCameraWrapper == null) {
            synchronized (VideoGather.class) {
                if (mCameraWrapper == null) {
                    mCameraWrapper = new VideoGather();
                }
            }
        }
        return mCameraWrapper;
    }

    public void setWaterMarkView(SurfaceView surfaceView) {
        mWaterMarkView = surfaceView;
    }

    public void doOpenCamera(CameraOperateCallback callback) {
        Log.d(TAG, "====zhongjihao====Camera open....");
        cameraCb = callback;
        if(mCamera != null)
            return;
        if (mCamera == null) {
            Log.d(TAG, "No front-facing camera found; opening default");
            mCamera = Camera.open();    // opens first back-facing camera
        }
        if (mCamera == null) {
            throw new RuntimeException("Unable to open camera");
        }
        LocationManager.getInstance().registerLocationListen(this);
        mWaterMarkInfo = new WaterMark.WaterMarkInfo();
        nv21ToBitmap = new YuvToBitmap(Factory.get().getApplicationContext());
        //开启一个线程
//        mHandlerThread = new HandlerThread( "WaterMarkthread");
//        mHandlerThread.start();
//        mHandler = new Handler(mHandlerThread.getLooper(),mCallback);
        Log.d(TAG, "====zhongjihao=====Camera open over....");
        cameraCb.cameraHasOpened();
    }

    public void doStartPreview(Activity activity, SurfaceHolder surfaceHolder) {
        if (mIsPreviewing) {
            return;
        }
        mContext = activity;
        setCameraDisplayOrientation(activity, Camera.CameraInfo.CAMERA_FACING_BACK);
        setCameraParamter(surfaceHolder);
        try {
            // 通过SurfaceView显示取景画面
            mCamera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "=====zhongjihao===Camera Preview Started...");
        mCamera.startPreview();
        mIsPreviewing = true;
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                Log.d(TAG, "=====zhongjihao===onAutoFocus----->success: "+success);
            }
        });

        new CopyAssetsTask(mContext).execute(new CopyAssetsTask.IWaterMarkReadyNotify(){
            @Override
            public void OnReady(){
                if (mWaterMarkInfo != null) {
                    Log.d(TAG,"WaterMark Ready");
                    mWaterMarkInfo.onReady();
                }
            }
        });
    }

    public void doStopCamera() {
        Log.d(TAG, "=====zhongjihao=======doStopCamera");
        LocationManager.getInstance().unRegisterLocationListen();
        // 如果camera不为null，释放摄像头
        if (mCamera != null) {
            mCamera.setPreviewCallbackWithBuffer(null);
            mCameraPreviewCallback = null;
            if (mIsPreviewing)
                mCamera.stopPreview();
            mIsPreviewing = false;
            mCamera.release();
            mCamera = null;
        }
        mContext = null;
//        if(mHandlerThread != null){
//            mHandlerThread.quitSafely();
//            mHandlerThread = null;
//        }
        mSyncEs.shutdownNow();
        nv21ToBitmap.free();
    }

    private void setCameraParamter(SurfaceHolder surfaceHolder) {
        if (!mIsPreviewing && mCamera != null) {
            mCameraParamters = mCamera.getParameters();
            List<Integer> previewFormats = mCameraParamters.getSupportedPreviewFormats();
            for(int i=0;i<previewFormats.size();i++){
                Log.d(TAG,"support preview format : "+previewFormats.get(i));
            }

            mCameraParamters.setPreviewFormat(ImageFormat.NV21);
            // Set preview size.
            List<Camera.Size> supportedPreviewSizes = mCameraParamters.getSupportedPreviewSizes();
            Collections.sort(supportedPreviewSizes, new Comparator<Camera.Size>() {
                @Override
                public int compare(Camera.Size o1, Camera.Size o2) {
                    Integer left = o1.width;
                    Integer right = o2.width;
                    return left.compareTo(right);
                }
            });

            DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
            Log.d(TAG, "====zhongjihao=====Screen width=" + dm.widthPixels + ", height=" + dm.heightPixels);
            for (Camera.Size size : supportedPreviewSizes) {
                if (size.width >= dm.heightPixels && size.height >= dm.widthPixels) {
                    if ((1.0f * size.width / size.height) == (1.0f * dm.heightPixels / dm.widthPixels)) {
                        previewSize = size;
                        Log.d(TAG, "====zhongjihao=====select preview size width=" + size.width + ",height=" + size.height);
                        break;
                    }
                }
            }
            preWidth = previewSize.width;
            preHeight = previewSize.height;
            mCameraParamters.setPreviewSize(previewSize.width, previewSize.height);

            //set fps range.
            int defminFps = 0;
            int defmaxFps = 0;
            List<int[]> supportedPreviewFpsRange = mCameraParamters.getSupportedPreviewFpsRange();
            for (int[] fps : supportedPreviewFpsRange) {
                Log.d(TAG, "=====zhongjihao=====setParameters====find fps:" + Arrays.toString(fps));
                if (defminFps <= fps[PREVIEW_FPS_MIN_INDEX] && defmaxFps <= fps[PREVIEW_FPS_MAX_INDEX]) {
                    defminFps = fps[PREVIEW_FPS_MIN_INDEX];
                    defmaxFps = fps[PREVIEW_FPS_MAX_INDEX];
                }
            }
            //设置相机预览帧率
            Log.d(TAG, "=====zhongjihao=====setParameters====defminFps:" + defminFps+"    defmaxFps: "+defmaxFps);
            mCameraParamters.setPreviewFpsRange(defminFps,defmaxFps);
            frameRate = defmaxFps / 1000;
            surfaceHolder.setFixedSize(previewSize.width, previewSize.height);

            // 加了水印的预览窗口也需要改变大小
            if (mWaterMarkView != null) {
                mWaterMarkView.getHolder().setFixedSize(previewSize.height,previewSize.width);
            }

            mCameraPreviewCallback = new CameraPreviewCallback();
            mCamera.addCallbackBuffer(new byte[previewSize.width * previewSize.height*3/2]);
            mCamera.setPreviewCallbackWithBuffer(mCameraPreviewCallback);
            List<String> focusModes = mCameraParamters.getSupportedFocusModes();
            for (String focusMode : focusModes){//检查支持的对焦
                Log.d(TAG, "=====zhongjihao=====setParameters====focusMode:" + focusMode);
                if (focusMode.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)){
                    mCameraParamters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                }else if (focusMode.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)){
                    mCameraParamters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                }else if(focusMode.contains(Camera.Parameters.FOCUS_MODE_AUTO)){
                    mCameraParamters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                }
            }
            Log.d(TAG, "=====zhongjihao=====setParameters====preWidth:" + preWidth+"   preHeight: "+preHeight+"  frameRate: "+frameRate);
            mCamera.setParameters(mCameraParamters);
        }
    }

    private void setCameraDisplayOrientation(Activity activity,int cameraId) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result = 0;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        Log.d(TAG, "=====zhongjihao=====setCameraDisplayOrientation=====result:" + result+"  rotation: "+rotation+"  degrees: "+degrees+"  orientation: "+info.orientation);
        mCamera.setDisplayOrientation(result);
    }

    public void takePicture(PictureCallback mJpegCallback){
        mIsCaptrue.set(true);
        ConcurrentMap<Long, Frame> map = mDataCaches.asMap();
        if (!map.isEmpty()) {
            Set<Long> keySet = map.keySet();
            long lastYuvTime = 0;
            for (Long key : keySet) {
                if (lastYuvTime < key)
                    lastYuvTime = key;
            }

            Frame frame = map.get(lastYuvTime);
            if(frame != null){
                String filename = FileUtil.getPicFileName(System.currentTimeMillis());
                Log.d(TAG, "takePicture------->filename: " + filename+" ,width: "+frame.width+" ,height: "+frame.height);
                takePictureInternal(mJpegCallback,filename, frame.nv21,frame.width,frame.height);
            }
        }else {
            Log.e(TAG, "takePicture------data Cache is empty!");
        }
        mIsCaptrue.set(false);
    }

    private void takePictureInternal(PictureCallback mJpegCallback,String fileName,byte[] data,int width,int height){
        Log.d(TAG, "takePictureInternal------->fileName: " + fileName+" ,width: "+width+" ,height: "+height);
        String filePath = Environment
                .getExternalStorageDirectory()
                + "/"+"watermark_android";

        File photoDir = new File(filePath);
        boolean isSdcardOk = true;
        if (!photoDir.exists()) {
            isSdcardOk = photoDir.mkdirs();
        }

        if (isSdcardOk) {
            File pictureFile = new File(filePath, fileName);
            if (!pictureFile.exists()) {
                FileOutputStream filecon = null;
                try {
                    pictureFile.createNewFile();
                    filecon = new FileOutputStream(pictureFile);
                    YuvImage image = new YuvImage(data, ImageFormat.NV21, width, height, null);
                    image.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 80, filecon);
                    filecon.flush();
                    Log.d(TAG, "takePictureInternal---->filePath: " + pictureFile.getAbsolutePath());
                    if (mJpegCallback != null) {
                        mJpegCallback.onPictureTaken(pictureFile.getAbsolutePath());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (filecon != null) {
                        try {
                            filecon.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public static class Frame{
        byte[] nv21;
        int width;
        int height;

        public Frame(byte[] nv21, int width, int height) {
            this.nv21 = nv21;
            this.width = width;
            this.height = height;
        }

        public byte[] getNv21() {
            return nv21;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

    class CameraPreviewCallback implements Camera.PreviewCallback {
        private CameraPreviewCallback() {

        }

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Size size = camera.getParameters().getPreviewSize();
            Log.d(TAG,"onPreviewFrame----->width: "+size.width+" ,height: "+size.height);

            //通过回调,拿到的data数据是原始数据
            if(data != null){
                if(!mIsCaptrue.get()){
                    try {
                        mSyncEs.execute(new WaterMarkTask(size,data,mWaterMarkInfo,mAddress,mDataCaches,nv21ToBitmap,mWaterMarkView));
                    }catch (Exception e){
                        Log.e(TAG, "draw watermark exception", e);
                        e.printStackTrace();
                    }
                }
                camera.addCallbackBuffer(data);
            }
            else {
                camera.addCallbackBuffer(new byte[size.width * size.height *3/2]);
            }
        }
    }

    @Override
    public void updateAddress(String addr){
        mAddress = addr;
    }

//    private Handler.Callback mCallback = new Handler.Callback() {
//        public boolean handleMessage(Message msg) {
//            switch (msg.what) {
//                case MSG_WATER_MAKR:{
//                    if (mHandler.hasMessages(MSG_WATER_MAKR)){
//                        mHandler.removeMessages(MSG_WATER_MAKR);
//                    }
//                    break;
//                }
//            }
//            return true;
//        }
//    };

}
