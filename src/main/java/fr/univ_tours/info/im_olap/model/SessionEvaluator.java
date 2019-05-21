package fr.univ_tours.info.im_olap.model;

import com.alexsxode.utilities.Logger;
import com.alexsxode.utilities.Nd4jUtils;
import com.alexsxode.utilities.collection.Pair;
import com.google.common.graph.*;
import fr.univ_tours.info.im_olap.compute.PageRank;
import fr.univ_tours.info.im_olap.graph.Graph;
import fr.univ_tours.info.im_olap.graph.Graphs;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.*;
import java.util.function.BiFunction;

public class SessionEvaluator<NodeT, EdgeT, EvalT> {

    /**
     * This represent the construction of a session graph at a certain position
     * @param <N>
     * @param <E>
     */
    @FunctionalInterface
    public interface IntermediateSessionBuilder<N,E> {
        MutableValueGraph<N,E> buildFromBaseAndSession(MutableValueGraph<N,E> baseGraph, Session session, int position);
    }

    /**
     * This represent the interpolation of a previous session graph with a new one (i.e. the modification introduced by a query)
     * @param <N>
     * @param <E>
     */
    @FunctionalInterface
    public interface GraphInterpolator<N,E> {
        MutableValueGraph<N,E> interpolate(MutableValueGraph<N,E> baseGraph, MutableValueGraph<N,E> previous, MutableValueGraph<N,E> next);
    }

    @FunctionalInterface
    public interface Evaluator<N,E,R> {
        R transform(MutableValueGraph<N,E> graph);
    }

    @FunctionalInterface
    public interface GainEvaluator<R> {
        Double evaluate(R previous, R next);
    }


    private IntermediateSessionBuilder<NodeT, EdgeT> queryGraphBuilder;
    private GraphInterpolator<NodeT, EdgeT> graphInterpolator;
    private Evaluator<NodeT, EdgeT, EvalT> graphEvaluator;

    public SessionEvaluator(IntermediateSessionBuilder<NodeT, EdgeT> queryGraphBuilder,
                            GraphInterpolator<NodeT, EdgeT> graphInterpolator,
                            Evaluator<NodeT, EdgeT, EvalT> graphEvaluator) {
        this.queryGraphBuilder = queryGraphBuilder;
        this.graphInterpolator = graphInterpolator;
        this.graphEvaluator = graphEvaluator;
    }

    public ArrayList<Pair<Query, EvalT>> evaluateSession(MutableValueGraph<NodeT, EdgeT> baseGraph, Session session) {

        ArrayList<Pair<Query, EvalT>> evals = new ArrayList<>();

        ArrayList<MutableValueGraph<NodeT, EdgeT>> sessionGraphs = new ArrayList<>();

        for (int i = 0; i < session.length() ; i++) {
            // create query graph from truncated session
            MutableValueGraph<NodeT, EdgeT> sessionGraph = this.queryGraphBuilder.buildFromBaseAndSession(baseGraph, session, i);

            sessionGraphs.add(sessionGraph);
        }

        //gains.add(new Pair<>(session.queries.get(0), 0.0));

        for (int i = 1; i < session.length(); i++) {

            Logger.logInfo("evaluateSession", "iteration i = ", i);

            MutableValueGraph<NodeT, EdgeT> previousGraph = sessionGraphs.get(i-1);
            MutableValueGraph<NodeT, EdgeT> queryGraph = sessionGraphs.get(i);

            // interpolate base graph to session graph
            MutableValueGraph<NodeT, EdgeT> interpolated = this.graphInterpolator.interpolate(baseGraph, previousGraph, queryGraph);

            EvalT newEval = this.graphEvaluator.transform(interpolated);

            evals.add(new Pair<>(session.queries.get(i), newEval));
        }

        return evals;
    }

    /**
     * Computes the gain between a query and the following query. The gain is associated with the new query only in a pair
     * @param evaluatedSession
     * @param gainEvaluator method for computing the gain as a double from two EvalT values
     * @param <T> type of the input arguments, usually numeric or vector like
     * @return the list of new query, gain pairs in the same order
     */
    public static <T> ArrayList<Pair<Query, Double>> computeGains(List<Pair<Query, T>> evaluatedSession, GainEvaluator<T> gainEvaluator) {
        
        ArrayList<Pair<Query, Double>> ret = new ArrayList<>(evaluatedSession.size());
        
        for (int i = 1; i < evaluatedSession.size(); i++) {

            Pair<Query, T> pair = evaluatedSession.get(i);

            double gain = gainEvaluator.evaluate(evaluatedSession.get(i-1).right, pair.right);
            
            Pair<Query, Double> resPair = new Pair<>(pair.left, gain);

            ret.add(resPair);
        }
        
        return ret;
    }


