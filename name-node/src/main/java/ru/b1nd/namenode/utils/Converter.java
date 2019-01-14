package ru.b1nd.namenode.utils;

import ru.b1nd.namenode.domain.Node;

public class Converter {

    private Converter() {
    }

    public static String getHostPortByNode(Node node) {
        return node.getHost() + ":" + node.getPort();
    }
}
