/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.QueryStructure;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author Elisa
 */
public class QuerySession extends Session {

    /*Ordered list of queries in a session**/
    private List<Qfset> queryList;
    private List<Qfset> relevantQueries;
    private int idQuestion;

    public QuerySession(String id) {
        super(id);
        this.queryList = new ArrayList<Qfset>();
        this.alignmentsIndexes = new HashSet<>();
        relevantQueries = new ArrayList<>();
    }

    public QuerySession(QuerySession s) {
        super(s.getId());
        this.queryList = s.queryList;
        this.template = s.template;
        this.alignmentsIndexes = new HashSet<>();
    }

    public QuerySession(List<Qfset> queries, String id) {
        super(id);
        this.queryList = queries;
        this.alignmentsIndexes = new HashSet<>();
    }

    public QuerySession(Qfset query, String id) {
        super(id);
        if (query == null) {
            query = new Qfset();
        }
        this.queryList.add(query);
        this.alignmentsIndexes = new HashSet<>();
    }

    @Override
    public List<Qfset> getQueries() {
        return queryList;
    }

    public void setQueries(List<Qfset> queries) {
        this.queryList = queries;
    }

    public void setQuery(int id, Qfset q) {
        this.queryList.set(id, q);
    }

    /**
     * Return the query in the session at the position specified by the input
     * parameter 'index'
     *
     * @param index position of query in the session
     * @return the requested GPSJQuery
     */
    public Qfset get(int index) {
        return queryList.get(index);
    }

    public int getId(Qfset query) {
        return queryList.indexOf(query);
    }

    public void remove(int queryId) {
        queryList.remove(queryId);
    }

    public void remove(Qfset q) {
        queryList.remove(q);
    }

    /**
     * Method to add a new query to the current session. The query will be added
     * at the bottom of the session
     */
    public boolean add(Qfset newQuery) {
        return this.queryList.add(newQuery);
    }

    public boolean addAll(List<Qfset> queries) {
        return this.queryList.addAll(queries);
    }

    public int size() {
        return this.queryList.size();
    }

    public String toString() {
        String result = "Session " + id + "\n";
        for (Qfset q : queryList) {
            result += q + "\n";
        }
        result += "END SESSION";
        return result;
    }

    public boolean isEqual(QuerySession s) {
        if (this.queryList.size() != s.size()) {
            return false;
        }

        for (int i = 0; i < this.queryList.size(); i++) {
            if (!queryList.get(i).isEqual(s.get(i))) {
                return false;
            }
        }

        return true;
    }

