package com.android.watermark;

import java.util.concurrent.ThreadFactory;

/**
 * Created by zhongjihao100@163.com on 19-9-25.
 */
public class NameThreadFactory implements ThreadFactory {
    private final String name;
    private int count;


    public NameThreadFactory(String name) {
        super();
        this.name = name;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName("T:"+name+"-"+count++);
        return thread;
    }
}
