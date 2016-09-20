package com.product.hao.newnotepad;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;

/**
 * Created by hao on 8/25/16.
 */
public class PromptCropDialog extends AlertDialog.Builder {
    Context context;
    CropPicListener cropPicListener=null;
    PromptCropDialog(Context context,final Intent data){
        super(context);
        this.context=context;
        setTitle("Want to crop the photo?");
        setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                cropPicListener.getBitmapAndDone(data);
            }
        }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                cropPicListener.getBitmapAndCropAndDone(data);
            }
        }).create();
    }
}
