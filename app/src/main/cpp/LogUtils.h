//
// Created by Tom on 2020/1/30.
//

#ifndef ANDROID_OPENGL_FILTER_LOGUTILS_H
#define ANDROID_OPENGL_FILTER_LOGUTILS_H

#ifdef ANDROID

#include <android/log.h>
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "filter", __VA_ARGS__)


#else
#define LOGD(...) printf("filter", __VA_ARGS_)

#endif

#endif //ANDROID_OPENGL_FILTER_LOGUTILS_H
