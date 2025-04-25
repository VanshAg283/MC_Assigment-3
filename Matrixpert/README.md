# Matrixpert – Matrix Calculator App

Matrixpert is an Android app that allows users to perform matrix operations (addition, subtraction, multiplication, and division) on matrices of any size. The app leverages a modern Jetpack Compose UI for input and output, and uses native C++ code (with the Eigen library) for efficient matrix computations via JNI.

## Features

- **Dynamic Matrix Input:** Users can specify the dimensions and elements of two matrices (A and B) of any size.
- **Supported Operations:** Add, Subtract, Multiply (dot product), and Divide (A × B⁻¹, where B is invertible).
- **Native Performance:** All matrix operations are performed in C++ using the Eigen library for high performance.
- **Error Handling:** The app checks for dimension mismatches, non-square/invertible matrices, and displays user-friendly error messages.
- **Modern UI:** Built with Jetpack Compose for a responsive and intuitive user experience.

## Architecture Overview

### 1. **UI Layer (Kotlin, Jetpack Compose)**

- **MainActivity.kt:**
  Sets up the Compose UI and theme, and displays the `MatrixScreen`.

- **MatrixScreen.kt:**
  - Provides the main UI for the app.
  - Lets users select the operation, input matrix dimensions and elements, and view results.
  - Handles user input, validation, and triggers native operations via `MatrixOps`.

- **MatrixInput & MatrixOutput Composables:**
  - `MatrixInput`: Renders a grid of text fields for matrix entry.
  - `MatrixOutput`: Displays the result matrix in a read-only grid.

### 2. **Native Interface Layer (Kotlin)**

- **MatrixOps.kt:**
  - Loads the native library (`matrixnative`).
  - Declares external functions for each matrix operation, mapping to C++ implementations via JNI.

### 3. **Native Computation Layer (C++ with Eigen)**

- **matrix_native.cpp:**
  - Implements JNI functions for each operation.
  - Uses Eigen's `MatrixXd` and related types for efficient matrix math.
  - Handles conversion between Java/Kotlin arrays and Eigen matrices.
  - Includes error checks (e.g., dimension match, invertibility).

## How It Works

1. **User Input:**
   - User enters dimensions and elements for matrices A and B.
   - User selects an operation (Add, Subtract, Multiply, Divide).

2. **Validation:**
   - The app checks if the operation is valid for the given dimensions (e.g., for multiplication, A's columns must match B's rows).

3. **Native Call:**
   - The app converts the matrix elements to `DoubleArray` and calls the corresponding native function via `MatrixOps`.

4. **C++ Computation:**
   - The native function receives the arrays, maps them to Eigen matrices, performs the operation, and returns the result as a new array.

5. **Result Display:**
   - The app formats and displays the result matrix, or shows an error if the operation failed.

## Example: Matrix Addition Flow

1. User enters 2x2 matrices A and B.
2. User selects "Add" and taps "Calculate".
3. App checks that both matrices are 2x2.
4. App calls `MatrixOps.addMatrices(a, b, 2, 2)`.
5. Native C++ code adds the matrices using Eigen and returns the result.
6. Result is displayed in a grid below the input.

## Key Files

- `app/src/main/java/com/example/matrixpert/MainActivity.kt`
  Entry point, sets up Compose UI.

- `app/src/main/java/com/example/matrixpert/MatrixScreen.kt`
  Main UI logic and composables for input/output.

- `app/src/main/java/com/example/matrixpert/MatrixOps.kt`
  JNI interface to native C++ code.

- `app/src/main/cpp/matrix_native.cpp`
  C++ implementation of matrix operations using Eigen.

## Matrix Operations Supported

| Operation   | Kotlin Function                | C++ Implementation         | Notes                                                      |
|-------------|-------------------------------|----------------------------|------------------------------------------------------------|
| Add         | `addMatrices`                 | `matA + matB`              | Same dimensions required                                   |
| Subtract    | `subtractMatrices`            | `matA - matB`              | Same dimensions required                                   |
| Multiply    | `dotProductMatrices`          | `matA * matB`              | A columns = B rows, result is (A rows × B cols)            |
| Divide      | `divideMatrices`              | `matA * matB.inverse()`    | B must be square and invertible, A columns = B rows        |


## Error Handling

- **Dimension Mismatch:**
  User is notified if matrix dimensions are incompatible for the selected operation.

- **Non-Invertible Matrix:**
  For division, if matrix B is not square or not invertible, an error is shown.

- **Input Validation:**
  Non-numeric or missing entries are treated as zero.

## Dependencies

- **Jetpack Compose** for UI.
- **Eigen** C++ library for matrix operations (included in native sources).
- **JNI** for Kotlin/C++ interoperability.

## Building and Running

1. Open the project in Android Studio.
2. Build the project (CMake will build the native library).
3. Run on an emulator or device.

## Credits

- Eigen C++ library: [http://eigen.tuxfamily.org/](http://eigen.tuxfamily.org/)
- Jetpack Compose: [https://developer.android.com/jetpack/compose](https://developer.android.com/jetpack/compose)

---