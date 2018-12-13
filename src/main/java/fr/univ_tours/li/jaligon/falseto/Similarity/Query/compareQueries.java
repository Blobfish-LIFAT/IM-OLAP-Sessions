/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.Similarity.Query;

import fr.univ_tours.li.jaligon.falseto.QueryStructure.Qfset;

/**
 *
 * @author Salim IGUE
 */
public class compareQueries implements I_compareQueries {

    private Qfset q1;
    private Qfset q2;

    public compareQueries(Qfset q1, Qfset q2) {
        this.q1 = q1;
        this.q2 = q2;
    }

    public Qfset getQ1() {
        return q1;
    }

    public void setQ1(Qfset q1) {
        this.q1 = q1;
    }

    public Qfset getQ2() {
        return q2;
    }

    public void setQ2(Qfset q2) {
        this.q2 = q2;
    }
    
            
            
            
    @Override
    public double compareTwoQueriesByJaccard() {
        double similarity = 0;
        similarity = new Jaccard(q1, q2, 0.35, 0.5, 0.15).computeSimilarity().getSimilarity();
       return similarity;
    
    }

    @Override
    public double compareTwoQueriesByJaccardAndStructureThresholdWithSeveralSelectionPerLevel() {
        double similarity = 0;
    similarity= new QueryComparisonByJaccardAndStructureThresholdWithSeveralSelectionPerLevel(q1, q2, 0.35, 0.5, 0.15).computeSimilarity().getSimilarity();
       return similarity;
    }
    
}
