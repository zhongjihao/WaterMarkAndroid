package com.android.watermark;

import android.text.TextUtils;
import android.util.Log;

import com.sprd.freetype.FreeTypeJni;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Point;
import org.opencv.core.Size;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhongjihao on 19-9-14.
 */
public class WaterMark {
    private static final String TAG = "WaterMark";

    private static final int FONT_FACE = Core.FONT_HERSHEY_PLAIN;
    private static final double FONT_SCALE = 1.8d;
    private static final int TEXT_THICKNESS = 1;
    private static final Scalar BACKGROUND_COLOR = new Scalar(0.0D, 0.0D, 255.0D, 5.0D);// BGR
    private static final int WATER_MARK_HEIGHT_MIN = 40;
    private static final int WATER_MARK_CONTENT_LEN_MAX = 40;
    public static final int TURN_FLAG_NONE = 0x00;
    private static boolean isDebug = true;
    public static final String TIME_FORMAT_WATERMARK_DISPLAY = "yyyy/MM/dd HH:mm:ss";
    private static final int IMAGE_TYPE_I420 = 1;
    private static final int IMAGE_TYPE_NV21 = 2;
    private static final int IMAGE_TYPE = IMAGE_TYPE_I420;


    public static class RGB {
        public float mRed;
        public float mGreen;
        public float mBlue;

        public RGB(float r, float g, float b) {
            mRed = r;
            mGreen = g;
            mBlue = b;
        }

        public RGB(final int rgb) {
            mRed  = (rgb >> 16) & 0xFF;
            mGreen = (rgb >> 8) & 0xFF;
            mBlue = rgb & 0xFF;
        }

        public int toRgb() {
            return  ((int)mRed << 16) | ((int)mGreen << 8) | (int)mBlue;
        }

        @Override
        public String toString() {
            return "r = " + mRed + ", g = " + mGreen + ", b = " + mBlue;
        }
    }

    public static String getCurrentTime(final String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);

