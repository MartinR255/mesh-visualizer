package com.code.mesh_visualizer;

import java.util.List;
import java.util.stream.Stream;

public class Transformations {

    public static Mat4 multiply(Mat4 mat1, Mat4 mat2) {
        Mat4 resultMatrix = new Mat4();
        List<List<Double>> leftMatrix = mat1.getMat4();
        List<List<Double>> rightMatrix = mat2.getMat4();

        Stream.iterate(0, index -> index + 1).limit(4).forEach(index -> {
            List<Double> currentLine = leftMatrix.get(index);
            Stream.iterate(0, columnIndex -> columnIndex + 1).limit(4).forEach(columnIndex -> {
                double value = Stream.iterate(0, valueIndex -> valueIndex + 1).limit(4)
                        .mapToDouble(valueIndex -> currentLine.get(valueIndex) * rightMatrix.get(valueIndex).get(columnIndex))
                        .sum();
                resultMatrix.setValue(index, columnIndex, value);
            });
        });

        return resultMatrix;
    }

    public static Vec4 multiply(Mat4 mat, Vec4 vec) {
        Vec4 resultVector = new Vec4();
        List<List<Double>> matrix = mat.getMat4();
        List<Double> multiplierVec = vec.getVec4();

        Stream.iterate(0, index -> index + 1).limit(4).forEach(index -> {
            List<Double> currentLine = matrix.get(index);
            double value = Stream.iterate(0, valueIndex -> valueIndex + 1).limit(4)
                    .mapToDouble(valueIndex -> currentLine.get(valueIndex) * multiplierVec.get(valueIndex))
                    .sum();
            resultVector.setValue(index, value);
        });

        return resultVector;
    }

    public static Vec4 multiply(Vec4 vec, double scalar) {
        Vec4 resultVector = new Vec4();
        List<Double> points = vec.getVec4();

        Stream.iterate(0, i -> i + 1).limit(points.size()-1)
                .forEach(i -> resultVector.setValue(i, points.get(i) * scalar));

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

    public static Vec4 addVec4(Vec4 v1, Vec4 v2, double vec4Type) {
        List<Double> v1Points = v1.getVec4();
        List<Double> v2Points = v2.getVec4();
        double q1 = v1Points.get(0) + v2Points.get(0);
        double q2 = v1Points.get(1) + v2Points.get(1);
        double q3 = v1Points.get(2) + v2Points.get(2);
        return new Vec4(q1, q2, q3, vec4Type);
    }

    public static Vec4 subtractVec4(Vec4 v1, Vec4 v2, double vec4Type) {
        List<Double> v1Points = v1.getVec4();
        List<Double> v2Points = v2.getVec4();
        double q1 = v1Points.get(0) - v2Points.get(0);
        double q2 = v1Points.get(1) - v2Points.get(1);
        double q3 = v1Points.get(2) - v2Points.get(2);
        return new Vec4(q1, q2, q3, vec4Type);
    }

    public static Vec4 vectorCrossProduct(Vec4 v1, Vec4 v2) {
        List<Double> v1Points = v1.getVec4();
        List<Double> v2Points = v2.getVec4();
        double ax = v1Points.get(0), ay = v1Points.get(1), az = v1Points.get(2);
        double bx = v2Points.get(0), by = v2Points.get(1), bz = v2Points.get(2);

        double cx = ay * bz - az * by;
        double cy = az * bx - ax * bz;
        double cz = ax * by - ay * bx;
        return new Vec4(cx, cy, cz, 0d);
    }

    public static Vec4 normalizeVector(Vec4 vec) {
        List<Double> points = vec.getVec4();
        double sumOfSquares = Stream.iterate(0, i -> i + 1).limit(vec.getVec4().size()-1)
                .mapToDouble(i -> Math.pow(vec.getVec4().get(i), 2)).sum();
        double len = Math.sqrt(sumOfSquares);
        return new Vec4(points.get(0) / len, points.get(1) / len, points.get(2) / len, 0d);
    }

    public static double dotProduct(Vec4 v1, Vec4 v2) {
        double product;
        List<Double> v1Points = v1.getVec4();
        List<Double> v2Points = v2.getVec4();

        product = Stream.iterate(0, i -> i + 1).limit(v1Points.size()-1)
                .mapToDouble(i -> v1Points.get(i) * v2Points.get(i)).sum();
        return product;
    }

    public static Vec4 getTriangleNormal(Vec4 p0, Vec4 p1, Vec4 p2) {
        Vec4 v0 = Transformations.normalizeVector(Transformations.subtractVec4(p1, p0, 0d));
        Vec4 v1 = Transformations.normalizeVector(Transformations.subtractVec4(p2, p1, 0d));
        return Transformations.vectorCrossProduct(v0, v1);
    }
}
