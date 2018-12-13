/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.Summary.Intention.Coverage;

import fr.univ_tours.li.jaligon.falseto.Generics.Generics;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import mondrian.olap.Hierarchy;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Log;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.MeasureFragment;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.ProjectionFragment;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Qfset;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.QuerySession;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.SelectionFragment;
//import fr.univ_tours.li.jaligon.falseto.logParsing.gpsj.LogParsing;

/**
 *
 * @author julien
 */
public class SummarizeTiltedWindow {

    private Log log;

    /*public SummarizeTiltedWindow(String file) throws IOException {
     Connection c = new Connection();
     c.open();

     LogParsing lp = new LogParsing(file);
     log = lp.ReadSessionLog();
     }*/
    public SummarizeTiltedWindow(Log log) {
        this.log = log;
    }

    public QuerySession summarizeLog(int nbWindow) {

        HashSet<QuerySession> sessions = log.getSessions();
        if (sessions.isEmpty()) {
            return new QuerySession("Summary");
        }

        QuerySession summary;

        if (sessions.size() == 1) {
            QuerySession session2 = sessions.iterator().next();
            summary = summarizeTiltedWindow(session2, session2, nbWindow);
        } else {
            Iterator<QuerySession> iterator = sessions.iterator();
            summary = summarizeTiltedWindow(iterator.next(), iterator.next(), nbWindow);

            while (iterator.hasNext()) {
                summary = summarizeTiltedWindow(summary, iterator.next(), nbWindow);
            }
        }

        return summary;
    }

    private QuerySession summarizeTiltedWindow(QuerySession session1, QuerySession session2, int nWindow) {
        QuerySession summary = new QuerySession("Summary");

        int currentWindow = 1;

        int nbQueriesSummarizedSession1 = 0;
        int nbQueriesSummarizedSession2 = 0;
        while (currentWindow <= nWindow) {
            ArrayList<Qfset> queries = new ArrayList<>();

            //extract the sub-session1 to be summarized
            double sizeSummarizedSubSession1 = session1.size() - nbQueriesSummarizedSession1;
            int nbQueriesToSummarizeSession1 = (int) Math.ceil((Math.pow(2, currentWindow) / Math.pow(2, nWindow)) * sizeSummarizedSubSession1);
            nbQueriesSummarizedSession1 += nbQueriesToSummarizeSession1;

            queries.addAll(session1.getQueries().subList((int)sizeSummarizedSubSession1 - nbQueriesToSummarizeSession1, (int) sizeSummarizedSubSession1));

            //extract the sub-session2 to be summarized
            double sizeSummarizedSubSession2 = session2.size() - nbQueriesSummarizedSession2;
            int nbQueriesToSummarizeSession2 = (int) Math.ceil((Math.pow(2, currentWindow) / Math.pow(2, nWindow)) * sizeSummarizedSubSession2);
            nbQueriesSummarizedSession2 += nbQueriesToSummarizeSession2;

            queries.addAll(session2.getQueries().subList((int)sizeSummarizedSubSession2 - nbQueriesToSummarizeSession2, (int) sizeSummarizedSubSession2));
            
            //compute the summary
            Qfset summarizedQuery = summarizedQueries(queries);
            summary.getQueries().add(0, summarizedQuery);
            
            currentWindow++;
        }

        return summary;
    }

