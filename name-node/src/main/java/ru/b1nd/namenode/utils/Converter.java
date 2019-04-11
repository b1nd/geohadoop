package ru.b1nd.namenode.utils;

import org.springframework.data.util.Pair;

import java.util.regex.Pattern;

public class Converter {

    private Converter() {
    }

    public static Pair<String, String> getHostPort(String hostPort) {
        var matcher = Pattern.compile("^(\\S+):(\\S+)$").matcher(hostPort);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Cannot get host and port from " + hostPort);
        }
        return Pair.of(matcher.group(1), matcher.group(2));
    }

    public static Pair<Integer, Integer> getWH(String name) {
        var matcher = Pattern.compile("w(\\d+)h(\\d+)\\.tif{1,2}$").matcher(name);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Cannot get w and h from " + name);
        }
        return Pair.of(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
    }
}
