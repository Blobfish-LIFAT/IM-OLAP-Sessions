/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.Summary.Intention.Clustering;

import java.util.Enumeration;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.QuerySession;
import fr.univ_tours.li.jaligon.falseto.Similarity.Session.CalculateGap;
import fr.univ_tours.li.jaligon.falseto.Similarity.Session.Matrix;
import fr.univ_tours.li.jaligon.falseto.Similarity.Session.SmithWaterman;
import weka.core.DistanceFunction;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.neighboursearch.PerformanceStats;

/**
 *
 * @author aligon_j
 */
public class SmithWatermanDistance implements DistanceFunction{
    
    Instances data;
    String[] options;
    
    public void setInstances(Instances i) {
        data = i;
    }

    public Instances getInstances() {
        return data;
    }

    public void setAttributeIndices(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getAttributeIndices() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setInvertSelection(boolean bln) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean getInvertSelection() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double distance(Instance instnc, Instance instnc1) {
        Matrix matrix = new Matrix((QuerySession) instnc, (QuerySession) instnc1, 0.33, 0.33, 0.33);
        matrix.fillMatrix_MatchMisMatch(0.7);
        matrix.applySymmetricIncreasingFunction();
        double gap = new CalculateGap(matrix).calculateExtGap_AvgMatch();
        SmithWaterman sw = new SmithWaterman(matrix, 0, gap, (QuerySession) instnc, (QuerySession) instnc1, 0.7, 0.35, 0.5, 0.15);

        double result = (1-sw.computeSimilarity().getSimilarity());

        return result;
    }

    public double distance(Instance instnc, Instance instnc1, PerformanceStats ps) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double distance(Instance instnc, Instance instnc1, double d) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double distance(Instance instnc, Instance instnc1, double d, PerformanceStats ps) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void postProcessDistances(double[] doubles) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void update(Instance instnc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    public Enumeration listOptions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setOptions(String[] strings) throws Exception {
        options = strings;
    }

    public String[] getOptions() {
        return options;
    }
    
}
