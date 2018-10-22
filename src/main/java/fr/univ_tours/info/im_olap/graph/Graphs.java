package fr.univ_tours.info.im_olap.graph;

import com.alexsxode.utilities.collection.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class Graphs {
    private Graphs(){}

    /**
     * Convert a Graph of edges of type Double to its matrix representation int the form of a two dimensional INDArray
     * @param graph a graph object
     * @param <N> type of the nodes labels of the graph
     * @return the matrix representation of the graph
     */
    public static <N extends Comparable<N>> Pair<INDArray, HashMap<N, Integer>> toINDMatrix(Graph<Double, N> graph){
        List<N> nodes = new ArrayList<>();
        nodes.addAll(graph.getNodes());

        HashMap<N, Integer> indexes = new HashMap<>();

        for (int i = 0; i < nodes.size(); i++) {
            indexes.put(nodes.get(i), i);
        }

        int size = nodes.size();

        INDArray matrix = Nd4j.zeros(size,size);

        for (int i = 0; i < size; i++) {
            N from = nodes.get(i);
            for (CPair<N, Double> to : graph.fromNode(from)){
                int toIndex = indexes.get(to.getA());
                matrix.put(i, toIndex, to.getB());
            }
        }

        return new Pair<>(matrix, indexes);
    }

    public static <N extends Comparable<N>> double degree(Graph<Double, N> graph, N n){
        return graph.toNode(n).stream().mapToDouble(x -> x.getB()).sum();
    }

    public static <N extends Comparable<N>> INDArray toDegreeMatrix(Graph<Double, N> graph){
        List<N> nodes = new ArrayList<>();
        nodes.addAll(graph.getNodes());

        int size = nodes.size();

        INDArray matrix = Nd4j.zeros(size,size);

        for (int i = 0; i < size; i++) {
            matrix.put(i,i, degree(graph, nodes.get(i)));
        }

        return matrix;
    }
}
