/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.Similarity.Session;

/**
 *
 * @author enrico.gallinucci2
 */
public class BaseRecommendation implements Comparable<BaseRecommendation> {
    
    private float avgRelevance;
    private int indexOfMostRelevantSession;
    private int startOfMostRelevantAlignment;
    private int endOfMostRelevantAlignment;

    public BaseRecommendation(float avgRelevance, int indexOfMostRelevantSession, int startOfMostRelevantAlignment, int endOfMostRelevantAlignment) {
        this.avgRelevance = avgRelevance;
        this.indexOfMostRelevantSession = indexOfMostRelevantSession;
        this.startOfMostRelevantAlignment = startOfMostRelevantAlignment;
        this.endOfMostRelevantAlignment = endOfMostRelevantAlignment;
    }

    public float getAvgRelevance() {
        return avgRelevance;
    }

    public void setAvgRelevance(float avgRelevance) {
        this.avgRelevance = avgRelevance;
    }

    public int getIndexOfMostRelevantSession() {
        return indexOfMostRelevantSession;
    }

    public void setIndexOfMostRelevantSession(int indexOfMostRelevantSession) {
        this.indexOfMostRelevantSession = indexOfMostRelevantSession;
    }

    public int getStartOfMostRelevantAlignment() {
        return startOfMostRelevantAlignment;
    }

    public void setStartOfMostRelevantAlignment(int startOfMostRelevantAlignment) {
        this.startOfMostRelevantAlignment = startOfMostRelevantAlignment;
    }

    public int getEndOfMostRelevantAlignment() {
        return endOfMostRelevantAlignment;
    }

    public void setEndOfMostRelevantAlignment(int endOfMostRelevantAlignment) {
        this.endOfMostRelevantAlignment = endOfMostRelevantAlignment;
    }
    
    
    /**
     * Compares BaseRecommendations to order them in descendent order
     * @param br the baseRecommendation to compare to "this"
     * @return 
     */
    @Override
    public int compareTo(BaseRecommendation br) {
        return this.avgRelevance > br.avgRelevance ? -1
                : this.avgRelevance < br.avgRelevance ? 1
                : (
                    this.indexOfMostRelevantSession < br.indexOfMostRelevantSession ? 1
                    : this.indexOfMostRelevantSession > br.indexOfMostRelevantSession ? 1
                    : (
                        this.startOfMostRelevantAlignment < br.startOfMostRelevantAlignment ? 1
                        : this.startOfMostRelevantAlignment > br.startOfMostRelevantAlignment ? 1
                        : (
                            this.endOfMostRelevantAlignment < br.endOfMostRelevantAlignment ? 1
                            : this.endOfMostRelevantAlignment > br.endOfMostRelevantAlignment ? 1
                            : 0
                        )
                    )
                );
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + Float.floatToIntBits(this.avgRelevance);
        hash = 41 * hash + this.indexOfMostRelevantSession;
        hash = 41 * hash + this.startOfMostRelevantAlignment;
        hash = 41 * hash + this.endOfMostRelevantAlignment;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BaseRecommendation other = (BaseRecommendation) obj;
        if (Float.floatToIntBits(this.avgRelevance) != Float.floatToIntBits(other.avgRelevance)) {
            return false;
        }
        if (this.indexOfMostRelevantSession != other.indexOfMostRelevantSession) {
            return false;
        }
        if (this.startOfMostRelevantAlignment != other.startOfMostRelevantAlignment) {
            return false;
        }
        if (this.endOfMostRelevantAlignment != other.endOfMostRelevantAlignment) {
            return false;
        }
        return true;
    }
    
    
}
