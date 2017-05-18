#ifndef SHAREDMSTTING_H
#define SHAREDMSTTING_H
#include <iostream>
#include <opencv/cv.h>
#include <opencv/highgui.h>
#include <cmath>
#include <vector>

using namespace std;

struct labelPoint {
    int x;
    int y;
    int label;
};

struct Tuple {
    CvScalar f;
    CvScalar b;
    double sigmaf;
    double sigmab;

    int flag;

};

struct Ftuple {
    CvScalar f;
    CvScalar b;
    double alphar;
    double confidence;
};

/*程序中认定cvPoint中 x为行，y为列，可能错误，但对程序结果没有影响*/
class SharedMatting {
public:
    // 构造函数
    SharedMatting();

    // 析构函数
    ~SharedMatting();

    // 加载原图
    void loadImage(char *filename);

    // 加载三分图
    void loadTrimap(char *filename);

    // 扩展已知区域大小
    void expandKnown();

    //
    void sample(CvPoint p, vector<CvPoint> &f, vector<CvPoint> &b);

    void gathering();

    void refineSample();

    void localSmooth();

    void solveAlpha();

    void save(char *filename);

    void Sample(vector< vector<CvPoint> > &F, vector< vector<CvPoint> > &B);

    void getMatte();

    void release();

    double mP(int i, int j, CvScalar f, CvScalar b);

    double nP(int i, int j, CvScalar f, CvScalar b);

    double eP(int i1, int j1, int i2, int j2);

    double pfP(CvPoint p, vector<CvPoint> &f, vector<CvPoint> &b);

    double aP(int i, int j, double pf, CvScalar f, CvScalar b);

    double gP(CvPoint p, CvPoint fp, CvPoint bp, double pf);

    double gP(CvPoint p, CvPoint fp, CvPoint bp, double dpf, double pf);

    double dP(CvPoint s, CvPoint d);

    double sigma2(CvPoint p);

    double distanceColor2(CvScalar cs1, CvScalar cs2);

    double comalpha(CvScalar c, CvScalar f, CvScalar b);


private:
    IplImage *pImg;
    IplImage *trimap;
    IplImage *matte;

    vector<CvPoint> uT;
    vector<struct Tuple> tuples;
    vector<struct Ftuple> ftuples;

    int height;
    int width;
    int kI;
    int kG;
    int **unknownIndex;//Unknown的索引信息；
    int **tri;
    int **alpha;
    double kC;

    int step;
    int channels;
    uchar *data;

};


#endif