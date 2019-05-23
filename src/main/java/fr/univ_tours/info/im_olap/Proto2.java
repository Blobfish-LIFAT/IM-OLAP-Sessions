package fr.univ_tours.info.im_olap;


import fr.univ_tours.info.im_olap.data.DopanLoader;
import fr.univ_tours.info.im_olap.data.Labels;
import fr.univ_tours.info.im_olap.model.Session;
import fr.univ_tours.info.im_olap.model.SessionGraph;
import java.util.List;

import static fr.univ_tours.info.im_olap.Proto1.dataDir;

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

        List<Session> sessions = SessionGraph.fixSessions(DopanLoader.loadDir(dataDir), "data/cubeSchemas/DOPAN_DW3.xml");
        Labels.addLabels(sessions, "data/labels/dopanCleanLogWithVeronikaLabels-FOCUS.csv", "veronikaLabel");
        Labels.addLabels(sessions, "data/labels/metricScoresWithSalimAndIandryLabels.csv", "salimLabel", "iandryLabel");

    }
}
