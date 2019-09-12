package com.lt.ms.demo.usbv4l2singlescaled;

import android.content.Context;
import android.text.TextUtils;

import com.ad.adas.AdasAppState;
import com.ad.adas.BuildConfig;
import com.ad.adas.R;
import com.ad.adas.model.DetectEvent;
import com.ad.adas.model.SpeedInfo;
import com.ad.adas.sms.SmsSpUtil;
import com.ad.adas.util.JT808EventUtil;
import com.ad.adas.util.TimeUtils;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;

import adas.g7.com.common.log.Glog;
import adas.g7.com.common.utils.BitUtils;

public class WaterMark {
    private static final String TAG = "WaterMark";

    private static final int FONT_FACE = Core.FONT_HERSHEY_PLAIN;
    private static final double FONT_SCALE = 1.0d;
    private static final int TEXT_THICKNESS = 1;
    private static final Scalar BACKGROUND_COLOR = new Scalar(0.0D, 0.0D, 255.0D, 5.0D);// BGR
    public static final RGB TEXT_COLOR_DEFAULT = new RGB(244.0f, 158.0f, 66.0f);
    private static final int WATER_MARK_HEIGHT_MIN = 35;
    private static final int WATER_MARK_CONTENT_LEN_MAX = 35;

    static {
        System.loadLibrary("watermark");
    }

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
            mRed = (rgb >> 16) & 0xFF;
            mGreen = (rgb >> 8) & 0xFF;
            mBlue = rgb & 0xFF;
        }

        public int toRgb() {
            return ((int) mRed << 16) | ((int) mGreen << 8) | (int) mBlue;
        }

        @Override
        public String toString() {
            return "r = " + mRed + ", g = " + mGreen + ", b = " + mBlue;
        }
    }

    public static class WaterMarkInfo {
        private final Context mContext;

        private boolean mReady = false;

        public boolean mBrake = false;
        public int mTurnFlag = AdasAppState.TURN_FLAG_NONE;
        public int mSpeed = 0;
        public int mSpeedMode = SpeedInfo.SPEED_MODEL_PLUS;
        public WaterMarkData mWatermarkCarInfo;

        public String mTime = TimeUtils.getCurrentTime(TimeUtils.TIME_FORMAT_WATERMARK_DISPLAY);
        public WaterMarkData mWatermarkTime;

        public String mAddress = null;
        public WaterMarkData mWatermarkLocation = null;

        public String mPlateNumber = null;
        private boolean mPlateNumberSupport = true;

        private String mAppVersion = BuildConfig.VERSION_NAME;
        private int mEventId = DetectEvent.TYPE_INVALID;

        public WaterMarkInfo(Context context) {
            mContext = context;
        }

        public void onReady() {
            final RGB textColor = SmsSpUtil.getWaterMarkTextColor(mContext);

            Glog.d(TAG, "textColor = " + textColor.toString());

            mWatermarkCarInfo = new WaterMarkData(textColor);
            mWatermarkCarInfo.updateContent(formatCarInfo());

            mWatermarkTime = new WaterMarkData(textColor);
            mWatermarkTime.updateContent(formatTimeInfo());

            mWatermarkLocation = new WaterMarkData(textColor);
            mWatermarkLocation.updateContent(formatLocationInfo());

            mPlateNumberSupport = SmsSpUtil.isWaterMarkPlateNumberSupport(mContext);

            mReady = true;
        }

        public void onBrakeChanged(final boolean brake) {
            if (!mReady || brake == mBrake)
                return;
            mBrake = brake;

            if (mWatermarkCarInfo != null)
                mWatermarkCarInfo.updateContent(formatCarInfo());
        }

        public void onSpeedChanged(final int speed, final int speedMode) {
            if (!mReady || (speed == mSpeed && speedMode == mSpeedMode))
                return;
            mSpeed = speed;
            mSpeedMode = speedMode;

            if (mWatermarkCarInfo != null)
                mWatermarkCarInfo.updateContent(formatCarInfo());
        }

        public void onTurnChanged(final int flag) {
            if (!mReady || mTurnFlag == flag)
                return;
            mTurnFlag = flag;

            if (mWatermarkCarInfo != null)
                mWatermarkCarInfo.updateContent(formatCarInfo());
        }

        public void onPlateNumberChanged(final String plateNumber) {
            if (!mReady || !mPlateNumberSupport) {
                mPlateNumber = null;
                return;
            }

            if (TextUtils.equals(plateNumber, mPlateNumber))
                return;
            mPlateNumber = plateNumber;
            if (mWatermarkCarInfo != null)
                mWatermarkCarInfo.updateContent(formatCarInfo());
        }

        private String formatCarInfo() {
            String turnInfo;
            String brakeInfo;

            if (BitUtils.isSet(mTurnFlag, AdasAppState.TURN_FLAG_LEFT) && BitUtils.isSet(mTurnFlag, AdasAppState.TURN_FLAG_RIGHT))
                turnInfo = mContext.getString(R.string.turn_info_double);
            else if (BitUtils.isSet(mTurnFlag, AdasAppState.TURN_FLAG_LEFT))
                turnInfo = mContext.getString(R.string.turn_info_left);
            else if (BitUtils.isSet(mTurnFlag, AdasAppState.TURN_FLAG_RIGHT))
                turnInfo = mContext.getString(R.string.turn_info_right);
            else
                turnInfo = mContext.getString(R.string.turn_info_none);

            brakeInfo = mContext.getString(mBrake ? R.string.brake_on : R.string.brake_off);

            final String carInfo = String.format(mContext.getString(R.string.watermark_car_info),
                    TextUtils.isEmpty(mPlateNumber) ? "" : mPlateNumber + " ",
                    mSpeed, SpeedInfo.getSpeedMode(mSpeedMode), turnInfo, brakeInfo);

//            Glog.d(TAG, "carInfo = " + carInfo + ", len = " + carInfo.length());
            return carInfo;
        }

        public void onTimeChanged(final String time) {
            if (!mReady || TextUtils.equals(time, mTime))
                return;
            mTime = time;

            if (mWatermarkTime != null)
                mWatermarkTime.updateContent(formatTimeInfo());
        }

        public void onVersionChanged(final String version) {
            if (!mReady)
                return;
            if (null == mAppVersion && null == version)
                return;
            if (null != mAppVersion && null != version && mAppVersion.equals(version))
                return;
            mAppVersion = version;

            if (mWatermarkTime != null)
                mWatermarkTime.updateContent(formatTimeInfo());
        }

        public void onEventChanged(final int eventId) {
            if (!mReady)
                return;
            if (eventId == mEventId)
                return;
            mEventId = eventId;

            if (mWatermarkTime != null)
                mWatermarkTime.updateContent(formatTimeInfo());
        }

        private String formatTimeInfo() {
            StringBuilder versionBuilder = new StringBuilder();

            if (!TextUtils.isEmpty(mAppVersion))
                versionBuilder.append(mAppVersion);

            if (mEventId != DetectEvent.TYPE_INVALID)
                versionBuilder.append("R" + mEventId);

            return String.format(mContext.getString(R.string.watermark_time), mTime, versionBuilder);
        }

        public void onLocationChanged(final String addr) {
            if (!mReady || TextUtils.equals(addr, mAddress))
                return;
            mAddress = addr;
            if (mWatermarkLocation != null)
                mWatermarkLocation.updateContent(formatLocationInfo());
        }

        private String formatLocationInfo() {
            if (!TextUtils.isEmpty(mAddress)) {
                return String.format(mContext.getString(R.string.watermark_location), mAddress);
            } else {
                return null;
            }
        }

        public ByteBuffer drawWaterMark(ByteBuffer frame, final int frameWidth, final int frameHeight) {
            int markX = 6, markY = 5;
            boolean ret;

            if (!mReady)
                return frame;

            if (mWatermarkLocation != null) {
                ret = yuvAddWaterMark(markX, markY, mWatermarkLocation.mNv21Data, mWatermarkLocation.mWidth,
                        mWatermarkLocation.mHeight, frame, frameWidth, frameHeight);
                if (ret)
                    markY += WATER_MARK_HEIGHT_MIN;
            }

            if (mWatermarkTime != null) {
                ret = yuvAddWaterMark(markX, markY, mWatermarkTime.mNv21Data, mWatermarkTime.mWidth,
                        mWatermarkTime.mHeight, frame, frameWidth, frameHeight);
                if (ret)
                    markY += WATER_MARK_HEIGHT_MIN;
            }

            if (mWatermarkCarInfo != null) {
                ret = yuvAddWaterMark(markX, markY, mWatermarkCarInfo.mNv21Data, mWatermarkCarInfo.mWidth,
                        mWatermarkCarInfo.mHeight, frame, frameWidth, frameHeight);
                if (ret)
                    markY += WATER_MARK_HEIGHT_MIN;
            }

            return frame;
        }

        public String getAppVersion() {
            return mAppVersion;
        }
    }

    public static class WaterMarkData {
        public int mWidth;
        public int mHeight;
        private String mContent;
        public byte[] mNv21Data = null;
        private final Scalar mTextColorScalar;

        public WaterMarkData(final RGB textColor) {
            mTextColorScalar = new Scalar(textColor.mRed, textColor.mGreen, textColor.mBlue);
        }

        public void updateContent(final String content) {
            if (content == null) {
                mNv21Data = null;
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
            createWatermark();
        }

        private void createWatermark() {
            Point origin = new Point();
            int[] baseline = new int[1];

            Size size = Core.getTextSize(mContent, FONT_FACE, FONT_SCALE, TEXT_THICKNESS, baseline);
            size.height = size.height * 2;
            size.width = size.width * 3 / 2;
            Glog.v(TAG, "mContent = " + mContent + ", size = " + size.toString());
            mWidth = (int) size.width;
            mHeight = Math.max(WATER_MARK_HEIGHT_MIN, (int) size.height);
            mWidth = (mWidth % 2 == 0) ? mWidth : mWidth + 1;
            mHeight = (mHeight % 2 == 0) ? mHeight : mHeight + 1;

            origin.x = 0;
            origin.y = size.height + (mHeight - size.height) / 2;

            Mat rgbMat = new Mat(new Size(mWidth, mHeight), CvType.CV_8UC4);
//            rgbMat.setTo(BACKGROUND_COLOR);

            if (!TextUtils.isEmpty(mContent)) {
                putWText(rgbMat, mContent, origin, FONT_FACE, FONT_SCALE, mTextColorScalar, TEXT_THICKNESS);
            }

            int yv12Width = mWidth;
            int yv12Height = mHeight * 3 / 2;

            Mat waterMarkMat = new Mat(yv12Height, yv12Width, CvType.CV_8UC1);
            Imgproc.cvtColor(rgbMat, waterMarkMat, Imgproc.COLOR_RGB2YUV_YV12);
            mNv21Data = new byte[yv12Width * yv12Height];

            waterMarkMat.get(0, 0, mNv21Data);
            Yv12ToNv21(mNv21Data, mWidth, mHeight);
        }
    }

    public static void Yv12ToNv21(byte[] input, int width, int height) {
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
    }

    private static boolean yuvAddWaterMark(int startX, int startY, byte[] waterMark,
                                           int waterMarkWidth, int waterMarkHeight, ByteBuffer yuv,
                                           int width, int height) {
        int j = 0;
        int k = 0;
        int length = waterMarkHeight + startY;

        int pos;
        int index;
        int x;
        int i;

        if (waterMark == null || waterMark.length == 0)
            return false;

        for (i = startY; i < length; ++i) {
            pos = startX + i * width;
            index = j * waterMarkWidth;
            yuv.position(pos);
            byte black = 16;

            for (x = index; x < waterMarkWidth + index; ++x) {
                if (waterMark[x] != black) {
                    yuv.put(waterMark[x]);
                } else {
                    yuv.position(yuv.position() + 1);
                }
            }

            ++j;
        }

        length = (waterMarkHeight + startY) / 2;

        for (i = startY / 2; i < length; ++i) {
            pos = startX / 2 * 2 + width * height + i * width;
            index = waterMarkWidth * waterMarkHeight + k * waterMarkWidth;
            yuv.position(pos);
            byte black = -128;

            for (x = index; x < waterMarkWidth + index; ++x) {
                if (waterMark[x] != black) {
                    yuv.put(waterMark[x]);
                } else {
                    yuv.position(yuv.position() + 1);
                }
            }

            ++k;
        }

        yuv.position(0);

        return true;
    }


    private static void yuvAddWaterMark(int startX, int startY, byte[] waterMark,
                                        int waterMarkWidth, int waterMarkHeight, byte[] yuv,
                                        int width, int height) {
        int j = 0;
        int k = 0;

        int i;
        for (i = startY; i < waterMarkHeight + startY; ++i) {
            System.arraycopy(waterMark, j * waterMarkWidth, yuv, startX + i * width, waterMarkWidth);
            ++j;
        }

        for (i = startY / 2; i < (waterMarkHeight + startY) / 2; ++i) {
            System.arraycopy(waterMark, waterMarkWidth * waterMarkHeight + k * waterMarkWidth, yuv, startX + width * height + i * width, waterMarkWidth);
            ++k;
        }
    }

    public static native boolean setFont(final String fontPath);

    public static void putWText(Mat img, String text, Point org, int fontFace, double fontScale, Scalar color, int thickness) {
        putWText(img.nativeObj, text, org.x, org.y, fontFace, fontScale, color.val[0], color.val[1], color.val[2], color.val[3], thickness);
    }

    private static native void putWText(long img_nativeObj, String text, double org_x, double org_y,
                                        int fontFace, double fontScale, double color_val0,
                                        double color_val1, double color_val2, double color_val3,
                                        int thickness);
}
