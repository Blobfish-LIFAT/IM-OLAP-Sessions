package com.alexsxode.utilities;


import ch.obermuhlner.math.big.BigDecimalMath;
import com.alexsxode.utilities.collection.Pair;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;


import static org.nd4j.linalg.ops.transforms.Transforms.sqrt;
import static org.nd4j.linalg.util.MathUtils.log2;

/**
 * Fait par Alexandre le 02/11/2018.
 */
public final class Nd4jUtils {
    private static final double root2 = Math.sqrt(2), threshold = 10e-30;

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
        //10e-3 is for reasonable errors in the computations
        if (!isDistibution(p, 10e-3) || !isDistibution(q, 10e-3))
            throw new IllegalArgumentException("p and q must be probability distributions !");
        if (q.rows() != 1 || p.rows() != 1)
            throw new IllegalArgumentException("Distributions should be line vectors !");

        MathContext context = new MathContext(100);
        BigDecimal[] pd = new BigDecimal[p.columns()];
        BigDecimal[] qd = new BigDecimal[q.columns()];

        BigDecimal s1 = new BigDecimal(0.0);
        BigDecimal s2 = new BigDecimal(0.0);

        for (int i = 0; i < pd.length; i++) {
            pd[i] = new BigDecimal(p.getDouble(i), context);
            qd[i] = new BigDecimal(q.getDouble(i), context);

            // summing over both distributions for normalization
            s1 = s1.add(pd[i]);
            s2 = s2.add(qd[i]);
        }

        // normalizing as BigDecimals for precision
        for (int i = 0; i < pd.length; i++) {
            pd[i] = pd[i].divide(s1, context);
            qd[i] = qd[i].divide(s2, context);
        }

        BigDecimal sum = new BigDecimal(0, context);
        for (int i = 0; i < p.columns(); i++) {
            BigDecimal qi = qd[i], pi = pd[i];
            if (BigDecimal.ZERO.equals(qi) && ! BigDecimal.ZERO.equals(pi)) {
                throw new IllegalArgumentException("Absolute continuity is required ! If q((i) = 0 then p(i) must be 0. i="+i);
            } else if (BigDecimal.ZERO.equals(qi) && BigDecimal.ZERO.equals(pi))
                continue;
            sum = sum.add( pi.multiply(BigDecimalMath.log2(pi.divide(qi, RoundingMode.HALF_DOWN), context)) );
        }
        return sum.doubleValue();

        /*
        double sum = 0;
        for (int i = 0; i < p.columns(); i++) {
            double qi = q.getDouble(i), pi = p.getDouble(i);
            if (isZero(qi) && ! isZero(pi)) {
                throw new IllegalArgumentException("Absolute continuity is required ! If q((i) = 0 then p(i) must be 0. i="+i);
            } else if (isZero(qi) && isZero(pi))
                continue;
            sum += pi*log2(pi/qi);
        }
        return sum;*/
    }

    public static double KullbackLeiblerDivergence(double[] x, double[] y) {
        boolean intersection = false;
        double kl = 0.0;

        for (int i = 0; i < x.length; i++) {
            if (x[i] != 0.0 && y[i] != 0.0) {
                intersection = true;
                kl += x[i] * Math.log(x[i] / y[i]);
            }
        }

        if (intersection) {
            return kl;
        } else {
            return Double.POSITIVE_INFINITY;
        }
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
        List<N> debug = new ArrayList<>(mapper.size());
        for (Map.Entry<N, Integer> entry : mapper.entrySet()) {
            pArr.putScalar(entry.getValue(), p.get(entry.getKey()));
            qArr.putScalar(entry.getValue(), q.get(entry.getKey()));
            debug.add(entry.getKey());
        }


        try {
            return kullbackLeibler(pArr, qArr);
        } catch (IllegalArgumentException e){
            int pos = Integer.parseInt(e.getMessage().split("=")[2]);
            System.out.println(debug.get(pos));
            System.out.println(p.get(debug.get(pos)));
            System.out.println(q.get(debug.get(pos)));
            throw new IllegalStateException(e);
        }

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
