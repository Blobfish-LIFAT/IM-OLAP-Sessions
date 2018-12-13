/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.Similarity.Session;

import fr.univ_tours.li.jaligon.falseto.QueryStructure.Qfset;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Session;
import fr.univ_tours.li.jaligon.falseto.Similarity.Query.QueryComparisonByJaccardAndStructureThresholdWithSeveralSelectionPerLevel;
import fr.univ_tours.li.jaligon.falseto.Similarity.Query.Similarity;

/**
 *
 * @author Julien
 */
public abstract class SessionComparison {

    /** First string to compare*/
    protected Session s1;
    /** Last string to compare*/
    protected Session s2;
    /*length of s1: number of rows**/
    protected int row;
    /*length of s2: number or columns**/
    protected int column;
    /* Threshold to state if two queries are equal**/
    protected double threshold;
    //associated with the type of similarity between query, when Edit Distance
    /* Cost for Insertion**/
    protected double insertionCost;
    /* Cost for Deletion**/
    protected double deletionCost;
    /* Cost for Substitution**/
    protected double substitutionCost;
    /* Cost for Transposition**/
    protected double transpositionCost;
    //weight for query similarity (e.g. it is used in comparison by structure or by_jaccard_structure_threshold)
    /*Weight for group by set*/
    protected double alpha;
    /*Weight for selection*/
    protected double beta;
    /*Weight for measure*/
    protected double gamma;

    public SessionComparison(Session s1, Session s2, double thre, double alpha, double beta, double gamma) {
        this.s1 = s1;
        this.s2 = s2;

        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;

        //size of the first session
        row = s1.size();
        //size of the second session
        column = s2.size();

        this.threshold = thre;
    }

    public Session getS1() {
        return s1;
    }

    public void setS1(Session s1) {
        this.s1 = s1;
    }

    public Session getS2() {
        return s2;
    }

    public void setS2(Session s2) {
        this.s2 = s2;
    }

    abstract Similarity computeSimilarity();

    /**
    This method compares two queries and it returns true if the queries can be considered equal.
     * @param q1 first query to compare
     * @param q2 second query to compare
     */
    public boolean IsEqual(Object q1, Object q2) {
        double similarity = 0;

        similarity = new QueryComparisonByJaccardAndStructureThresholdWithSeveralSelectionPerLevel((Qfset) q1, (Qfset) q2, alpha, beta, gamma).computeSimilarity().getSimilarity();

        return similarity >= threshold;
    }

    /**
    This method compares two queries and it returns the similarity degree.
     * @param q1 first query to compare
     * @param q2 second query to compare
     */
    public double similarityDegree(Object q1, Object q2) {
        double similarity = 0;
        similarity = new QueryComparisonByJaccardAndStructureThresholdWithSeveralSelectionPerLevel((Qfset) q1, (Qfset) q2, alpha, beta, gamma).computeSimilarity().getSimilarity();

        return similarity;
    }
}
