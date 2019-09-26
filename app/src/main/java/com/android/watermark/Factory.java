package com.android.watermark;

import android.content.Context;

/**
 * Created by zhongjihao on 19-9-25.
 */
public abstract class Factory {
    private static volatile Factory sInstance;
    protected static boolean sRegistered = false;
    protected static boolean sInitialized = false;

    public static Factory get() {
        return sInstance;
    }

    protected static void setInstance(final Factory factory) {
        // Not allowed to call this after real application initialization is complete
        Assert.isTrue(!sRegistered);
        Assert.isTrue(!sInitialized);

        sInstance = factory;
    }

    public abstract Context getApplicationContext();
    public abstract void onRequiredInit();
}
