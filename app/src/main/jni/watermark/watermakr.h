//
// Created by zhongjihao on 19-8-26.
//

#ifndef WATERMARK_H
#define WATERMARK_H

#include <stdio.h>
#include <stdint.h>
#include <string.h>

#define FORMAT_NV21 1
#define FORMAT_NV12 2
#define FORMAT_YV12 3
#define FORMAT_I420 4


/**
 * Nv21(分辨率8x4):
 * YYYYYYYY
 * YYYYYYYY
 * YYYYYYYY
 * YYYYYYYY
 * VUVUVUVU
 * VUVUVUVU
 *
 * Nv12(分辨率8x4):
 * YYYYYYYY
 * YYYYYYYY
 * YYYYYYYY
 * YYYYYYYY
 * UVUVUVUV
 * UVUVUVUV
 *
 * 分辨率8x4
 * YUV420P(I420):
 * YYYYYYYY
 * YYYYYYYY
 * YYYYYYYY
 * YYYYYYYY
 * UUUU
 * UUUU
 * VVVV
 * VVVV
 *
 * 分辨率8x4
 * YV12:
 * YYYYYYYY
 * YYYYYYYY
 * YYYYYYYY
 * YYYYYYYY
 * VVVV
 * VVVV
 * UUUU
 * UUUU
 **/

class WaterMark
{
public:
    WaterMark();
    ~WaterMark();

    //YV12 -> I420
    void Yv12ToI420(unsigned char* pYv12, unsigned char* pI420, int width, int height);
    //I420 -> YV12
    void I420ToYv12(unsigned char* pI420, unsigned char* pYv12, int width, int height);
    //NV21 -> I420
    void Nv21ToI420(unsigned char* pNv21,unsigned char* pI420,int width,int height);
    //I420 -> NV21
    void I420ToNv21(unsigned char* pI420,unsigned char* pNv21,int width,int height);
    //NV21 -> NV12
    void Nv21ToNv12(unsigned char* pNv21,unsigned char* pNv12,int width,int height);
    //NV12 -> NV21
    void Nv12ToNv21(unsigned char* pNv12,unsigned char* pNv21,int width,int height);

    /**
     * yuvType yuv类型
     * startX,startY 开始裁剪的坐标位置
     * srcYuv 原始YUV数据
     * srcW,srcH 原始YUV数据的分辨率
     * tarYuv 存储裁剪的数据
     * cutW,cutH 裁剪的分辨率
     **/
    void cutCommonYuv(int yuvType, int startX,int startY,unsigned char *srcYuv, int srcW,int srcH,unsigned char *tarYuv,int cutW, int cutH);

    /**
     * yuvType yuv类型
     * dstBuf 目标BUF
     * srcYuv 源yuv
     * srcW  宽
     * srcH 高
     * dirty_Y/dirty_UV 冗余数据
     **/
    void getSpecYuvBuffer(int yuvType,unsigned char *dstBuf, unsigned char *srcYuv, int srcW, int srcH,int dirty_Y,int dirty_UV);

    /**
     * yuvType yuv类型
     * startX,startY 需要添加水印的位置
     * waterMarkData 水印YUV数据，可以通过读取水印文件获取
     * waterMarkW,waterMarkH 水印数据的分辨率
     * yuvData 源YUV图像数据
     * yuvW,yuvH 源YUV的分辨率
     **/
    void yuvAddWaterMark(int yuvType, int startX, int startY, unsigned char *waterMarkData,
                         int waterMarkW, int waterMarkH, unsigned char *yuvData, int yuvW, int yuvH);
};


#endif //WATERMARK_H