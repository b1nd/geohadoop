package ru.b1nd.namenode.domain;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Objects;

@Entity
public class Partition extends IDGeneratedValue {

    @ManyToOne
    private File file;

    @ManyToOne
    private Node node;

    private Integer w;
    private Integer h;

    public Partition() { }

    public Partition(File file, Node node, Integer w, Integer h) {
        this.file = file;
        this.node = node;
        this.w = w;
        this.h = h;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Partition)) return false;
        Partition partition = (Partition) o;
        return Objects.equals(file, partition.file) &&
                Objects.equals(node, partition.node) &&
                Objects.equals(w, partition.w) &&
                Objects.equals(h, partition.h);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, node, w, h);
    }

    @Override
    public String toString() {
        return "Partition{" +
                "file=" + file +
                ", node=" + node +
                ", w=" + w +
                ", h=" + h +
                '}';
    }
}
