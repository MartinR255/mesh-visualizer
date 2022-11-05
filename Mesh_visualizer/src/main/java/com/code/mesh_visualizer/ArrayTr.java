package com.code.mesh_visualizer;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ArrayTr {

    public static List<Double> normalizeValues(List<Double> values) {
        if (values.isEmpty()) return values;
        double minValue = values.stream().min(Double::compare).get();
        double maxValue = values.stream().max(Double::compare).get();
        double divisor = maxValue - minValue;
        return values.stream().map(val -> (val - minValue) / divisor).toList();
    }

    public static <T> T[] concatWithStream(T[] array1, T[] array2) {
        return Stream.concat(Arrays.stream(array1), Arrays.stream(array2))
                .toArray(size -> (T[]) Array.newInstance(array1.getClass().getComponentType(), size));
    }
}
