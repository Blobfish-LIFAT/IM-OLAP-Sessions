package fr.univ_tours.info.im_olap.graph;

import com.alexsxode.utilities.collection.Pair;
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
