package fr.univ_tours.info.im_olap;

import com.alexsxode.utilities.collection.Pair;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import fr.univ_tours.info.im_olap.data.DopanLoader;
import fr.univ_tours.info.im_olap.graph.Graphs;
import fr.univ_tours.info.im_olap.mondrian.MondrianConfig;
import fr.univ_tours.info.im_olap.mondrian.CubeUtils;

import fr.univ_tours.info.im_olap.model.Query;
import fr.univ_tours.info.im_olap.model.QueryPart;
import mondrian.olap.*;
import fr.univ_tours.info.im_olap.model.*;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.NDArrayFactory;
import org.nd4j.linalg.factory.Nd4j;
import java.util.*;
import java.util.stream.Collectors;

public class Proto1 {
    // I know you don't like static variables but it's easier for now since type are not set yet
    // just use function arguments
    static String dataDir = "data/logs/dopan_converted";
    private static String cubeSchema = "data/cubeSchemas/DOPAN_DW3.xml";


    public static void main(String[] args) {

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
        DimensionsGraph.injectSchema(base, "data/cubeSchemas/DOPAN_DW3.xml");

        System.out.println("Injecting filters...");
        FiltersGraph.injectCompressedFilters(base, mdUtils);

        System.out.printf("Schema graph size is %s nodes and %s edges.%n", base.nodes().size(), base.edges().size());

        //INDArray test = Graphs.toINDMatrix(base).left;



        //System.exit(0);
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


        //System.out.println(Eigen.symmetricGeneralizedEigenvalues(toINDArray(base)));
        System.out.printf("Graph size is %s nodes and %s edges.%n", base.nodes().size(), base.edges().size());

        System.out.println("Normalizing edges weights...");

        Graphs.normalizeWeightsi(base);

        System.out.println("Converting graph to matrix...");

        Pair<INDArray, HashMap<QueryPart, Integer>> pair = Graphs.toINDMatrix(base);
        System.out.println(Arrays.toString(pair.left.shape()));


        System.out.println("Checking matrix validity...");

        System.out.println("Sum on first row: "+pair.left.getRow(0).sumNumber());

        System.out.println("Sum on last row: "+pair.left.getRow(pair.left.rows()-1).sumNumber());

        System.out.println("Dereferencing INDArray...");

        pair = null;

        System.out.println("Creating GraphUpdate evaluator...");

        GraphUpdate graphUpdate = new GraphUpdate(GraphUpdate::simpleInterconnections,
                GraphUpdate::replaceEdges,
                GraphUpdate.KLForGraphs());


        System.out.println("Evaluating session...");
        List<Pair<Query, Double>> liste =  graphUpdate.evaluateSession(base, s1);
        liste.stream().forEach(p -> {
            System.out.println();
            System.out.println(p.left);
            System.out.println("value : " + p.right);
        });
        System.out.println("End of evaluation.");
    }

    @Deprecated
    public static <V> INDArray toSparseINDArray(ValueGraph<V, ? extends Number> in){
        NDArrayFactory factory = Nd4j.sparseFactory();
        INDArray out = factory.createSparseCOO(new float[]{0f},new int[][]{{0},{0}}, new long[]{in.nodes().size(), in.nodes().size()});
        System.out.println(Arrays.toString(out.shape()));
        int i = 0;
        for (V u : in.nodes()){
            int j = 0;
            for (V v : in.nodes()){
                Optional<? extends Number> val = in.edgeValue(u, v);
                double n = val.map(Number::doubleValue).orElse(0d);
                out.put(i, j, n);
                j++;
            }
            i++;
        }
        return out;
    }


}
