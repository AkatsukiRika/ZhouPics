#ifndef ZHOUPICS_CVUTILS_H
#define ZHOUPICS_CVUTILS_H

#include <opencv2/opencv.hpp>
#include <android/bitmap.h>
#include <arm_fp16.h>

class CvUtils {
public:
  static cv::Mat bitmapToMat(const AndroidBitmapInfo bitmapInfo, const void* pixels);
  static jobject matToBitmap(JNIEnv* env, const cv::Mat mat);
  static std::vector<float> convertToFloat(const std::vector<float16_t>& input);
};

#endif //ZHOUPICS_CVUTILS_H
