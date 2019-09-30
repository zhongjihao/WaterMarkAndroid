package com.android.watermark;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.SurfaceView;

/**
 * Created by zhongjihao on 19-9-25.
 */
public class WaterMarkSurfaceView extends SurfaceView {
    public WaterMarkSurfaceView(Context context) {
        super(context);
    }

    public WaterMarkSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WaterMarkSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //surfaceHolder设置背景透明时清除canvas内容
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
    }
}
