package fr.univ_tours.li.jaligon.falseto.Recommendation;

import java.util.List;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.QuerySession;
import fr.univ_tours.li.jaligon.falseto.Similarity.Session.CalculateGap;
import fr.univ_tours.li.jaligon.falseto.Similarity.Session.Matrix;
import fr.univ_tours.li.jaligon.falseto.Similarity.Session.SmithWatermanWithScore;

/**
 * Computes the pairwise similarity between the given sessions and gives a
 * score to each query based on the alignment: the higher the score, the
 * more alignments found. The score of each query is saved in the property
 * "score" of the respective queries in the sessionList given as input.
 * 
 * @author enrico.gallinucci2
 */
public class Scoring {
    
    private List<QuerySession> sessionList;
    
    public List<QuerySession> getSessionList(){
        return sessionList;
    }
    
    /**
     * @param sessionsList List of sessions to be scored
     */
    public Scoring(List<QuerySession> sessionList){
        this.sessionList = sessionList;
    }
    
    /**
     * Computes the pairwise similarity between the given sessions and gives a
     * score to each query based on the alignment (the higher the score, the
     * more alignments found). The score of each query is saved in the property
     * "score" of the respective queries in the sessionList.
     */
    public void computeScoring(){
        initializeScores();
        for (int i = 0; i < sessionList.size(); i++) {
            for (int j = i + 1; j < sessionList.size(); j++) {
                double begin = System.nanoTime();
                double thre = 0.7;//threshold of comparison for queries and sessions (have to be the same for a normalized result)
                Matrix matrix = new Matrix(sessionList.get(i), sessionList.get(j), 0.33, 0.33, 0.33);//the matrix used in Smith Waterman (based on Edit Distance if i remember well); 0.33 values should always be the same
                matrix.fillMatrix_MatchMisMatch(thre);//set the threshold for comparing queries

                double gap = new CalculateGap(matrix).calculateExtGap_AvgMatch();//the gap that is allowed between two sessions
                SmithWatermanWithScore sw = new SmithWatermanWithScore(matrix, 0, gap, sessionList.get(i), sessionList.get(j), thre, 0.35, 0.5, 0.15);//0.35 -> the weigth for the projections; 0.5 -> the weight for the selections; 0.15 -> the weight for the measures 
                sw.computeSimilarityAndScore();
                //System.out.println(System.nanoTime()-begin);
            }
        }
    }
    
    /**
     * Resets the score of each query of each session.
     */
    private void initializeScores(){
        for (int i = 0; i < sessionList.size(); i++) {
            for (int j = 0; j < sessionList.get(i).size(); j++) {
                sessionList.get(i).get(j).resetScore();
            }
        }
    }
}
