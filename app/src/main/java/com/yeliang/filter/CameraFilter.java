package com.yeliang.filter;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.yeliang.R;
import com.yeliang.utils.OpenGlUtils;

/**
 * Author: yeliang
 * Date: 2019/12/2
 * Time: 3:46 PM
 * Description:
 */

public class CameraFilter extends BaseFrameFilter {

    private float[] matrix;

    public CameraFilter(Context context) {
        super(context, R.raw.camera_vertex, R.raw.camera_frag);
    }

    public void setMatrix(float[] matrix) {
        this.matrix = matrix;
    }

    @Override
    protected void initCoordinate() {
        mGLTextureBuffer.clear();

        float[] TEXTURE = {
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 0.0f,
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


        //4.1 传递顶点坐标
        mGLVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, mGLVertexBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);

        //4.2 传递纹理坐标
        mGLTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, mGLTextureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);

        //5 变换矩阵
        GLES20.glUniformMatrix4fv(vMatrix, 1, false, matrix, 0);

        //6 激活纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        //7 绑定纹理
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glUniform1i(vTexture, 0);

        //8 绘制方式
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        //9 解绑
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        return mFrameBufferTextures[0];
    }
}
