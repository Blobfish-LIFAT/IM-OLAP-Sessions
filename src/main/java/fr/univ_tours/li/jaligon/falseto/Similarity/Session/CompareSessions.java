/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package fr.univ_tours.li.jaligon.falseto.Similarity.Session;

import fr.univ_tours.li.jaligon.falseto.QueryStructure.QuerySession;

/**
 *
 * @author 21308124t
 */
public class CompareSessions implements I_compareSession{
    private QuerySession session1;
    private QuerySession session2;
    
    public CompareSessions(QuerySession session1, QuerySession session2) {
        this.session1 = session1;
        this.session2 = session2;
    }
    /**
     * Cette méthode permet de calculer la similarité entre deux sessions
     * en utilisant l'algorithme de SmithWaterman
     * @return la similarité correspondante
     */
    @Override
    public double compareTwoSessionsBySW() {
        double thre = 0.7;//threshold of comparison for queries and sessions (have to be the same for a normalized result)
        Matrix matrix = new Matrix(session1, session2, 0.33, 0.33, 0.33);//the matrix used in Smith Waterman (based on Edit Distance if i remember well); 0.33 values should always be the same
        matrix.fillMatrix_MatchMisMatch(thre);//set the threshold for comparing queries
        //matrix.applySymmetricIncreasingFunction();//for considering that the last queries of the sessions are more important for the similarity than the others
        double gap = new CalculateGap(matrix).calculateExtGap_AvgMatch();//the gap that is allowed between two sessions
        SmithWaterman sw = new SmithWaterman(matrix, 0, gap, session1, session2, thre, 0.35, 0.5, 0.15);//0.35 -> the weigth for the projections; 0.5 -> the weight for the selections; 0.15 -> the weight for the measures
        return sw.computeSimilarity().getSimilarity();
    }
    /**
     * Cette méthode permet de calculer la similarité entre deux sessions
     * en utilisant l'algorithme de SmithWaterman Sigmoid
     * @return la similarité correspondante
     */
    @Override
    public double compareTwoSessionsBySWSigmoid() {
        double thre = 0.7;//threshold of comparison for queries and sessions (have to be the same for a normalized result)
        MatrixInverseSigmoid matrix = new MatrixInverseSigmoid(session1, session2, 0.33, 0.33, 0.33);//the matrix used in Smith Waterman (based on Edit Distance if i remember well); 0.33 values should always be the same
        matrix.fillMatrix_MatchMisMatch(thre);//set the threshold for comparing queries
        //matrix.applySymmetricIncreasingFunction();//for considering that the last queries of the sessions are more important for the similarity than the others
        double gap = new CalculateGapInverseSigmoid(matrix).calculateExtGap_AvgMatch();//the gap that is allowed between two sessions
        SmithWatermanInverseSigmoid sw = new SmithWatermanInverseSigmoid(matrix, 0, gap, session1, session2, thre, 0.35, 0.5, 0.15);//0.35 -> the weigth for the projections; 0.5 -> the weight for the selections; 0.15 -> the weight for the measures
        return sw.computeSimilarity().getSimilarity();
    }
    
    public QuerySession getSession1() {
        return session1;
    }
    
    public void setSession1(QuerySession session1) {
        this.session1 = session1;
    }
    
    public QuerySession getSession2() {
        return session2;
    }
    
    public void setSession2(QuerySession session2) {
        this.session2 = session2;
    }
    
}
