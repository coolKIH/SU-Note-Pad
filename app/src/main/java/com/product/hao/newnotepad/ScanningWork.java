package com.product.hao.newnotepad;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by hao on 8/24/16.
 */
public class ScanningWork extends AsyncTask<String,Void,String>{
    ScanningCompleteListener scanningCompleteListener = null;
    Bitmap bitmap;
    String result;
    ProgressDialog progressDialog;
    Context context;
    public ScanningWork(Context ctxt,Bitmap bm){
        this.context = ctxt;
        this.bitmap = bm;
        result = "Something wrong...><..";
        progressDialog = ProgressDialog.show(context,"Wait","Scanning...");
    }
    @Override
    protected String doInBackground(String... strings) {
        makeTessDataReady();
        try {
            Log.w("DBP start scanning"," Now result is "+result);
            TessBaseAPI tessBaseAPI = new TessBaseAPI();
            tessBaseAPI.init(MainActivity_hbg.getSdPath(), MainActivity_hbg.getLang());
            tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
            tessBaseAPI.setImage(bitmap);
            result = tessBaseAPI.getUTF8Text();
            Log.w("DBP scanning complete"," Now result is "+result);
            tessBaseAPI.clear();
            tessBaseAPI.end();
        }catch (Exception e){
            Log.w("DBP doInBg",e.getMessage());
        }

        for(int i=0;i<5;i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        scanningCompleteListener.setTextResult(s);
    }

    void makeTessDataReady(){
        String tessDataDirPath = MainActivity_hbg.getSdPath()+ File.separator+"tessdata/";
        if(!(new File(tessDataDirPath+MainActivity_hbg.getLang()+".traineddata")).exists()){
            try {
                AssetManager assetManager = context.getAssets();
                InputStream in = assetManager.open("tessdata/" + MainActivity_hbg.getLang() + ".traineddata");
                OutputStream out = new FileOutputStream(tessDataDirPath+MainActivity_hbg.getLang()+".traineddata");
                byte[]buf = new byte[1024];
                int len;
                while((len=in.read(buf))>0){
                    out.write(buf,0,len);
                }
                in.close();
                out.close();
            }catch (IOException e){
                Log.w("DBP get lang ready",e.getMessage());
            }
        }
    }
}

