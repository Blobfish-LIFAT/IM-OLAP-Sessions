package fr.univ_tours.info.im_olap.graph;

import java.util.Objects;

public class CPair<A extends Comparable<A>, B extends Comparable<B>> implements Comparable<CPair<A,B>> {
    public final A a;
    public final B b;

    public CPair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }


    @Override
    public String toString() {
        return "CPair{" +
                "a=" + a +
                ", b=" + b +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CPair<?, ?> cPair = (CPair<?, ?>) o;
        return Objects.equals(a, cPair.a) &&
                Objects.equals(b, cPair.b);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b);
    }

    @Override
    public int compareTo(CPair<A, B> o) {
        int comp = a.compareTo(o.a);
        if (comp != 0){
            return comp;
        }
        else{
            return b.compareTo(o.b);
        }

    }
}
