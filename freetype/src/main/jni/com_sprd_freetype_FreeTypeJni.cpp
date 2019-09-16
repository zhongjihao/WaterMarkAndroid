/**
 * Created by zhongjihao@sprdtrum.com on 15/09/19.
 */

#define LOG_TAG "FREETYPE_JNI"

#include "com_sprd_freetype_FreeTypeJni.h"
#include "include/opencv2/core/core.hpp"
#include "include/CvxText.h"
#include "log.h"
#include <string>

using namespace cv;

static CvxText *gCvxText = NULL;

static void throwJavaException(JNIEnv *env, const std::exception *e, const char *method)
{
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

JNIEXPORT jboolean JNICALL Java_com_sprd_freetype_FreeTypeJni_setFont
  (JNIEnv *env, jclass jcls, jstring jfontPath)
{
    const char *fontPath = env->GetStringUTFChars(jfontPath, NULL);
    FT_Error ret = FT_Err_Ok;

    float p = 1.0;

    LOGD("%s: fontPath:%s", __FUNCTION__,fontPath);
    if (gCvxText == NULL) {
        gCvxText = new CvxText();
        ret = gCvxText->init(fontPath);
        if (ret != FT_Err_Ok) {
            delete gCvxText;
            gCvxText = NULL;
            LOGE("%s: init font failed",__FUNCTION__);
        } else {
            gCvxText->setFont(NULL, NULL, NULL, &p);
        }
    }

    env->ReleaseStringUTFChars(jfontPath, fontPath);

    return ret == FT_Err_Ok;

}

JNIEXPORT void JNICALL Java_com_sprd_freetype_FreeTypeJni_putWText
  (JNIEnv *env, jclass jcls, jlong jimg_nativeObj, jstring jtext, jdouble jorg_x, jdouble jorg_y, jint jfontFace, jdouble jfontScale, jdouble jcolor_val0, jdouble jcolor_val1, jdouble jcolor_val2, jdouble jcolor_val3, jint jthickness)
{
    static const char method_name[] = "core::putWText()";
    const char *utf_text = env->GetStringUTFChars(jtext, NULL);

    if (gCvxText != NULL) {
        try {
            Mat &img = *((Mat *) jimg_nativeObj);
            const char * n_text = (utf_text ? utf_text : "");

            Point org((int) jorg_x, (int) jorg_y);
            Scalar color(jcolor_val0, jcolor_val1, jcolor_val2, jcolor_val3);
            int ret = gCvxText->putText(img, n_text, org, (int) jfontFace, (double) jfontScale, color);

        } catch (const std::exception &e) {
            throwJavaException(env, &e, method_name);
        } catch (...) {
            throwJavaException(env, 0, method_name);
        }
    }

    env->ReleaseStringUTFChars(jtext, utf_text);

}
