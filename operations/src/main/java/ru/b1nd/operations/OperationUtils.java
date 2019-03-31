package ru.b1nd.operations;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import ru.b1nd.operations.model.binary.AddOperation;
import ru.b1nd.operations.model.binary.DivideOperation;
import ru.b1nd.operations.model.binary.MultiplyOperation;
import ru.b1nd.operations.model.binary.SubtractOperation;
import ru.b1nd.operations.model.UploadOperation;

import java.util.Arrays;
import java.util.stream.Collectors;

public class OperationUtils {

    public enum OperationType {
        UPLOAD(UploadOperation.class),
        ADD(AddOperation.class),
        SUBTRACT(SubtractOperation.class),
        MULTIPLY(MultiplyOperation.class),
        DIVIDE(DivideOperation.class)
        ;

        private Class operation;

        OperationType(Class operation) {
            this.operation = operation;
        }

        public Class getOperation() {
            return operation;
        }
    }

    private static final BiMap<String, Class> nameTypeMap;
    private static final BiMap<Class, String> typeNameMap;

    static {
        nameTypeMap = ImmutableBiMap.copyOf(Arrays.stream(OperationType.values())
                .collect(Collectors.toMap(OperationType::name, OperationType::getOperation)));
        typeNameMap = nameTypeMap.inverse();
    }

    private OperationUtils() {
    }

    public static String getNameByType(Class type) {
        return typeNameMap.get(type);
    }

    public static Class getTypeByName(String name) {
        return nameTypeMap.get(name);
    }

}
