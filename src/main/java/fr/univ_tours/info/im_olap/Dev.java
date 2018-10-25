package fr.univ_tours.info.im_olap;

import fr.univ_tours.info.im_olap.graph.OGraph;
import fr.univ_tours.info.im_olap.model.LoadSessions;
import fr.univ_tours.info.im_olap.model.QueryPart;
import fr.univ_tours.info.im_olap.model.Session;
import fr.univ_tours.info.im_olap.model.SessionGraph;


import java.util.List;

public class Dev {
    public static void main(String[] args) {
        List<Session> sessions = LoadSessions.loadFromDir("data/session_set_1");
        System.out.printf("Working on %d sessions%n", sessions.size());

        OGraph<Double, QueryPart> base = SessionGraph.buildBaseGraph(sessions.subList(0,39));
        System.out.printf("Node count in Base: %d%nEdges: %d %n", base.nodeCount(), base.edgeCount());
        SessionGraph.injectSchema(base, "data/schema.xml");
        System.out.printf("Node count in Topology (Base + Schema): %d %nEdges: %d %n", base.nodeCount(), base.edgeCount());
        OGraph<Double, QueryPart> usage = SessionGraph.buildUsageGraph(base.getNodes(), sessions.subList(40,50));
        System.out.printf("Node count in Usage: %d %nEdges: %d %n", usage.nodeCount(), usage.edgeCount());

        usage.getNodes().forEach(base::addNode);

        System.out.printf("Node count in Topology + nodes from Usage: %d %nEdges: %d %n", base.nodeCount(), base.edgeCount());



    }
}
