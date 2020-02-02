#include <jni.h>
#include <jni.h>
#include <string>
#include "FaceTrack.h"
#include "LogUtils.h"

using namespace std;



extern "C"
JNIEXPORT void JNICALL
Java_com_yeliang_face_FaceTrack_native_1start(JNIEnv *env, jobject instance, jlong self) {

    if(self == 0){
        return;
    }

    FaceTrack *me =(FaceTrack*) self;
    me->startTracking();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_yeliang_face_FaceTrack_native_1stop(JNIEnv *env, jobject instance, jlong self) {

    if(self == 0){
        return;
    }

    FaceTrack * me = (FaceTrack *) self;
    me->stopTracking();
    delete me;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_yeliang_face_FaceTrack_native_1detector(JNIEnv *env, jobject instance, jlong self,
                                                 jbyteArray data_, jint cameraId, jint width,
                                                 jint height) {

    //LOGD("detector self = %ld", self);
   if(self == 0){
       //LOGD("detector self = %ld", self);
       return NULL;
   }

    jbyte *data = env->GetByteArrayElements(data_, NULL);
    FaceTrack *me = (FaceTrack *) self;
    Mat src(height + height / 2, width, CV_8UC1, data);

    cvtColor(src, src, CV_YUV2RGBA_NV21);

    if(cameraId == 1){
        //前置 逆时针旋转90
        rotate(src, src, ROTATE_90_COUNTERCLOCKWISE);

        //y翻转
        flip(src, src, 1);
    } else {
        rotate(src, src, ROTATE_90_CLOCKWISE);
    }

    cvtColor(src, src, COLOR_RGBA2GRAY);
    //直方图均衡化 增强对比效果
    equalizeHist(src, src);

    vector<Rect2f> rects;
    me->detector(src, rects);
    env->ReleaseByteArrayElements(data_, data, 0);

    int w = src.cols;
    int h = src.rows;
    src.release();

    int ret = rects.size();

    //LOGD("detector ret = %d", ret);

    if(ret){
        jclass clazz = env->FindClass("com/yeliang/face/Face");

        //LOGD("detector ret = %d", &clazz);

        jmethodID  construct = env->GetMethodID(clazz, "<init>","(IIII[F)V");

        int size = ret * 2;
        jfloatArray  floatArray = env->NewFloatArray(size);

        for(int i = 0, j = 0; i < size; j++){
            float f[2] = {rects[j].x, rects[j].y};
            env->SetFloatArrayRegion(floatArray, i, 2, f);
            i += 2;
        }

        Rect2f faceRect = rects[0];
        int width = faceRect.width;
        int height = faceRect.height;
        jobject  face = env->NewObject(clazz, construct, width, height, w, h, floatArray);

        //LOGD("detector ret = %d", &face);
        return face;
    }

    return NULL;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_yeliang_face_FaceTrack_native_1create(JNIEnv *env, jobject instance, jstring model_,
                                               jstring seeta_) {
    const char *model = env->GetStringUTFChars(model_, 0);
    const char *seeta = env->GetStringUTFChars(seeta_, 0);

    FaceTrack *faceTrack = new FaceTrack(model, seeta);

    //LOGD("detector create = %ld", faceTrack);

    env->ReleaseStringUTFChars(model_, model);
    env->ReleaseStringUTFChars(seeta_, seeta);

    return reinterpret_cast<jlong>(faceTrack);
}
