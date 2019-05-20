package fr.univ_tours.info.im_olap.model;


import java.util.*;

public class QueryPart implements Comparable<QueryPart>{

    private static TreeMap<Integer, List<QueryPart>> dimension_qps = new TreeMap<>();
    private static TreeMap<Integer, List<QueryPart>> measure_qps = new TreeMap<>();
    private static TreeMap<Integer, List<QueryPart>> filter_qps = new TreeMap<>();


    public static int hashCode(Type t, String value, String level) {
        return t.hashCode() * value.hashCode() * (level == null ? 1 : level.hashCode());
    }

    public static QueryPart newDimension(String value) {
        Type t = Type.DIMENSION;

        List<QueryPart> queryParts = dimension_qps.computeIfAbsent(hashCode(t, value, null), x -> {
            List<QueryPart> l = new ArrayList<>();
            l.add(new QueryPart(t, value, null));
            return l;
        });

        for (QueryPart queryPart : queryParts) {
            if (queryPart.value.equals(value)) {
                return queryPart;
            }
        }

        QueryPart queryPart = new QueryPart(t, value, null);

        queryParts.add(queryPart);

        return queryPart;
    }

    public static QueryPart newMeasure(String value) {
        Type t = Type.MEASURE;

        List<QueryPart> queryParts = measure_qps.computeIfAbsent(hashCode(t, value, null), x -> {
            List<QueryPart> l = new ArrayList<>();
            l.add(new QueryPart(t, value, null));
            return l;
        });

        for (QueryPart queryPart : queryParts) {
            if (queryPart.value.equals(value)) {
                return queryPart;
            }
        }

        QueryPart queryPart = new QueryPart(t, value, null);

        queryParts.add(queryPart);

        return queryPart;
    }

    public static QueryPart newFilter(String value, String level) {
        Type t = Type.FILTER;

        List<QueryPart> queryParts = filter_qps.computeIfAbsent(hashCode(t, value, level), x -> {
            List<QueryPart> l = new ArrayList<>();
            l.add(new QueryPart(t, value, level));
            return l;
        });

        for (QueryPart queryPart : queryParts) {
            if (queryPart.value.equals(value) && queryPart.level.equals(level)) {
                return queryPart;
            }
        }

        QueryPart queryPart = new QueryPart(t, value, level);

        queryParts.add(queryPart);

        return queryPart;
    }

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

    private QueryPart(Type t, String value, String level) {
        this.t = t;
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
                ", '" + this.contentDisplay() + '\'' +
                '}';
    }

    public String contentDisplay(){
        if (t == Type.FILTER)
            return level + "=" + value;
        else
            return value;
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
        return hashCode(this.t, this.value, this.level);
    }
}


