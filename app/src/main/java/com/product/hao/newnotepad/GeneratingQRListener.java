package com.product.hao.newnotepad;

import android.graphics.Bitmap;

/**
 * Created by hao on 9/2/16.
 */
public interface GeneratingQRListener {
    public void displayQRForNote(Bitmap bitmap);
    public Bitmap getQRForNote();
}
