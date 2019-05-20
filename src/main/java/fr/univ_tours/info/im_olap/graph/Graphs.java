package fr.univ_tours.info.im_olap.graph;

import com.alexsxode.utilities.collection.Pair;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.*;

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

    /**
     * Get the corresponding matrix representation of a Graph
     * @param in graph to be converted
     * @param <V> type of vertices
     * @param <E> type of the edges, must be a Number
     * @return transition matrix representation of the graph
     */
    public static <V, E extends Number> Pair<INDArray, HashMap<V, Integer>> toINDMatrix(ValueGraph<V,E> in){
        List<V> nodes = new ArrayList<>();
        nodes.addAll(in.nodes());

        HashMap<V, Integer> indexes = new HashMap<>();

        for (int i = 0; i < nodes.size(); i++) {
            indexes.put(nodes.get(i), i);
        }

        int size = nodes.size();

        INDArray matrix = Nd4j.zeros(size,size);

        for (int i = 0; i < size; i++) {
            V from = nodes.get(i);
            for (V to : in.successors(from)){
                int toIndex = indexes.get(to);
                matrix.put(i, toIndex, in.edgeValue(from, to).get());
            }
        }

        return new Pair<>(matrix, indexes);
    }


    /**
     * Create graph from a transition matrix
     * @param graphMatrix transition matrix
     * @param mapper mapping from Nodes to position in matrix
     * @param <N> Node type
     * @return Graph with nodes of type N and edges of type Double
     */
    public static <N extends Comparable<N>> OGraph<Double, N> fromINDMatrix(INDArray graphMatrix, HashMap<N, Integer> mapper) {

        Graph<Double, N> graph = new OGraph<>();

        HashMap<Integer, N> reverseMapper = new HashMap<>();

        for (Map.Entry<N, Integer> entry : mapper.entrySet()) {
            reverseMapper.put(entry.getValue(),entry.getKey());
            graph.addNode(entry.getKey());
        }


        for (int i = 0; i < graphMatrix.size(0); i++) {
            for (int j = 0; j < graphMatrix.size(1); j++) {
                N from = reverseMapper.get(i);
                N to = reverseMapper.get(j);
                double val = graphMatrix.getDouble(i,j);
                if (val != 0.0) {
                    graph.setEdge(from, to, val);
                }
            }
        }

        return null;
    }

    public static <N extends Comparable<N>> OGraph<Double, N> fromINDMatrix(Pair<INDArray, HashMap<N, Integer>> matrixMapperPair) {
        return fromINDMatrix(matrixMapperPair.left, matrixMapperPair.right);
    }

    public static <N extends Comparable<N>> INDArray sortedINDMatrix(Graph<Double, N> graph){
        List<N> nodes = new ArrayList<>();
        nodes.addAll(graph.getNodes());
        nodes.sort(Comparator.naturalOrder());
        //System.out.println(nodes);

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

        return matrix;
    }

    public static <N extends Comparable<N>> double degree(Graph<Double, N> graph, N n){
        return graph.toNode(n).stream().mapToDouble(x -> x.getB()).sum();
    }

    /**
     * /!\ Mutates input graph
     * Normalize weights on all outgoing edges for each node
     * @param graph graph to be normalized
     * @param <N> type of nodes
     * @return reference to the input graph
     */
    public static <N extends Comparable<N>> Graph<Double, N> normalizeWeightsi(Graph<Double, N> graph) {

        for (N node : graph.getNodes()) {

            List<CPair<N, Double>> targets = graph.fromNode(node);

            double weightSum = targets
                    .stream()
                    .mapToDouble(x -> x.right)
                    .sum();

            for (CPair<N, Double> cPair : targets) {
                graph.setEdge(node, cPair.left, cPair.right/weightSum);
            }

        }

        return graph;
    }

    /**
     * /!\ Mutates input graph
     * Normalize weights on all outgoing edges for each node
     * @param graph graph to be normalized
     * @param <N> type of nodes
     * @return reference to the input graph
     */
    public static <N> MutableValueGraph<N, Double> normalizeWeightsi(MutableValueGraph<N, Double> graph) {

        for (N node : graph.nodes()) {

            Set<N> targets = graph.successors(node);

            double weightSum = targets
                    .stream()
                    .mapToDouble(x -> graph.edgeValue(node, x).get())
                    .sum();

            for (N target : targets) {
                graph.putEdgeValue(node, target, graph.edgeValue(node, target).get()/weightSum);
            }

        }

        return graph;
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