        return formatter.format(Calendar.getInstance().getTime());
    }

    public static class WaterMarkInfo {

        private boolean mReady = false;

        private boolean mBrake = false;
        private int mTurnFlag = TURN_FLAG_NONE;
        private int mSpeed = 0;
        private WaterMarkData mWatermarkCarInfo;

        private String mTime = null;
        private WaterMarkData mWatermarkTime;

        private String mAddress = null;
        private WaterMarkData mWatermarkLocation = null;

        private String mPlateNumber = "";
        private static final Scalar TEXT_COLOR = new Scalar(255.0D, 255.0D, 0.0D);// RGB

        public WaterMarkInfo() {
            mTime = getCurrentTime(TIME_FORMAT_WATERMARK_DISPLAY);
        }

        public void onReady() {
            if(mWatermarkTime == null){
                mWatermarkTime = new WaterMarkData(TEXT_COLOR);
            }
            mWatermarkTime.updateContent(mTime);

            if(mWatermarkLocation == null){
                mWatermarkLocation = new WaterMarkData(TEXT_COLOR);
            }
            mWatermarkLocation.updateContent(mAddress);

            mReady = true;
        }

        public void onTimeChanged(final String time) {
            if (!mReady || TextUtils.equals(time, mTime))
                return;
            mTime = time;

            if (mWatermarkTime != null)
                mWatermarkTime.updateContent(mTime);
        }

        public void onLocationChanged(final String addr) {
            if (!mReady || TextUtils.equals(addr, mAddress))
                return;
            mAddress = addr;
            if (mWatermarkLocation != null)
                mWatermarkLocation.updateContent(mAddress);
        }

        public ByteBuffer drawWaterMark(ByteBuffer frame, final int frameWidth, final int frameHeight) {
            int markX = 5, markY = 5;
            boolean ret;
            if (!mReady)
                return frame;

            if (mWatermarkLocation != null) {
                if(IMAGE_TYPE == IMAGE_TYPE_I420){
                    ret = i420AddWaterMark(markX, markY, mWatermarkLocation.mI420Data, mWatermarkLocation.mWidth,
                            mWatermarkLocation.mHeight, frame, frameWidth, frameHeight);
                }else{
                    ret = nv21AddWaterMark(markX, markY, mWatermarkLocation.mI420Data, mWatermarkLocation.mWidth,
                            mWatermarkLocation.mHeight, frame, frameWidth, frameHeight);
                }
                if (ret)
                    markY += WATER_MARK_HEIGHT_MIN;
            }

            if (mWatermarkTime != null) {
                if(IMAGE_TYPE == IMAGE_TYPE_I420){
                    ret = i420AddWaterMark(markX, markY, mWatermarkTime.mI420Data, mWatermarkTime.mWidth,
                            mWatermarkTime.mHeight, frame, frameWidth, frameHeight);
                }else{
                    ret = nv21AddWaterMark(markX, markY, mWatermarkTime.mI420Data, mWatermarkTime.mWidth,
                            mWatermarkTime.mHeight, frame, frameWidth, frameHeight);
                }
                if (ret)
                    markY += WATER_MARK_HEIGHT_MIN;
                if(isDebug){
                    // Log.d(TAG,"drawWaterMark--------------------------------------------------"+ret);
                }
            }

            if (mWatermarkCarInfo != null) {
                if(IMAGE_TYPE == IMAGE_TYPE_I420){
                    ret = i420AddWaterMark(markX, markY, mWatermarkCarInfo.mI420Data, mWatermarkCarInfo.mWidth,
                            mWatermarkCarInfo.mHeight, frame, frameWidth, frameHeight);
                }else{
                    ret = nv21AddWaterMark(markX, markY, mWatermarkCarInfo.mI420Data, mWatermarkCarInfo.mWidth,
                            mWatermarkCarInfo.mHeight, frame, frameWidth, frameHeight);
                }
                if (ret)
                    markY += WATER_MARK_HEIGHT_MIN;
            }

            return frame;
        }
    }

    public static class WaterMarkData {
        private int mWidth;
        private int mHeight;
        private String mContent;
        private byte[] mI420Data = null;
        private final Scalar mTextColorScalar;

        public WaterMarkData(final Scalar scalar) {
            mTextColorScalar = scalar;// BGR
        }

        public void updateContent(final String content) {
            if (content == null) {
                mI420Data = null;
                return;
            }
            final String formattedContent;

            if (content.length() > WATER_MARK_CONTENT_LEN_MAX)
                formattedContent = content.substring(0, WATER_MARK_CONTENT_LEN_MAX / 2 - 3)
                        + "..." + content.substring(content.length() - WATER_MARK_CONTENT_LEN_MAX / 2);
            else
                formattedContent = content;

            if (TextUtils.equals(formattedContent, mContent))
                return;

            mContent = formattedContent;
            if(isDebug){
                Log.d(TAG,"updateContent :"+mContent);
            }
            createWatermark();
        }

        private void createWatermark() {
            Point origin = new Point();
            int[] baseline = new int[1];
            double[] outSize = new double[2];

            FreeTypeJni.getTextSize(mContent, FONT_FACE, FONT_SCALE, TEXT_THICKNESS, baseline,outSize);
            Size size = new Size(outSize[0],outSize[1]);
            size.height = size.height * 2;
            size.width = size.width * 3 / 2;
            //Log.d(TAG, "mContent = " + mContent + ", size = " + size.toString());
            mWidth = (int) size.width;
            mHeight = Math.max(WATER_MARK_HEIGHT_MIN, (int) size.height);
            mWidth = (mWidth % 2 == 0) ? mWidth : mWidth + 1;
            mHeight = (mHeight % 2 == 0) ? mHeight : mHeight + 1;

            origin.x = 0;
            origin.y = size.height + (mHeight - size.height) / 2;

            Mat rgbMat = new Mat(new Size(mWidth, mHeight), CvType.CV_8UC4);
//            rgbMat.setTo(BACKGROUND_COLOR);

            if (!TextUtils.isEmpty(mContent)) {
                if(containChinese(mContent)){
                    FreeTypeJni.putWText(rgbMat, mContent, origin, FONT_FACE, FONT_SCALE, mTextColorScalar, TEXT_THICKNESS);
                }else {
                    FreeTypeJni.putText(rgbMat, mContent, origin, FONT_FACE, FONT_SCALE, mTextColorScalar,TEXT_THICKNESS);
                }
            }

            if(isDebug){
                Log.d(TAG,"createWatermark :"+mContent);
            }
            int yuvWidth = mWidth;
            int yuvHeight = mHeight * 3 / 2;

            Mat waterMarkMat = new Mat(yuvHeight, yuvWidth, CvType.CV_8UC1);

            if(IMAGE_TYPE == IMAGE_TYPE_I420){
                Imgproc.cvtColor(rgbMat, waterMarkMat, Imgproc.COLOR_RGB2YUV_I420);
                mI420Data = new byte[yuvWidth * yuvHeight];
                waterMarkMat.get(0, 0, mI420Data);
            }else if(IMAGE_TYPE == IMAGE_TYPE_NV21){
                Imgproc.cvtColor(rgbMat, waterMarkMat, Imgproc.COLOR_RGB2YUV_YV12);
                mI420Data = new byte[yuvWidth * yuvHeight];
                waterMarkMat.get(0, 0, mI420Data);
                Yv12ToNv21(mI420Data, mWidth, mHeight);
            }
        }
    }

    private static void Yv12ToNv21(byte[] input, int width, int height) {
        int frameSize = width * height;
        int qFrameSize = frameSize / 4;
        byte[] vu = new byte[frameSize / 2];
        System.arraycopy(input, frameSize, vu, 0, frameSize / 2);

        for (int i = 0; i < qFrameSize; ++i) {
            byte v = vu[i];
            byte u = vu[qFrameSize + i];
            input[frameSize + i * 2] = v;
            input[frameSize + i * 2 + 1] = u;
        }
        if(isDebug){
            Log.d(TAG,"Yv12ToNv21 ----------------------------------------------------------");
        }
    }

    private static boolean nv21AddWaterMark(int startX, int startY, byte[] nv21WaterMark,
                                            int waterMarkWidth, int waterMarkHeight, ByteBuffer nv21,
                                            int width, int height) {
        int j = 0;
        int k = 0;
        int length = waterMarkHeight + startY;

        int pos;
        int index;
        int x;
        int i;

        if (nv21WaterMark == null || nv21WaterMark.length == 0)
            return false;

        for (i = startY; i < length; ++i) {
            pos = startX + i * width;
            index = j * waterMarkWidth;
            nv21.position(pos);
            byte black = 16;

            for (x = index; x < waterMarkWidth + index; ++x) {
                if (nv21WaterMark[x] != black) {
                    nv21.put(nv21WaterMark[x]);
                } else {
                    nv21.position(nv21.position() + 1);
                }
            }

            ++j;
        }

        length = (waterMarkHeight + startY) / 2;

        for (i = startY / 2; i < length; ++i) {
            pos = startX + width * height + i * width;
            index = waterMarkWidth * waterMarkHeight + k * waterMarkWidth;
            nv21.position(pos);
            byte black = -128;

            for (x = index; x < waterMarkWidth + index; ++x) {
                if (nv21WaterMark[x] != black) {
                    nv21.put(nv21WaterMark[x]);
                } else {
                    nv21.position(nv21.position() + 1);
                }
            }

            ++k;
        }

        nv21.position(0);

        return true;
    }


    private static boolean i420AddWaterMark(int startX, int startY, byte[] i420WaterMark,
                                            int waterMarkWidth, int waterMarkHeight, ByteBuffer i420,
                                            int width, int height) {
        int j = 0;
        int k = 0;
        int l = 0;

        int pos;
        int index;
        int x;
        int i;

        if (i420WaterMark == null || i420WaterMark.length == 0) {
            return false;
        }

        if(width-waterMarkWidth<startX){
            startX = 0;
        }

        if(height-waterMarkHeight<startY){
            startY = 0;
        }

        //Y
        int length = waterMarkHeight + startY;
        for (i = startY; i < length; ++i) {
            pos = startX + i * width;
            index = j * waterMarkWidth;
            i420.position(pos);
            byte black = 16;

            for (x = index; x < waterMarkWidth + index; ++x) {
                if (i420WaterMark[x] != black) {
                    i420.put(i420WaterMark[x]);
                } else {
                    i420.position(i420.position() + 1);
                }
            }
            ++j;
        }


        int uvStartX = startX/2;
        int uvStartY = startY/2;

        //U
        int offsetU = width * height;
        int offsetWU = waterMarkWidth * waterMarkHeight;
        length = (int) Math.ceil(waterMarkHeight / 2.0);
        for (i = uvStartY; i < length + uvStartY; ++i) {
            pos = uvStartX + offsetU + i * width/2;
            index = offsetWU + k * waterMarkWidth/2;
            i420.position(pos);
            byte black = -128;

            int ulw = (int) Math.floor(waterMarkWidth / 2.0);
            int t = 0;
            for (x = index; t < ulw; ++x,t++) {
                if (i420WaterMark[x] != black) {
                    i420.put(i420WaterMark[x]);
                } else {
                    i420.position(i420.position() + 1);
                }
            }
            ++k;
        }

        //V
        int offsetV = width * height * 5 / 4;
        int offsetWV = waterMarkWidth * waterMarkHeight * 5 / 4;
        length = (int) Math.ceil(waterMarkHeight / 2.0);
        for (i = uvStartY; i < length + uvStartY; ++i) {
            pos = uvStartX + offsetV + i * width/2;
            index = offsetWV  + l * waterMarkWidth/2;
            i420.position(pos);
            byte black = -128;

            int vlw = (int) Math.floor(waterMarkWidth / 2.0);
            int t = 0;
            for (x = index; t < vlw; ++x,t++) {
                if (i420WaterMark[x] != black) {
                    i420.put(i420WaterMark[x]);
                } else {
                    i420.position(i420.position() + 1);
                }
            }
            ++l;
        }


        i420.position(0);

        return true;
    }

    private static boolean containChinese(String inputString){
        //四段范围，包含全面"[\u4e00-\u9fa5]"
        String regex ="[\\u4E00-\\u9FA5\\u2E80-\\uA4CF\\uF900-\\uFAFF\\uFE30-\\uFE4F]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(inputString);
        return matcher.find();
    }
}
