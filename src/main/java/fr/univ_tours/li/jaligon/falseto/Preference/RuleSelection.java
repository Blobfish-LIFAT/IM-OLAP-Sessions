package fr.univ_tours.li.jaligon.falseto.Preference;

import fr.univ_tours.li.jaligon.falseto.QueryStructure.SelectionFragment;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Qfset;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Fragment;
import java.util.ArrayList;
import java.util.HashSet;

import weka.core.Instances;

import mondrian.olap.Member;

public class RuleSelection { // should be static?

    private int nbPrefAfterSelectCons;

    /**
     * Select the useful prefered fragment, until the number of slider is reached.
     * If a prefered fragment is not found for a slider, the confidence and support are reduced.
     * @param query the query to personalize.
     * @param slider the number of wanted preferences.
     * @param dataset the dataset.
     * @param minsup the minimum of support.
     * @param nbRulesExp the number of wanted rules.
     * @param minNbRules ??
     * @param minconf the minimum of confidence.
     * @param toolow ??
     * @return the set of useful prefered fragments.
     * @throws Exception
     */
    public Qfset selectConsWithExtraction(
            Qfset query,
            int slider,
            Instances dataset,
            Double minsup,
            int nbRulesExp,
            int minNbRules,
            Double minconf,
            Double toolow) throws Exception {
        //		System.out.println("Selecting rules");

        Qfset result = null, bestResult = null;
        Double confidence = (double) 1;
        boolean again = true;
        ArrayList<Rule> extracted = null;
        int nbConstructsPreviously = 0;
        int nbAddedPref = 0;

        while (again) {

            extracted = ExtractRulesFromLogWithFPGrowth.launchExtractionWithoutAdjustment(dataset, confidence, minsup, nbRulesExp, minNbRules, minconf, toolow);
            //System.out.println("NbRules=" + extracted.size());
            ArrayList<Rule> extractedCopy = (ArrayList<Rule>) extracted.clone();

            extractedCopy = removeNonMatching(extractedCopy, query);
            extractedCopy = removeNonPresentAttributes(extractedCopy, query);

            //System.out.println(extractedCopy.toString());

            ScoredCons setC = computeScores(extractedCopy);

            //System.out.println(setC);

            result = new Qfset();
            nbAddedPref = 0;

            while (nbAddedPref <= slider && !setC.isEmpty()) {
                Fragment cons = setC.getMax();
                setC.remove(cons);
                //System.out.println("testing:"+cons);
                if (isUseful(cons, result, query)) {

                    if (result.existSimilars(cons)) {
                        result.add(cons);
                    } else {
                        if (nbAddedPref < slider) {
                            result.add(cons);
                        }
                        nbAddedPref++;
                    }

                    //System.out.println("add cons: "+cons + "\n nbAddedPref:"+nbAddedPref);
                } else {
                    if (result.existSimilars(cons)) {
                        //System.out.println("removing:"+cons);
                        result.removeSimilars(cons);
                        nbAddedPref--;
                        setC.removeSimilars(cons);
                    }
                }
            }


            if (slider == nbAddedPref || minsup <= toolow || extracted.size() == nbRulesExp) {
                again = false;
            } else if (confidence < minconf) {//result.length<minNbRules ||
                minsup = minsup - 0.1;
                confidence = (double) 1;
            } else {
                confidence = confidence - 0.1;
            }

            if (nbAddedPref >= nbConstructsPreviously) {
                nbConstructsPreviously = nbAddedPref;
                bestResult = result;
            }
        } // end while

        //nbPrefAfterSelectCons=nbAddedPref;
        //System.out.println("Support=" + minsup);
        //System.out.println("Confidence=" + confidence);
        //System.out.println("NbRules=" + extracted.size());

        return bestResult;
    }

