package fr.univ_tours.info.im_olap.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Session {
    public List<Query> queries;
    String type;
    String filename; //this is needed to keep consistent ordering of sessions accross systems

    public Session(List<Query> queries, String type, String filename) {
        this.queries = queries;
        this.type = type;
        this.filename = filename;
    }

    public int length() {
        return queries.size();
    }

    public String getType() {
        return type;
    }

    public String getFilename() {
        return filename;
    }

    public List<QueryPart> allParts(){
        List<QueryPart> parts = new ArrayList<>();
        for (Query q : queries){
            parts.addAll(Arrays.asList(q.flat()));
        }
        return parts;
    }
}
