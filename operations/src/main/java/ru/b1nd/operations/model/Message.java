package ru.b1nd.operations.model;

import ru.b1nd.operations.OperationUtils;

import java.util.Objects;

public class Message<T> {
    private String name;
    private T body;

    public Message(T body) {
        this.name = OperationUtils.getNameByType(body.getClass());
        this.body = body;
    }

    public String getName() {
        return name;
    }

    public T getBody() {
        return body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;
        Message<?> message = (Message<?>) o;
        return Objects.equals(name, message.name) &&
                Objects.equals(body, message.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, body);
    }

    @Override
    public String toString() {
        return "Message{" +
                "name='" + name + '\'' +
                ", body=" + body +
                '}';
    }
}
