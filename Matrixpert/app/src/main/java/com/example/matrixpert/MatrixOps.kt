package com.example.matrixpert

object MatrixOps {
    init {
        System.loadLibrary("matrixnative")
    }

    external fun addMatrices(a: DoubleArray, b: DoubleArray, rows: Int, cols: Int): DoubleArray
    external fun subtractMatrices(a: DoubleArray, b: DoubleArray, rows: Int, cols: Int): DoubleArray
    external fun dotProductMatrices(a: DoubleArray, b: DoubleArray, rA: Int, cA: Int, rB: Int, cB: Int): DoubleArray
    external fun divideMatrices(a: DoubleArray, b: DoubleArray, rA: Int, cA: Int, rB: Int, cB: Int): DoubleArray
}