package fr.univ_tours.info.im_olap;

import org.nd4j.linalg.api.ndarray.BaseSparseNDArray;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ndarray.SparseFormat;
import org.nd4j.linalg.cpu.nativecpu.SparseNDArrayCOO;
import org.nd4j.linalg.factory.BaseSparseNDArrayFactory;
import org.nd4j.linalg.factory.NDArrayFactory;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.factory.Nd4jBackend;
import org.nd4j.nativeblas.Nd4jBlas;
import org.nd4j.nativeblas.SparseNd4jBlas;

public class Proto2 {
    public static void main(String[] args) throws Exception{

        NDArrayFactory factory = Nd4j.sparseFactory();
        //INDArray montruc = factory.create(10,10);
        INDArray montruc = factory.createSparseCOO(new float[]{0f},new int[][]{{0},{0}}, new long[]{20,20});
        montruc.put(5,5, 2.0);
        System.out.println(montruc.getDouble(new int[]{1,5}));
        System.out.println(montruc.toDense());

    }
}
