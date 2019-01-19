package ru.b1nd.operations.model;

import java.util.Objects;

public class UploadOperation {
    private String file;
    private String from;
    private String to;

    public UploadOperation(String file, String from, String to) {
        this.file = file;
        this.from = from;
        this.to = to;
    }

    public UploadOperation() {
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UploadOperation that = (UploadOperation) o;
        return Objects.equals(file, that.file) &&
                Objects.equals(from, that.from) &&
                Objects.equals(to, that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, from, to);
    }

    @Override
    public String toString() {
        return "UploadOperation{" +
                "file='" + file + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                '}';
    }
}