package fr.univ_tours.info.im_olap;

import ch.obermuhlner.math.big.BigDecimalMath;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.NDArrayFactory;
import org.nd4j.linalg.factory.Nd4j;

import java.math.BigDecimal;
import java.math.MathContext;

import static org.nd4j.linalg.util.MathUtils.log2;

public class Proto2 {
    public static void main(String[] args) {
/*
        NDArrayFactory factory = Nd4j.sparseFactory();
        //INDArray montruc = factory.create(10,10);
        INDArray montruc = factory.createSparseCOO(new float[]{0f},new int[][]{{0},{0}}, new long[]{20,20});
        montruc.put(5,5, 2.0);
        System.out.println(montruc.getDouble(new int[]{1,5}));
        System.out.println(montruc.toDense());
*/

        INDArray p = null, q = null;
        MathContext context = new MathContext(100);
        BigDecimal[] pd = new BigDecimal[p.columns()];
        BigDecimal[] qd = new BigDecimal[q.columns()];

        for (int i = 0; i < pd.length; i++) {
            pd[i] = new BigDecimal(p.getDouble(i));
            qd[i] = new BigDecimal(q.getDouble(i));
        }

        BigDecimal sum = new BigDecimal(0);
        for (int i = 0; i < p.columns(); i++) {
            BigDecimal qi = qd[i], pi = pd[i];
            if (BigDecimal.ZERO.equals(qi) && ! BigDecimal.ZERO.equals(pi)) {
                throw new IllegalArgumentException("Absolute continuity is required ! If q((i) = 0 then p(i) must be 0. i="+i);
            } else if (BigDecimal.ZERO.equals(qi) && BigDecimal.ZERO.equals(pi))
                continue;
            sum.add( pi.multiply(BigDecimalMath.log2(pi.divide(qi), context)) );
        }
        //return sum;

    }
}
