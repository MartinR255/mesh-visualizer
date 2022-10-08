package com.code.mesh_visualizer;

import java.util.ArrayList;
import java.util.List;

public class Face {
    private List<Point> points;

    public Face(Point p1, Point p2, Point p3) {
        points = new ArrayList<>();
        points = List.of(p1, p2, p3);
    }

    public List<Point> getPoints() {
        return points;
    }
}
