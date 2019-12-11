package com.yeliang.record;

import android.content.Context;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.view.Surface;

import com.yeliang.filter.ScreenFilter;


/**
 * Author: yeliang
 * Date: 2019/12/10
 * Time: 7:54 PM
 * Description:
 */

public class EGLBase {

    private final EGLSurface mEGlSurface;
    private final ScreenFilter mScreenFilter;
    private EGLDisplay mEglDisplay;

    private EGLConfig mEglConfig;
    private EGLContext mEglContext;

    public EGLBase(Context context, int width, int height, Surface surface, EGLContext eglContext) {

        //1 创建并配置egl
        createEGL(eglContext);

        //2 创建EGLSurface
        int[] attribList = {EGL14.EGL_NONE};
        mEGlSurface = EGL14.eglCreateWindowSurface(mEglDisplay, mEglConfig, surface, attribList, 0);

        //3 绑定当前线程的显示设备上下文
        boolean makeCurrentSuc = EGL14.eglMakeCurrent(mEglDisplay, mEGlSurface, mEGlSurface, mEglContext);
        if (!makeCurrentSuc) {
            throw new RuntimeException("eglMakeCurrent failed!");
        }

        //4 在虚拟屏幕画
        mScreenFilter = new ScreenFilter(context);
        mScreenFilter.onReady(width, height);

    }

    private void createEGL(EGLContext eglContext) {
        //1 创建显示设备
        mEglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (mEglDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay failed");
        }

        //2 初始化
        int[] version = new int[2];
        if (!EGL14.eglInitialize(mEglDisplay, version, 0, version, 1)) {
            throw new RuntimeException("eglInitialize failed");
        }


        //3 配置EGL
        int[] attrib_list = {
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_NONE
        };

        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfig = new int[1];

        boolean getConfigSuc = EGL14.eglChooseConfig(mEglDisplay, attrib_list, 0, configs, 0, configs.length, numConfig, 0);
        if (!getConfigSuc) {
            throw new IllegalArgumentException("eglChooseConfig failed");
        }

        //4 创建EGlContext
        mEglConfig = configs[0];
        int[] ctxAttribList = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION,
                2,
                EGL14.EGL_NONE
        };
        mEglContext = EGL14.eglCreateContext(mEglDisplay, mEglConfig, eglContext, ctxAttribList, 0);

        if (mEglContext == EGL14.EGL_NO_CONTEXT) {
            throw new RuntimeException("EGL Context Error");
        }

    }

    public void draw(int textureId, long timeStamp) {
        boolean makeCurrentSuc = EGL14.eglMakeCurrent(mEglDisplay, mEGlSurface, mEGlSurface, mEglContext);
        if (!makeCurrentSuc) {
            throw new RuntimeException("eglMakeCurrent failed!");
        }
        mScreenFilter.onDrawFrame(textureId);
        EGLExt.eglPresentationTimeANDROID(mEglDisplay, mEGlSurface, timeStamp);

        EGL14.eglSwapBuffers(mEglDisplay, mEGlSurface);
    }

    public void release() {
        EGL14.eglDestroySurface(mEglDisplay, mEGlSurface);
        EGL14.eglMakeCurrent(mEglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
        EGL14.eglDestroyContext(mEglDisplay, mEglContext);
        EGL14.eglReleaseThread();
        EGL14.eglTerminate(mEglDisplay);
    }
}
