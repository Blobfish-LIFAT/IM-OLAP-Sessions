package fr.univ_tours.info.im_olap;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.nd4j.linalg.api.ndarray.INDArray;

import static org.nd4j.linalg.ops.transforms.Transforms.relu;
import static org.nd4j.linalg.ops.transforms.Transforms.sqrt;
import static org.nd4j.linalg.util.MathUtils.log2;

/**
 * Fait par Alexandre le 02/11/2018.
 */
public final class Nd4jUtils {
    public static final double root2 = Math.sqrt(2), threshold = 10e-10;

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
        if (q.rows() != 1 || p.rows() != 1)
            throw new IllegalArgumentException("Distributions should be line vectors !");
        double sum = 0;
        for (int i = 0; i < p.columns(); i++) {
            double qi = q.getDouble(i), pi = p.getDouble(i);
            if (isZero(qi) && ! isZero(pi))
                throw new IllegalArgumentException("Absolute continuity is required ! If q((i) = 0 then p(i) must be 0.");
            sum += p.getDouble(i)*log2(pi/qi);
        }
        return sum;
    }

    public static double normalizedEntropy(INDArray p){
        double sum = 0;
        for (int i = 0; i < p.columns(); i++) {
            sum += p.getDouble(i)*log2(p.getDouble(i));
        }
        sum = - sum;
        return sum/log2(p.columns());
    }

    public static boolean isZero(double value){
        return value >= -threshold && value <= threshold;
    }

    public static boolean isDistibution(INDArray vector, double epsilon){
        return Math.abs(vector.sumNumber().doubleValue() - 1) <= epsilon;
    }
}
