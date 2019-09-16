/**
 * Created by zhongjihao@sprdtrum.com on 15/09/19.
 * OpenCV汉字输出接口,实现了汉字输出功能
 */


#ifndef WATERMARKANDROID_CVXTEXT_H
#define WATERMARKANDROID_CVXTEXT_H

#include <iostream>
#include "ft2build.h"
#include FT_FREETYPE_H

#include "opencv2/opencv.hpp"
#include "opencv2/core/core_c.h"
#include "opencv2/core/core.hpp"
#include "opencv2/core/types_c.h"
#include "opencv/highgui.h"
#include "opencv/cv.h"
#include "freetype/freetype.h"

using namespace std;
using namespace cv;


class CvxText
{
private:
    //禁止拷贝或赋值
    CvxText(const CvxText&);
    CvxText& operator=(const CvxText&);

    //输出当前字符, 更新m_pos位置
    void putWChar(IplImage* img, wchar_t wc, CvPoint &pos, CvScalar color);
private:
    FT_Library m_library; //字库
    FT_Face m_face;       //字体

    //默认的字体输出参数
    int m_fontType;
    CvScalar m_fontSize;
    bool m_fontUnderline;
    float m_fontDiaphaneity;

public:
    CvxText();
    virtual ~CvxText();

    FT_Error init(const char* font_path);

    void release();


    /**
     * 获取字体
     * @param type        字体类型
     * @param size        字体大小
     * @param underline   下画线
     * @param diaphaneity 透明度
     */
    void getFont(int* type,CvScalar* size = NULL, bool* underline = NULL, float* diaphaneity = NULL);

    /**
     * 设置字体
     * @param type          字体类型
     * @param size          字体大小
     * @param underline     下画线
     * @param diaphaneity   透明度
     */
    void setFont(int* type, CvScalar* size = NULL, bool* underline = NULL, float* diaphaneity = NULL);

    /**
     * 恢复原始的字体设置
     */
    void restoreFont();

    /**
     * 输出汉字(颜色默认为黑色)。遇到不能输出的字符将停止
     * @param img    输出的yuv
     * @param text   文本内容
     * @param pos    文本位置
     * @return       返回成功输出的字符长度，失败返回-1
     */
    int putText(IplImage* img, const char* text, CvPoint pos);

    /**
     * 输出汉字(颜色默认为黑色)。遇到不能输出的字符将停止
     * @param img    输出的yuv
     * @param text   文本内容
     * @param pos    文本位置
     * @return       返回成功输出的字符长度，失败返回-1
     */
    int putText(IplImage* img, const wchar_t* text, CvPoint pos);

    /**
     * 输出汉字。遇到不能输出的字符将停止
     * @param img     输出的yuv
     * @param text    文本内容
     * @param pos     文本位置
     * @param color   文本颜色
     * @return        返回成功输出的字符长度，失败返回-1
     */
    int putText(IplImage* img, const char* text, CvPoint pos, CvScalar color);

    /**
    * 输出汉字。遇到不能输出的字符将停止
    * @param img     输出的yuv
    * @param text    文本内容
    * @param pos     文本位置
    * @param color   文本颜色
    * @return        返回成功输出的字符长度，失败返回-1
    */
    int putText(IplImage* img, const wchar_t* text, CvPoint pos, CvScalar color);

    int putText(cv::Mat &img, const char* text, cv::Point org,
                int fontFace, double fontScale, cv::Scalar color);


};



#endif //WATERMARKANDROID_CVXTEXT_H
