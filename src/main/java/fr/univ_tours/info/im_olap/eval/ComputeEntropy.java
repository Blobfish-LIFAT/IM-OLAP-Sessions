package fr.univ_tours.info.im_olap.eval;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.nd4j.linalg.util.MathUtils.log2;

public class ComputeEntropy {
    public static void main(String[] args) throws Exception {
        /*Files.lines(Paths.get("data/objectname_topics.csv")).map(l -> l.split(",")).forEach(l -> {
            System.out.print("Entropy for " + l[0] + " is ");
            INDArray mean = Nd4j.create(10);
            for (int i = 1; i < 11; i++) {
                mean.putScalar(i - 1, Double.parseDouble(l[i]));
            }

            double sum = 0;
            for (int i = 0; i < mean.columns(); i++) {
                sum += mean.getDouble(i)*log2(mean.getDouble(i));
            }
            sum = -sum;
            System.out.println("Entropy for " + l[0] + " is " + sum/log2(10));
        });*/

        Files.lines(Paths.get("data/userprofile.csv")).map(l -> l.split("\\|")).forEach(l -> {
            String[] tmp = l[1].split(",");
            INDArray mean = Nd4j.create(10);
            for (int i = 0; i < 10; i++) {
                mean.putScalar(i, Double.parseDouble(tmp[i]));
            }

            double sum = 0;
            for (int i = 0; i < mean.columns(); i++) {
                sum += mean.getDouble(i)*log2(mean.getDouble(i));
            }
            sum = -sum;
            System.out.println("Entropy for " + l[0] + " is " + sum/log2(10));
        });
    }
}
