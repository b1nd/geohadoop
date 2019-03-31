package ru.b1nd.operations.model;

import java.util.Objects;

public class BinaryOperation {
    protected String left;
    protected String right;
    protected Integer w;
    protected Integer h;
    protected String file;

    public BinaryOperation() { }

    public BinaryOperation(String left, String right, Integer w, Integer h, String file) {
        this.left = left;
        this.right = right;
        this.w = w;
        this.h = h;
        this.file = file;
    }

    public String getLeft() {
        return left;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public String getRight() {
        return right;
    }

    public void setRight(String right) {
        this.right = right;
    }

    public Integer getW() {
        return w;
    }

    public void setW(Integer w) {
        this.w = w;
    }

    public Integer getH() {
        return h;
    }

    public void setH(Integer h) {
        this.h = h;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BinaryOperation)) return false;
        BinaryOperation that = (BinaryOperation) o;
        return Objects.equals(left, that.left) &&
                Objects.equals(right, that.right) &&
                Objects.equals(w, that.w) &&
                Objects.equals(h, that.h) &&
                Objects.equals(file, that.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right, w, h, file);
    }

    @Override
    public String toString() {
        return "BinaryOperation{" +
                "left='" + left + '\'' +
                ", right='" + right + '\'' +
                ", w=" + w +
                ", h=" + h +
                ", file='" + file + '\'' +
                '}';
    }
}
