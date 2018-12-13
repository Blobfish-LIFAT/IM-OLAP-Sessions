/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.searching;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Log;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Qfset;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.QuerySession;
import fr.univ_tours.li.jaligon.falseto.Similarity.Query.QueryComparisonByJaccardAndStructureThresholdWithSeveralSelectionPerLevel;

/**
 *
 * @author Salim Igue
 */
public class log_searching implements I_search {

    HashSet<QuerySession> log;

    public log_searching(HashSet<QuerySession> log) {
        this.log = log;
    }

    public HashSet<QuerySession> getLog() {
        return log;
    }

    public void setLog(HashSet<QuerySession> log) {
        this.log = log;
    }
    
    
    @Override
    public QuerySession sessionGenerationLogSearch(HashSet<QuerySession> question) {
        
    QuerySession result = new QuerySession("synthetic");

        List<QuerySession> questionList = new ArrayList<>(question);
        Random r = new Random();
        
        QuerySession randomSession = questionList.get(r.nextInt(questionList.size()));
        
        Qfset initialQuery = randomSession.get(0);
        result.add(initialQuery);

        boolean stop = false;

        int probability = 12;

        Log l = new Log(log);
        
        while (!stop) {
            int size = result.size();

            HashSet<QuerySession> subsetLog = l.select(result.get(result.size() - 1));

            if (!subsetLog.isEmpty()) {
                double maxSim = 0;
                Qfset closestQuery = null;

                for (QuerySession qs : subsetLog) {
                    for (Qfset q : qs.getQueries()) {
                        Qfset currentQuery = result.get(result.size() - 1);

                        boolean next = false;
                        for (Qfset qR : result.getQueries()) {
                            if (qR.isEqual(q)) {
                                next = true;
                            }
                        }

                        if (next) {
                            continue;
                        }

                        QueryComparisonByJaccardAndStructureThresholdWithSeveralSelectionPerLevel comp = new QueryComparisonByJaccardAndStructureThresholdWithSeveralSelectionPerLevel(q, currentQuery, 0.35, 0.5, 0.15);
                        double sim = comp.computeSimilarity().getSimilarity();
                        if (sim > maxSim) {
                            maxSim = sim;
                            closestQuery = q;
                        }
                    }
                }

                if(closestQuery != null)
                result.add(closestQuery);
            }

            if (result.size() == size || 0 == r.nextInt(probability)) {
                stop = true;
            } else {
                probability--;
            }
        }
        return result; }
    
}
