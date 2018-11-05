package fr.univ_tours.info.im_olap;

import fr.univ_tours.info.im_olap.graph.Graphs;
import fr.univ_tours.info.im_olap.graph.OGraph;
import fr.univ_tours.info.im_olap.graph.PageRank;
import fr.univ_tours.info.im_olap.model.*;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;


import java.util.Arrays;
import java.util.List;

import static fr.univ_tours.info.im_olap.graph.PageRank.normalizeRowsi;

public class Dev {
    public static void main(String[] args) {
        List<Session> sessions = LoadSessions.loadFromDir("data/session_set_1");
        System.out.printf("Working on %d sessions%n", sessions.size());

        for (Session s : sessions){
            int n = 0;
            for (Query q : s.queries)
                n += q.flat().length;
            System.out.printf("%s;%s%n", s.getType(), n);
        }

        OGraph<Double, QueryPart> base = SessionGraph.buildTopologyGraph(sessions.subList(0,39), "data/schema.xml");

        OGraph<Double, QueryPart> usage = SessionGraph.buildUsageGraph(base.getNodes(), sessions.subList(40,50));

        usage.getNodes().forEach(base::addNode);
        base.getNodes().forEach(n -> base.setEdge(n,n,1.0));


        INDArray topology = Graphs.sortedINDMatrix(base);
        INDArray tp = Graphs.sortedINDMatrix(usage);

        double alpha = 0.5; double epsilon = 0.05;

        INDArray uniform = Nd4j.ones(topology.shape());

        normalizeRowsi(topology);
        normalizeRowsi(tp);
        normalizeRowsi(uniform);

        // (1-e)*((1-a)*topo - a*tp) + e*uniform
        INDArray pr = topology.mul(1-alpha).add(tp.mul(alpha)).mul(1 - epsilon).add(uniform.mul(epsilon));

        INDArray pinf = PageRank.pageRank(pr, 42);


    }
}
