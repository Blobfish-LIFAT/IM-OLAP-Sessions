/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.Similarity.Session;

/**
 *
 * @author enrico.gallinucci2
 */
public class AlignmentIndexes {

    
    
    private int sessionIndex;
    private int start;
    private int end;
    
    public AlignmentIndexes(int end) {
        this.end = end;
    }
    
    public void setStart(int start) {
        this.start = start;
    }
    
    public void setSessionIndex(int sessionIndex) {
        this.sessionIndex = sessionIndex;
    }
    
    public int getStart(){
        return start;
    }
    
    public int getEnd(){
        return end;
    }
    
    public int getSessionIndex(){
        return sessionIndex;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + this.sessionIndex;
        hash = 71 * hash + this.start;
        hash = 71 * hash + this.end;
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
        final AlignmentIndexes other = (AlignmentIndexes) obj;
        if (this.sessionIndex != other.sessionIndex) {
            return false;
        }
        if (this.start != other.start) {
            return false;
        }
        if (this.end != other.end) {
            return false;
        }
        return true;
    }

    
}
