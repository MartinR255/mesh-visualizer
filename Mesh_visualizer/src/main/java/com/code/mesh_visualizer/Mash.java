package com.code.mesh_visualizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Mash {
    private List<Vec4> points;
    private List<Face> faces;

    public Mash() {
        points = new ArrayList<>();
        faces = new ArrayList<>();
    }

    public void createMash(String file_path) {
        readDataFromFile(file_path);
    }

    public void clearMash() {
        points.clear();
        faces.clear();
    }

    private void readDataFromFile(String file_path) {
        try {
            List<String> all_lines = Files.readAllLines(Paths.get(file_path));
            for (String line : all_lines.subList(1, all_lines.size())) {
                //System.out.println(line);
                String[] line_split = line.split(" ");
                if (line_split[0].equals("v")) {
                    Vec4 point = new Vec4(Double.parseDouble(line_split[1]), Double.parseDouble(line_split[2]), Double.parseDouble(line_split[3]), 1d);
                    points.add(point);
                } else if (line_split[0].equals("f")) {
                    int v1 = Integer.parseInt(line_split[1]) - 1;
                    int v2 = Integer.parseInt(line_split[2]) - 1;
                    int v3 = Integer.parseInt(line_split[3]) - 1;

                    Face face = new Face(points.get(v1), points.get(v2), points.get(v3));
                    faces.add(face);
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    public List<Face> getFaces() {
        return faces;
    }
}
