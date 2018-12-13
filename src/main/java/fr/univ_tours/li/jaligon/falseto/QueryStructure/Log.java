/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.QueryStructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import fr.univ_tours.li.jaligon.falseto.Similarity.Query.QueryComparisonByJaccardAndStructureThresholdWithSeveralSelectionPerLevel;
import fr.univ_tours.li.jaligon.falseto.Similarity.Session.CalculateGap;
import fr.univ_tours.li.jaligon.falseto.Similarity.Session.Matrix;
import fr.univ_tours.li.jaligon.falseto.Similarity.Session.SmithWaterman;
//import oracle.net.aso.i;

/**
 *
 * @author julien
 */
public class Log {

    private HashSet<QuerySession> sessions;

    public Log() {
        sessions = new HashSet<QuerySession>();
    }

    public Log(HashSet<QuerySession> sessions) {
        this.sessions = sessions;
    }

    public HashSet<QuerySession> getSessions() {
        return sessions;
    }

    public boolean add(QuerySession session) {
        return sessions.add(session);
    }

    public boolean addAll(HashSet<QuerySession> sessions) {
        return this.sessions.addAll(sessions);
    }

    public HashSet<QuerySession> select(Qfset query) {
        HashSet<QuerySession> result = new HashSet<QuerySession>();

        for (QuerySession session : sessions) {
            for (Qfset q : session.getQueries()) {
                if (query.cover(q)) {
                    result.add(session);
                    break;
                }
            }
        }

        return result;
    }
    
    public HashSet<QuerySession> select(Qfset query, double threshold) {
        HashSet<QuerySession> result = new HashSet<QuerySession>();

        for (QuerySession session : sessions) {
            for (Qfset q : session.getQueries()) {
                QueryComparisonByJaccardAndStructureThresholdWithSeveralSelectionPerLevel similarity = new QueryComparisonByJaccardAndStructureThresholdWithSeveralSelectionPerLevel(query, q, 0.35, 0.5, 0.15);
                if(similarity.computeSimilarity().getSimilarity() >= threshold){
                    result.add(session);
                    break;
                }
            }
        }

        return result;
    }

    //returns the sessions of the logs where each query q, of the sequences of queries given in the parameter, covers each query of a session of the log according to the tilted time window principle
    public HashSet<QuerySession> select(List<Qfset> queries) {

        int totalPercentage = 1;
        int multiply = 1;

        ArrayList<Double> percentages = new ArrayList<Double>();
        percentages.add(1.0);

        for (int i = 1; i < queries.size(); i++) {
            multiply *= 2;
            percentages.add((double) multiply);
            totalPercentage += multiply;
        }

        HashSet<QuerySession> result = new HashSet<QuerySession>();

        for (QuerySession session : sessions) {
            double precedent = 0;
            int id = queries.size()-1;
            int add = 0;
            for (Double d : percentages) {

                d = d / (double) totalPercentage;

                

                int begin = (int) Math.ceil(precedent * session.size());
                int end = (int) Math.ceil((precedent + d) * session.size());

                while (end <= begin) {
                    end++;
                }

                boolean coverage = true;
                
                //System.out.println(d+" "+(session.size() - 1 - begin)+" "+(session.size() - end)+" "+session.size());

                for (int i = session.size() - 1 - begin; i >= session.size() - end; i--) {
                    if (!queries.get(id).cover(session.get(i))) {
                        coverage = false;
                        break;
                    }
                }

                if (coverage) {
                    add++;
                }

                precedent += d;

                id--;
            }
            if (add == queries.size()) {
                result.add(session);
            }
            System.out.println();
        }

        return result;
    }

    //returns the list of the log sessions being similar to qs (higher than a given threshold)
    public HashSet<QuerySession> select(QuerySession qs, double threshold) {
        HashSet<QuerySession> qsList = new HashSet<>();

        double thre = 0.7;//threshold of comparison for queries and sessions (have to be the same for a normalized result)

        for (QuerySession qsl : this.getSessions()) {
            Matrix matrix = new Matrix(qs, qsl, 0.33, 0.33, 0.33);//the matrix used in Smith Waterman (based on Edit Distance if i remember well); 0.33 values should always be the same
            matrix.fillMatrix_MatchMisMatch(thre);//set the threshold for comparing queries
            matrix.applySymmetricIncreasingFunction();//for considering that the last queries of the sessions are more important for the similarity than the others

            double gap = new CalculateGap(matrix).calculateExtGap_AvgMatch();//the gap that is allowed between between two sessions
            SmithWaterman sw = new SmithWaterman(matrix, 0, gap, qs, qsl, thre, 0.35, 0.5, 0.15);//0.35 -> the weigth for the projections; 0.5 -> the weight for the selections; 0.15 -> the weight for the measures 

            double sim = sw.computeSimilarity().getSimilarity();

            if (sim >= threshold) {
                qsList.add(qsl);
            }
        }

        return qsList;
    }

    public Log copy() {
        Log l = new Log();

        for (QuerySession s : sessions) {
            l.add(s.copy());
        }

        return l;
    }
    
}
