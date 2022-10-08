package com.code.mesh_visualizer;

public class Point {
    private double x, y, z;
    private int q;

    Point(double x, double y, double z, int q) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.q = q;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }
}
