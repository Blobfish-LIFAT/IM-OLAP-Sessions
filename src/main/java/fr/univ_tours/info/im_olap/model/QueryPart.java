package fr.univ_tours.info.im_olap.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.nio.ByteBuffer;
import java.util.*;

public class QueryPart implements Comparable<QueryPart>{

    private static TreeMap<Integer, List<QueryPart>> dimension_qps = new TreeMap<>();
    private static TreeMap<Integer, List<QueryPart>> measure_qps = new TreeMap<>();
    private static TreeMap<Integer, List<QueryPart>> filter_qps = new TreeMap<>();

    public enum Type {
        DIMENSION(0), FILTER(1), MEASURE(2);
        private int value;
        private static Map map = new HashMap<>();

        private Type(int value) {
            this.value = value;
        }

        static {
            for (Type pageType : Type.values()) {
                map.put(pageType.value, pageType);
            }
        }

        public static Type valueOf(int pageType) {
            return (Type) map.get(pageType);
        }

        public int getValue() {
            return value;
        }

        public byte[] getBytes(){
            ByteBuffer b = ByteBuffer.allocate(4);
            b.putInt(value);
            return b.array();
        }
    }

    private static final HashMap<Type, String> display = new HashMap<>();

    static {
        display.put(Type.DIMENSION, "Dimension");
        display.put(Type.FILTER, "Filter");
        display.put(Type.MEASURE, "Measure");
    }


    /* Instance variables */
    @Expose
    @SerializedName("type")
    Type t;
    @Expose
    @SerializedName("value")
    String value;


    public static QueryPart newDimension(String value) {
        return getQueryPart(value, Type.DIMENSION, dimension_qps);
    }


    public static QueryPart newMeasure(String value) {
        return getQueryPart(value, Type.MEASURE, measure_qps);
    }

    public static QueryPart newFilter(String value) {
        return getQueryPart(value, Type.FILTER, filter_qps);
    }

    public static QueryPart newFilter(String value, String level) {
        return getQueryPart(value, Type.FILTER, filter_qps);
    }

    private QueryPart(){

    }

    private static QueryPart build(Type t, String value){
        QueryPart qp = new QueryPart();
        qp.t = t;
        qp.value = value;
        return qp;
    }


    private static QueryPart getQueryPart(String value, Type t, TreeMap<Integer, List<QueryPart>> dimension_qps) {
        List<QueryPart> queryParts = dimension_qps.computeIfAbsent(Objects.hash(t, value), x -> {
            List<QueryPart> l = new ArrayList<>();
            l.add(build(t, value));
            return l;
        });

        for (QueryPart queryPart : queryParts) {
            if (queryPart.value.equals(value)) {
                return queryPart;
            }
        }

        QueryPart queryPart = build(t, value);

        queryParts.add(queryPart);

        return queryPart;
    }

    public Optional<String> getHierarchy(){
        if (t == Type.MEASURE)
            return Optional.empty();
        return Optional.of(value.split("\\.")[0]);
    }

    public void debugDumpMaps(){
        System.out.println("--- Dimensions ---");
        System.out.println(dimension_qps.values());
        System.out.println("--- Filters ---");
        System.out.println(filter_qps.values());
        System.out.println("--- Measures ---");
        System.out.println(measure_qps.values());
    }

    public boolean isFilter(){ return t == Type.FILTER;}

    public boolean isMeasure() { return t == Type.MEASURE;}

    public boolean isDimension() {return t == Type.DIMENSION;}

    public Type getType() {
        return t;
    }

    public String getValue() {
        return value;
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

        QueryPart other = (QueryPart) obj;
        if (this.t == other.t){
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
        return Objects.hash(this.t, this.value);
    }
}


