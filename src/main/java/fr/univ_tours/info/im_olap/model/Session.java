package fr.univ_tours.info.im_olap.model;

import java.util.List;

public class Session {
    public List<Query> queries;
    String type;

    public Session(List<Query> queries, String type) {
        this.queries = queries;
        this.type = type;
    }
}
