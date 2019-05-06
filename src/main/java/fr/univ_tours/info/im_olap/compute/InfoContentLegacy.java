package fr.univ_tours.info.im_olap.compute;

import fr.univ_tours.info.im_olap.graph.Graphs;
import fr.univ_tours.info.im_olap.graph.OGraph;
import fr.univ_tours.info.im_olap.model.*;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;

import static fr.univ_tours.info.im_olap.compute.PageRank.normalizeRowsi;
import static org.nd4j.linalg.util.MathUtils.log2;

public class InfoContentLegacy {
    static String explo = "Explorative", sliceDrill = "Slice and Drill", goal = "Goal Oriented", sliceAll = "Slice All";
    static String[] explos = new String[]{explo, sliceDrill, goal, sliceAll};
    static String sessionsDir = "data/session_set_2", schemaPath = "data/schema.xml", userProfile = goal;
    static int baseSize = 400, userSize = 10;
    static double alpha = 0.7, epsilon = 0.005;

    static DecimalFormat df = new DecimalFormat("#.##");

    static PrintWriter output = null;
    static String outputPath = "data/out/infoContent.csv";

    public static void main(String[] args) throws Exception{
        BufferedWriter out = Files.newBufferedWriter(Paths.get(outputPath));
        output = new PrintWriter(out);

        //System.out.println("userProfile;evalProfile;alpha;IM");
        output.println("userProfile;evalProfile;alpha;position;info");

            for (int j = 0; j < explos.length; j++) {
                userProfile = explos[j];
                for (int l = 0; l < explos.length; l++) {


                        runTest(explos[l]);

                }
            }


        output.close();
        out.close();
    }

    private static void runTest(String evalProfile) {
        List<Session> sessions = LoadSessionsLegacy.loadFromDir(sessionsDir);
        Collections.shuffle(sessions);

        List<Session> user = new ArrayList<>();
        List<Session> learning = new ArrayList<>();
        List<Session> eval = new ArrayList<>();

        int qota = userSize, qota2 = sessions.size() - userSize - baseSize;
        for (Session session : sessions){
            if (session.getType().equals(evalProfile) && qota2 > 0) {
                eval.add(session);
                qota2--;
            }else if (session.getType().equals(userProfile) && qota > 0) {
                user.add(session);
                qota--;
            }
            else
                learning.add(session);
        }

        //System.out.printf("sessions=%d, user=%d, learning=%d, eval=%d%n", sessions.size(), user.size(), learning.size(), eval.size());

        learning.addAll(eval);

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
        //INDArray pr = topology.mul(1-alpha).add(tp.mul(alpha)).mul(1 - epsilon).add(uniform.mul(epsilon));
        INDArray pr = topology.mul(1-alpha).add(tp.mul(alpha));
        INDArray pinf = PageRank.pageRank(pr, 42);





        TreeMap<QueryPart, Integer> querryMap = new TreeMap<>();
        List<QueryPart> baseParts = new ArrayList<>(base.getNodes());

        Collections.sort(baseParts);

        //baseParts.forEach(System.out::println);

        for (int i = 0; i < baseParts.size(); i++) {
            querryMap.putIfAbsent(baseParts.get(i), i);
        }

        for (Session session : eval){
            int pos = 0;
            for (Query query : session.queries) {

                double sum = 0;

                for (QueryPart queryPart : query.flat()) {
                    try {
                        sum += log2(pinf.getDouble(0, querryMap.get(queryPart)));
                    } catch (NullPointerException e) {
                        System.err.println(queryPart);
                    }
                }

                output.printf("%s;%s;%s;%d;%s%n", userProfile, evalProfile, String.valueOf(alpha), pos, String.valueOf(-sum));

                pos++;
            }
            output.flush();
        }

    }

}
