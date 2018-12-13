/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.Recommendation;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import mondrian.olap.Level;

import fr.univ_tours.li.jaligon.falseto.QueryStructure.Fragment;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.MeasureFragment;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.ProjectionFragment;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Qfset;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.QuerySession;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.SelectionFragment;
import fr.univ_tours.li.jaligon.falseto.Similarity.Query.QueryComparisonByJaccardAndStructureThresholdWithSeveralSelectionPerLevel;
import fr.univ_tours.li.jaligon.falseto.Similarity.Session.CalculateGap;
import fr.univ_tours.li.jaligon.falseto.Similarity.Session.Matrix;
import fr.univ_tours.li.jaligon.falseto.Similarity.Session.SmithWaterman;

/**
 * Computes the Alignment-Scoring-Recommendation-Adaptation algorithm. A better
 * name could be found for this class.
 *
 * @author enrico.gallinucci2
 */
public class ASRA {

    //Parameters necessary upon creation
    private QuerySession currentSession;
    private List<QuerySession> sessionList;

    //Objects necessary in order to compute each algorithm
    private Alignment alignment;
    private Scoring scoring;
    private Recommendation recommendation;
    private Adaptation adaptation;

    //Parameters which store results and useful informations
    private boolean recommendationFound;

    public ASRA(List<QuerySession> sessionList, QuerySession currentSession) {
        this.sessionList = sessionList;
        this.currentSession = currentSession;
    }

    public ASRA(HashSet<QuerySession> sessionList, List<Qfset> currentSession) {
        ArrayList<QuerySession> list = new ArrayList<>(sessionList);
        this.sessionList = list;

        QuerySession current = new QuerySession(currentSession, "current_session");
        this.currentSession = current;
    }

    public QuerySession computeASRA() throws Exception {
        alignment = new Alignment(sessionList, currentSession);
        alignment.computeAlignment();

        scoring = new Scoring(alignment.getFutureOfAlignedSessionsList());
        scoring.computeScoring();

        recommendation = new Recommendation(currentSession, scoring.getSessionList());
        recommendation.computeRecommendation();

        if (recommendation.getRecommendedSubsequence().size() == 0) {
            recommendationFound = false;
        } else {
            recommendationFound = true;
        }

        if (recommendationFound) {

            adaptation = new Adaptation(recommendation.getBaseRecommendations(), scoring.getSessionList(), currentSession,
                    alignment.getAlignementList(), sessionList);
            boolean recommendationAdapted = adaptation.computeAdaptation();

            if (recommendationAdapted) {
                return wellFormed(adaptation.getAdaptedSession());
            }
        }

        return null;
    }

    private QuerySession wellFormed(QuerySession qs) {
        for (Qfset q : qs.getQueries()) {
            for (SelectionFragment sf : q.getSelectionPredicates()) {
                HashSet<ProjectionFragment> pf = q.getAttributesFromHierarchy(sf.getLevel().getHierarchy());
                Level lp = pf.iterator().next().getLevel();

                if (lp.getDepth() < sf.getLevel().getDepth()) {
                    int distance = sf.getLevel().getDepth() - lp.getDepth();
                    q.drillDownLevel(lp, distance);
                }

            }
        }
        return qs;
    }

    public boolean isRecommendationFound() {
        return recommendationFound;
    }

    private double measureObviousness(QuerySession currentSession, QuerySession recommendedSession) {
        double nbSimilarQueries = 0;

        for (Qfset qReco : recommendedSession.getQueries()) {
            HashSet<Fragment> fragmentsReco = new HashSet<>();
            fragmentsReco.addAll(qReco.getAttributes());
            fragmentsReco.addAll(qReco.getMeasures());
            fragmentsReco.addAll(qReco.getSelectionPredicates());

            for (Qfset qCurrent : currentSession.getQueries()) {
                HashSet<Fragment> fragmentsCurrent = new HashSet<>();
                fragmentsCurrent.addAll(qCurrent.getAttributes());
                fragmentsCurrent.addAll(qCurrent.getMeasures());
                fragmentsCurrent.addAll(qCurrent.getSelectionPredicates());

                if (fragmentsReco.containsAll(fragmentsCurrent) && fragmentsCurrent.containsAll(fragmentsReco)) {
                    nbSimilarQueries++;
                }
            }

        }

        return nbSimilarQueries / ((double) currentSession.size() + (double) recommendedSession.size() - (double) nbSimilarQueries);

    }

