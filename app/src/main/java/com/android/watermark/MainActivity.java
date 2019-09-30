package com.android.watermark;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, VideoGather.CameraOperateCallback,SurfacePreview.PermissionNotify{
    private final static String TAG = "MainActivity";
    private Button btnStart;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private SurfacePreview mSurfacePreview;
    private WaterMarkSurfaceView mWaterMarkView;
    private boolean hasPermission;
    private static final int TARGET_PERMISSION_REQUEST = 100;

    // 要申请的权限
    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.CAMERA,Manifest.permission.ACCESS_FINE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        hasPermission = false;
        btnStart = (Button) findViewById(R.id.btn_takePic);
        mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);
        mSurfaceView.setKeepScreenOn(true);
        mWaterMarkView = (WaterMarkSurfaceView) findViewById(R.id.watermark_surface_view);
        // 获得SurfaceView的SurfaceHolder
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        // 设置surface不需要自己的维护缓存区
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // 为srfaceHolder添加一个回调监听器
        mSurfacePreview = new SurfacePreview(this,this);
        mSurfaceHolder.addCallback(mSurfacePreview);
        btnStart.setOnClickListener(this);

        // 版本判断。当手机系统大于 23 时，才有必要去判断权限是否获取
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查该权限是否已经获取
            for (int i = 0; i < permissions.length; i++) {
                int result = ContextCompat.checkSelfPermission(this, permissions[i]);
                // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
                if (result != PackageManager.PERMISSION_GRANTED) {
                    hasPermission = false;
                    break;
                } else
                    hasPermission = true;
            }
            if(!hasPermission){
                // 如果没有授予权限，就去提示用户请求
                ActivityCompat.requestPermissions(this,
                        permissions, TARGET_PERMISSION_REQUEST);
            }
        }
        LocationManager.getInstance().onStart();
        WaterMarkWrap.newInstance().startWaterMarkEngine();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocationManager.getInstance().onStop();
        VideoGather.getInstance().doStopCamera();
        WaterMarkWrap.newInstance().stopWaterMarkEngine();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            finish();
            System.exit(0);//正常退出
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_takePic:
                VideoGather.getInstance().takePicture(new PictureCallback(){
                    @Override
                    public void onPictureTaken(String path){
                        Log.d(TAG,"take picture done------>path: "+path);
                    }
                });
                break;
        }
    }

    @Override
    public void cameraHasOpened() {
        VideoGather.getInstance().setWaterMarkView(mWaterMarkView);
        VideoGather.getInstance().doStartPreview(this, mSurfaceHolder);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                && (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            if(requestCode == TARGET_PERMISSION_REQUEST){
                btnStart.setEnabled(true);
                hasPermission = true;
                // 打开摄像头
                VideoGather.getInstance().doOpenCamera(this);
            }
        }else{
            btnStart.setEnabled(false);
            hasPermission = false;
            Toast.makeText(this, getText(R.string.no_permission_tips), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public boolean hasPermission(){
        return  hasPermission;
    }

}
