/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.Similarity.Query;

import fr.univ_tours.li.jaligon.falseto.Generics.Generics.QueryComparisonType;
import static fr.univ_tours.li.jaligon.falseto.Generics.Generics.QueryComparisonType.BY_JACCARD_STRUCTURE;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Qfset;

/**
 *
 * @author Julien
 */
public class QueryComparison {

    protected Qfset query1;
    protected Qfset query2;
    //Weight for the group by set
    protected double alpha;
    //Weight for the selection
    protected double beta;
    //Weight for the measure
    protected double gamma;
    

    public QueryComparison(Qfset query1, Qfset query2, double alpha, double beta, double gamma) {
        this.query1 = query1;
        this.query2 = query2;
        this.alpha=alpha;
        this.beta=beta;
        this.gamma=gamma;
        
    }

    public Qfset getQuery1() {
        return query1;
    }

    public void Qfset(Qfset query1) {
        this.query1 = query1;
    }

    public Qfset getQuery2() {
        return query2;
    }

    public void setQuery2(Qfset query2) {
        this.query2 = query2;
    }
    
    public Similarity computeSimilarity() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("You cannot computeSimilarity in the superclass");};

    public QueryComparison QueryComparisonFactory(QueryComparisonType queryComparisonType)
    {
        switch(queryComparisonType)
        {
            case BY_JACCARD_STRUCTURE:
                return new QueryComparisonByJaccardAndStructureThresholdWithSeveralSelectionPerLevel(query1, query2, alpha, beta, gamma);
            default:
                return new QueryComparisonByJaccardAndStructureThresholdWithSeveralSelectionPerLevel(query1, query2, alpha, beta, gamma);
        }
    }
}
