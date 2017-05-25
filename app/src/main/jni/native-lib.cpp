#include <jni.h>
#include <string>
#include <android/log.h>
#include <cv.h>
#include <cxcore.h>
#include <highgui.h>
#include "sharedmatting.h"
#define LOG    "shared-matting" // 这个是自定义的LOG的标识
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG,__VA_ARGS__) // 定义LOGD类型
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG,__VA_ARGS__) // 定义LOGI类型
#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG,__VA_ARGS__) // 定义LOGW类型
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG,__VA_ARGS__) // 定义LOGE类型
#define LOGF(...)  __android_log_print(ANDROID_LOG_FATAL,LOG,__VA_ARGS__) // 定义LOGF类型
char* jstringTostring(JNIEnv* env, jstring jstr)
{
    char* rtn = NULL;
    jclass clsstring = env->FindClass("java/lang/String");
    jstring strencode = env->NewStringUTF("utf-8");
    jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr= (jbyteArray)env->CallObjectMethod(jstr, mid, strencode);
    jsize alen = env->GetArrayLength(barr);
    jbyte* ba = env->GetByteArrayElements(barr, JNI_FALSE);
    if (alen > 0)
    {
        rtn = (char*)malloc(alen + 1);

        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    env->ReleaseByteArrayElements(barr, ba, 0);
    return rtn;
}
// 内轮廓填充
// 参数:
// 1. pBinary: 输入二值图像，单通道，位深IPL_DEPTH_8U。
// 2. dAreaThre: 面积阈值，当内轮廓面积小于等于dAreaThre时，进行填充。
void FillInternalContours(IplImage *pBinary, double dAreaThre)
{
    double dConArea;
    CvSeq *pContour = NULL;
    CvSeq *pConInner = NULL;
    CvMemStorage *pStorage = NULL;
    // 执行条件
    if (pBinary)
    {
        // 查找所有轮廓
        pStorage = cvCreateMemStorage(0);
        cvFindContours(pBinary, pStorage, &pContour, sizeof(CvContour), CV_RETR_CCOMP, CV_CHAIN_APPROX_SIMPLE);
        // 填充所有轮廓
        cvDrawContours(pBinary, pContour, CV_RGB(127, 127, 127), CV_RGB(0, 0, 0), 2, CV_FILLED, 8, cvPoint(0, 0));
        // 外轮廓循环
        for (; pContour != NULL; pContour = pContour->h_next)
        {
            // 内轮廓循环
            for (pConInner = pContour->v_next; pConInner != NULL; pConInner = pConInner->h_next)
            {
                // 内轮廓面积
//                dConArea = fabs(cvContourArea(pConInner, CV_WHOLE_SEQ));
//                if (dConArea <= dAreaThre)
//                {
                    cvDrawContours(pBinary, pConInner, CV_RGB(255, 255, 255), CV_RGB(255, 255, 255), 0, CV_FILLED, 8, cvPoint(0, 0));
//                }
            }
        }
        const char* destName = "/storage/emulated/0/dest_contours.png";
        cvSaveImage(destName, pBinary);
        cvReleaseMemStorage(&pStorage);
        pStorage = NULL;
    }
}
// extern 作用有两个
// 1. 于"C"连用修饰变量或者函数时,告诉编译器按照C的规则编译函数名
// 2. 不与"C"在一起时,它的作用就是声明函数或全局变量的作用范围的关键字,它是声明不是定义
extern "C"
JNIEXPORT void JNICALL
Java_com_jb_sharedmatting_MainActivity_cvFindContours(JNIEnv *env, jobject instance,
                                                      jstring filePath_) {
    IplImage* pImg = NULL;
//    IplImage* pContourImg = NULL;
//    CvMemStorage* storage = cvCreateMemStorage(0);
//    CvSeq* contour = 0;
//    int mode = CV_RETR_CCOMP;
    pImg = cvLoadImage(jstringTostring(env, filePath_), CV_8U);
    FillInternalContours(pImg, 0);
//    pContourImg = cvCreateImage(cvGetSize(pImg), IPL_DEPTH_8U, 3);
//    cvCvtColor(pImg, pContourImg, CV_GRAY2BGR);
//    cvFindContours(pImg, storage, &contour, sizeof(CvContour), mode, CV_CHAIN_APPROX_SIMPLE, cvPoint(0, 0));
//    cvDrawContours(pContourImg, contour->v_next, CV_RGB(0, 0, 255), CV_RGB(255, 0, 0), 0, CV_FILLED, 8, cvPoint(0, 0));
//    const char* destName = "/storage/emulated/0/dest_contours.png";
//    cvSaveImage(destName, pImg);

}

extern "C"
JNIEXPORT void JNICALL
Java_com_jb_sharedmatting_MainActivity_handleImage(JNIEnv *env, jobject instance, jstring fileName_,
                                                   jstring trimapName_, jstring matteName_) {
    // TODO
    LOGE("before declare SharedMatting");
    SharedMatting sm;
    LOGE("start load Image!!!!!!!!!!!!!!!!!!!!!!!!!");
    sm.loadImage(jstringTostring(env, fileName_));
    LOGE("start load Trimap-Image!!!!!!!!!!!!!!!!!!!!!!!!!");
    sm.loadTrimap(jstringTostring(env, trimapName_));
    //sm.solveAlpha();
    LOGE("start expandKnown!!!!!!!!!!!!!!!!!!!!!!!!!");
    sm.expandKnown();// 30s
    LOGE("start gathering!!!!!!!!!!!!!!!!!!!!!!!!!");
    sm.gathering(); // 18s
    LOGE("start refineSample!!!!!!!!!!!!!!!!!!!!!!!!!");
    sm.refineSample();// 14s
    LOGE("start localSmooth!!!!!!!!!!!!!!!!!!!!!!!!!");
    sm.localSmooth();//9s
    sm.getMatte();
    LOGE("start save!!!!!!!!!!!!!!!!!!!!!!!!!");
    sm.save(jstringTostring(env, matteName_));

}

extern "C"
// 代表可以被Java调用
JNIEXPORT
// 返回值类型
jstring
// 声明遵守JNI Java调用C的规则
JNICALL
// 函数名
Java_com_jb_sharedmatting_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";

    return env->NewStringUTF(hello.c_str());
}

