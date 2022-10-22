package com.code.mesh_visualizer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validators {

    static public boolean isDouble(String value) {
        Pattern pattern = Pattern.compile("[0-9]+(.[0-9]+|)");
        Matcher matcher = pattern.matcher(value);
        return matcher.find();
    }
}
