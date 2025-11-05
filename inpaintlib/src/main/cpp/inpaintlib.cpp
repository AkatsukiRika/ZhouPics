#include <jni.h>
#include <string>
#include <android/log.h>
#include "loader/InpaintLoader.h"

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "inpaintlib", __VA_ARGS__)

extern "C"
JNIEXPORT jobject JNICALL
Java_com_tgwgroup_inpaintlib_NativeLib_runInpaint(JNIEnv *env, jobject thiz, jobject image_bitmap, jobject mask_bitmap, jstring model_file) {
  if (model_file == nullptr) {
    LOGE("model_file is null");
    return nullptr;
  }

  const char* modelPath = env->GetStringUTFChars(model_file, nullptr);
  if (!modelPath) {
    LOGE("GetStringUTFChars failed");
    return nullptr;
  }

  FILE* fp = fopen(modelPath, "rb");
  if (!fp) {
    LOGE("fopen failed: %s", modelPath);
    env->ReleaseStringUTFChars(model_file, modelPath);
    return nullptr;
  }

  if (fseek(fp, 0, SEEK_END) != 0) {
    LOGE("fseek to end failed");
    fclose(fp);
    env->ReleaseStringUTFChars(model_file, modelPath);
    return nullptr;
  }

  long fileLength = ftell(fp);
  if (fileLength <= 0) {
    LOGE("ftell invalid length: %ld", fileLength);
    fclose(fp);
    env->ReleaseStringUTFChars(model_file, modelPath);
    return nullptr;
  }
  rewind(fp);

  std::unique_ptr<char[]> buffer(new(std::nothrow) char[fileLength]);
  if (!buffer) {
    LOGE("alloc buffer failed, size=%ld", fileLength);
    fclose(fp);
    env->ReleaseStringUTFChars(model_file, modelPath);
    return nullptr;
  }

  size_t readN = fread(buffer.get(), 1, static_cast<size_t>(fileLength), fp);
  fclose(fp);
  if (readN != static_cast<size_t>(fileLength)) {
    LOGE("fread size mismatch: read=%zu, expect=%ld", readN, fileLength);
    env->ReleaseStringUTFChars(model_file, modelPath);
    return nullptr;
  }

  env->ReleaseStringUTFChars(model_file, modelPath);

  auto inpaintLoader = std::make_unique<InpaintLoader>();
  return inpaintLoader->runInference(env, image_bitmap, mask_bitmap, buffer.get(), static_cast<off_t>(fileLength));
}