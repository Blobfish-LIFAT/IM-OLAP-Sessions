/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.QueryStructure;

/**
 * Different version of Qfset, built for debug purpose only (in particolar,
 * to compute the pairwise similarity in a list of queries from different
 * sessions).
 * 
 * @author enrico.gallinucci2
 */
public class QfsetForComparison {
    
    private String session;
    private String template;
    private Qfset qfset;
    private int progressive;
    
    public QfsetForComparison(String session, int progressive, String template, Qfset qfset){
        this.session = session;
        this.progressive = progressive;
        this.template = template;
        this.qfset = qfset;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public Qfset getQfset() {
        return qfset;
    }

    public void setQfset(Qfset qfset) {
        this.qfset = qfset;
    }

    public int getProgressive() {
        return progressive;
    }

    public void setProgressive(int progressive) {
        this.progressive = progressive;
    }
    
    
}