    // New query graph construction

    public static MutableValueGraph<QueryPart, Double> simpleInterconnections(MutableValueGraph<QueryPart, Double> baseGraph, Session session, int actualQueryIndex) {

        List<Query> queries = session.queries.subList(0, actualQueryIndex+1);

        Set<QueryPart> parts = queries.stream().map(Query::getAllParts).reduce((x,y) -> {x.addAll(y); return x;} ).get();

        ArrayList<QueryPart> partList = new ArrayList<>(parts);

        MutableValueGraph<QueryPart, Double> graph = com.google.common.graph.Graphs.copyOf(baseGraph);

        for (int i = 0; i < partList.size(); i++) {
            for (int j = i; j < partList.size(); j++) {
                graph.putEdgeValue(partList.get(i), partList.get(j), 1.0);
            }
        }


        return graph;
    }

    public static <N,E> MutableValueGraph<N,E> onlyBaseGraph() {
        return null;
    }


    // Interpolation update

    public static BiFunction<MutableValueGraph<QueryPart, Double>,MutableValueGraph<QueryPart, Double>,MutableValueGraph<QueryPart, Double>>
        linearInterpolation(double alpha) {
        return (baseGraph, queryGraph) -> {

            return null;
        };
    }

    public static <N,E> MutableValueGraph<N,E> returnNew(MutableValueGraph<N, E> baseGraph,
                                                         MutableValueGraph<N, E> source,
                                                         MutableValueGraph<N, E> new_edges_graph) {
        return new_edges_graph;
    }

    public static <N,E> MutableValueGraph<N, E> replaceEdges(MutableValueGraph<N, E> baseGraph,
                                                             MutableValueGraph<N, E> source,
                                                             MutableValueGraph<N, E> new_edges_graph) {

        MutableValueGraph<N, E> newGraph = com.google.common.graph.Graphs.copyOf(source);

        for (EndpointPair<N> edge : new_edges_graph.edges()) {
            newGraph.putEdgeValue(edge, new_edges_graph.edgeValue(edge).get());
        }

        return newGraph;
    }


    // utility

    public static <N> Pair<INDArray, HashMap<N, Integer>> pageRank(MutableValueGraph<N,Double> graph) {
        Graphs.normalizeWeightsi(graph); // ensure outgoing edges values sums up to 1
        return PageRank.pagerank(graph, 50);
    }

    // Gain computation

    public static <N extends Comparable<N>> Double KullbackLeibler(Pair<INDArray, HashMap<N,Integer>> pair1, Pair<INDArray, HashMap<N,Integer>> pair2) {

        pair1.left.divi(pair1.left.sumNumber());
        pair2.left.divi(pair2.left.sumNumber());

        HashMap<N, Double> m1 = Nd4jUtils.mappedINDarrayToMap(pair1);
        HashMap<N, Double> m2 = Nd4jUtils.mappedINDarrayToMap(pair2);

        Logger.logInfo("KLForGraphs","m1 size: ",m1.size()," \t m2 size: ", m2.size());

        double res = Nd4jUtils.kullbackLeibler(m1, m2);

        Logger.logInfo("KLForGraphs","KL = ",res);

        return res;
    }

    public static <N extends Comparable<N>> Double AbsoluteDiff(Pair<INDArray, HashMap<N,Integer>> pair1, Pair<INDArray, HashMap<N,Integer>> pair2) {

        pair1.left.divi(pair1.left.sumNumber());
        pair2.left.divi(pair2.left.sumNumber());

        HashMap<N, Double> m1 = Nd4jUtils.mappedINDarrayToMap(pair1);
        HashMap<N, Double> m2 = Nd4jUtils.mappedINDarrayToMap(pair2);


        Logger.logInfo("AbsoluteDiff","m1 size: ",m1.size()," \t m2 size: ", m2.size());


        int i = 0;
        double s = 0.0;
        double diff = 0.0;
        for (Map.Entry<N, Double> entry : m2.entrySet()) {
            //System.out.println("i=" + i + ": "+entry.getValue());
            s += entry.getValue();

            diff += Math.abs(entry.getValue()-m1.get(entry.getKey()));

            i++;
        }

        Logger.logInfo("AbsoluteDiff", "sum: " , s);
        Logger.logInfo("AbsoluteDiff","abs diff = ",diff);

        Logger.logInfo("AbsoluteDiff","KL = ",diff);

        return diff;
    }

}
