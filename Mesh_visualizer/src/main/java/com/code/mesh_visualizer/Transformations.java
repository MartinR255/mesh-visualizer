package com.code.mesh_visualizer;

import java.util.List;

public class Transformations {

    public static Mat4 multiply(Mat4 mat1, Mat4 mat2) {
        Mat4 resultMatrix = new Mat4();
        List<List<Double>> leftMatrix = mat1.getMat4();
        List<List<Double>> rightMatrix = mat2.getMat4();
        for (int index = 0; index < 4; index++) {
            List<Double> currentLine = leftMatrix.get(index);
            for (int columnIndex = 0; columnIndex < 4; columnIndex++) {
                double value = 0d;
                for (int valueIndex = 0; valueIndex < 4; valueIndex++) {
                    value += currentLine.get(valueIndex) * rightMatrix.get(valueIndex).get(columnIndex);
                }
                resultMatrix.setValue(index, columnIndex, value);
            }
        }
        return resultMatrix;
    }

    public static Vec4 multiply(Mat4 mat, Vec4 vec) {
        Vec4 resultVector = new Vec4();
        List<List<Double>> matrix = mat.getMat4();
        List<Double> multiplierVec = vec.getVec4();
        for (int index = 0; index < 4; index++) {
            List<Double> currentLine = matrix.get(index);
            double value = 0;
            for (int valueIndex = 0; valueIndex < 4; valueIndex++) {
                value += currentLine.get(valueIndex) * multiplierVec.get(valueIndex);
            }
            resultVector.setValue(index, value);
        }
        return resultVector;
    }

    public static Mat4 getMirrorMatrixOverX() {
        Mat4 mirrorMatrix = new Mat4();
        mirrorMatrix.setValue(1, 1, -1);
        mirrorMatrix.setValue(2, 2, -1);
        return mirrorMatrix;
    }

    public static Mat4 addTranslation(double x, double y, double z, Mat4 toMatrix) {
        Mat4 translationMatrix = new Mat4();
        translationMatrix.setValue(0, 3, x);
        translationMatrix.setValue(1, 3, y);
        translationMatrix.setValue(2, 3, z);

        return Transformations.multiply(translationMatrix, toMatrix);
    }

    public static Mat4 addScaling(double scaleValue, Mat4 toMatrix) {
        Mat4 scalingMatrix = new Mat4();
        scalingMatrix.setValue(0, 0, scaleValue);
        scalingMatrix.setValue(1, 1, scaleValue);
        scalingMatrix.setValue(2, 2, scaleValue);

        return Transformations.multiply(scalingMatrix, toMatrix);
    }

    public static Mat4 addRotation(double x, double y, double z, Mat4 toMatrix) {
        if (x != 0.0) {
            toMatrix = Transformations.multiply(getXRotationMatrix(x), toMatrix);
        }

        if (y != 0.0) {
            toMatrix = Transformations.multiply(getYRotationMatrix(y), toMatrix);
        }

        if (z != 0.0) {
            toMatrix = Transformations.multiply(getZRotationMatrix(z), toMatrix);
        }

        return toMatrix;
    }

    private static Mat4 getXRotationMatrix(double x) {
        Mat4 rotationXMatrix = new Mat4();
        rotationXMatrix.setValue(1, 1, Math.cos(x));
        rotationXMatrix.setValue(1, 2, -Math.sin(x));
        rotationXMatrix.setValue(2, 1, Math.sin(x));
        rotationXMatrix.setValue(2, 2, Math.cos(x));

        return rotationXMatrix;
    }

    private static Mat4 getYRotationMatrix(double y) {
        Mat4 rotationYMatrix = new Mat4();
        rotationYMatrix.setValue(0, 0, Math.cos(y));
        rotationYMatrix.setValue(0, 2, Math.sin(y));
        rotationYMatrix.setValue(2, 0, -Math.sin(y));
        rotationYMatrix.setValue(2, 2, Math.cos(y));

        return rotationYMatrix;
    }

    private static Mat4 getZRotationMatrix(double z) {
        Mat4 rotationZMatrix = new Mat4();
        rotationZMatrix.setValue(0, 0, Math.cos(z));
        rotationZMatrix.setValue(0, 1, -Math.sin(z));
        rotationZMatrix.setValue(1, 0, Math.sin(z));
        rotationZMatrix.setValue(1, 1, Math.cos(z));

        return rotationZMatrix;
    }
}
