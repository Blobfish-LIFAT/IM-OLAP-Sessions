/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.Summary.Intention.Clustering;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Log;
import fr.univ_tours.li.jaligon.falseto.summaryBrowsingDesign.SummaryGUI;

/**
 *
 * @author Salim IGUE
 */
public class Hierarchical_clust implements I_hierarchical_clust{

    Log log;

    public Hierarchical_clust(Log log) {
        this.log = log;
    }

    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }
    
    /**
     * Cette méthode permet de créer un clustering hiérarchique en fonction d'un log
     * @return le cluster représentant la racine de la hierarchie
     */
    @Override
    public Cluster CreateHierarchicalClustering() {
   
        if (log.getSessions().size() != 1) {
            HierarchicalClustering hc = null;

            try {
                hc = new HierarchicalClustering(log.getSessions());
            } catch (Exception ex) {
                Logger.getLogger(SummaryGUI.class.getName()).log(Level.SEVERE, null, ex);
            }

            final SummarizationHierarchicalTree a = new SummarizationHierarchicalTree(hc.getGraph(), log);
            a.defineCluster();
            try {
                a.defineSummaries();
            } catch (IOException ex) {
                Logger.getLogger(SummaryGUI.class.getName()).log(Level.SEVERE, null, ex);
            }

            return a.getRoot();
        } else {
            Cluster c = new Cluster();
            c.setLog(log);
            c.getSummaries().get(0);
             return c;
        }
    
    }
    
}
