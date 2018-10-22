package fr.univ_tours.info.im_olap.graph;

import com.alexsxode.utilities.collection.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.HashMap;
import java.util.Map;

public class PageRank {
    private PageRank(){}

    public static INDArray getStochasticUniformMatrix(int n){

        INDArray matrix = Nd4j.ones(n,n);
        matrix.divi(n);

        return matrix;
    }

    public static INDArray getBiasedStochasticMatrix(int n, int[] indexes){
        INDArray matrix = Nd4j.zeros(n,n);

        for (int i = 0; i < indexes.length; i++) {
            int j = indexes[i];
            for (int k = 0; k < n; k++) {
                matrix.put(j,k,1);
            }
        }
        matrix.divi(indexes.length);
        return matrix;
    }


    /**
     * @param topicMap stochastic (by line) matrix (n queryparts, p topics) in intent space
     * @param indexMap map objects to the desired unique index of the row in the resulting matrix
     * @param userVec user vector in intent space (p,1)
     * @param m used on the user vector to elevate each element to the power
     * @param <N> type of the topic objects
     * @return a biased, stochastic (line) matrix of the the same repeated row
     */
    public static <N> INDArray topicBiasedTPMatrix(HashMap<N, INDArray> topicMap, HashMap<N, Integer> indexMap, INDArray userVec, double m){
        INDArray biasedMatrix = Nd4j.zeros(indexMap.size(), indexMap.size());

        //userVec = org.nd4j.linalg.ops.transforms.Transforms.pow(userVec, m); // Iu^(m) (element wise)

        INDArray orderedTopics = Nd4j.zeros(topicMap.size(), userVec.rows());

        for (Map.Entry<N, INDArray> entry : topicMap.entrySet()){
            Integer index = indexMap.get(entry.getKey());

            if (index != null){
                orderedTopics.getRow(index).putiRowVector(entry.getValue());
            }
            else {
                System.out.println(entry.getKey() + " is null");
            }
        }

        INDArray biasedUserVec = org.nd4j.linalg.ops.transforms.Transforms.pow(orderedTopics.mmul(userVec).transpose(), m); // transpose(W.Iu^(m))
        biasedUserVec.divi(biasedUserVec.sumNumber()); // normalize vector

        for (int i = 0; i < biasedMatrix.rows(); i++) {
            biasedMatrix.getRow(i).putiRowVector(biasedUserVec); // We fill up the rows with the biasedVector
        }

        return biasedMatrix;
    }


    /**
     * Calculate the probability distribution for each node after convergence
     * @param graph weighted transition graph
     * @param alpha , 0 < alpha < 1, determine the teleportation factor to a random node with a probability of (1-alpha)
     * @param biasMatrix the matrix that will be mixed in to add a teleportation bias
     * @param iter number of iterration
     * @param <N> type of nodes
     * @return a mapping from nodes to their probability of apparition after iter iterrations
     */
    public static <N extends Comparable<N>> HashMap<N,Double> pageRank(Graph<Double,N> graph, double alpha, INDArray biasMatrix, int iter){
        if (alpha <0 || alpha >= 1){
            throw new IllegalArgumentException("Alpha should be strictly between 0 and 1");
        }

        Pair<INDArray, HashMap<N,Integer>> pair = Graphs.toINDMatrix(graph);

        HashMap<N,Integer> indexMap = pair.getB();

        INDArray matrix = pair.getA().dup();
        for (int i = 0; i < matrix.size(0); i++) {
            INDArray row = matrix.getRow(i);
            double sum = row.sum(1).getDouble(0);
            matrix.putRow(i,row.div(sum));
        }

        matrix.muli(alpha);

        INDArray bias = biasMatrix.mul(1-alpha);

        INDArray res = matrix.add(bias);



        INDArray vector = Nd4j.ones(1, matrix.size(0));
        vector.divi(matrix.size(0));
        //System.out.println("starting vector: "+vector);

        //System.out.println(res);

        for (int i = 1; i < iter+1; i++) {
            vector.mmuli(res);
            //System.out.println("mmuli: "+vector);
            //System.out.println("end of iteration "+i);
        }

        HashMap<N, Double> probMap = new HashMap<>();

        for (Map.Entry<N, Integer> entry : indexMap.entrySet()){
            N key = entry.getKey();
            probMap.put(key, vector.getDouble(entry.getValue()));
        }

        return probMap;
    }

    public static <N extends Comparable<N>> HashMap<N,Double> personalizedPageRank(Graph<Double,N> graph, HashMap<N,INDArray> topicMap, INDArray userVec, double alpha, double m, int iter){
        HashMap<N, Integer> indexMap = Graphs.toINDMatrix(graph).getB();

        INDArray biasMatrix = topicBiasedTPMatrix(topicMap, indexMap, userVec, m);

        return pageRank(graph, alpha, biasMatrix, iter);

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
