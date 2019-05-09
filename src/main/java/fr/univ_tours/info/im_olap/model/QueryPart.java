package fr.univ_tours.info.im_olap.model;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.BiFunction;

public class QueryPart implements Comparable<QueryPart>{
    public enum Type {
        DIMENSION, FILTER, MEASURE
    }

    static final HashMap<Type, String> display = new HashMap<>();
    private static Gson gson;

    static {
        display.put(Type.DIMENSION, "Dimension");
        display.put(Type.FILTER, "Filter");
        display.put(Type.MEASURE, "Measure");
        gson = new Gson();
    }

    /* Matcher for JSON Filters */
    public static final BiFunction<QueryPart, QueryPart, Boolean> strict = new BiFunction<QueryPart, QueryPart, Boolean>() {
        @Override
        public Boolean apply(QueryPart queryPart, QueryPart queryPart2) {
            Filter f1 = gson.fromJson(queryPart.value, Filter.class);
            Filter f2 = gson.fromJson(queryPart2.value, Filter.class);

            return f1.level.equals(f2.level) && f1.value.equals(f2.value);
        }
    };

    public static final BiFunction<QueryPart, QueryPart, Boolean> levelOnly = new BiFunction<QueryPart, QueryPart, Boolean>() {
        @Override
        public Boolean apply(QueryPart queryPart, QueryPart queryPart2) {
            Filter f1 = gson.fromJson(queryPart.value, Filter.class);
            Filter f2 = gson.fromJson(queryPart2.value, Filter.class);

            return f1.level.equals(f2.level);
        }
    };

    private static BiFunction<QueryPart, QueryPart, Boolean> filterMatchPolicy = levelOnly;

    /* Instance variables */
    Type t;
    String value;
    boolean filterIsJson = false;

    public QueryPart(Type t, String value) {
        this.t = t;
        this.value = value;
    }

    public QueryPart(Type t, String value, boolean json) {
        this.t = t;
        this.value = value;
        filterIsJson = json;
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

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass())
            return false;
        if (this.t == ((QueryPart)obj).t){
            if (this.t == Type.FILTER && this.isFilterIsJson() && ((QueryPart)obj).isFilterIsJson())
                return filterMatchPolicy.apply(this, (QueryPart) obj);
            else
                return ((QueryPart)obj).value.equals(this.value);
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

    boolean isFilterIsJson(){
        return filterIsJson;
    }

    public void setFilterJson() {
        filterIsJson = true;
    }

    @Override
    public int hashCode() {
        return t.hashCode() * value.hashCode();
    }
}
class Filter{
    String level;
    String value;

    public Filter(){}

    public Filter(String level, String value) {
        this.level = level;
        this.value = value;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
