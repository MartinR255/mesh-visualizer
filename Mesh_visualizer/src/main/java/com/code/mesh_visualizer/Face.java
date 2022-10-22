package com.code.mesh_visualizer;

import java.util.ArrayList;
import java.util.List;

public class Face {
    private List<Vec4> points;

    public Face(Vec4 p1, Vec4 p2, Vec4 p3) {
        points = new ArrayList<>(List.of(p1, p2, p3));
    }

    public List<Vec4> getPoints() {
        return points;
    }
}
