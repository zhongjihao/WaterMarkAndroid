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
    private static final int TEXT_THICKNESS = 2;
    private static final Scalar BACKGROUND_COLOR = new Scalar(0.0D, 0.0D, 255.0D, 5.0D);// BGR
    private static final int WATER_MARK_HEIGHT_MIN = 50;
    private static final int WATER_MARK_CONTENT_LEN_MAX = 50;

    private static boolean isDebug = true;

    private static final int IMAGE_TYPE_I420 = 1;
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

    public static class WaterMarkInfo {

        private boolean mReady = false;

        private String mTime = null;
        private WaterMarkData mWatermarkTime;

        private String mAddress = null;
        private WaterMarkData mWatermarkLocation = null;


        private static final Scalar TEXT_COLOR = new Scalar(255.0D, 255.0D, 0.0D);// RGB

        public WaterMarkInfo() {
            mTime = TimeUtil.getCurrentTime(TimeUtil.TIME_FORMAT_WATERMARK_DISPLAY);
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

        public void drawWaterMark(byte[] frame, final int frameWidth, final int frameHeight) {
            int markX = 20, markY = 20;
            boolean ret;
            if (!mReady)
                return;

            if (mWatermarkLocation != null) {
                if(IMAGE_TYPE == IMAGE_TYPE_I420){
//                    ret = i420AddWaterMark(markX, markY, mWatermarkLocation.mI420Data, mWatermarkLocation.mWidth,
//                            mWatermarkLocation.mHeight, frame, frameWidth, frameHeight);
                  //  WaterMarkWrap.newInstance().yuvAddWaterMark(1,markX, markY,mWatermarkLocation.mNv21Data, mWatermarkLocation.mWidth, mWatermarkLocation.mHeight, frame, frameWidth, frameHeight);

                }
               // if (ret)
                    markY += WATER_MARK_HEIGHT_MIN;
            }

            if (mWatermarkTime != null) {
                if(IMAGE_TYPE == IMAGE_TYPE_I420){
//                    ret = i420AddWaterMark(markX, markY, mWatermarkTime.mI420Data, mWatermarkTime.mWidth,
//                            mWatermarkTime.mHeight, frame, frameWidth, frameHeight);
                    byte[] cutBuf = new byte[mWatermarkTime.mWidth * mWatermarkTime.mHeight *3/2];
                    WaterMarkWrap.newInstance().cutCommonYuv(1,markX, markY,frame, frameWidth, frameHeight,cutBuf,mWatermarkTime.mWidth , mWatermarkTime.mHeight);
                    WaterMarkWrap.newInstance().getSpecYuvBuffer(1,cutBuf,mWatermarkTime.mNv21Data, mWatermarkTime.mWidth, mWatermarkTime.mHeight,0x10,0x80);
                    WaterMarkWrap.newInstance().yuvAddWaterMark(1,markX, markY, cutBuf, mWatermarkTime.mWidth, mWatermarkTime.mHeight, frame, frameWidth, frameHeight);
                    Log.d(TAG,"markX: "+markX+"  markY: "+markY+"   waterTime: "+mWatermarkTime.mNv21Data);
                }
              //  if (ret)
                    markY += WATER_MARK_HEIGHT_MIN;
                if(isDebug){
                    // Log.d(TAG,"drawWaterMark--------------------------------------------------"+ret);
                }
            }

            return;
        }
    }

    public static class WaterMarkData {
        private int mWidth;
        private int mHeight;
        private String mContent;
        private byte[] mI420Data = null;
        private byte[] mNv21Data = null;
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
                mNv21Data = new byte[yuvWidth * yuvHeight];
                WaterMarkWrap.newInstance().I420ToNv21(mI420Data,mNv21Data,mWidth,mHeight);
            }
        }
    }

    private static boolean containChinese(String inputString){
        //四段范围，包含全面"[\u4e00-\u9fa5]"
        String regex ="[\\u4E00-\\u9FA5\\u2E80-\\uA4CF\\uF900-\\uFAFF\\uFE30-\\uFE4F]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(inputString);
        return matcher.find();
    }
}
