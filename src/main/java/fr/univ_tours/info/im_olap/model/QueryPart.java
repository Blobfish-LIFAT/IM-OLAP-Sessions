package fr.univ_tours.info.im_olap.model;

import java.util.HashMap;

public class QueryPart {
    enum Type {
        DIMENSION, FILTER, MEASURE
    }

    static final HashMap<Type, String> display = new HashMap<>();

    static {
        display.put(Type.DIMENSION, "Dimension");
        display.put(Type.FILTER, "Filter");
        display.put(Type.MEASURE, "Measure");
    }

    Type t;
    String value;

    public QueryPart(Type t, String value) {
        this.t = t;
        this.value = value;
    }

    @Override
    public String toString() {
        return "QueryPart{" +
                "" + display.get(t) +
                ", '" + value + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass())
            return false;
        return this.t == ((QueryPart)obj).t && ((QueryPart)obj).value.equals(this.value);
    }
}
