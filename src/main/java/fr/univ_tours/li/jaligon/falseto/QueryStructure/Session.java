/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.QueryStructure;
//import fr.univ_tours.li.jaligon.falseto.queryStructure.GPSJQuery;
import java.util.HashSet;
import java.util.List;
import fr.univ_tours.li.jaligon.falseto.Similarity.Session.AlignmentIndexes;
import weka.core.Instance;

/**
 *
 * @author Elisa
 */
public abstract class Session  extends Instance{

    private static final long serialVersionUID = 1L;
    public String id;
    protected String template;
    protected List<Qfset> queries;
    protected HashSet<AlignmentIndexes> alignmentsIndexes;

    public Session(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public List<Qfset> getQueries() {
        return queries;
    }
    
    public HashSet<AlignmentIndexes> getAlignmentsIndexes() {
        return alignmentsIndexes;
    }
    
    public void addAlignmentIndexes(AlignmentIndexes alignmentIndexes){
        this.alignmentsIndexes.add(alignmentIndexes);
    }

    @Override
    public abstract String toString();

    public abstract Qfset get(int i);

    public abstract boolean add(Qfset q);

    public abstract int size();

    public abstract boolean cover(Session s);
    
    public abstract Session extractSubsequence(int start);
    
    public abstract Session extractSubsequence(int start, int end);
}