    /*
     private QuerySession summarizeTiltedWindow(QuerySession session1, QuerySession session2, int nWindow) {
     QuerySession summary = new QuerySession("Summary");

     int totalPercentage = 1;
     int multiply = 1;

     ArrayList<Double> percentages = new ArrayList<Double>();
     percentages.add(1.0);

     for (int i = 1; i < nWindow; i++) {
     multiply *= 2;
     percentages.add((double) multiply);
     totalPercentage += multiply;
     }

     ArrayList<Qfset> summarizedQueries = new ArrayList<Qfset>();

     double precedentS1 = 0;

     double precedentS2 = 0;
     for (Double d : percentages) {
     ArrayList<Qfset> queries = new ArrayList<Qfset>();

     d = d / totalPercentage;

     //System.out.println(d + " " + session1.size() + " " + session2.size());

     int begin = (int) Math.ceil(precedentS1 * session1.size());
     int end = (int) Math.ceil((precedentS1 + d) * session1.size());

     if(nWindow == 14)
     {
     System.out.println("begin "+begin+" end "+end);
     }
            
     while (end <= begin) {
     end++;
     }

     for (int i = session1.size() - 1 - begin; i >= session1.size() - end; i--) {
     queries.add(session1.get(i));
     }

     if (session2.size() != 0) {
     int begin2 = (int) Math.ceil(precedentS2 * session2.size());
     int end2 = (int) Math.ceil((precedentS2 + d) * session2.size());

     while (end2 <= begin2) {
     end2++;
     }

     for (int i = session2.size() - 1 - begin2; i >= session2.size() - end2; i--) {
     queries.add(session2.get(i));
     }
     }

     precedentS1 += d;
     precedentS2 += d;

     Qfset summarizedQuery = summarizedQueries(queries);

     summarizedQueries.add(0, summarizedQuery);
     }

     summary.addAll(summarizedQueries);

     return summary;
     }
     */
    private Qfset summarizedQueries(List<Qfset> queries) {
        Qfset firstQuery = queries.get(0);
        queries.remove(0);

        return summarizeQuerySet(firstQuery, queries);
    }

    /*private HashSet<QuerySession> summarizeQuerySessionsV2(QuerySession session1, QuerySession session2) {
     QuerySession s1;
     QuerySession s2;


     if (session1.size() <= session2.size()) {
     s1 = session1.copy();
     s2 = session2.copy();
     } else {
     s1 = session2.copy();
     s2 = session1.copy();
     }

     HashSet<QuerySession> result = new HashSet<QuerySession>();
     QuerySession s = new QuerySession("summmary");
     summarizeAllPossibilitiesQuerySessions(s1, s2, s, result);

     return result;
     }*/

    /*private void summarizeAllPossibilitiesQuerySessions(QuerySession session1, QuerySession session2, QuerySession s, HashSet<QuerySession> result) {

     if (session1.size() == 1) {
     //System.out.println("Session 1: " + session1.size());
     //System.out.println("Session 2: " + session2.size());

     QuerySession se = new QuerySession("summary");
     se.getQueries().addAll(s.getQueries());
     Qfset summary = summarizeQuerySet(session1.get(0), session2.getQueries());
     if (!summary.isQTop()) {
     se.add(summary);


     int count = 0;
     boolean add = true;
     for (QuerySession sess : result) {
     if (sess.size() == se.size()) {
     for (int i = 0; i < sess.size(); i++) {
     if (sess.get(i).isEqual(se.get(i))) {
     count++;
     }
     }
     if (se.size() == count) {
     add = false;
     break;
     }
     }
     }

     if (add) {
     result.add(se);
     }
     //System.out.println();
     }
     } else {
     //for (int j=0;j<session1.size()-1;j++) {
     int limit = session2.size() - session1.size() + 1;
     ArrayList<Qfset> queries = new ArrayList<Qfset>();

     for (int i = 0; i < limit; i++) {
     QuerySession se = new QuerySession("summary");
     se.getQueries().addAll(s.getQueries());

     queries.add(session2.get(i));

     //System.out.println("Session 1: " + session1.size());
     //System.out.println("Session 1: " + session1);
     //System.out.println("Session 2: " + session2.size());
     //System.out.println("Session 2: " + session2);

     Qfset summary = summarizeQuerySet(session1.get(0), queries);

     //if (!summary.isQTop()) {
     se.add(summary);
     QuerySession s1 = new QuerySession("Temp1");
     s1.getQueries().addAll(session1.getQueries());
     s1.getQueries().remove(session1.get(0));

     QuerySession s2 = new QuerySession("Temp2");
     s2.getQueries().addAll(session2.getQueries());
     s2.getQueries().removeAll(queries);

     summarizeAllPossibilitiesQuerySessions(s1, s2, se, result);
     //}
     }
     //}
     }
     }*/
    private Qfset summarizeQuerySet(Qfset q, List<Qfset> queries) {
        Qfset summary = q.copy();

        for (Qfset query : queries) {
            summary = summarizePair(summary, query);
        }

        return summary;
    }

