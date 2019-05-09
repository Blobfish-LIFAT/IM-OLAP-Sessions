package fr.univ_tours.info.im_olap.model;

import com.alexsxode.utilities.Nd4jUtils;
import com.alexsxode.utilities.collection.Pair;
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
            Graph<Double, QueryPart> ng1 = g1.clone();
            Graphs.normalizeWeightsi(ng1);

            Graph<Double, QueryPart> ng2 = g2.clone();
            Graphs.normalizeWeightsi(ng2);

            Pair<INDArray, HashMap<QueryPart, Integer>> pair1 = PageRank.pagerank(ng1, 42);
            Pair<INDArray, HashMap<QueryPart, Integer>> pair2 = PageRank.pagerank(ng2, 42);


            pair1.left.divi(pair1.left.sumNumber());
            pair2.left.divi(pair2.left.sumNumber());

            HashMap<QueryPart, Double> m1 = Nd4jUtils.mappedINDarrayToMap(pair1);
            HashMap<QueryPart, Double> m2 = Nd4jUtils.mappedINDarrayToMap(pair2);

            /* Old test code
            System.out.printf("m1 size: %d \t m2 size: %d%n", m1.size(), m2.size());
            HashSet<QueryPart> test = new HashSet<>(m2.keySet());
            test.removeAll(m1.keySet());
            System.out.println(test.size());
            System.out.println(test);
            test.stream().collect(Collectors.groupingBy(q -> q.t)).forEach((t, l) -> System.out.println(t + "   " + l.size()));
            int t = (int) test.stream().filter(QueryPart::isDimension).filter(queryPart -> queryPart.value.contains("[Tout]")).count();
            System.out.println(t);
            System.out.println(test.stream().filter(QueryPart::isDimension).filter(queryPart -> !queryPart.value.contains("[Tout]")).collect(Collectors.toList()));
            */

            int i = 0;
            double s = 0.0;
            double diff = 0.0;
            for (Map.Entry<QueryPart, Double> entry : m2.entrySet()) {
                //System.out.println("i=" + i + ": "+entry.getValue());
                s += entry.getValue();

                diff += Math.abs(entry.getValue()-m1.get(entry.getKey()));

                i++;
            }


            System.out.println("sum: " + s);
            System.out.println("abs diff = "+diff);

            double res = Nd4jUtils.kullbackLeibler(m1, m2);

            System.out.println("KL = "+res);

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