    private double getNbSimilarQueries(QuerySession currentSession, QuerySession recommendedSession) {

        double nbSimilarQueries = 0;

        for (Qfset qReco : recommendedSession.getQueries()) {
            HashSet<Fragment> fragmentsReco = new HashSet<>();
            fragmentsReco.addAll(qReco.getAttributes());
            fragmentsReco.addAll(qReco.getMeasures());
            fragmentsReco.addAll(qReco.getSelectionPredicates());

            for (Qfset qCurrent : currentSession.getQueries()) {
                HashSet<Fragment> fragmentsCurrent = new HashSet<>();
                fragmentsCurrent.addAll(qCurrent.getAttributes());
                fragmentsCurrent.addAll(qCurrent.getMeasures());
                fragmentsCurrent.addAll(qCurrent.getSelectionPredicates());

                if (fragmentsReco.containsAll(fragmentsCurrent) && fragmentsCurrent.containsAll(fragmentsReco)) {
                    nbSimilarQueries++;
                }
            }

        }

        return nbSimilarQueries / (double) recommendedSession.getQueries().size();
    }

    private double measureConsistency(QuerySession recommendedSubsequence) {
        //System.out.println("RECOMMENDED WITH ADAPT"+recommendedSubsequence);
        double result = 0;

        for (int i = 0; i < recommendedSubsequence.size() - 1; i++) {
            Qfset query1 = recommendedSubsequence.get(i);
            Qfset query2 = recommendedSubsequence.get(i + 1);

            result += OLAPdifference(query1, query2);
        }

        return result / ((double) recommendedSubsequence.size() - 1.0);
    }

    private int OLAPdifference(Qfset q1, Qfset q2) {
        int nb = 0;
        boolean isPresent;

        for (MeasureFragment mf1 : q1.getMeasures()) {
            isPresent = false;
            for (MeasureFragment mf2 : q2.getMeasures()) {
                if (mf1.getAttribute() == mf2.getAttribute()) {
                    isPresent = true;
                }
            }

            if (!isPresent) {
                nb++;
            }
        }

        for (MeasureFragment mf1 : q2.getMeasures()) {
            isPresent = false;
            for (MeasureFragment mf2 : q1.getMeasures()) {
                if (mf1.getAttribute() == mf2.getAttribute()) {
                    isPresent = true;
                }
            }

            if (!isPresent) {
                nb++;
            }
        }

        for (SelectionFragment sf1 : q1.getSelectionPredicates()) {
            isPresent = false;
            for (SelectionFragment sf2 : q2.getSelectionPredicates()) {
                if (sf1.getLevel() == sf2.getLevel() && sf1.getValue() == sf2.getValue()) {
                    isPresent = true;
                }
            }
            if (!isPresent) {
                nb++;
            }
        }

        for (SelectionFragment sf1 : q2.getSelectionPredicates()) {
            isPresent = false;
            for (SelectionFragment sf2 : q1.getSelectionPredicates()) {
                if (sf1.getLevel() == sf2.getLevel()) {//for counting 1 time if a selection value has been changed (detected just before)
                    isPresent = true;
                }
            }
            if (!isPresent) {
                nb++;
            }
        }

        for (ProjectionFragment pf1 : q1.getAttributes()) {
            for (ProjectionFragment pf2 : q2.getAttributes()) {
                if (pf1.getLevel().getHierarchy() == pf2.getLevel().getHierarchy()) {
                    nb += Math.abs(pf1.getLevel().getDepth() - pf2.getLevel().getDepth());
                }
            }
        }

        return nb;
    }

    private double measureForesight(QuerySession currentSession, QuerySession recommendedSession) {
        Qfset lastCS = currentSession.getQueries().get(currentSession.getQueries().size() - 1);
        //Qfset firstRS = recommendedSession.getQueries().get(0);
        Qfset lastRS = recommendedSession.getQueries().get(recommendedSession.getQueries().size() - 1);

        QueryComparisonByJaccardAndStructureThresholdWithSeveralSelectionPerLevel qc = new QueryComparisonByJaccardAndStructureThresholdWithSeveralSelectionPerLevel(lastRS, lastCS, 0.35, 0.5, 0.15);
        double sim = qc.computeSimilarity().getSimilarity();

        return 1.0 - sim;
    }

    private double measureForesightWeighted(QuerySession currentSession, QuerySession recommendedSession) {
        Qfset lastCS = currentSession.getQueries().get(currentSession.getQueries().size() - 1);
        //Qfset firstRS = recommendedSession.getQueries().get(0);
        Qfset lastRS = recommendedSession.getQueries().get(recommendedSession.getQueries().size() - 1);

        QueryComparisonByJaccardAndStructureThresholdWithSeveralSelectionPerLevel qc = new QueryComparisonByJaccardAndStructureThresholdWithSeveralSelectionPerLevel(lastRS, lastCS, 0.35, 0.5, 0.15);
        double sim1 = 1.0 - qc.computeSimilarity().getSimilarity();

        double thre = 0.7;
        Matrix matrix = new Matrix(currentSession, recommendedSession, 0.33, 0.33, 0.33);//the matrix used in Smith Waterman (based on Edit Distance if i remember well); 0.33 values should always be the same
        matrix.fillMatrix_MatchMisMatch(thre);//set the threshold for comparing queries
        matrix.applySymmetricIncreasingFunction();//for considering that the last queries of the sessions are more important for the similarity than the others
        double gap = new CalculateGap(matrix).calculateExtGap_AvgMatch();//the gap that is allowed between between two sessions
        SmithWaterman sw = new SmithWaterman(matrix, 0, gap, currentSession, recommendedSession, thre, 0.35, 0.5, 0.15);//0.35 -> the weigth for the projections; 0.5 -> the weight for the selections; 0.15 -> the weight for the measures 
        double sim2 = 1.0 - sw.computeSimilarity().getSimilarity();

        return sim1 * sim2;
    }

