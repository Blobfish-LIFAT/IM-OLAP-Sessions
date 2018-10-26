package fr.univ_tours.info.im_olap;

import fr.univ_tours.info.im_olap.graph.Graphs;
import fr.univ_tours.info.im_olap.graph.OGraph;
import fr.univ_tours.info.im_olap.graph.PageRank;
import fr.univ_tours.info.im_olap.model.LoadSessions;
import fr.univ_tours.info.im_olap.model.QueryPart;
import fr.univ_tours.info.im_olap.model.Session;
import fr.univ_tours.info.im_olap.model.SessionGraph;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.*;

import static fr.univ_tours.info.im_olap.graph.PageRank.normalizeRowsi;
import static org.nd4j.linalg.util.MathUtils.log2;

public class IMRun {
    static String sessionsDir = "data/session_set_1", schemaPath = "data/schema.xml",
            userProfile = "Slice and Drill", evalSet = "data/session_set_2";
    static int userSize = 5;
    static double alpha = 0.5, epsilon = 0.05;

    public static void main(String[] args) {
        List<Session> sessions = LoadSessions.loadFromDir(sessionsDir);
        List<Session> user = new ArrayList<>();
        List<Session> learning = new ArrayList<>();

        int qota = userSize;
        for (Session session : sessions){
            if (session.getType().equals(userProfile) && qota > 0)
                user.add(session);
            else
                learning.add(session);
        }

        OGraph<Double, QueryPart> base = SessionGraph.buildTopologyGraph(learning, schemaPath);

        OGraph<Double, QueryPart> usage = SessionGraph.buildUsageGraph(base.getNodes(), user);

        usage.getNodes().forEach(base::addNode);
        base.getNodes().forEach(n -> base.setEdge(n,n,1.0));


        INDArray topology = Graphs.sortedINDMatrix(base);
        INDArray tp = Graphs.sortedINDMatrix(usage);


        INDArray uniform = Nd4j.ones(topology.shape());

        normalizeRowsi(topology);
        normalizeRowsi(tp);
        normalizeRowsi(uniform);

        // (1-e)*((1-a)*topo - a*tp) + e*uniform
        INDArray pr = topology.mul(1-alpha).add(tp.mul(alpha)).mul(1 - epsilon).add(uniform.mul(epsilon));

        INDArray pinf = PageRank.pageRank(pr, 42);

        List<Session> toEval = LoadSessions.loadFromDir(evalSet);

        TreeMap<QueryPart, Integer> querryMap = new TreeMap<>();
        List<QueryPart> baseParts = new ArrayList<>(base.getNodes());

        Collections.sort(baseParts);

        //baseParts.forEach(System.out::println);

        for (int i = 0; i < baseParts.size(); i++) {
            querryMap.putIfAbsent(baseParts.get(i), i);
        }

        for (Session session : toEval){
            List<QueryPart> parts = new ArrayList<>();
            session.queries.forEach(query -> parts.addAll(Arrays.asList(query.flat())));

            double sum = 0;

            for (QueryPart queryPart : parts) {
                sum += log2(pinf.getDouble(0, querryMap.get(queryPart)));
            }

            System.out.printf("Interestingness for '%s' is %s%n", session.getFilename(), String.valueOf(-sum/parts.size()));
        }
    }
}
