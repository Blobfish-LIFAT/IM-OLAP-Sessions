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

import java.lang.reflect.Array;
import java.util.*;

import static fr.univ_tours.info.im_olap.graph.PageRank.normalizeRowsi;
import static org.nd4j.linalg.util.MathUtils.log2;

public class IMRun {
    static String explo = "Explorative", sliceDrill = "Slice and Drill", goal = "Goal Oriented", sliceAll = "Slice All";
    static String[] explos = new String[]{explo, sliceDrill, goal, sliceAll};

    static String sessionsDir = "data/session_set_1", schemaPath = "data/schema.xml",
            userProfile = explo, evalProfile = sliceDrill;
    static int baseSize = 40, userSize = 7;
    static double alpha = 0.5, epsilon = 0.05;

    public static void main(String[] args) {
        ArrayList<String> profiles = new ArrayList<>(Arrays.asList(explos));
        profiles.remove(userProfile);

        System.out.println("sessionFilename;userProfile;evalProfile;alpha;IM");
        for (String profile : profiles){
            evalProfile = profile;
            for (int i = 1; i < 10; i++) {
                alpha = i/10.0;
                runTest();
            }
        }

    }

    private static void runTest() {
        List<Session> sessions = LoadSessions.loadFromDir(sessionsDir);
        Collections.shuffle(sessions);

        List<Session> user = new ArrayList<>();
        List<Session> learning = new ArrayList<>();
        List<Session> eval = new ArrayList<>();

        int qota = userSize, qota2 = sessions.size() - userSize - baseSize;
        for (Session session : sessions){
            if (session.getType().equals(userProfile) && qota > 0) {
                user.add(session);
                qota--;
            } else if (session.getType().equals(evalProfile) && qota2 > 0) {
                eval.add(session);
                qota2--;
            } else
                learning.add(session);
        }

        //System.out.printf("sessions=%d, user=%d, learning=%d, eval=%d%n", sessions.size(), user.size(), learning.size(), eval.size());

        OGraph<Double, QueryPart> base = SessionGraph.buildTopologyGraph(learning, schemaPath);
        SessionGraph.injectCousins(base, sessions);

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


        TreeMap<QueryPart, Integer> querryMap = new TreeMap<>();
        List<QueryPart> baseParts = new ArrayList<>(base.getNodes());

        Collections.sort(baseParts);

        //baseParts.forEach(System.out::println);

        for (int i = 0; i < baseParts.size(); i++) {
            querryMap.putIfAbsent(baseParts.get(i), i);
        }

        for (Session session : eval){
            List<QueryPart> parts = new ArrayList<>();
            session.queries.forEach(query -> parts.addAll(Arrays.asList(query.flat())));

            double sum = 0;
            int size = parts.size();

            for (QueryPart queryPart : parts) {
                try {
                    sum += log2(pinf.getDouble(0, querryMap.get(queryPart)));
                }catch (NullPointerException e){
                    System.err.println(queryPart);
                    size--;
                }
            }

            System.out.printf("%s;%s;%s;%f;%s%n", session.getFilename(), userProfile, evalProfile, alpha, String.valueOf(-sum/size));
        }
    }
}
