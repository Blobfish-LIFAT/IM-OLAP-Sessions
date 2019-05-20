package fr.univ_tours.info.im_olap.model;

import com.alexsxode.utilities.Logger;
import com.alexsxode.utilities.Nd4jUtils;
import com.alexsxode.utilities.collection.Pair;
import com.google.common.graph.*;
import fr.univ_tours.info.im_olap.compute.PageRank;
import fr.univ_tours.info.im_olap.graph.Graph;
import fr.univ_tours.info.im_olap.graph.Graphs;
import fr.univ_tours.info.im_olap.graph.OGraph;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class GraphUpdate {

    public final static GraphUpdate SIMPLE_GRAPH_UPDATE = new GraphUpdate(
            GraphUpdate::simpleInterconnections,
            GraphUpdate::replaceEdges,
            GraphUpdate.KLForGraphs()
            );

    private BiFunction<Session, Integer, MutableValueGraph<QueryPart, Double>> queryGraphBuilder;
    private BiFunction<MutableValueGraph<QueryPart, Double>,MutableValueGraph<QueryPart, Double>,MutableValueGraph<QueryPart, Double>> graphInterpolator;
    private BiFunction<MutableValueGraph<QueryPart, Double>,MutableValueGraph<QueryPart, Double>, Double> infoGainFormula;

    public GraphUpdate(BiFunction<Session, Integer, MutableValueGraph<QueryPart, Double>> queryGraphBuilder,
                       BiFunction<MutableValueGraph<QueryPart, Double>, MutableValueGraph<QueryPart, Double>,
                                  MutableValueGraph<QueryPart, Double>> graphInterpolator,
                       BiFunction<MutableValueGraph<QueryPart, Double>, MutableValueGraph<QueryPart, Double>, Double> infoGainFormula) {
        this.queryGraphBuilder = queryGraphBuilder;
        this.graphInterpolator = graphInterpolator;
        this.infoGainFormula = infoGainFormula;
    }

    public ArrayList<Pair<Query, Double>> evaluateSession(MutableValueGraph<QueryPart, Double> baseGraph, Session session) {

        ArrayList<Pair<Query, Double>> gains = new ArrayList<>();

        ArrayList<MutableValueGraph<QueryPart, Double>> sessionGraphs = new ArrayList<>();

        for (int i = 0; i < session.length() ; i++) {
            // create query graph from truncated session
            MutableValueGraph<QueryPart, Double> sessionGraph = this.queryGraphBuilder.apply(session, i);
            //((OGraph<Double,QueryPart>) sessionGraph).checkSync();
            sessionGraphs.add(sessionGraph);
        }

        //gains.add(new Pair<>(session.queries.get(0), 0.0));

        for (int i = 1; i < session.length(); i++) {

            Logger.logInfo("evaluateSession", "iteration i = ", i);

            MutableValueGraph<QueryPart, Double> previousGraph = sessionGraphs.get(i-1);
            MutableValueGraph<QueryPart, Double> queryGraph = sessionGraphs.get(i);

            // interpolate base graph to session graph
            MutableValueGraph<QueryPart, Double> interpolatedPrevious = this.graphInterpolator.apply(baseGraph, previousGraph);
            MutableValueGraph<QueryPart, Double> interpolated = this.graphInterpolator.apply(baseGraph, queryGraph);


            //((OGraph<Double, QueryPart>)interpolated).checkSync();

            double gain = this.infoGainFormula.apply(interpolatedPrevious, interpolated); // compute information gain

            gains.add(new Pair<>(session.queries.get(i), gain));
        }

        return gains;
    }

    // New query graph construction

    public static MutableValueGraph<QueryPart, Double> simpleInterconnections(Session session, int actualQueryIndex) {

        List<Query> queries = session.queries.subList(0, actualQueryIndex+1);

        Set<QueryPart> parts = queries.stream().map(Query::getAllParts).reduce((x,y) -> {x.addAll(y); return x;} ).get();

        ArrayList<QueryPart> partList = new ArrayList<>(parts);

        MutableValueGraph<QueryPart, Double> graph = ValueGraphBuilder.directed().allowsSelfLoops(true).build();

        for (int i = 0; i < partList.size(); i++) {
            for (int j = i; j < partList.size(); j++) {
                graph.putEdgeValue(partList.get(i), partList.get(j), 1.0);
            }
        }


        return graph;
    }

    public static BiFunction<Session, Integer, MutableValueGraph<QueryPart, Double>>
            alsoUseSchema(MutableValueGraph<QueryPart, Double> schema,
                          double schemaWeight) {
        return ((session, index) -> {
            return null;
        });
    }


    // Interpolation update

    public static BiFunction<MutableValueGraph<QueryPart, Double>,MutableValueGraph<QueryPart, Double>,MutableValueGraph<QueryPart, Double>>
        linearInterpolation(double alpha) {
        return (baseGraph, queryGraph) -> {

            return null;
        };
    }

    public static <N,E> MutableValueGraph<N, E>
            replaceEdges(MutableValueGraph<N, E> source, MutableValueGraph<N, E> new_edges_graph) {

        MutableValueGraph<N, E> newGraph = com.google.common.graph.Graphs.copyOf(source);

        for (EndpointPair<N> edge : new_edges_graph.edges()) {
            newGraph.putEdgeValue(edge, new_edges_graph.edgeValue(edge).get());
        }

        return newGraph;
    }


    // utility


    public static BiFunction<MutableValueGraph<QueryPart, Double>, MutableValueGraph<QueryPart, Double>, Double> KLForGraphs() {

        return (g1, g2) -> {
            MutableValueGraph<QueryPart, Double> ng1 = com.google.common.graph.Graphs.copyOf(g1);

            Graphs.normalizeWeightsi(ng1);


            MutableValueGraph<QueryPart, Double> ng2 = com.google.common.graph.Graphs.copyOf(g2);

            Graphs.normalizeWeightsi(ng2);


            Pair<INDArray, HashMap<QueryPart, Integer>> pair1 = PageRank.pagerank(ng1, 42);
            Pair<INDArray, HashMap<QueryPart, Integer>> pair2 = PageRank.pagerank(ng2, 42);


            pair1.left.divi(pair1.left.sumNumber());
            pair2.left.divi(pair2.left.sumNumber());

            HashMap<QueryPart, Double> m1 = Nd4jUtils.mappedINDarrayToMap(pair1);
            HashMap<QueryPart, Double> m2 = Nd4jUtils.mappedINDarrayToMap(pair2);


            Logger.logInfo("KLForGraphs","m1 size: ",m1.size()," \t m2 size: ", m2.size());


            int i = 0;
            double s = 0.0;
            double diff = 0.0;
            for (Map.Entry<QueryPart, Double> entry : m2.entrySet()) {
                //System.out.println("i=" + i + ": "+entry.getValue());
                s += entry.getValue();

                diff += Math.abs(entry.getValue()-m1.get(entry.getKey()));

                i++;
            }


            Logger.logInfo("KLForGraphs", "sum: " , s);
            Logger.logInfo("KLForGraphs","abs diff = ",diff);




            double res = Nd4jUtils.kullbackLeibler(m1, m2);

            Logger.logInfo("KLForGraphs","KL = ",res);

            return res;
        };
    }


    public static double absoluteDiff(Graph<Double, QueryPart> g1, Graph<Double, QueryPart> g2) {

        Graph<Double, QueryPart> ng1 = g1.clone();
        Graphs.normalizeWeightsi(ng1);

        Graph<Double, QueryPart> ng2 = g2.clone();
        Graphs.normalizeWeightsi(ng2);

        HashMap<QueryPart, Double> m1 = Nd4jUtils.mappedINDarrayToMap(PageRank.pagerank(ng1, 50));
        HashMap<QueryPart, Double> m2 = Nd4jUtils.mappedINDarrayToMap(PageRank.pagerank(ng2, 50));

        double diff = 0;
        for (Map.Entry<QueryPart, Double> entry : m1.entrySet()) {

            diff += Math.abs(entry.getValue() - m2.get(entry.getKey()));

        }

        return diff;
    }


}
