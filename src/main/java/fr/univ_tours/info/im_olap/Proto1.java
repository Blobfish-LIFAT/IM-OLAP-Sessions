package fr.univ_tours.info.im_olap;

import com.alexsxode.utilities.collection.Pair;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import fr.univ_tours.info.im_olap.data.DopanLoader;
import fr.univ_tours.info.im_olap.graph.Graphs;
import fr.univ_tours.info.im_olap.graph.OGraph;
import fr.univ_tours.info.im_olap.mondrian.MondrianConfig;
import fr.univ_tours.info.im_olap.mondrian.CubeUtils;

import fr.univ_tours.info.im_olap.model.Query;
import fr.univ_tours.info.im_olap.model.QueryPart;
import mondrian.olap.*;
import fr.univ_tours.info.im_olap.model.*;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.eigen.Eigen;
import org.nd4j.linalg.factory.NDArrayFactory;
import org.nd4j.linalg.factory.Nd4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

        System.out.println("\"Fixing\" sessions...");
        List<Session> sessions = SessionGraph.fixSessions(DopanLoader.loadDir(dataDir), cubeSchema);
        Session s1 = sessions.get(0);

        System.out.println("Creating cube utils...");
        CubeUtils mdUtils = new CubeUtils(olap, s1.getCubeName());
        System.out.println("Doing stuff with hierarchies...");
        List<Hierarchy> allHierarchies = Arrays
                .stream(mdUtils.getCube().getDimensions())
                .flatMap(d -> Arrays.stream(d.getHierarchies()))
                .collect(Collectors.toList());

        System.out.println("Collecting user session...");
        List<Session> thisUser = sessions.stream().filter(s -> s.getUserName().equals(s1.getUserName())).collect(Collectors.toList());
        thisUser.remove(s1);


        System.out.println("Building topology graph...");
        //OGraph<Double, QueryPart> base = SessionGraph.buildTopologyGraph(thisUser, "data/cubeSchemas/DOPAN_DW3.xml");
        MutableValueGraph<QueryPart, Double> base = ValueGraphBuilder.directed().allowsSelfLoops(true).build();

        System.out.println("Checking sync...");
        //base.checkSync();

        System.out.println("Injecting filters...");
        SessionGraph.injectFilters(base, mdUtils);

        System.out.println("Checking sync...");
        //base.checkSync();

        //MutableValueGraph<QueryPart, Double> graph = ValueGraphBuilder.directed().allowsSelfLoops(true).build();

        //SessionGraph.injectFiltersGuava(graph, mdUtils);
        //System.out.println(Eigen.eigenvalues(toINDArray(base)));


        /**
         * Ben's stuff
         */

        //((OGraph<Double, QueryPart>)base).checkSync();


        for (Query query : s1.queries) {
            for (QueryPart qp : query.getAllParts()) {
                base.addNode(qp);
            }
        }

        //((OGraph<Double, QueryPart>)base).checkSync();

        for (QueryPart queryPart : base.nodes()) {
            base.putEdgeValue(queryPart, queryPart, 1.0);
        }

        //((OGraph<Double, QueryPart>)base).checkSync();


        Set<QueryPart> queryParts = new HashSet<>(base.nodes());

        queryParts.removeAll(s1.queries.stream().flatMap(x -> x.getAllParts().stream()).collect(Collectors.toSet()));

        //((OGraph<Double, QueryPart>)base).checkSync();

        if (queryParts.isEmpty()) {
            System.err.println("Error: some query parts are in session but not in the base graph!");
            System.out.println(queryParts);
        }

        //((OGraph<Double, QueryPart>)base).checkSync();

        GraphUpdate graphUpdate = new GraphUpdate(GraphUpdate::simpleInterconnections,
                GraphUpdate::replaceEdges,
                GraphUpdate.KLForGraphs());

        INDArray test = toSparseINDArray(base);

        System.out.println("Evaluating session...");
        /*List<Pair<Query, Double>> liste =  graphUpdate.evaluateSession(base, s1);
        liste.stream().forEach(p -> {
            System.out.println();
            System.out.println(p.left);
            System.out.println("value : " + p.right);
        });*/
        System.out.println("End of evaluation.");
    }

    public static <V> INDArray toINDArray(ValueGraph<V, ? extends Number> in){
        //Nd4j.setDataType(DataBuffer.Type.FLOAT);
        System.out.println(in.nodes().size());
        try {
            StringBuilder sb = new StringBuilder();
            in.nodes().stream().forEach(t -> sb.append(t.toString() + "\n"));
            Files.write(Paths.get("/tmp/truc.log"), sb.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        INDArray out = Nd4j.create(in.nodes().size(), in.nodes().size());
        int i = 0;
        for (V u : in.nodes()){
            int j = 0;
            for (V v : in.nodes()){
                Optional<? extends Number> val = in.edgeValue(u, v);
                double n = val.map(Number::doubleValue).orElse(0d);
                out.putScalar(i, j, n);
                j++;
            }
            i++;
        }
        return out;
    }

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
