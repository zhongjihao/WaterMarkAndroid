package com.android.watermark;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;

/**
 * Created by zhongjihao100@163.com on 19-9-25.
 * weixin: 18626455927
 */
public class YuvToBitmap {
    private RenderScript rs;
    private ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;
    private Type.Builder yuvType, rgbaType;
    private Allocation in, out;
    private Paint paint;
    private Bitmap bmpout = null;

    public YuvToBitmap(Context context) {
        rs = RenderScript.create(context);
        yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
    }

    public Paint createPaint() {
        if (paint == null) {
            paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(50);
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setColor(Color.BLUE);
        }
        return paint;
    }

    public Bitmap nv21ToBitmap(byte[] nv21, int width, int height){
        if (yuvType == null){
            yuvType = new Type.Builder(rs, Element.U8(rs)).setX(nv21.length);
            in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);

            rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height);
            out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);
        }

        in.copyFrom(nv21);

        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);

        if(bmpout == null){
            bmpout = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }
        out.copyTo(bmpout);

        return bmpout;
    }

    public void free(){
        if(bmpout != null){
            bmpout.recycle();
            bmpout = null;
        }
    }

    /**
     * 给图片添加水印
     *
     * @param originBitmap 原始图片
     * @param degree 旋转角度
     * @param watermark 水印文字
     * @param paint 绘制水印的画笔对象
     * @return 最终处理的结果
     */
    public static Bitmap createWaterMarkBitmap(Bitmap originBitmap, int degree, String watermark, Paint paint) {
        int width = originBitmap.getWidth();
        int height = originBitmap.getHeight();
        Bitmap resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBitmap);
        canvas.save();
        canvas.rotate(degree, width / 2, height / 2);
        canvas.drawBitmap(originBitmap, 0, 0, null);
        canvas.restore();
        int textWidht = (int) paint.measureText(watermark);
        FontMetrics fontMetrics = paint.getFontMetrics();
        int textHeight = (int) (fontMetrics.ascent - fontMetrics.descent);
        int x = (width - textWidht) / 2;
        int y = (height - textHeight) / 2;
        y = (int) (y - fontMetrics.descent);
        canvas.drawText(watermark, x, y, paint);
        canvas.drawText(String.valueOf(System.currentTimeMillis()), x, y + textHeight, paint);
        // 立即回收无用内存
        originBitmap.recycle();
        return resultBitmap;
    }
}
