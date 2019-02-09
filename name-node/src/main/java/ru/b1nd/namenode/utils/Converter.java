package ru.b1nd.namenode.utils;

import ru.b1nd.namenode.domain.Node;

import java.util.regex.Pattern;

public class Converter {

    private Converter() {
    }

    public static Node getNodeByHostPort(String hostPort) {
        var matcher = Pattern.compile("^(\\S+):(\\S+)$").matcher(hostPort);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Cannot get node from " + hostPort);
        }
        return new Node(matcher.group(1), matcher.group(2));
    }
}
