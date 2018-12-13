/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.Similarity.Query;

/**
 *
 * @author Elisa
 */
public class StructureSimilarity extends Similarity {

    private double measureSimilarity;
    private double groupBySimilarity;
    private double selectionCriteriaSimilarity;

    private double alpha;
    private double beta;
    private double gamma;
    

    public StructureSimilarity(double measureSimilarity, double groupBySimilarity, double selectCriteriaSimilarity, double alpha, double beta, double gamma) {
        this.measureSimilarity = measureSimilarity;
        this.groupBySimilarity = groupBySimilarity;
        this.selectionCriteriaSimilarity = selectCriteriaSimilarity;
        //System.out.println("Measure : "+measureSimilarity);
        //System.out.println("GB : "+groupBySimilarity);
        //System.out.println("Sel : "+selectCriteriaSimilarity);
        this.alpha=alpha;
        this.beta=beta;
        this.gamma=gamma;
    }

    public StructureSimilarity() {
        this.groupBySimilarity = -1;
        this.measureSimilarity = -1;
        this.selectionCriteriaSimilarity = -1;
    }

    public double sumSimilarityValue() {
        return alpha * groupBySimilarity + gamma * measureSimilarity + beta * selectionCriteriaSimilarity;
    }

    public double getGroupBySimilarity() {
        return groupBySimilarity;
    }

    public void setGroupBySimilarity(double groupBySimilarity) {
        this.groupBySimilarity = groupBySimilarity;
    }

    public double getMeasureSimilarity() {
        return measureSimilarity;
    }

    public void setMeasureSimilarity(double measureSimilarity) {
        this.measureSimilarity = measureSimilarity;
    }

    public double getSelectionCriteriaSimilarity() {
        return selectionCriteriaSimilarity;
    }

    public void setSelectionCriteriaSimilarity(double selectionCriteriaSimilarity) {
        this.selectionCriteriaSimilarity = selectionCriteriaSimilarity;
    }

    public double getSimilarity() {

        return (double) this.sumSimilarityValue()/(alpha+beta+gamma);
        //return (double)(measureSimilarity*0.20+groupBySimilarity*0.30+selectionCriteriaSimilarity*0.50);
    }    
}