    private double measureForesightAverage(QuerySession currentSession, QuerySession recommendedSession) {
        Qfset lastCS = currentSession.getQueries().get(currentSession.getQueries().size() - 1);
        double avgForesight = 0;

        for (int i = 0; i < recommendedSession.getQueries().size(); i++) {
            Qfset queryRS = recommendedSession.getQueries().get(i);

            QueryComparisonByJaccardAndStructureThresholdWithSeveralSelectionPerLevel qc = new QueryComparisonByJaccardAndStructureThresholdWithSeveralSelectionPerLevel(queryRS, lastCS, 0.35, 0.5, 0.15);
            double sim = qc.computeSimilarity().getSimilarity();

            avgForesight += (1.0 - sim) / recommendedSession.getQueries().size();
        }

        return avgForesight;
    }

    private double measureNovelty(QuerySession recommendedSessions) {

        double result = 0;

        for (QuerySession sessionLog : sessionList) {
            double thre = 0.7;//threshold of comparison for queries and sessions (have to be the same for a normalized result)

            Matrix matrix = new Matrix(recommendedSessions, sessionLog, 0.33, 0.33, 0.33);//the matrix used in Smith Waterman (based on Edit Distance if i remember well); 0.33 values should always be the same
            matrix.fillMatrix_MatchMisMatch(thre);//set the threshold for comparing queries
            matrix.applySymmetricIncreasingFunction();//for considering that the last queries of the sessions are more important for the similarity than the others

            double gap = new CalculateGap(matrix).calculateExtGap_AvgMatch();//the gap that is allowed between between two sessions
            SmithWaterman sw = new SmithWaterman(matrix, 0, gap, recommendedSessions, sessionLog, thre, 0.35, 0.5, 0.15);//0.35 -> the weigth for the projections; 0.5 -> the weight for the selections; 0.15 -> the weight for the measures 
            result += 1.0 - sw.computeSimilarity().getSimilarity();
        }

        return result / (double) sessionList.size();

    }

    private double measureNoveltyMinimum(QuerySession recommendedSessions) {

        double resultMin = Double.MAX_VALUE;

        for (QuerySession sessionLog : sessionList) {
            double thre = 0.7;//threshold of comparison for queries and sessions (have to be the same for a normalized result)
            double result;
            Matrix matrix = new Matrix(recommendedSessions, sessionLog, 0.33, 0.33, 0.33);//the matrix used in Smith Waterman (based on Edit Distance if i remember well); 0.33 values should always be the same
            matrix.fillMatrix_MatchMisMatch(thre);//set the threshold for comparing queries
            matrix.applySymmetricIncreasingFunction();//for considering that the last queries of the sessions are more important for the similarity than the others

            double gap = new CalculateGap(matrix).calculateExtGap_AvgMatch();//the gap that is allowed between between two sessions
            SmithWaterman sw = new SmithWaterman(matrix, 0, gap, recommendedSessions, sessionLog, thre, 0.35, 0.5, 0.15);//0.35 -> the weigth for the projections; 0.5 -> the weight for the selections; 0.15 -> the weight for the measures 
            result = 1.0 - sw.computeSimilarity().getSimilarity();

            if (result < resultMin) {
                resultMin = result;
            }
        }

        return resultMin;

    }

    private double measureAdaptation(QuerySession currentSession, QuerySession rsAdapted) {

        HashSet<Fragment> fragmentsAdapted = new HashSet<>();

        for (Qfset q : rsAdapted.getQueries()) {
            fragmentsAdapted.addAll(q.getAttributes());
            fragmentsAdapted.addAll(q.getMeasures());
            fragmentsAdapted.addAll(q.getSelectionPredicates());
        }

        HashSet<Fragment> fragments = new HashSet<>();

        for (Qfset q : currentSession.getQueries()) {
            fragments.addAll(q.getAttributes());
            fragments.addAll(q.getMeasures());
            fragments.addAll(q.getSelectionPredicates());
        }

        HashSet<Fragment> intersection = new HashSet<>();

        for (Fragment f : fragments) {
            if (fragmentsAdapted.contains(f) && !intersection.contains(f)) {
                intersection.add(f);
            }
        }

        return (double) intersection.size() / (double) fragments.size();
    }

    public List<QuerySession> getSessionList() {
        return sessionList;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public Scoring getScoring() {
        return scoring;
    }

    public Recommendation getRecommendation() {
        return recommendation;
    }

    public QuerySession getCurrentSession() {
        return currentSession;
    }

    public Adaptation getAdaptation() {
        return adaptation;
    }
}
