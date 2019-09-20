package com.android.watermark;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;

import com.sprd.freetype.FreeTypeJni;

import java.io.File;


public class CopyAssetsTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "CopyAssetsTask";
    private AssetManager mAssetManager;
    private Context mContext;

    private static final String ASSERT_DIR_FONTS = "fonts";
    private static final String FONT_FZKTJW = "FZKTJW.ttf";

    public interface IWaterMarkReadyNotify{
        public void OnReady();
    }

    private IWaterMarkReadyNotify iWaterMarkReadyNotify;

    public void execute(IWaterMarkReadyNotify l){
        iWaterMarkReadyNotify = l;
        execute();
    }

    public CopyAssetsTask(Context context) {
        mContext = context;
        mAssetManager = context.getAssets();
    }

    private void copyFonts() {
        try {
            String[] faceModelsAssets = mAssetManager.list(ASSERT_DIR_FONTS);
            String assetPath;
            String targetPath;
            File targetDir = new File(mContext.getFilesDir(), ASSERT_DIR_FONTS);

            if (!targetDir.exists()) {
                targetDir.mkdir();
            }

            Log.d(TAG, "copyFonts...");
            for (String src : faceModelsAssets) {
                Log.d(TAG, "copyFonts: " + src);

                assetPath = ASSERT_DIR_FONTS + File.separator + src;
                targetPath = targetDir.getAbsolutePath() + File.separator + src;

                FileUtil.copyAssetToFileDir(mAssetManager, assetPath, targetPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "copyDBWSModels exception : " + e.toString());
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        copyFonts();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        File targetDir = new File(mContext.getFilesDir(), ASSERT_DIR_FONTS);
        String fontPath = targetDir.getAbsolutePath() + File.separator + FONT_FZKTJW;
        Log.d(TAG, "onPostExecute------setFont--->fontPath: "+fontPath);
        boolean ret = FreeTypeJni.setFont(fontPath);
        Log.d(TAG, "onPostExecute------setFont--->ret: " +ret+"   fontPath: "+fontPath);

        if(iWaterMarkReadyNotify != null){
            iWaterMarkReadyNotify.OnReady();
        }

        if (!ret) {
            File fd = new File(fontPath);
            long size = fd.exists() ? fd.length() : 0;
            Log.e(TAG, "setFont failed" + fontPath + ", size = " + size);

            if (fd.exists()){
                fd.delete();
            }
        }
    }
}
