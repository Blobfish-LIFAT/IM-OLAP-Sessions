package fr.univ_tours.info.im_olap.model;

import fr.univ_tours.info.im_olap.graph.OGraph;

import java.util.List;

public class SessionGraph {
    public static OGraph<Integer, QueryPart> buildBaseGraph(List<Session> sessions){
        OGraph<Integer, QueryPart> result = new OGraph<>();
        for (Session session : sessions){
            for (int i = 0; i < session.length(); i++) {

                Query q1 = session.queries.get(i);
                QueryPart[] q1parts = q1.flat();

                for (int j = 0; j < q1parts.length; j++) {
                    for (int k = j + 1; k < q1parts.length; k++) {
                        //TODO add symmetric edge here
                    }
                }

                if (i < session.length() - 1){
                    Query q2 = session.queries.get(i + 1);
                    QueryPart[] q2parts = q2.flat();
                    for (int j = 0; j < q1parts.length; j++) {
                        for (int k = 0; k < q2parts.length; k++) {
                            //TODO add edges between q1 and q2
                        }
                    }
                }
            }
        }
        return result;
    }

    public static OGraph<Integer, QueryPart> buildUsageGraph(){
        return null;
        //TODO find how to build graph with same nodes, careful of matrix conversion
    }
}
