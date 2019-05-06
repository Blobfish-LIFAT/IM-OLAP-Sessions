package fr.univ_tours.info.im_olap.model;

import com.alexsxode.utilities.collection.Pair;
import fr.univ_tours.info.im_olap.compute.PageRank;
import fr.univ_tours.info.im_olap.graph.Graph;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class GraphUpdate {

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



        return null;
    }








    // Interpolation update

    public static BiFunction<Graph<Double, QueryPart>,Graph<Double, QueryPart>,Graph<Double, QueryPart>>
        linearInterpolation(double alpha) {
        return (baseGraph, queryGraph) -> {

            return null;
        };
    }



    // utility


    public static BiFunction<Graph<Double, QueryPart>, Graph<Double, QueryPart>, Double> KLForGraphs() {

        return (g1, g2) -> {


            return null;
        };
    }


}
