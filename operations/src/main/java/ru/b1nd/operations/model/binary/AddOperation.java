package ru.b1nd.operations.model.binary;

import ru.b1nd.operations.model.BinaryOperation;

public class AddOperation extends BinaryOperation {
    public AddOperation() {
    }

    public AddOperation(String left, String right, Integer w, Integer h, String file) {
        super(left, right, w, h, file);
    }
}
