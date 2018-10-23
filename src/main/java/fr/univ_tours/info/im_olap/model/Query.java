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


}
