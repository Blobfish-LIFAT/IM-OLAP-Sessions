package fr.univ_tours.info.im_olap.model;

import java.util.HashSet;
import java.util.Set;

public class Query {
    Set<QueryPart> dimensions;
    Set<QueryPart> filters;
    Set<QueryPart> measures;

    Query(){
        dimensions = new HashSet<>();
        filters = new HashSet<>();
        measures = new HashSet<>();
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

    @Override
    public String toString() {
        return "Query {" +
                "\ndimensions=" + dimensions +
                "\nfilters=" + filters +
                "\nmeasures=" + measures +
                '}';
    }
}
