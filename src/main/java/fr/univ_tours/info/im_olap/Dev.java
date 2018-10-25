package fr.univ_tours.info.im_olap;

import fr.univ_tours.info.im_olap.graph.OGraph;
import fr.univ_tours.info.im_olap.model.LoadSessions;
import fr.univ_tours.info.im_olap.model.QueryPart;
import fr.univ_tours.info.im_olap.model.Session;
import fr.univ_tours.info.im_olap.model.SessionGraph;
import org.dom4j.*;


import java.util.List;

public class Dev {
    public static void main(String[] args) {
        List<Session> sessions = LoadSessions.loadFromDir("data/session_set_1");
        System.out.printf("Working on %d sessions%n", sessions.size());

        OGraph<Integer, QueryPart> base = SessionGraph.buildBaseGraph(sessions);
        System.out.println(base.nodeCount());
        SessionGraph.injectSchema(base, "data/schema.xml");
        System.out.println(base.nodeCount());


    }
}
