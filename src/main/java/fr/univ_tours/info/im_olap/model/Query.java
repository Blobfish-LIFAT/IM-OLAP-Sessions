package fr.univ_tours.info.im_olap.model;

import java.util.*;

public class Query {
    Set<QueryPart> dimensions;
    Set<QueryPart> filters;
    Set<QueryPart> measures;

    public Query(){
        dimensions = new HashSet<>();
        filters = new HashSet<>();
        measures = new HashSet<>();
    }

    public Query(Collection<QueryPart> parts){
        this();
        this.addAll(parts);
    }

    public QueryPart[] flat(){
        QueryPart[] result = new QueryPart[dimensions.size()+ filters.size()+measures.size()];
        int i = 0;
        for (QueryPart p : dimensions){
            result[i++] = p;
        }for (QueryPart p : filters){
            result[i++] = p;
        }for (QueryPart p : measures){
            result[i++] = p;
        }
        return result;
    }

    public HashSet<QueryPart> getAllParts() {
        HashSet<QueryPart> allParts = new HashSet<>(this.getDimensions());
        allParts.addAll(this.getFilters());
        allParts.addAll(this.getMeasures());
        return allParts;
    }

    public Set<QueryPart> getDimensions() {
        return dimensions;
    }

    public void setDimensions(Set<QueryPart> dimensions) {
        this.dimensions = dimensions;
    }

    public Set<QueryPart> getFilters() {
        return filters;
    }

    public void setFilters(Set<QueryPart> filters) {
        this.filters = filters;
    }

    public Set<QueryPart> getMeasures() {
        return measures;
    }

    public void setMeasures(Set<QueryPart> measures) {
        this.measures = measures;
    }

    @Override
    public String toString() {
        return "Query {" +
                "\ndimensions=" + dimensions +
                "\nfilters=" + filters +
                "\nmeasures=" + measures +
                '}';
    }

    public void addAll(Collection<QueryPart> parts){
        parts.forEach(queryPart -> {
            switch (queryPart.t){
                case DIMENSION:
                    this.dimensions.add(queryPart);
                    break;
                case FILTER:
                    this.filters.add(queryPart);
                    break;
                case MEASURE:
                    this.measures.add(queryPart);
                    break;
            }
        });
    }
}
