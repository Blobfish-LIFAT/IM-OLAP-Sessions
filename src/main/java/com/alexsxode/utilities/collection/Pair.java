package com.alexsxode.utilities.collection;

import fr.univ_tours.info.im_olap.graph.CPair;

import java.util.Objects;

public class Pair<L, R> {
    public final L left;
    public final R right;

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }

    public L getA(){
        return left;
    }

    public R getB(){
        return right;
    }


    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(getA(), pair.getA()) &&
                Objects.equals(getB(), pair.getB());
    }

    @Override
    public String toString() {
        return left.toString();
    }
}
