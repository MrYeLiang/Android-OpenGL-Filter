package com.yeliang.widget;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.yeliang.filter.ScreenFilter;
import com.yeliang.utils.CameraHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Author: yeliang
 * Date: 2019/12/2
 * Time: 7:35 PM
 * Description:
 */

public class CommonRender implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    private GLSurfaceView mSurfaceView;
    private CameraHelper mCameraHelper;
    private int[] mTextures;
    private SurfaceTexture mSurfaceTexture;
    private ScreenFilter mScreenFilter;
    private float[] mtx = new float[16];

    public CommonRender(GLSurfaceView surfaceView) {
        mSurfaceView = surfaceView;
    }

    public void openCamera(int width, int height) {
        if (mCameraHelper == null) {
            mCameraHelper = new CameraHelper(Camera.CameraInfo.CAMERA_FACING_BACK, width, height);
        }

        mCameraHelper.startPreview(mSurfaceTexture);
    }

    public void closeCamera() {
        if (mCameraHelper != null) {
            mCameraHelper.stopPreview();
        }
    }


    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {


        mTextures = new int[1];
        GLES20.glGenTextures(mTextures.length, mTextures, 0);
        mSurfaceTexture = new SurfaceTexture(mTextures[0]);
        mSurfaceTexture.setOnFrameAvailableListener(this);

        mScreenFilter = new ScreenFilter(mSurfaceView.getContext());
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mScreenFilter.onReady(width, height);
    }


    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        mSurfaceTexture.updateTexImage();
        mSurfaceTexture.getTransformMatrix(mtx);
        mScreenFilter.onDrawFrame(mTextures[0], mtx);
    }

    /*==========================================================*/
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mSurfaceView.requestRender();
    }
}
