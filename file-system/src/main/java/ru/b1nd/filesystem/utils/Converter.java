package ru.b1nd.filesystem.utils;

public final class Converter {

    private Converter() {
    }

    public static String partName(int w, int h) {
        return "w" + w + "h" + h + ".tif";
    }
}
