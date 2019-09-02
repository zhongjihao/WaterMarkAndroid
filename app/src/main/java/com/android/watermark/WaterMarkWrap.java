package com.android.watermark;


/**
 * Created by zhongjihao on 18-4-25.
 */
public class WaterMarkWrap {
    private long cPtr;
    private static WaterMarkWrap mInstance;
    private static Object lockobj = new Object();

    private WaterMarkWrap() {
        cPtr = 0;
    }

    public static WaterMarkWrap newInstance() {
        synchronized (lockobj) {
            if (mInstance == null) {
                mInstance = new WaterMarkWrap();
            }
        }
        return mInstance;
    }

    //启动yuv引擎
    public void startWaterMarkEngine() {
        cPtr = WaterMarkJni.startWaterMarkEngine();
    }

    /**
     *YV12 -> I420
     */
    public void Yv12ToI420(byte[] pYv12, byte[] pI420, int width, int height) {
        if (cPtr != 0) {
            WaterMarkJni.Yv12ToI420(cPtr, pYv12, pI420, width, height);
        }
    }

    /**
     *I420 -> YV12
     */
    public void I420ToYv12(byte[] pI420, byte[] pYv12, int width, int height) {
        if (cPtr != 0) {
            WaterMarkJni.I420ToYv12(cPtr,pI420, pYv12,width,height);
        }
    }

    /**
     * NV21 -> I420
     */
    public void Nv21ToI420(byte[] pNv21,byte[] pI420,int width,int height) {
        if (cPtr != 0) {
            WaterMarkJni.Nv21ToI420(cPtr, pNv21,pI420, width,height);
        }
    }

    /**
     * I420 -> NV21
     */
    public void I420ToNv21(byte[] pI420,byte[] pNv21,int width,int height) {
        if (cPtr != 0) {
            WaterMarkJni.I420ToNv21(cPtr, pI420, pNv21, width, height);
        }
    }

    /**
     * NV21 -> NV12
     */
    public void Nv21ToNv12(byte[] pNv21,byte[] pNv12,int width,int height){
        if (cPtr != 0) {
            WaterMarkJni.Nv21ToNv12(cPtr,pNv21,pNv12, width, height);
        }
    }

    /**
     *NV12 -> NV21
     */
    public void Nv12ToNv21(byte[] pNv12,byte[] pNv21,int width,int height){
        if (cPtr != 0) {
            WaterMarkJni.Nv12ToNv21(cPtr,pNv12,pNv21, width, height);
        }
    }

    /**
     * yuvType yuv类型
     * startX,startY 开始裁剪的坐标位置
     * srcYuv 原始YUV数据
     * srcW,srcH 原始YUV数据的分辨率
     * tarYuv 存储裁剪的数据
     * cutW,cutH 裁剪的分辨率
     **/
    public void cutCommonYuv(long cPtr,int yuvType, int startX,int startY,byte[] srcYuv, int srcW,int srcH,byte[] tarYuv,int cutW, int cutH){
        if (cPtr != 0) {
            WaterMarkJni.cutCommonYuv(cPtr,yuvType,startX,startY,srcYuv,srcW,srcH,tarYuv,cutW,cutH);
        }
    }

    /**
     * yuvType yuv类型
     * dstBuf 目标BUF
     * srcYuv 源yuv
     * srcW  宽
     * srcH 高
     * dirty_Y/dirty_UV 冗余数据
     **/
    public void getSpecYuvBuffer(long cPtr,int yuvType,byte[] dstBuf, byte[] srcYuv, int srcW, int srcH,int dirty_Y,int dirty_UV){
        if (cPtr != 0) {
            WaterMarkJni.getSpecYuvBuffer(cPtr,yuvType,dstBuf,srcYuv,srcW,srcH,dirty_Y,dirty_UV);
        }
    }

    /**
     * yuvType yuv类型
     * startX,startY 需要添加水印的位置
     * waterMarkData 水印YUV数据，可以通过读取水印文件获取
     * waterMarkW,waterMarkH 水印数据的分辨率
     * yuvData 源YUV图像数据
     * yuvW,yuvH 源YUV的分辨率
     **/
    public void yuvAddWaterMark(int yuvType, int startX, int startY, byte[] waterMarkData,
                         int waterMarkW, int waterMarkH, byte[] yuvData, int yuvW, int yuvH){
        if (cPtr != 0) {
            WaterMarkJni.yuvAddWaterMark(cPtr, yuvType,  startX,  startY, waterMarkData, waterMarkW,  waterMarkH,yuvData,  yuvW,  yuvH);
        }
    }

    //停止yuv引擎
    public void stopWaterMarkEngine() {
        if (cPtr != 0) {
            WaterMarkJni.stopWaterMarkEngine(cPtr);
        }
        mInstance = null;
    }
}