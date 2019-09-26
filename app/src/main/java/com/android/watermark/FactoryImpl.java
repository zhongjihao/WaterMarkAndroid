package com.android.watermark;

import android.content.Context;

/**
 * Created by zhongjihao on 19-9-25.
 */
public class FactoryImpl extends Factory{
    private Context mApplicationContext;
    private WaterMarkApplication mApplication;

    private FactoryImpl() {

    }

    public static Factory register(final Context applicationContext,final WaterMarkApplication application) {
        // This only gets called once (from BugleApplication.onCreate), but its not called in tests.
        Assert.isTrue(!sRegistered);
        Assert.isNull(Factory.get());

        final FactoryImpl factory = new FactoryImpl();
        Factory.setInstance(factory);
        sRegistered = true;

        // At this point Factory is published. Services can now get initialized and depend on
        // Factory.get().

        factory.mApplicationContext = applicationContext;
        factory.mApplication = application;

        factory.onRequiredInit();

        return factory;
    }

    @Override
    public void onRequiredInit(){
        if (sInitialized) {
            return;
        }
        sInitialized = true;

        mApplication.initializeSync(this);

        final Thread asyncInitialization = new Thread() {
            @Override
            public void run() {
                // Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                mApplication.initializeAsync(FactoryImpl.this);
            }
        };
        asyncInitialization.start();
    }

    @Override
    public  Context getApplicationContext(){
        return mApplicationContext;
    }
}
