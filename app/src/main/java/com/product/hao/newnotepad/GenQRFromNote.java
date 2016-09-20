package com.product.hao.newnotepad;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

/**
 * Created by hao on 9/2/16.
 */
public class GenQRFromNote extends AsyncTask<String,Void,String>{
    Context activity;
    GeneratingQRListener genQRHelper = null;
    ProgressDialog progressDialog = null;
    Bitmap bitmap = null;
    public GenQRFromNote(Context context){
        activity = context;
        progressDialog = ProgressDialog.show(context,"Wait","Generating QR...");
    }

    @Override
    protected String doInBackground(String... params) {
        bitmap = genQRHelper.getQRForNote();
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(!progressDialog.isShowing()){
            progressDialog.show();
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(progressDialog.isShowing()){
            progressDialog.dismiss();
        }
        genQRHelper.displayQRForNote(bitmap);
    }
}