    private Qfset summarizePair(Qfset Qfset1, Qfset Qfset2) {
        Qfset summary = new Qfset();

        HashSet<ProjectionFragment> attributes = suppGroupBySet(Qfset1, Qfset2);
        HashSet<MeasureFragment> measures = suppMeasures(Qfset1, Qfset2);
        HashSet<SelectionFragment> selections = suppSelections(Qfset1, Qfset2);

        summary.addAllAttributes(attributes);
        summary.addAllMeasures(measures);
        summary.addAllSelectionPredicates(selections);

        return summary;
    }

    private HashSet<ProjectionFragment> suppGroupBySet(Qfset Qfset1, Qfset Qfset2) {
        HashSet<ProjectionFragment> attributes = new HashSet<ProjectionFragment>();

        for (ProjectionFragment attributeQ1 : Qfset1.getAttributes()) {
            Hierarchy dimension = attributeQ1.getHierarchy();

            ProjectionFragment attributeQ2 = Qfset2.getAttributeFromHierarchy(dimension);

            if (attributeQ1.getLevel().getDepth() <= attributeQ2.getLevel().getDepth()) {
                attributes.add(attributeQ1);
            } else {
                attributes.add(attributeQ2);
            }
        }

        return attributes;
    }

    private HashSet<MeasureFragment> suppMeasures(Qfset Qfset1, Qfset Qfset2) {
        HashSet<MeasureFragment> measures = new HashSet<MeasureFragment>();

        for (MeasureFragment m1 : Qfset1.getMeasures()) {
            for (MeasureFragment m2 : Qfset2.getMeasures()) {
                if (m1.isEqual(m2)) {
                    measures.add(m1);
                    break;
                }
            }
        }

        return measures;
    }

    private HashSet<SelectionFragment> suppSelections(Qfset Qfset1, Qfset Qfset2) {
        HashSet<SelectionFragment> selections = new HashSet<SelectionFragment>();

        for (SelectionFragment s1 : Qfset1.getSelectionPredicates()) {
            for (SelectionFragment s2 : Qfset2.getSelectionPredicates()) {
                if (s1.isEqual(s2)) {
                    selections.add(s1);
                    break;
                }
            }
        }

        return selections;
    }

    public void saveLogFile() {
        PrintWriter sortie = null;

        try {
            FileWriter f = new FileWriter(Generics.webPath + "Summaries/summary_" + getNumberQueries() + "queries_" + getNumberSessions() + "sessions.txt");
            BufferedWriter buffer = new BufferedWriter(f);
            sortie = new PrintWriter(buffer);

            for (QuerySession s : log.getSessions()) {
                QuerySession qs = (QuerySession) s;
                for (int i = 0; i < qs.size(); i++) {
                    sortie.println("#" + i + "\n" + ((Qfset) qs.get(i)).toString());
                }
            }

            sortie.println("#end");

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(
                    "File Error");
        } finally {
            sortie.close();
        }

    }

    private int getNumberQueries() {
        int nbqueries = 0;
        for (QuerySession QuerySession : log.getSessions()) {
            QuerySession qs = (QuerySession) QuerySession;
            nbqueries += qs.size();
        }

        return nbqueries;
    }

    private int getNumberSessions() {
        return log.getSessions().size();
    }

}
