#ifndef ZHOUPICS_INPAINTLOADER_H
#define ZHOUPICS_INPAINTLOADER_H

#include <jni.h>
#include <opencv2/opencv.hpp>

class InpaintLoader {
public:
  jobject runInference(JNIEnv* env, jobject imageBitmap, jobject maskBitmap, const char* modelBuffer, off_t modelSize);
};

#endif //ZHOUPICS_INPAINTLOADER_H
