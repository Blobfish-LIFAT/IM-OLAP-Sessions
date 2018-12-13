package fr.univ_tours.li.jaligon.falseto.Preference;

import fr.univ_tours.li.jaligon.falseto.QueryStructure.Fragment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

// a couple cons,score
public class ScoredCons {

    private HashMap<Fragment, Double> tuples;

    /**
     * Create a new empty set of couple "head,score".
     */
    public ScoredCons() {
        tuples = new HashMap<Fragment, Double>();
    }

    /**
     * Associate a score to an head of rule.
     * The more similar head of rules are, the higher score will be.
     * @param extracted the association rules.
     */
    // not very efficient
    // should sort extracted by cons first
    public ScoredCons(ArrayList<Rule> extracted) {
        this();
        int i = 0;
        while (i < extracted.size()) {
            Rule r = extracted.get(i);
            Fragment cons = r.getHead();
            if (!tuples.containsKey(cons) && i != extracted.size()) {
                int count = 1;
                double confSum = r.getConfidence();
                int j = i + 1;
                while (j < extracted.size()) {
                    Rule s = extracted.get(j);
                    if (r.getHead().isEqual(s.getHead())) {
                        count++;
                        confSum += s.getConfidence();
                    }
                    j++;
                }
                double score = confSum / count;
                tuples.put(cons, score);
            }
            i++;
        }
    }

    /**
     * Test if the list of head of rules/score is empty.
     * @return true if the list is empty, false otherwise.
     */
    public boolean isEmpty() {
        return tuples.isEmpty();
    }

    /**
     *
     * @return the head of rules whose its score is the max score among all heads of rules.
     */
    public Fragment getMax() {
        Set<Fragment> keySet = tuples.keySet();
        Fragment max = null;
        float maxScore = 0;
        for (Fragment f : keySet) {
            if (tuples.get(f) > maxScore) {
                max = f;
            }
        }
        return max;
    }

    /**
     * Remove an head of rule.
     * @param cons2 the head of the rule to remove.
     */
    public void remove(Fragment cons2) {
        tuples.remove(cons2);
    }

    /**
     * Remove the similar heads of rules.
     * @param cons2
     */
    public void removeSimilars(Fragment cons2) {
        Set<Fragment> keySet = tuples.keySet();
        for (Fragment f : keySet) {
            if (f.isSimilar(cons2)) {
                tuples.remove(f);
            }
        }

    }

    /**
     * 
     * @return the string of the set of heads of rules.
     */
    @Override
    public String toString() {
        return tuples.toString();
    }
}
