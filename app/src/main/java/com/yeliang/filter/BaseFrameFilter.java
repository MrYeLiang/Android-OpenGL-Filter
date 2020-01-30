package com.yeliang.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.yeliang.utils.OpenGlUtils;

/**
 * Created by yeliang on 2020/1/18.
 */

public class BaseFrameFilter extends BaseFilter {
    protected int[] mFrameBuffers;
    protected int[] mFrameBufferTextures;

    BaseFrameFilter(Context context, int vertexShaderId, int fragShaderId) {
        super(context, vertexShaderId, fragShaderId);
    }

    private void destroyFrameBuffers() {
        if (mFrameBufferTextures != null) {
            GLES20.glDeleteTextures(1, mFrameBufferTextures, 0);
            mFrameBufferTextures = null;
        }

        if (mFrameBuffers != null) {
            GLES20.glDeleteFramebuffers(1, mFrameBuffers, 0);
            mFrameBuffers = null;
        }
    }

    @Override
    public void onReady(int width, int height) {
        super.onReady(width, height);
        if (mFrameBuffers != null) {
            destroyFrameBuffers();
        }

        mFrameBuffers = new int[1];

        //1 创建fbo
        GLES20.glGenFramebuffers(mFrameBuffers.length, mFrameBuffers, 0);

        mFrameBufferTextures = new int[1];

        //2 创建fbo纹理
        OpenGlUtils.glConfigureTextures(mFrameBufferTextures);

        //3 绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFrameBufferTextures[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mOutputWidth, mOutputHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

        //4 fbo绑定纹理
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mFrameBufferTextures[0], 0);

        //5 解绑
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    @Override
    public int onDrawFrame(int textureId) {
        return 0;
    }

    @Override
    public void release() {
        super.release();
        destroyFrameBuffers();
    }
}