    public boolean cover(Session s) {
        /*if (this.getId().equals(s.getId())) {
         //System.out.println(s);
         return true;
         }*/

        QuerySession qs = (QuerySession) s;
        if (this.queryList.size() > qs.queryList.size()) {
            return false;
        }

        int partition = 0;
        for (Qfset q1 : this.queryList) {
            ArrayList<Qfset> qfsetList = new ArrayList<Qfset>();

            for (int i = partition; i < qs.size(); i++) {
                /*if(SummarizeWIthCoverage.test)
                 {
                 System.out.println("Partitition : "+partition+" \n Coverage between "+q1+" and "+qs.get(i)+" = "+q1.cover(qs.get(i)));
                 }*/
                if (q1.belongsQTop()) {
                    if (q1.isEqual(qs.get(i))) {
                        qfsetList.add(qs.get(i));
                        partition = (i + 1);
                        break;
                    } else {
                        return false;
                    }
                }

                if (q1.cover(qs.get(i))) {

                    qfsetList.add(qs.get(i));
                } else {
                    partition = i;
                    if (qfsetList.isEmpty()) {
                        return false;
                    }

                    break;
                }
            }

            if (qfsetList.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public List<Qfset> coverage(Qfset query, Session s) {
        /*if (this.getId().equals(s.getId())) {
         //System.out.println("THIS : "+this);
         //System.out.println("SESSION : "+s);

         List<Qfset> list = new ArrayList<Qfset>();
         list.add(query);
         return list;
         }*/

        QuerySession qs = (QuerySession) s;

        int partition = 0;
        for (Qfset q1 : qs.getQueries()) {
            ArrayList<Qfset> qfsetList = new ArrayList<Qfset>();

            for (int i = partition; i < this.size(); i++) {

                if (q1.belongsQTop()) {
                    if (q1.isEqual(this.get(i))) {
                        qfsetList.add(this.get(i));
                        partition = (i + 1);
                        break;
                    }
                } else {
                    if (q1.cover(this.get(i))) {
                        qfsetList.add(this.get(i));
                    } else {
                        partition = i;
                        break;
                    }
                }
            }

            if (q1 == query) {
                return qfsetList;
            }
        }

        return null;
    }

    public QuerySession copy() {
        QuerySession qs = new QuerySession(this.id);

        for (Qfset qfset : this.queryList) {
            qs.add(qfset.copy());
        }

        return qs;
    }

    public Attribute attribute(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Attribute attributeSparse(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Attribute classAttribute() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int classIndex() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean classIsMissing() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double classValue() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Instances dataset() {

        return this.m_Dataset;
    }

    public void deleteAttributeAt(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Enumeration enumerateAttributes() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean equalHeaders(Instance instnc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String equalHeadersMsg(Instance instnc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean hasMissingValue() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int index(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void insertAttributeAt(int i) {
    }

    public boolean isMissing(int i) {
        return false;
    }

    public boolean isMissingSparse(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isMissing(Attribute atrbt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Instance mergeInstance(Instance instnc) {
        return null;
    }

    public int numAttributes() {
        return 1;
    }

    public int numClasses() {
        return 0;
    }

    public int numValues() {
        return 0;
    }

    public void replaceMissingValues(double[] doubles) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setClassMissing() {
    }

    public void setClassValue(double d) {
    }

    public void setValue(int i, double d) {
    }

    public void setValueSparse(int i, double d) {
    }

    public double[] toDoubleArray() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String toStringNoWeight(int i) {
        return this.id;
    }

    public String toStringNoWeight() {
        return this.id;
    }

    public String toStringMaxDecimalDigits(int i) {
        return this.id;
    }

    public String toString(int i, int i1) {
        return this.id;
    }

    public String toString(Attribute atrbt, int i) {
        return this.id;
    }

    public double value(int i) {
        return Integer.valueOf(this.id);
    }

    public double valueSparse(int i) {
        return 0;
    }

    public double value(Attribute atrbt) {
        return 0;
    }

    /**
     * Extracts a subsequence of queries from the session.
     *
     * @param start Index of the first query to extract
     * @return The new session with the extarced queries
     *
     * @author Enrico
     */
    @Override
    public QuerySession extractSubsequence(int start) {
        return this.extractSubsequence(start, this.queryList.size() - 1);
    }

    /**
     * Extracts a subsequence of queries from the session.
     *
     * @param start Index of the first query to extract
     * @param end Index of the last query to extract
     * @return The new session with the extarced queries
     *
     * @author Enrico
     */
    @Override
    public QuerySession extractSubsequence(int start, int end) {
        QuerySession extractedSession = new QuerySession(this.id);
        extractedSession.setTemplate(this.template);

        if (!(start > end || start < 0 || end >= this.queryList.size())) {
            for (int i = start; i <= end; i++) {
                extractedSession.add(this.get(i));
            }
        }
        return extractedSession;
    }

    /**
     * Return the value of highest score in the session's queries
     *
     * @return
     */
    public double getMaxScore() {
        double maxScore = -1;
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).getScore() > maxScore) {
                maxScore = this.get(i).getScore();
            }
        }
        return maxScore;
    }

    public boolean intersects(QuerySession s2) {
        for (int i = 0; i < this.size(); i++) {
            for (int j = 0; j < s2.size(); j++) {
                if (this.get(i).equals(s2.get(j))) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isTop() {
        for (Qfset q : queryList) {
            if (!q.isqTop()) {
                return false;
            }
        }

        return true;
    }

    public List<Qfset> getRelevantQueries() {
        return relevantQueries;
    }

    public void setRelevantQueries(List<Qfset> relevantQueries) {
        this.relevantQueries = relevantQueries;
    }

    public int getIdQuestion() {
        return idQuestion;
    }

    public void setIdQuestion(int nbQuestion) {
        this.idQuestion = nbQuestion;
    }
    
}
