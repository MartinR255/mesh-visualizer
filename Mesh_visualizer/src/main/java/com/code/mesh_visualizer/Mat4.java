package com.code.mesh_visualizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Mat4 {
    private List<List<Double>> matrix;

    public Mat4() {
        matrix = Stream.generate(() -> new ArrayList<>(Collections.nCopies(4, 0.0)))
                .limit(4)
                .collect(Collectors.toList());
        setValue(0, 0, 1d);
        setValue(1, 1, 1d);
        setValue(2, 2, 1d);
        setValue(3, 3, 1d);
    }

    public void setValue(int line, int column, double value) {
        matrix.get(line).set(column, value);
    }

    public List<List<Double>> getMat4() {
        return matrix;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (List<Double> l : matrix) {
            result.append(l).append("\n");
        }
        return result.toString();
    }
}
