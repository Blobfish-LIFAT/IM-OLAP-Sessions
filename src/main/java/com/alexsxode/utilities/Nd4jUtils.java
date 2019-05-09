package com.alexsxode.utilities;

import com.alexsxode.utilities.collection.Pair;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.HashMap;
import java.util.Map;

import static org.nd4j.linalg.ops.transforms.Transforms.relu;
import static org.nd4j.linalg.ops.transforms.Transforms.sqrt;
import static org.nd4j.linalg.util.MathUtils.log2;

/**
 * Fait par Alexandre le 02/11/2018.
 */
public final class Nd4jUtils {
    public static final double root2 = Math.sqrt(2), threshold = 10e-2;

    /**
     * Computes the Hellinger distance between two vectors representing probability distribution
     * @param p
     * @param q
     * @return
     */
    public static double hellinger(INDArray p, INDArray q){
        return (1.0/root2)*sqrt(p).sub(sqrt(q)).norm2Number().doubleValue();
    }

    /**
     * Computes the Kullback Leibler divergence between two distribution represented by line vectors
     * @param p
     * @param q
     * @return
     */
    public static double kullbackLeibler(INDArray p, INDArray q){
        if (!isDistibution(p, threshold) || !isDistibution(q, threshold))
            throw new IllegalArgumentException("p and q must be probability distributions !");
        if (q.rows() != 1 || p.rows() != 1)
            throw new IllegalArgumentException("Distributions should be line vectors !");
        double sum = 0;
        for (int i = 0; i < p.columns(); i++) {
            double qi = q.getDouble(i), pi = p.getDouble(i);
            if (isZero(qi) && ! isZero(pi))
                throw new IllegalArgumentException("Absolute continuity is required ! If q((i) = 0 then p(i) must be 0. i="+i);
            else if (isZero(qi) && isZero(pi))
                continue;
            sum += p.getDouble(i)*log2(pi/qi);
        }
        return sum;
    }

    /**
     * Computes the Kullback Leibler divergence between two distribution represented by Hashmaps
     * @param p starting distribution
     * @param q destination distribution
     * @param <N> type of discrete events
     * @return
     */
    public static <N extends Comparable<N>> double kullbackLeibler(HashMap<N, Double> p, HashMap<N, Double> q) {

        HashMap<N, Integer> mapper = new HashMap<>();

        int i = 0;
        for (Map.Entry<N, Double> entry : p.entrySet()) {
            mapper.put(entry.getKey(), i);
            i++;
        }

        INDArray pArr = Nd4j.create(mapper.size());

        INDArray qArr = pArr.dup();

        for (Map.Entry<N, Integer> entry : mapper.entrySet()) {
            pArr.putScalar(entry.getValue(), p.get(entry.getKey()));
            qArr.putScalar(entry.getValue(), q.get(entry.getKey()));
        }

        return kullbackLeibler(pArr, qArr);
    }

    public static double normalizedEntropy(INDArray p){
        double sum = 0;
        for (int i = 0; i < p.columns(); i++) {
            sum += p.getDouble(i)*log2(p.getDouble(i));
        }
        sum = - sum;
        return sum/log2(p.columns());
    }

    public static String vecToString(INDArray vector, String delimiter){
        StringBuilder builder = new StringBuilder();
        builder.append(vector.getDouble(0));
        for (int i = 1; i < vector.columns(); i++) {
            builder.append(delimiter);
            builder.append(vector.getDouble(i));
        }
        return builder.toString();
    }

    public static boolean isZero(double value){
        return value >= -threshold && value <= threshold;
    }

    public static boolean isDistibution(INDArray vector, double epsilon){
        return Math.abs(vector.sumNumber().doubleValue() - 1) <= epsilon;
    }

    // INDArray utility

    public static <N extends Comparable<N>> HashMap<N, Double> mappedINDarrayToMap(INDArray array, HashMap<N, Integer> map) {

        HashMap<N, Double> ret = new HashMap<>();

        for (Map.Entry<N, Integer> entry : map.entrySet()) {
            ret.put(entry.getKey(), array.getDouble(entry.getValue()));
        }

        return ret;
    }

    public  static <N extends Comparable<N>> HashMap<N, Double> mappedINDarrayToMap(Pair<INDArray, HashMap<N, Integer>> mappedPair) {
        return mappedINDarrayToMap(mappedPair.left, mappedPair.right);
    }

}
