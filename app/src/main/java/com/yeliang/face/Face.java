package com.yeliang.face;

import java.util.Arrays;

/**
 * Created by yeliang on 2020/1/16.
 */

public class Face {

    //保存人脸的关键点坐标
    public float[] landmarks;

    //保存人脸宽高
    public int width;
    public int height;

    //送去检测图片的宽高
    public int imgWidth;
    public int imgHeight;

    public Face(int width, int height, int imgWidth, int imgHeight,float[] landmarks) {
        this.landmarks = landmarks;
        this.width = width;
        this.height = height;
        this.imgWidth = imgWidth;
        this.imgHeight = imgHeight;
    }

    @Override
    public String toString() {
        return "Face{" +
                "landmarks=" + Arrays.toString(landmarks) +
                ", width=" + width +
                ", height=" + height +
                ", imgWidth=" + imgWidth +
                ", imgHeight=" + imgHeight +
                '}';
    }
}
