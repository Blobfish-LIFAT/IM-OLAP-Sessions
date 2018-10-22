package com.alexsxode.utilities.collection;

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
    public String toString() {
        return left.toString();
    }
}
