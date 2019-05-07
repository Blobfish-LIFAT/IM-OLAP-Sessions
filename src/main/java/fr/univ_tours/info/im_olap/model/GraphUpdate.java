package fr.univ_tours.info.im_olap.model;

import com.alexsxode.utilities.Nd4jUtils;
import com.alexsxode.utilities.collection.Pair;
import fr.univ_tours.info.im_olap.graph.Graph;
import fr.univ_tours.info.im_olap.graph.Graphs;
import fr.univ_tours.info.im_olap.graph.OGraph;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.*;
import java.util.function.BiFunction;

public class GraphUpdate {

    public final static GraphUpdate SIMPLE_GRAPH_UPDATE = new GraphUpdate(
            GraphUpdate::simpleInterconnections,
            GraphUpdate::replaceEdges,
            GraphUpdate.KLForGraphs()
            );

    private BiFunction<Session, Integer, Graph<Double, QueryPart>> queryGraphBuilder;
    private BiFunction<Graph<Double, QueryPart>,Graph<Double, QueryPart>,Graph<Double, QueryPart>> graphInterpolator;
    private BiFunction<Graph<Double, QueryPart>,Graph<Double, QueryPart>, Double> infoGainFormula;

    public GraphUpdate(BiFunction<Session, Integer, Graph<Double, QueryPart>> queryGraphBuilder,
                       BiFunction<Graph<Double, QueryPart>, Graph<Double, QueryPart>, Graph<Double, QueryPart>> graphInterpolator,
                       BiFunction<Graph<Double, QueryPart>, Graph<Double, QueryPart>, Double> infoGainFormula) {
        this.queryGraphBuilder = queryGraphBuilder;
        this.graphInterpolator = graphInterpolator;
        this.infoGainFormula = infoGainFormula;
    }

    public ArrayList<Pair<Graph<Double, QueryPart>, Double>> evaluateSession(Graph<Double, QueryPart> baseGraph, Session session) {

        ArrayList<Pair<Graph<Double, QueryPart>, Double>> gains = new ArrayList<>();

        gains.add(new Pair<>(baseGraph, 0.0));

        for (int i = 0; i < session.length(); i++) {

            Graph<Double, QueryPart> queryGraph = this.queryGraphBuilder.apply(session, i); // create query graph from truncated session

            Graph<Double, QueryPart> interpolated = this.graphInterpolator.apply(baseGraph, queryGraph); // interpolate base graph to session graph

            double gain = this.infoGainFormula.apply(gains.get(i).left, interpolated); // compute information gain

            gains.add(new Pair<>(interpolated, gain));
        }

        return gains;
    }

    // New query graph construction

    public static Graph<Double, QueryPart> simpleInterconnections(Session session, int actualQueryIndex) {

        List<Query> queries = session.queries.subList(0, actualQueryIndex+1);

        Set<QueryPart> parts = queries.stream().map(Query::getAllParts).reduce((x,y) -> {x.addAll(y); return x;} ).get();

        ArrayList<QueryPart> partList = new ArrayList<>(parts);

        OGraph<Double, QueryPart> graph = new OGraph<>();

        for (int i = 0; i < partList.size(); i++) {
            for (int j = i + 1; j < partList.size(); j++) {
                graph.setEdge(partList.get(i), partList.get(j), 1.0);
            }
        }


        return graph;
    }

    public static BiFunction<Session, Integer, Graph<Double, QueryPart>> alsoUseSchema(Graph<Double, QueryPart> schema, double schemaWeight) {
        return ((session, index) -> {
            return null;
        });
    }


    // Interpolation update

    public static BiFunction<Graph<Double, QueryPart>,Graph<Double, QueryPart>,Graph<Double, QueryPart>>
        linearInterpolation(double alpha) {
        return (baseGraph, queryGraph) -> {

            return null;
        };
    }

    public static <E extends Comparable<E>,N extends Comparable<N>> Graph<E, N>
            replaceEdges(Graph<E, N> source, Graph<E, N> new_edges_graph) {

        Graph<E,N> newGraph = source.clone();

        for (Graph.Edge<N, E> edge : new_edges_graph.getEdges()) {
            newGraph.setEdge(edge);
        }

        return newGraph;
    }


    // utility


    public static BiFunction<Graph<Double, QueryPart>, Graph<Double, QueryPart>, Double> KLForGraphs() {

        return (g1, g2) -> {
            Pair<INDArray, HashMap<QueryPart, Integer>> p1 = Graphs.toINDMatrix(g1);
            Pair<INDArray, HashMap<QueryPart, Integer>> p2 = Graphs.toINDMatrix(g2);

            HashMap<QueryPart, Double> m1 = Nd4jUtils.mappedINDarrayToMap(p1);
            HashMap<QueryPart, Double> m2 = Nd4jUtils.mappedINDarrayToMap(p2);

            return Nd4jUtils.kullbackLeibler(m1, m2);
        };
    }


}
