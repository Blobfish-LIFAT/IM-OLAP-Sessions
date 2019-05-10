package fr.univ_tours.info.im_olap.model;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

public class QueryPart implements Comparable<QueryPart>{
    public enum Type {
        DIMENSION, FILTER, MEASURE
    }

    static final HashMap<Type, String> display = new HashMap<>();

    static {
        display.put(Type.DIMENSION, "Dimension");
        display.put(Type.FILTER, "Filter");
        display.put(Type.MEASURE, "Measure");
    }


    /* Instance variables */
    Type t;
    String value, level;

    public QueryPart(Type t, String value) {
        this.t = t;
        this.value = value;
    }

    public QueryPart(String level, String value) {
        this.t = Type.FILTER;
        this.value = value;
        this.level = level;
    }

    public Optional<String> getHierarchy(){
        if (t == Type.MEASURE)
            return Optional.empty();
        return Optional.of(value.split("\\.")[0]);
    }

    public boolean isFilter(){
        return t == Type.FILTER;
    }

    public boolean isMeasure() { return t == Type.MEASURE;}

    public boolean isDimension() {return t == Type.DIMENSION;}

    @Override
    public String toString() {
        return "QueryPart{" +
                "" + display.get(t) +
                ", '" + value + '\'' +
                '}';
    }

    //FIXME This can be costly when dealing with filters
    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass())
            return false;

        QueryPart other = (QueryPart) obj;
        if (this.t == other.t){
            if (this.t == Type.FILTER) {
                return other.value.equals(this.value) && other.level.equals(this.level);
            } else
                return other.value.equals(this.value);
        } else
            return false;

    }

    @Override
    public int compareTo(QueryPart o) {
        if (this.equals(o))
            return 0;
        else
            return this.value.compareTo(o.value);
    }


    @Override
    public int hashCode() {
        return t.hashCode() * value.hashCode();
    }
}