    /**
     * Select the useful head of rules to apply a preference in the query to personalize.
     * @param extracted the association rules.
     * @param query the query to personalize.
     * @param slider the number of wanted preferences.
     * @return the set of useful prefered fragments.
     */
    // algo 1 of the paper
    public Qfset selectCons(
            ArrayList<Rule> extracted, // specific structure? something better than ArrayList?
            Qfset query,
            int slider) {
        //System.out.println("Selecting rules");

        ArrayList<Rule> extractedCopy = (ArrayList<Rule>) extracted.clone();

        extractedCopy = removeNonMatching(extractedCopy, query);
        extractedCopy = removeNonPresentAttributes(extractedCopy, query);

        //System.out.println(extractedCopy.toString());

        ScoredCons setC = computeScores(extractedCopy);

        //System.out.println(setC);

        Qfset result = new Qfset();
        int nbAddedPref = 0;

        while (nbAddedPref <= slider && !setC.isEmpty()) {
            Fragment cons = setC.getMax();
            setC.remove(cons);
            //System.out.println("testing:"+cons);
            if (isUseful(cons, result, query)) {

                if (result.existSimilars(cons)) {
                    result.add(cons);
                } else {
                    if (nbAddedPref < slider) {
                        result.add(cons);
                    }
                    nbAddedPref++;
                }

                //System.out.println("add cons: " + cons + "\n nbAddedPref:" + nbAddedPref);
            } else {
                if (result.existSimilars(cons)) {
                    //System.out.println("removing:" + cons);
                    result.removeSimilars(cons);
                    nbAddedPref--;
                    setC.removeSimilars(cons);
                }
            }
        }
        nbPrefAfterSelectCons = nbAddedPref;
        //System.out.println("done");
        return result;
    }

    /**
     * Associate a score for each head of rules.
     * @param extracted the association rules.
     * @return a list of scores for each head of rules.
     */
    private ScoredCons computeScores(ArrayList<Rule> extracted) {
        return new ScoredCons(extracted);
    }

    /**
     * Remove the rules whose the items of the body are not in the query to personalize.
     * @param extracted the association rules.
     * @param query the query to personalize.
     * @return a new list of association rules.
     */
    private ArrayList<Rule> removeNonMatching(ArrayList<Rule> extracted, Qfset query) {
        ArrayList<Rule> clone = (ArrayList<Rule>) extracted.clone();
        for (Rule r : clone) {
            Qfset ant = r.getBody();
            if (!query.contains(ant)) {
                extracted.remove(r);
            }
        }
        return extracted;
    }

    /**
     * Remove the rules whose the items of the head are not in the query to personalize (for the projections and measures).
     * @param extracted the association rules.
     * @param query the query to personalize.
     * @return a new list of association rules.
     */
    private ArrayList<Rule> removeNonPresentAttributes(ArrayList<Rule> extracted, Qfset query) {
        ArrayList<Rule> clone = (ArrayList<Rule>) extracted.clone();
        for (Rule r : clone) {
            Fragment cons = r.getHead();
            if ((cons.getType() == 0 && !query.containProjections(cons))
                    || (cons.getType() == 2 && !query.containMeasures(cons))) {
                extracted.remove(r);
            }
        }
        return extracted;
    }

    /**
     * Test if the prefered query is useful.
     * @param cons the head of rule.
     * @param result the prefered query.
     * @param query the query to personalize.
     * @return true if the head of rule is useful for the query to personalize, false otherwise.
     */
    // algo 2 of the paper
    public boolean isUseful(Fragment cons, Qfset result, Qfset query) {
        ArrayList<Fragment> rs = result.getSimilars(cons);
        ArrayList<Fragment> qs = query.getSimilars(cons);
        rs.add(cons);
        //System.out.println(rs + " " + qs);
        if (cons.getType() == 0 || cons.getType() == 2) { // projection or measure
            if (rs.containsAll(qs)) {
                return false;
            }
        }
        if (cons.getType() == 1) { // selection
            //System.out.println("check cons: "+cons);
            HashSet<Member> mrs = new HashSet<Member>();
            HashSet<Member> mqs = new HashSet<Member>();
            for (Fragment f : rs) {
                mrs.add(((SelectionFragment) f).getValue());
                //System.out.println("rs: "+f);
            }
            for (Fragment f : qs) {
                mqs.add(((SelectionFragment) f).getValue());
                //System.out.println("qs: "+f);
            }
            //System.out.println("mqs"+mqs);
            //System.out.println("mrs"+mrs);
            if (!(mqs.containsAll(mrs) && !mqs.equals(mrs)) && !mqs.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    /**
     *
     * @return the number of useful fouhd preferences.
     */
    public int getNbPrefAfterSelectCons() {
        return nbPrefAfterSelectCons;
    }
}
