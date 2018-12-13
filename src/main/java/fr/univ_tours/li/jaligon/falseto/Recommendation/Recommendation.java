package fr.univ_tours.li.jaligon.falseto.Recommendation;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeSet;

import fr.univ_tours.li.jaligon.falseto.QueryStructure.Qfset;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.QuerySession;
import fr.univ_tours.li.jaligon.falseto.Similarity.Query.QueryComparisonByJaccardAndStructureThresholdWithSeveralSelectionPerLevel;
import fr.univ_tours.li.jaligon.falseto.Similarity.Session.AlignmentIndexes;
import fr.univ_tours.li.jaligon.falseto.Similarity.Session.BaseRecommendation;
import fr.univ_tours.li.jaligon.falseto.Similarity.Session.CalculateGap;
import fr.univ_tours.li.jaligon.falseto.Similarity.Session.Matrix;
import fr.univ_tours.li.jaligon.falseto.Similarity.Session.SmithWaterman;

/**
 * Scans the scoredSessionsList and finds the highest-scored 3-queries
 * subsequence
 *
 * @author enrico.gallinucci2
 */
public class Recommendation {

    //Parameters necessary upon creation
    private QuerySession currentSession;
    private List<QuerySession> scoredSessionList;
    
    //Parameters which store results and useful informations
    private int indexOfSessionWithHighestScoredQuery = -1;
    private int indexOfHighestScoredQuery = -1;
    private int indexOfFirstRecommendedQuery = -1;
    private QuerySession recommendedSubsequence = new QuerySession("-1");
    private TreeSet<BaseRecommendation> baseRecommendations = new TreeSet<>();
    
    private SmithWaterman swRecommendation;
    private double highestScore = 0;
    private boolean recommendationFound;

    public QuerySession getRecommendedSubsequence() {
        return recommendedSubsequence;
    }

    public double getHighestScore() {
        return highestScore;
    }

    public boolean isRecommendationFound() {
        return recommendationFound;
    }

    public QuerySession getFutureOfRecommendedSession() {
        return scoredSessionList.get(indexOfSessionWithHighestScoredQuery);
    }

    public Recommendation(QuerySession currentSession, List<QuerySession> scoredSessionList) {
        this.currentSession = currentSession;
        this.scoredSessionList = scoredSessionList;
    }
    
    public boolean computeRecommendation(){
        recommendedSubsequence = new QuerySession("recommended");
        return computeRecommendationAlignedSequences();
    }
    
    /**
     * Scans the scoredSessionsList and returns the most relevant alignment
     */
    public boolean computeRecommendationAlignedSequences() {
        int indexOfMostRelevantSession = -1, startOfMostRelevantAlignment = -1, endOfMostRelevantAlignment = -1;
        float highestAvgRelevance = -1;
        HashSet<BaseRecommendation> baseRecommendationsHS = new LinkedHashSet<>();
                
        for(int i=0; i<scoredSessionList.size(); i++) {
            QuerySession session = scoredSessionList.get(i);
            HashSet<AlignmentIndexes> alignmentsIndexes = session.getAlignmentsIndexes();
            
            for(AlignmentIndexes ai : alignmentsIndexes){
                if(currentSession.intersects(session.extractSubsequence(ai.getStart(),ai.getEnd()))){
                    continue;
                }
                
                float relevance = 0;
                for(int k=ai.getStart(); k<=ai.getEnd(); k++){
                    relevance += session.get(k).getScore();
                }
                float avgRelevance = relevance / ( ai.getEnd() - ai.getStart() + 1 );
                
                
                
                if(avgRelevance > 0) {
                    baseRecommendationsHS.add(new BaseRecommendation(avgRelevance, i, ai.getStart(), ai.getEnd()));
                    recommendationFound = true;
                }
                if(avgRelevance > highestAvgRelevance){
                    highestAvgRelevance = avgRelevance;
                    indexOfMostRelevantSession = i;
                    startOfMostRelevantAlignment = ai.getStart();
                    endOfMostRelevantAlignment = ai.getEnd();
                }
            }
        }
        
        DecimalFormat f = (DecimalFormat)DecimalFormat.getInstance();
        DecimalFormatSymbols custom=new DecimalFormatSymbols();
        custom.setDecimalSeparator(',');
        f.setDecimalFormatSymbols(custom);
        
        if(recommendationFound) {
            baseRecommendations = new TreeSet(baseRecommendationsHS);
            
            recommendedSubsequence = scoredSessionList.get(indexOfMostRelevantSession)
                    .extractSubsequence(startOfMostRelevantAlignment, endOfMostRelevantAlignment);
            
            return true;
        }
        else {
            return false;
        }
    }
    
    public int getMatchedQueriesSize(){
        if(swRecommendation==null) return 0;
        else return swRecommendation.getBestSubsequenceLength();
    }

    public int getPositionOfRecommendationInFutureOfCurrentSession() {
        return swRecommendation.getFirstMatchingQueryInBestSubsequence().getRow_index_prev();
    }

    public int getPositionOfRecommendationInFutureOfLogSession() {
        //return indexOfHighestScoredQuery+1;
        return indexOfFirstRecommendedQuery + 1;
    }

    public TreeSet<BaseRecommendation> getBaseRecommendations() {
        return baseRecommendations;
    }
    
    
}