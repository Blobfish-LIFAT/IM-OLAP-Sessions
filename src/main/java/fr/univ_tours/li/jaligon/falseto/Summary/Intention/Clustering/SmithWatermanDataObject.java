/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.Summary.Intention.Clustering;

import java.io.Serializable;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.QuerySession;
import fr.univ_tours.li.jaligon.falseto.Similarity.Session.CalculateGap;
import fr.univ_tours.li.jaligon.falseto.Similarity.Session.Matrix;
import fr.univ_tours.li.jaligon.falseto.Similarity.Session.SmithWaterman;
import weka.clusterers.forOPTICSAndDBScan.DataObjects.DataObject;
import weka.clusterers.forOPTICSAndDBScan.Databases.Database;
import weka.core.Instance;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;

/**
 *
 * @author julien
 */
public class SmithWatermanDataObject implements DataObject, Serializable, RevisionHandler {

    /**
     * Holds the original instance
     */
    private Instance instance;

    /**
     * Holds the (unique) key that is associated with this DataObject
     */
    private String key;

    /**
     * Holds the ID of the cluster, to which this DataObject is assigned
     */
    private int clusterID;

    /**
     * Holds the status for this DataObject (true, if it has been processed, else false)
     */
    private boolean processed;

    /**
     * Holds the coreDistance for this DataObject
     */
    private double c_dist;

    /**
     * Holds the reachabilityDistance for this DataObject
     */
    private double r_dist;

    /**
     * Holds the database, that is the keeper of this DataObject
     */
    private Database database;
    
    public SmithWatermanDataObject(Instance originalInstance, String key, Database database) {
        this.database = database;
        this.key = key;
        instance = originalInstance;
        clusterID = DataObject.UNCLASSIFIED;
        processed = false;
        c_dist = DataObject.UNDEFINED;
        r_dist = DataObject.UNDEFINED;
    }

    
    
    public boolean equals(DataObject d) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double distance(DataObject d) {
        Matrix matrix = new Matrix((QuerySession) this.getInstance(), (QuerySession) d.getInstance(), 0.33, 0.33, 0.33);
        matrix.fillMatrix_MatchMisMatch(0.7);
        matrix.applySymmetricIncreasingFunction();
        double gap = new CalculateGap(matrix).calculateExtGap_AvgMatch();
        SmithWaterman sw = new SmithWaterman(matrix, 0, gap, (QuerySession) this.getInstance(), (QuerySession) d.getInstance(), 0.7, 0.35, 0.5, 0.15);

        double result = (1-sw.computeSimilarity().getSimilarity());

        return result;
    }

    public Instance getInstance() {
        return this.instance;
    }

    /**
     * Returns the key for this DataObject
     * @return key
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the key for this DataObject
     * @param key The key is represented as string
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Sets the clusterID (cluster), to which this DataObject belongs to
     * @param clusterID Number of the Cluster
     */
    public void setClusterLabel(int clusterID) {
        this.clusterID = clusterID;
    }

    /**
     * Returns the clusterID, to which this DataObject belongs to
     * @return clusterID
     */
    public int getClusterLabel() {
        return clusterID;
    }

    /**
     * Marks this dataObject as processed
     * @param processed True, if the DataObject has been already processed, false else
     */
    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    /**
     * Gives information about the status of a dataObject
     * @return True, if this dataObject has been processed, else false
     */
    public boolean isProcessed() {
        return processed;
    }

    /**
     * Sets a new coreDistance for this dataObject
     * @param c_dist coreDistance
     */
    public void setCoreDistance(double c_dist) {
        this.c_dist = c_dist;
    }

    /**
     * Returns the coreDistance for this dataObject
     * @return coreDistance
     */
    public double getCoreDistance() {
        return c_dist;
    }

    /**
     * Sets a new reachability-distance for this dataObject
     */
    public void setReachabilityDistance(double r_dist) {
        this.r_dist = r_dist;
    }

    /**
     * Returns the reachabilityDistance for this dataObject
     */
    public double getReachabilityDistance() {
        return r_dist;
    }

    public String toString() {
        return instance.toString();
    }
    
    /**
     * Returns the revision string.
     * 
     * @return		the revision
     */
    public String getRevision() {
      return RevisionUtils.extract("$Revision: 1.5 $");
    }
    
}
