#include <jni.h>
#include <Eigen/Dense>
#include <vector>

using namespace Eigen;
using RowMatrixXd = Matrix<double, Dynamic, Dynamic, RowMajor>;
extern "C"
JNIEXPORT jdoubleArray JNICALL
Java_com_example_matrixpert_MatrixOps_addMatrices(
        JNIEnv *env, jobject,
        jdoubleArray aArray, jdoubleArray bArray,
        jint rows, jint cols) {

    jdouble *a = env->GetDoubleArrayElements(aArray, nullptr);
    jdouble *b = env->GetDoubleArrayElements(bArray, nullptr);

    Map<MatrixXd> matA(a, rows, cols);
    Map<MatrixXd> matB(b, rows, cols);

    MatrixXd result = matA + matB;

    jdoubleArray resultArray = env->NewDoubleArray(rows * cols);
    env->SetDoubleArrayRegion(resultArray, 0, rows * cols, result.data());

    env->ReleaseDoubleArrayElements(aArray, a, 0);
    env->ReleaseDoubleArrayElements(bArray, b, 0);

    return resultArray;
}

extern "C"
JNIEXPORT jdoubleArray JNICALL
Java_com_example_matrixpert_MatrixOps_subtractMatrices(
        JNIEnv *env, jobject,
        jdoubleArray aArray, jdoubleArray bArray,
        jint rows, jint cols) {

    jdouble *a = env->GetDoubleArrayElements(aArray, nullptr);
    jdouble *b = env->GetDoubleArrayElements(bArray, nullptr);

    Map<MatrixXd> matA(a, rows, cols);
    Map<MatrixXd> matB(b, rows, cols);

    MatrixXd result = matA - matB;

    jdoubleArray resultArray = env->NewDoubleArray(rows * cols);
    env->SetDoubleArrayRegion(resultArray, 0, rows * cols, result.data());

    env->ReleaseDoubleArrayElements(aArray, a, 0);
    env->ReleaseDoubleArrayElements(bArray, b, 0);

    return resultArray;
}

extern "C"
JNIEXPORT jdoubleArray JNICALL
Java_com_example_matrixpert_MatrixOps_multiplyMatrices(
        JNIEnv *env, jobject,
        jdoubleArray aArray, jdoubleArray bArray,
        jint rows, jint cols) {

    jdouble *a = env->GetDoubleArrayElements(aArray, nullptr);
    jdouble *b = env->GetDoubleArrayElements(bArray, nullptr);

    Map<MatrixXd> matA(a, rows, cols);
    Map<MatrixXd> matB(b, rows, cols);

    MatrixXd result = matA.cwiseProduct(matB); // Element-wise multiplication

    jdoubleArray resultArray = env->NewDoubleArray(rows * cols);
    env->SetDoubleArrayRegion(resultArray, 0, rows * cols, result.data());

    env->ReleaseDoubleArrayElements(aArray, a, 0);
    env->ReleaseDoubleArrayElements(bArray, b, 0);

    return resultArray;
}

extern "C"
JNIEXPORT jdoubleArray JNICALL
Java_com_example_matrixpert_MatrixOps_dotProductMatrices(
        JNIEnv *env, jobject,
        jdoubleArray aArray, jdoubleArray bArray,
        jint rA, jint cA, jint rB, jint cB) {

    jdouble *a = env->GetDoubleArrayElements(aArray, nullptr);
    jdouble *b = env->GetDoubleArrayElements(bArray, nullptr);

    Map<RowMatrixXd> matA(a, rA, cA);
    Map<RowMatrixXd> matB(b, rB, cB);

    RowMatrixXd result = matA * matB;

    jdoubleArray resultArray = env->NewDoubleArray(rA * cB);
    env->SetDoubleArrayRegion(resultArray, 0, rA * cB, result.data());

    env->ReleaseDoubleArrayElements(aArray, a, 0);
    env->ReleaseDoubleArrayElements(bArray, b, 0);

    return resultArray;
}

extern "C"
JNIEXPORT jdoubleArray JNICALL
Java_com_example_matrixpert_MatrixOps_divideMatrices(
        JNIEnv *env, jobject,
        jdoubleArray aArray, jdoubleArray bArray,
        jint rA, jint cA, jint rB, jint cB) {

    jdouble *a = env->GetDoubleArrayElements(aArray, nullptr);
    jdouble *b = env->GetDoubleArrayElements(bArray, nullptr);

    Map<RowMatrixXd> matA(a, rA, cA);
    Map<RowMatrixXd> matB(b, rB, cB);

    // Check if B is square and invertible
    if (rB != cB) {
        // Return empty array to signal error
        jdoubleArray resultArray = env->NewDoubleArray(0);
        env->ReleaseDoubleArrayElements(aArray, a, 0);
        env->ReleaseDoubleArrayElements(bArray, b, 0);
        return resultArray;
    }

    if (matB.determinant() == 0) {
        // Return empty array to signal error
        jdoubleArray resultArray = env->NewDoubleArray(0);
        env->ReleaseDoubleArrayElements(aArray, a, 0);
        env->ReleaseDoubleArrayElements(bArray, b, 0);
        return resultArray;
    }

    RowMatrixXd result = matA * matB.inverse();

    jdoubleArray resultArray = env->NewDoubleArray(rA * cB);
    env->SetDoubleArrayRegion(resultArray, 0, rA * cB, result.data());

    env->ReleaseDoubleArrayElements(aArray, a, 0);
    env->ReleaseDoubleArrayElements(bArray, b, 0);

    return resultArray;
}