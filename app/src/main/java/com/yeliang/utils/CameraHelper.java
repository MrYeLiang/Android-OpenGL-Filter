package com.yeliang.utils;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.IOException;
import java.util.List;

/**
 * Author: yeliang
 * Date: 2019/12/2
 * Time: 7:24 PM
 * Description:
 */

public class CameraHelper implements Camera.PreviewCallback {

    private int mCameraId;
    private SurfaceTexture mSurfaceTexture;
    private Camera mCamera;

    public static int mWidth;
    public static int mHeight;
    private byte[] buffer;

    private boolean mCameraIsOpen;

    private Camera.PreviewCallback mPreviewCallBack;

    public CameraHelper(int cameraId, int previewWidth, int previewHeight, SurfaceTexture surfaceTexture) {
        mCameraId = cameraId;
        mWidth = previewWidth;
        mHeight = previewHeight;

        mSurfaceTexture = surfaceTexture;
    }

    public void startPreview() {
        if (mCameraIsOpen) {
            return;
        }
        mCameraIsOpen = true;

        //1 打开相机
        mCamera = Camera.open(mCameraId);

        //2 设置预览参数
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewFormat(ImageFormat.NV21);
        setPreviewSize(parameters);
        mCamera.setParameters(parameters);

        //3 设置数据回调
        buffer = new byte[mWidth * mHeight * 3 / 2];
        mCamera.addCallbackBuffer(buffer);
        mCamera.setPreviewCallbackWithBuffer(this);

        try {
            //4 设置预览纹理
            mCamera.setPreviewTexture(mSurfaceTexture);

            //5 开始预览
            mCamera.startPreview();

            //6 设置自动聚焦
            if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                loopAutoFocus();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private HandlerThread mThread;
    private Handler mHandler;

    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            beginFocus();
        }
    };

    private void loopAutoFocus() {
        mThread = new HandlerThread("thread_focus");
        mThread.start();

        mHandler = new Handler(mThread.getLooper());
        mHandler.postDelayed(mRunnable, 1000);
    }

    private void beginFocus() {
        if (!mCameraIsOpen) {
            return;
        }

        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                camera.cancelAutoFocus();
                mHandler.postDelayed(mRunnable, 1000);
            }
        });
    }

    private void setPreviewSize(Camera.Parameters parameters) {
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        Camera.Size size = supportedPreviewSizes.get(0);

        int m = Math.abs(size.height * size.width - mWidth * mHeight);
        supportedPreviewSizes.remove(0);

        for (Camera.Size next : supportedPreviewSizes) {
            int n = Math.abs(next.height * next.width - mWidth * mHeight);
            if (n < m) {
                m = n;
                size = next;
            }
        }

        mWidth = size.width;
        mHeight = size.height;

        Log.i("CameraHelper", "setPreviewSize mWidth = " + mWidth + "mHeight = " + mHeight);
        parameters.setPreviewSize(mWidth, mHeight);
    }

    public void switchCamera() {
        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        } else {
            mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
        stopPreview();
        startPreview();
    }

    public void stopPreview() {
        if (mCamera != null && mCameraIsOpen) {
            mCameraIsOpen = false;
            mCamera.stopPreview();
            mCamera.release();

            if (mThread != null) {
                mThread.quitSafely();
            }
        }
    }

    public int getCameraId() {
        return mCameraId;
    }

    public void setPreviewCallBack(Camera.PreviewCallback previewCallBack) {
        mPreviewCallBack = previewCallBack;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (null != mPreviewCallBack) {
            mPreviewCallBack.onPreviewFrame(data, camera);
        }

        camera.addCallbackBuffer(buffer);
    }
}
