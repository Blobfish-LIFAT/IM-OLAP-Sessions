/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.Summary.Intention.Clustering;

import java.util.ArrayList;
import java.util.List;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Log;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.QuerySession;
import fr.univ_tours.li.jaligon.falseto.Summary.Intention.Coverage.SummarizeTiltedWindow;

/**
 *
 * @author julien
 */
public class Cluster {

    private List<QuerySession> summaries;
    private Log sessions;
    private ArrayList<Cluster> children;
    private double distance;
    public String label;

    public Cluster() {
        summaries = new ArrayList<QuerySession>();
        children = new ArrayList<Cluster>();
    }

    public Cluster(Log sessions, ArrayList<Cluster> next) throws Exception {
        this.sessions = sessions;
        this.children = next;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setLog(Log sessions) {
        this.sessions = sessions;
    }

    public void setChildren(ArrayList<Cluster> children) {
        this.children = children;
    }

    public Log getLog() {
        return sessions;
    }

    public ArrayList<Cluster> getChildren() {
        return children;
    }

    public void summarize(int nbWindow){
        boolean summarize = true;
        for (QuerySession session : summaries) {
            if (session.size() == nbWindow) {
                summarize = false;
            }
        }

        if (summarize) {
            SummarizeTiltedWindow summarization = new SummarizeTiltedWindow(sessions);
            summaries.add(summarization.summarizeLog(nbWindow));
        }
    }

    public QuerySession getSummary(int nbwindow) {
        for (QuerySession session : summaries) {
            if (session.size() == nbwindow) {
                return session;
            }
        }

        return null;
    }

    public List<QuerySession> getSummaries() {
        return summaries;
    }
}
