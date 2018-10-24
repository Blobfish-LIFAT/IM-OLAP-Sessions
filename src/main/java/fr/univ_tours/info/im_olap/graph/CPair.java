package fr.univ_tours.info.im_olap.graph;

import com.alexsxode.utilities.collection.Pair;

import java.util.Objects;

public class CPair<A extends Comparable<A>, B extends Comparable<B>> extends Pair<A,B> implements Comparable<CPair<A,B>> {

    public CPair(A a, B b) {
        super(a,b);
    }

    @Override
    public String toString() {
        return "CPair{" +
                "a=" + getA() +
                ", b=" + getB() +
                '}';
    }

    @Override
    public int compareTo(CPair<A, B> o) {
        int comp = getA().compareTo(o.getA());
        if (comp != 0){
            return comp;
        }
        else{
            return getB().compareTo(o.getB());
        }

    }
}
