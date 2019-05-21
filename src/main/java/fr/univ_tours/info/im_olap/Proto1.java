package fr.univ_tours.info.im_olap;

import com.alexsxode.utilities.collection.Pair;
import com.google.common.graph.MutableValueGraph;
import fr.univ_tours.info.im_olap.compute.PageRank;
import fr.univ_tours.info.im_olap.data.DopanLoader;
import fr.univ_tours.info.im_olap.graph.Graphs;
import fr.univ_tours.info.im_olap.model.*;
import fr.univ_tours.info.im_olap.mondrian.CubeUtils;
import fr.univ_tours.info.im_olap.mondrian.MondrianConfig;
import mondrian.olap.Connection;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.eigen.Eigen;
import org.nd4j.linalg.factory.Nd4j;


import java.util.*;
import java.util.stream.Collectors;

public class Proto1 {
    // I know you don't like static variables but it's easier for now since type are not set yet
    // just use function arguments
    static String dataDir = "data/logs/dopan_converted";
    private static String cubeSchema = "data/cubeSchemas/DOPAN_DW3.xml";


    public static void main(String[] args) {
        //Nd4j.setDefaultDataTypes(DataType.DOUBLE, DataType.DOUBLE);

        System.out.println("Connecting to Mondrian...");
        Connection olap = MondrianConfig.getMondrianConnection();

        System.out.println("Loading sessions...");
        List<Session> sessions = SessionGraph.fixSessions(DopanLoader.loadDir(dataDir), cubeSchema);
        Session s1 = sessions.get(0);

        System.out.println("Creating cube utils...");
        CubeUtils mdUtils = new CubeUtils(olap, s1.getCubeName());


        System.out.println("Collecting user session...");
        List<Session> thisUser = sessions.stream().filter(s -> s.getUserName().equals(s1.getUserName())).collect(Collectors.toList());
        thisUser.remove(s1);
        sessions.removeAll(thisUser);


        System.out.println("Building topology graph...");
        MutableValueGraph<QueryPart, Double> base = SessionGraph.buildFromLog(sessions);
        Set<QueryPart> baseNodes = new HashSet<>(base.nodes());
        DimensionsGraph.injectSchema(base, "data/cubeSchemas/DOPAN_DW3.xml");

        System.out.println("Injecting filters...");
        FiltersGraph.injectCompressedFilters(base, mdUtils);

        (new HashSet<>(base.nodes())).stream().filter(n -> !baseNodes.contains(n)).forEach(base::removeNode);

        System.out.printf("Schema graph size is %s nodes and %s edges.%n", base.nodes().size(), base.edges().size());
/*
        SparseStore demo = toMatrixNorm(base);
        System.out.println("Matrix filled");

        for (int i = 0; i < 42; i++) {
            System.out.printf("Matrix multiplication iteration n°%s%n", i);
            demo.multiply(demo, demo);
        }
*/
        /**
         * Ben's stuff
         */

        System.out.println("Adding session query parts...");

        for (Query query : s1.queries) {
            for (QueryPart qp : query.getAllParts()) {
                base.addNode(qp);
            }
        }

        System.out.println("Ensuring all nodes have self edges...");

        for (QueryPart queryPart : base.nodes()) {
            base.putEdgeValue(queryPart, queryPart, 1.0);
        }

        System.out.printf("Graph size is %s nodes and %s edges.%n", base.nodes().size(), base.edges().size());

        System.out.println("Normalizing edges weights...");

        Graphs.normalizeWeightsi(base);

        System.out.println("Converting graph to matrix...");

        Pair<INDArray, HashMap<QueryPart, Integer>> pair = Graphs.toINDMatrix(base);

        System.out.println("Matrix shape:");
        System.out.println(Arrays.toString(pair.left.shape()));


        System.out.println("Checking matrix validity...");

        System.out.println("Sum on first row: "+pair.left.getRow(0).sumNumber());

        System.out.println("Sum on last row: "+pair.left.getRow(pair.left.rows()-1).sumNumber());

        for (int i = 1; i < pair.left.rows() - 1 ; i++) {
            System.out.print(pair.left.getRow(i).sumNumber()+", ");
        }

        Pair<INDArray, HashMap<QueryPart, Integer>> pgRes = PageRank.pagerank(base, 500);

        System.out.println();

        System.out.print("Sum of the stationary distribution: ");
        System.out.println(pgRes.left.sumNumber());

        System.out.println();

        System.out.println("Computing Eigen...");
        System.out.println(Eigen.symmetricGeneralizedEigenvalues(pair.left));


        System.out.println("Dereferencing INDArray...");
        //Nd4j.getMemoryManager().collect(pair.left);
        pair = null;

        System.out.println("Creating SessionEvaluator evaluator...");

        SessionEvaluator<QueryPart, Double, Pair<INDArray, HashMap<QueryPart, Integer>>> sessionEvaluator =
                new SessionEvaluator<>(SessionEvaluator::simpleInterconnections,
                                        SessionEvaluator::replaceEdges,
                                        SessionEvaluator::pageRank);


        System.out.println("Evaluating session...");
        ArrayList<Pair<Query, Pair<INDArray, HashMap<QueryPart, Integer>>>> liste = sessionEvaluator.evaluateSession(base, s1);

        int i = 0;
        for (Pair<Query, Pair<INDArray, HashMap<QueryPart, Integer>>> p : liste ) {
            System.out.println();
            System.out.println("Query number "+i);

            /*
            ArrayList<Pair<QueryPart, Double>> displayList = new ArrayList<>();

            for (Map.Entry<QueryPart, Integer> entry : p.right.right.entrySet()) {
                displayList.add(new Pair<>(entry.getKey(), p.right.left.getDouble(entry.getValue())));
            }

            displayList.sort(Comparator.comparing(x -> x.left.toString()));

            for (Pair<QueryPart, Double> pair1 : displayList) {
                System.out.print(pair1.right.toString() + ",");
            }
            System.out.println();

            */


            i++;
        }

        // helps to infer evaluator
        SessionEvaluator.GainEvaluator<Pair<INDArray, HashMap<QueryPart, Integer>>> kullbackLeibler = SessionEvaluator::KullbackLeibler;

        System.out.println();
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println();

        System.out.println("Computing gains...");
        System.out.println();

        ArrayList<Pair<Query, Double>> gains = SessionEvaluator.computeGains(liste, kullbackLeibler);

        for (Pair<Query, Double> pair1 : gains) {

            System.out.println();
            System.out.println("Query:");
            System.out.println(pair1.left);
            System.out.println("Gain: "+pair1.right);
        }

        // helps to infer evaluator
        SessionEvaluator.GainEvaluator<Pair<INDArray, HashMap<QueryPart, Integer>>> absoluteDiff = SessionEvaluator::AbsoluteDiff;

        System.out.println();
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println();

        System.out.println("Computing gains in absolute diff...");
        System.out.println();

        gains = SessionEvaluator.computeGains(liste, absoluteDiff);

        for (Pair<Query, Double> pair1 : gains) {

            System.out.println();
            System.out.println("Query:");
            System.out.println(pair1.left);
            System.out.println("Gain: "+pair1.right);
        }

        System.out.println("End of evaluation.");
    }

/*
    public static <V> SparseStore<Double> toMatrixNorm(ValueGraph<V, ? extends Number> in) {
        List<V> nodes = new ArrayList<>(in.nodes());
        HashMap<V, Integer> indexes = new HashMap<>();

        for (int i = 0; i < nodes.size(); i++) {
            indexes.put(nodes.get(i), i);
        }

        int size = nodes.size();
        final SparseStore<Double> out = SparseStore.PRIMITIVE.make(size, size);

        for (int i = 0; i < size; i++) {
            V from = nodes.get(i);
            double sum = 0;
            for (V to : in.successors(from))
                sum += in.edgeValue(from, to).get().doubleValue();
            for (V to : in.successors(from)){
                int toIndex = indexes.get(to);
                out.set(i, toIndex, in.edgeValue(from, to).get().doubleValue()/sum);
            }
        }

        return out;
    }
*/

}

