package com.yeliang.widget;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.yeliang.filter.CameraFilter;
import com.yeliang.filter.ScreenFilter;
import com.yeliang.record.MediaRecorder;
import com.yeliang.utils.CameraHelper;

import java.io.IOException;

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

    private CameraFilter mCameraFilter;
    private ScreenFilter mScreenFilter;

    private float[] mtx = new float[16];
    private MediaRecorder mMediaRecorder;

    CommonRender(GLSurfaceView surfaceView) {
        mSurfaceView = surfaceView;
    }

    void openCamera(int width, int height) {
        if (mCameraHelper == null) {
            mCameraHelper = new CameraHelper(Camera.CameraInfo.CAMERA_FACING_BACK, width, height);
        }

        mCameraHelper.startPreview(mSurfaceTexture);
    }

    void closeCamera() {
        if (mCameraHelper != null) {
            mCameraHelper.stopPreview();
        }
    }


    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

        mTextures = new int[1];
        //1 创建Texture
        GLES20.glGenTextures(mTextures.length, mTextures, 0);

        //2 创建SurfaceTexture
        mSurfaceTexture = new SurfaceTexture(mTextures[0]);

        mSurfaceTexture.setOnFrameAvailableListener(this);

        mCameraFilter = new CameraFilter(mSurfaceView.getContext());
        mScreenFilter = new ScreenFilter(mSurfaceView.getContext());

        //渲染线程EGL上下文
        EGLContext eglContext = EGL14.eglGetCurrentContext();
        mMediaRecorder = new MediaRecorder(mSurfaceView.getContext(), "/sdcard/record.mp4",CameraHelper.mHeight,CameraHelper.mWidth,eglContext);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mCameraFilter.onReady(width, height);
        mScreenFilter.onReady(width,height);
    }


    @Override
    public void onDrawFrame(GL10 gl10) {
        //1 清屏 表示把屏幕清理为什么颜色
        GLES20.glClearColor(0, 0, 0, 0);

        //2 执行 glClearColor传的屏幕颜色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        //3 更新纹理，这一步骤之后才能从SurfaceTexture中获得数据来渲染
        mSurfaceTexture.updateTexImage();

        //4 获取变换矩阵
        mSurfaceTexture.getTransformMatrix(mtx);

        mCameraFilter.setMatrix(mtx);

        int textureId = mCameraFilter.onDrawFrame(mTextures[0]);
        mScreenFilter.onDrawFrame(textureId);

        mMediaRecorder.encodeFrame(textureId, mSurfaceTexture.getTimestamp());
    }

    /*==========================================================*/
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mSurfaceView.requestRender();
    }

    public void startRecord(float speed){
        try {
            mMediaRecorder.start(speed);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecord(){
        mMediaRecorder.stop();
    }

}
