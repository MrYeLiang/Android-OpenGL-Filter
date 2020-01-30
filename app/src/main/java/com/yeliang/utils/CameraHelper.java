package com.yeliang.utils;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
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
    public final static int WIDTH = 480;
    public final static int HEIGHT = 640;

    private final int mCameraId;
    private SurfaceTexture mSurfaceTexture;
    private Camera mCamera;

    public static int mWidth;
    public static int mHeight;
    private byte[] buffer;

    private boolean mCameraIsOpen;

    private Camera.PreviewCallback mPreviewCallBack;

    public CameraHelper(int cameraId, int previewWidth, int previewHeight) {
        mCameraId = cameraId;
        mWidth = previewWidth;
        mHeight = previewHeight;
    }

    public void startPreview(SurfaceTexture surfaceTexture) {
        if (mCameraIsOpen) {
            return;
        }
        mCameraIsOpen = true;
        mSurfaceTexture = surfaceTexture;

        mCamera = Camera.open(mCameraId);

        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewFormat(ImageFormat.NV21);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        setPreviewSize(parameters);
        mCamera.setParameters(parameters);

        try {
            mCamera.setPreviewTexture(mSurfaceTexture);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void setPreviewSize(Camera.Parameters parameters) {
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        Camera.Size size = supportedPreviewSizes.get(0);

        int m = Math.abs(size.height * size.width - mWidth * mHeight);
        supportedPreviewSizes.remove(0);

        for (Camera.Size next : supportedPreviewSizes) {
            Log.i("CameraHelper", "support mWidth = " + next.width + "mHeight = " + next.height);

            int n = Math.abs(next.height * next.width - mWidth * mHeight);
            if (n < m) {
                m = n;
                size = next;
            }
        }

        mWidth = size.width;
        mHeight = size.height;

        buffer = new byte[mWidth * mHeight * 3 / 2];
        mCamera.addCallbackBuffer(buffer);
        mCamera.setPreviewCallbackWithBuffer(this);

        Log.i("CameraHelper", "setPreviewSize mWidth = " + mWidth + "mHeight = " + mHeight);
        parameters.setPreviewSize(mWidth, mHeight);
    }

    public void stopPreview() {
        if (mCamera != null) {
            mCameraIsOpen = false;
            mCamera.stopPreview();
            mCamera.release();
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
        if(null != mPreviewCallBack){
            mPreviewCallBack.onPreviewFrame(data, camera);
        }

        camera.addCallbackBuffer(buffer);
    }
}
