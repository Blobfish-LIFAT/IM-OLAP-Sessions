package fr.univ_tours.li.jaligon.falseto.Recommendation;

import java.util.ArrayList;
import java.util.List;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Qfset;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.QuerySession;
import fr.univ_tours.li.jaligon.falseto.Similarity.Query.Similarity;
import fr.univ_tours.li.jaligon.falseto.Similarity.Session.CalculateGapInverseSigmoid;
import fr.univ_tours.li.jaligon.falseto.Similarity.Session.MatrixInverseSigmoid;
import fr.univ_tours.li.jaligon.falseto.Similarity.Session.SmithWatermanInverseSigmoid;

/**
 * Computes the alignment of the logSessions with the currentSession and
 * stores the future of the logSessions that have been succesfully aligned.
 * It uses the SmithWatermanInverseSigmoid algorithm in order to align the
 * logSessions to the current query of the currentSession. 
 * 
 * @author Julien (algorithm)
 * @author Enrico (class)
 */
public class Alignment {

    private List<QuerySession> sessionList;
    private QuerySession currentSession;
    private List<QuerySession> futureOfAlignedSessionsList;
    private List<ArrayList<ArrayList<Qfset>>> alignementList;//get the list of local alignement between the current session and the log session
    
    /**
     * @param sessionList List of all sessions (which includes the current session)
     * @param currentSession Current session (which is shorter than the one in the sessionList)
     * @param currentSessionIndex Index of the currentSession in the sessionList 
     */
    public Alignment(List<QuerySession> sessionList, QuerySession currentSession){
        this.sessionList = sessionList;
        this.currentSession = currentSession;
        futureOfAlignedSessionsList = new ArrayList<>();
        alignementList = new ArrayList<>();
    }
    
    public double a=0, b=0, c=0, d=0, e=0, f=0, it=0;
    public double ba=0, bb=0, bc=0, bz=0;
    private double begin;
    /**
     * Aligns the logSessions to the currentSession
     */
    public void computeAlignment(){
        for (int i=0; i<sessionList.size(); i++){
            
            double thre = 0.7;//threshold of comparison for queries and sessions (have to be the same for a normalized result)
 
            begin = System.currentTimeMillis();
            MatrixInverseSigmoid matrix = new MatrixInverseSigmoid(currentSession, sessionList.get(i), 0.33, 0.33, 0.33);//the matrix used in Smith Waterman (based on Edit Distance if i remember well); 0.33 values should always be the same
            a += System.currentTimeMillis() - begin;
            begin = System.currentTimeMillis();
            matrix.fillMatrix_MatchMisMatch(thre);//set the threshold for comparing queries
            b += System.currentTimeMillis() - begin;
            //bz += matrix.z; ba += matrix.a; bb += matrix.b; bc += matrix.c; 
            
            begin = System.currentTimeMillis();
            matrix.applySymmetricIncreasingFunction();//for considering that the last queries of the sessions are more important for the similarity than the others
            c += System.currentTimeMillis() - begin;

            begin = System.currentTimeMillis();
            double gap = new CalculateGapInverseSigmoid(matrix).calculateExtGap_AvgMatch();//the gap that is allowed between between two sessions
            d += System.currentTimeMillis() - begin;
            begin = System.currentTimeMillis();
            SmithWatermanInverseSigmoid sw = new SmithWatermanInverseSigmoid(matrix, 0, gap, currentSession, sessionList.get(i), thre, 0.35, 0.5, 0.15);//0.35 -> the weigth for the projections; 0.5 -> the weight for the selections; 0.15 -> the weight for the measures 
            e += System.currentTimeMillis() - begin;
            
            begin = System.currentTimeMillis();
            Similarity sim = sw.computeSimilarity();     
            f += System.currentTimeMillis() - begin;
            //System.out.println((System.nanoTime()-begin));
            
            //it++;
            if(sim.getSimilarity()>0)
            	alignementList.add(sw.getAlignment());
            
            //System.out.println(sw.getAlignment());
            
            QuerySession futureOfLogSession = sw.getFutureLogQueries(SmithWatermanInverseSigmoid.ALIGNMENT_TYPE.DISCARD_NOT_ALIGNED_TO_CURRENT_QUERY);
            if(futureOfLogSession.size()>0) futureOfAlignedSessionsList.add(futureOfLogSession);
        }
    }
    
    public List<QuerySession> getFutureOfAlignedSessionsList(){
        return futureOfAlignedSessionsList;
    }

    public List<ArrayList<ArrayList<Qfset>>> getAlignementList() {
        return alignementList;
    }
}
