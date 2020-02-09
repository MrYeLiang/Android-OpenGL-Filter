package com.yeliang.widget;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

/**
 * Author: yeliang
 * Date: 2019/12/2
 * Time: 7:52 PM
 * Description:
 */

public class CommonSurfaceView extends GLSurfaceView {

    private CommonRender mCommonRender;

    public CommonSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        mCommonRender = new CommonRender(this);
        setRenderer(mCommonRender);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        mCommonRender.onSurfaceDestroyed();
    }

    public void openCamera() {
        mCommonRender.openCamera(getHeight(), getWidth());
    }

    public void closeCamera() {
        mCommonRender.closeCamera();
    }

    public void openBeauty(){
        mCommonRender.openBeauty();
    }

    public void closeBeauty(){
        mCommonRender.closeBeauty();
    }

    public void startRecord(){
        mCommonRender.startRecord(1.0f);
    }

    public void stopRecord(){
        mCommonRender.stopRecord();
    }
}
