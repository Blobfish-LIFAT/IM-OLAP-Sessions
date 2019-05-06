package fr.univ_tours.info.im_olap.compute;

import com.alexsxode.utilities.collection.Pair;
import fr.univ_tours.info.im_olap.graph.Graph;
import fr.univ_tours.info.im_olap.graph.Graphs;
import fr.univ_tours.info.im_olap.graph.NOGraph;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.HashMap;
import java.util.Map;

public class PageRank {
    private PageRank(){}

    /**
     * @param n size of the resulting square matrix (n,n)
     * @return aij = 1/n for any i and j
     */
    public static INDArray getStochasticUniformMatrix(int n){

        INDArray matrix = Nd4j.ones(n,n);
        matrix.divi(n);

        return matrix;
    }

    /**
     * /!\ IMPURE, updates matrix in place
     * /!\ doesn't handle rows summing to zero
     * @param matrix matrix to be normalized by rows (each row sums to 1)
     */
    public static void normalizeRowsi(INDArray matrix){
        for (int i = 0; i < matrix.size(0); i++) {
            matrix.getRow(i).divi(matrix.getRow(i).sumNumber());
        }
    }


    /**
     * Calculate the probability distribution for each node after convergence
     * @param weights the line-normalized transition matrix
     * @param iter number of iterration
     * @param <N> type of nodes
     * @return a mapping from nodes to their probability of apparition after iter iterations
     */
    public static <N extends Comparable<N>> INDArray pageRank(
            INDArray weights,
            int iter){

        INDArray matrix = weights.dup();

        INDArray vector = Nd4j.ones(1, matrix.size(0));
        vector.divi(matrix.size(0));

        for (int i = 1; i < iter+1; i++) {
            vector.mmuli(matrix);
        }

        return vector;
    }

    public static void main(String[] args){

        INDArray uniform = getStochasticUniformMatrix(5);
        //System.out.println(uniform+"\n");

        //INDArray eigenv = pageRankINDArray(uniform, 0.1, new int[]{1,2});
        //System.out.println(eigenv);

        Graph<Double, String> graph = new NOGraph<>();
        graph.setEdge("A","B", 20.0);
        graph.setEdge("C","B", 1.0);
        graph.setEdge("A","C", 5.0);
        graph.setEdge("D","B", 3.0);
        graph.setEdge("B","B", 2.0);

        INDArray matrix = (Graphs.toINDMatrix(graph)).getA();

        System.out.println(matrix);

        for (int i = 0; i < matrix.size(0); i++) {
            INDArray col = matrix.getColumn(i);
            matrix.putColumn(i,col.div(col.sum(0)));
        }

        System.out.println(matrix);


    }

}
