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
}
