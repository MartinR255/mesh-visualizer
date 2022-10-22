package com.code.mesh_visualizer;

import java.util.ArrayList;
import java.util.List;

public class Vec4 {
    private List<Double> vector;

    public Vec4(double x, double y, double z, double q) {
        vector = new ArrayList<>(List.of(x, y, z, q));
    }
    public Vec4() {
        vector = new ArrayList<>(List.of(0d, 0d, 0d, 0d));
    }

    public void setValue(int position, double value) {
        vector.set(position, value);
    }

    public List<Double> getVec4() {
        return vector;
    }

    @Override
    public String toString() {
        return "vector : " + vector;
    }
}
