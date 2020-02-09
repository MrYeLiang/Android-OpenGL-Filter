package com.yeliang.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.yeliang.R;

/**
 * Author: yeliang
 * Date: 2020-02-09
 * Time: 16:53
 * Description:
 */
public class BeautyFilter extends BaseFrameFilter {

    private int width;
    private int height;

    public BeautyFilter(Context context) {
        super(context, R.raw.screen_vertex, R.raw.beauty_frag);

        width = GLES20.glGetUniformLocation(mGLProgramId, "width");
        height = GLES20.glGetUniformLocation(mGLProgramId, "height");
    }

    @Override
    protected void initCoordinate() {
        mGLTextureBuffer.clear();

        float[] TEXTURE = {
                0.0f, 0.0f,
                1.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f
        };
        mGLTextureBuffer.put(TEXTURE);
    }

    @Override
    public int onDrawFrame(int textureId) {
        //1 设置窗口大小
        GLES20.glViewport(0, 0, mOutputWidth, mOutputHeight);

        //2 绑定fbo
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);


        //3 使用着色器
        GLES20.glUseProgram(mGLProgramId);
        GLES20.glUniform1i(width, mOutputWidth);
        GLES20.glUniform1i(height, mOutputHeight);

        //4.1 传递顶点坐标
        mGLVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, mGLVertexBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);

        //4.2 传递纹理坐标
        mGLTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, mGLTextureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);

        //5 绑定2D纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(vTexture, 0);

        //6 绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        //7 解绑
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        return mFrameBufferTextures[0];
    }
}
