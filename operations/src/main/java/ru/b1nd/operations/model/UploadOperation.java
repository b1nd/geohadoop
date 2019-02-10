package ru.b1nd.operations.model;

import java.util.Objects;

public class UploadOperation {
    private String file;
    private Integer w;
    private Integer h;
    private String from;

    public UploadOperation(String file, Integer w, Integer h, String from) {
        this.file = file;
        this.w = w;
        this.h = h;
        this.from = from;
    }

    public String getFile() {
        return file;
    }

    public Integer getW() {
        return w;
    }

    public Integer getH() {
        return h;
    }

    public String getFrom() {
        return from;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UploadOperation)) return false;
        UploadOperation that = (UploadOperation) o;
        return Objects.equals(file, that.file) &&
                Objects.equals(w, that.w) &&
                Objects.equals(h, that.h) &&
                Objects.equals(from, that.from);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, w, h, from);
    }

    @Override
    public String toString() {
        return "UploadOperation{" +
                "file='" + file + '\'' +
                ", w=" + w +
                ", h=" + h +
                ", from='" + from + '\'' +
                '}';
    }
}