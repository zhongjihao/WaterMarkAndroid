//#define DEBUG
#define LOG_TAG "watermark"

#include <jni.h>
#include <string>
#include <opencv2/core/core.hpp>
#include <CvxText.h>
#include "common.h"

using namespace cv;

CvxText *gCvxText = NULL;


static void throwJavaException(JNIEnv *env, const std::exception *e, const char *method) {
    std::string what = "unknown exception";
    jclass je = 0;

    if (e) {
        std::string exception_type = "std::exception";

        if (dynamic_cast<const cv::Exception *>(e)) {
            exception_type = "cv::Exception";
            je = env->FindClass("org/opencv/core/CvException");
        }

        what = exception_type + ": " + e->what();
    }

    if (!je) je = env->FindClass("java/lang/Exception");
    env->ThrowNew(je, what.c_str());

    LOGE("%s caught %s", method, what.c_str());
    (void) method;        // avoid "unused" warning
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_lt_ms_demo_usbv4l2singlescaled_WaterMark_setFont(JNIEnv *env, jclass type,
                                                          jstring fontPath_) {
    const char *fontPath = env->GetStringUTFChars(fontPath_, 0);
    FT_Error ret = FT_Err_Ok;

    float p = 1.0;

    LOGD("setFont:%s", fontPath);
    if (gCvxText == NULL) {
        gCvxText = new CvxText();
        ret = gCvxText->init(fontPath);
        if (ret != FT_Err_Ok) {
            gCvxText->release();
            gCvxText = NULL;
            LOGE("init font failed");
        } else {
            gCvxText->setFont(NULL, NULL, NULL, &p);
        }
    }

    env->ReleaseStringUTFChars(fontPath_, fontPath);

    return ret == FT_Err_Ok;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_lt_ms_demo_usbv4l2singlescaled_WaterMark_putWText__JLjava_lang_String_2DDIDDDDDI(
        JNIEnv *env, jclass type, jlong img_nativeObj, jstring text, jdouble org_x, jdouble org_y,
        jint fontFace, jdouble fontScale, jdouble color_val0, jdouble color_val1,
        jdouble color_val2, jdouble color_val3, jint thickness) {
    static const char method_name[] = "core::putWText()";
    const char *utf_text = env->GetStringUTFChars((jstring) text, 0);

    LOGD("%s", method_name);
    if (gCvxText != NULL) {
        try {
            Mat &img = *((Mat *) img_nativeObj);
            std::string n_text(utf_text ? utf_text : "");
            int ret;

            Point org((int) org_x, (int) org_y);
            Scalar color(color_val0, color_val1, color_val2, color_val3);

            if (gCvxText != NULL) {
                ret = gCvxText->putText(img, n_text, org, (int) fontFace, (double) fontScale,
                                        color);
                LOGD("putText: ret = %d, len = %d", ret, strlen(utf_text));
//            } else {
//                cv::putText(img, n_text, org, (int) fontFace, (double) fontScale, color,
//                            (int) thickness);
            }
        } catch (const std::exception &e) {
            throwJavaException(env, &e, method_name);
        } catch (...) {
            throwJavaException(env, 0, method_name);
        }
    }

    env->ReleaseStringUTFChars((jstring) text, utf_text);
}

