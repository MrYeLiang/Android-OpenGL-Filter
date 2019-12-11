package com.yeliang.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.yeliang.R;

/**
 * Author: yeliang
 * Date: 2019/12/7
 * Time: 5:11 PM
 * Description:
 */

public class ScreenFilter extends BaseFilter {
    public ScreenFilter(Context context) {
        super(context, R.raw.screen_vertex, R.raw.screen_frag);
    }

    @Override
    public int onDrawFrame(int textureId) {
        //1 设置窗口大小
        GLES20.glViewport(0, 0, mOutputWidth, mOutputHeight);

        //2 使用着色器
        GLES20.glUseProgram(mGLProgramId);


        //3.1 传递顶点坐标
        mGLVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, mGLVertexBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);

        //3.2 传递纹理坐标
        mGLTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, mGLTextureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);

        //4 激活纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        //5 解绑
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(vTexture, 0);

        //6 绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        return textureId;
    }
}
