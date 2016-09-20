package com.product.hao.newnotepad;

import android.content.Intent;

/**
 * Created by hao on 8/25/16.
 */
public interface CropPicListener {
    public void getBitmapAndDone(Intent data);
    public void getBitmapAndCropAndDone(Intent data);
}
