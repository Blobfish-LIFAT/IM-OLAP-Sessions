package fr.univ_tours.li.jaligon.falseto.Preference;

import fr.univ_tours.li.jaligon.falseto.QueryStructure.SelectionFragment;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Qfset;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.ProjectionFragment;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Fragment;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.MeasureFragment;
import java.util.ArrayList;
import java.util.HashSet;

import weka.associations.Apriori;
import weka.associations.AprioriItemSet;
import weka.associations.ItemSet;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class ExtractRulesFromLogWithApriori {

    /**
     * Create the attributes, which are the names of projection, selection and measure fragments, and set their values to 'yes' according to these fragments.
     * @param theLogs the set of Qfset of a log.
     * @return an Instances, the dataset used by Weka.
     */
    public static Instances preprocess(ArrayList<Qfset> theLogs) {
        //System.out.println("preprocessing...");
        // one attribute per fragment
        FastVector attributes = new FastVector(ProjectionFragment.getAllProjections().size()
                + SelectionFragment.getAllSelections().size()
                + MeasureFragment.getAllMeasures().size());
        FastVector attValues = new FastVector();
        attValues.addElement("yes");
        //attValues.addElement("no");

        for (ProjectionFragment p : ProjectionFragment.getAllProjections().values()) {
            attributes.addElement(new Attribute(p.toString(), attValues));

        }

        for (SelectionFragment p : SelectionFragment.getAllSelections().values()) {
            attributes.addElement(new Attribute(p.toString(), attValues));
        }

        for (MeasureFragment p : MeasureFragment.getAllMeasures().values()) {
            attributes.addElement(new Attribute(p.toString(), attValues));
        }

        Instances dataset = new Instances("logs", attributes, theLogs.size());


        for (Qfset q : theLogs) {
            			//SparseInstance inst=new SparseInstance(attributes.size());
            Instance inst = new Instance(attributes.size());
            //DenseInstance inst = new DenseInstance(attributes.size());

            HashSet<ProjectionFragment> projections = q.getAttributes();
            HashSet<SelectionFragment> selections = q.getSelectionPredicates();
            HashSet<MeasureFragment> measures = q.getMeasures();


            for (ProjectionFragment p : projections) {
                for (int j = 0; j < attributes.size(); j++) {
                    if (p.toString().compareTo(((Attribute) attributes.elementAt(j)).name()) == 0) {
                        inst.setValue(((Attribute) attributes.elementAt(j)), "yes");
                    }
                }

            }
            for (SelectionFragment p : selections) {
                for (int j = 0; j < attributes.size(); j++) {
                    if (p.toString().compareTo(((Attribute) attributes.elementAt(j)).name()) == 0) {
                        inst.setValue((Attribute) attributes.elementAt(j), "yes");
                    }
                }
            }
            for (MeasureFragment p : measures) {
                for (int j = 0; j < attributes.size(); j++) {
                    if (p.toString().compareTo(((Attribute) attributes.elementAt(j)).name()) == 0) {
                        inst.setValue((Attribute) attributes.elementAt(j), "yes");
                    }
                }
            }

            dataset.add(inst);
            inst.setDataset(dataset);
        }

        //print(attributes,dataset);

        //System.out.println("done");
        return dataset;

    }

    /**
     * Launch the Apriori algorithm (implemented by Weka).
     * @param dataset the dataset
     * @param confidence the confidence
     * @param minsup the minimal support
     * @param nbRulesExp the number of wanted rules.
     * @param minNbRules ??
     * @param minconf the minimal confidence.
     * @param toolow ??
     * @return the result of Apriori algorithm.
     * @throws Exception
     */
    public static Apriori extract(Instances dataset,
            Double confidence,
            Double minsup,
            Integer nbRulesExp,
            Integer minNbRules,
            Double minconf,
            Double toolow) throws Exception {


        Apriori apriori = new Apriori();

        String supString = minsup.toString();
        String confString = confidence.toString();
        String nbString = nbRulesExp.toString();
        String[] options = {"-N", nbString, "-T", "0", "-C", confString, "-D", "0.05", "-U", "1.0", "-M", supString, "-S", "-1.0", "-c", "-1", "-I", "yes"};

        apriori.setOptions(options);
        apriori.buildAssociations(dataset);


        return apriori;
    }

    /**
     * Launch the Apriori algorithm, by adjusting the confidence (by step of 0.1).
     * If the confidence is lower than the minimum confidence, then the support is reduced to 0.1 and the confidence is adjusted to 1.
     * @param dataset the dataset
     * @param minsup the minimum support
     * @param nbRulesExp the number of wanted rules.
     * @param minNbRules ??
     * @param minconf the minimal confidence.
     * @param toolow ??
     * @return the result of Apriori algorithm.
     * @throws Exception
     */
    public static Apriori adjustConfidence(Instances dataset,
            Double minsup,
            Integer nbRulesExp,
            Integer minNbRules,
            Double minconf,
            Double toolow) throws Exception {

        //System.out.print("Adjusting confidence");
        boolean allCovered = false;
        boolean changes = true;
        Double confidence = (double) 1;
        Apriori apriori = null;
        int nbIterations = 0;

        while (!allCovered && changes) {
            Instances datasetCopy = new Instances(dataset);
            //System.out.println("confidence=" + confidence);
            //ArrayList<Rule> extracted=new ArrayList<Rule>();
            apriori = new Apriori();

            String supString = minsup.toString();
            String confString = confidence.toString();
            String nbString = nbRulesExp.toString();
            String[] options = {"-N", nbString, "-T", "0", "-C", confString, "-D", "0.05", "-U", "1.0", "-M", supString, "-S", "-1.0", "-c", "-1", "-I", "yes"};

            apriori.setOptions(options);
            apriori.buildAssociations(dataset);

            FastVector[] result = apriori.getAllTheRules();

            int nbRules = result[1].size();

            for (int i = 0; i < nbRules; i++) {

                ItemSet body = (AprioriItemSet) result[0].elementAt(i);
                ItemSet head = (ItemSet) result[1].elementAt(i);
                ArrayList<String> headItems = getItems(head, dataset);

                int[] toDelete = new int[datasetCopy.numInstances()];//why toDelete is never used ? (except for the assignement).
                int k = 0;
                for (int j = 0; j < datasetCopy.numInstances(); j++) {
                    if (body.containedBy(datasetCopy.instance(j)) && head.containedBy(datasetCopy.instance(j))
                            && datasetCopy.numInstances() != 0
                            && headItems.size() == 1 //	&& (Double) result[2].elementAt(i)==1
                            ) {
                        //dataset.delete(j);//cannot modify while explored
                        toDelete[k++] = j;
                    }
                }
                for (int j = k - 1; j >= 0; j--) {
                    datasetCopy.delete(j);
                }

            }

            nbIterations++;
            if (datasetCopy.numInstances() == 0) {
                allCovered = true;
            } else if (minsup < toolow) {
                changes = false;
            } else if (confidence < minconf) {//result.length<minNbRules ||
                minsup = minsup - 0.1;
                confidence = (double) 1;
            } else {
                confidence = confidence - 0.1;
            }

        }
        //System.out.println("done: confidence is: " + confidence);
        //System.out.println("support is: " + minsup);
        return apriori;
    }

    /**
     * Launch the Apriori algorithm, by adjusting the confidence (by step of 0.1).
     * The Qfset of the query which will be personalize, is used to remove the rules which are not covered by this Qfset.
     * @param qfs the Qfset of the query to personalize.
     * @param dataset the dataset.
     * @param minsup the minimum support.
     * @param nbRulesExp the number of wanted rules.
     * @param minNbRules ??
     * @param minconf the minimum confidence.
     * @param toolow ??
     * @return the result of Apriori algorithm.
     * @throws Exception
     */
    public static Apriori adjustConfidenceWithQfset(
            Qfset qfs,
            Instances dataset,
            Double minsup,
            Integer nbRulesExp,
            Integer minNbRules,
            Double minconf,
            Double toolow) throws Exception {

        //System.out.print("Adjusting confidence");
        boolean allCovered = false;
        boolean changes = true;
        Double confidence = (double) 1;
        Apriori apriori = null;
        int nbIterations = 0;

        while (!allCovered && changes) {
            Instances datasetCopy = new Instances(dataset);
            //System.out.println("confidence=" + confidence);
            apriori = new Apriori();

            String supString = minsup.toString();
            String confString = confidence.toString();
            String nbString = nbRulesExp.toString();
            String[] options = {"-N", nbString, "-T", "0", "-C", confString, "-D", "0.05", "-U", "1.0", "-M", supString, "-S", "-1.0", "-c", "-1", "-I", "yes"};

            apriori.setOptions(options);
            apriori.buildAssociations(dataset);

            FastVector[] result = apriori.getAllTheRules();

            int nbRules = result[1].size();

            for (int i = 0; i < nbRules; i++) {

                ItemSet body = (AprioriItemSet) result[0].elementAt(i);
                ItemSet head = (ItemSet) result[1].elementAt(i);
                ArrayList<String> headItems = getItems(head, dataset);
                headItems.addAll(getItems(head, dataset));

                ArrayList<String> queryItems = qfs.listStrings();

                int[] toDelete = new int[datasetCopy.numInstances()];
                int k = 0;
                for (int j = 0; j < datasetCopy.numInstances(); j++) {
                    if (body.containedBy(datasetCopy.instance(j)) && head.containedBy(datasetCopy.instance(j))
                            && datasetCopy.numInstances() != 0
                            && headItems.size() == 1
                            && queryItems.containsAll(headItems) //	&& (Double) result[2].elementAt(i)==1
                            ) {
                        //dataset.delete(j);//cannot modify while explored
                        toDelete[k++] = j;
                    }
                }
                for (int j = k - 1; j >= 0; j--) {
                    datasetCopy.delete(j);
                }

            }

            nbIterations++;
            if (datasetCopy.numInstances() == 0) {
                allCovered = true;
            } else if (minsup < toolow) {
                changes = false;
            } else if (confidence < minconf) {//result.length<minNbRules ||
                minsup = minsup - 0.1;
                //System.out.println("support=" + minsup);
                confidence = (double) 1;
            } else {
                confidence = confidence - 0.1;
            }

        }
        //System.out.println("done: confidence is: " + confidence);
        //System.out.println("support is: " + minsup);
        return apriori;
    }

    /**
     * Launch the extraction of association rules with the Apriori Algorithm and taking into account the Qfset of the query to personalize.
     * This Qfset is used to remove the rules which are not covered by this Qfset in {@link #adjustConfidenceWithQfset(Qfset qfs, Instances dataset, Double minsup, Integer nbRulesExp, Integer minNbRules, Double minconf, Double toolow)}.
     * @param qfs the Qfset of the query to personalize.
     * @param dataset the dataset.
     * @param minsup the minimum support.
     * @param nbRulesExp the number of wanted rules.
     * @param minNbRules ??
     * @param minconf the minimum confidence.
     * @param toolow ??
     * @return a list of association rules (in the Rule class format).
     * @throws Exception
     */
    public static ArrayList<Rule> launchExtractionWithQfs(
            Qfset qfs,
            Instances dataset,
            Double minsup,
            int nbRulesExp,
            int minNbRules,
            Double minconf,
            Double toolow) throws Exception {
        //System.out.println("Launching extraction");

        Apriori apriori = adjustConfidenceWithQfset(qfs, dataset, minsup, nbRulesExp, minNbRules, minconf, toolow);

        // print results
        //System.out.println(apriori);

        return rulesToFragments(apriori, dataset);
    }

    /**
     * Launch the extraction of association rules using only the Apriori algorithm.
     * @param dataset the dataset.
     * @param confidence the confidence.
     * @param minsup the minimum support.
     * @param nbRulesExp the number of wanted rules.
     * @param minNbRules ??
     * @param minconf the minimum confidence.
     * @param toolow ??
     * @return a list of association rules (in the Rule class format).
     * @throws Exception
     */
    public static ArrayList<Rule> launchExtractionWithoutAdjustment(Instances dataset,
            Double confidence,
            Double minsup,
            int nbRulesExp,
            int minNbRules,
            Double minconf,
            Double toolow) throws Exception {
        //System.out.println("Launching extraction");
        Apriori apriori = extract(dataset, confidence, minsup, nbRulesExp, minNbRules, minconf, toolow);
        //System.out.println(apriori);
        //System.out.println("Extraction done");
        return rulesToFragments(apriori, dataset);
    }

    /**
     * Launch the extraction of association rules using the Apriori algorithm and adjusting the confidence.
     * @param dataset the dataset.
     * @param minsup the minimum support.
     * @param nbRulesExp the number of wanted rules.
     * @param minNbRules ??
     * @param minconf the minimum confidence.
     * @param toolow ??
     * @return a list of association rules (in the Rule class format).
     * @throws Exception
     */
    public static ArrayList<Rule> launchExtraction(Instances dataset,
            Double minsup,
            int nbRulesExp,
            int minNbRules,
            Double minconf,
            Double toolow) throws Exception {
        //System.out.println("Launching extraction");

        Apriori apriori = adjustConfidence(dataset, minsup, nbRulesExp, minNbRules, minconf, toolow);

        // print results
        //System.out.println(apriori);

        //System.out.println("Extraction done");
        return rulesToFragments(apriori, dataset);
    }

    /**
     * Convert the assocation rules found with Apriori algorithm in the Rule class format {@link Rule}.
     * @param apriori the result of the Apriori algorithm.
     * @param dataset the dataset.
     * @return a list of rules.
     */
    public static ArrayList<Rule> rulesToFragments(Apriori apriori, Instances dataset) {

        FastVector[] result = apriori.getAllTheRules();
        ArrayList<Rule> extracted = new ArrayList<Rule>();

        int nbRules = result[1].size();
        // rule creation
        for (int i = 0; i < nbRules; i++) {
            ItemSet body = (AprioriItemSet) result[0].elementAt(i);
            ItemSet head = (ItemSet) result[1].elementAt(i);
            float support = head.support() / dataset.numInstances();
            double confidence = (Double) result[2].elementAt(i);

            ArrayList<String> bodyItems = getItems(body, dataset);
            ArrayList<String> headItems = getItems(head, dataset);

            //head

            if (headItems.size() == 1) { // otherwise skips the rule

                String headString = headItems.get(0);

                Fragment headFrag = null;
                for (ProjectionFragment p : ProjectionFragment.getAllProjections().values()) {
                    if (p.toString().compareTo(headString) == 0) {
                        headFrag = p;
                    }
                }

                for (SelectionFragment p : SelectionFragment.getAllSelections().values()) {
                    if (p.toString().compareTo(headString) == 0) {
                        headFrag = p;
                    }
                }

                for (MeasureFragment p : MeasureFragment.getAllMeasures().values()) {
                    if (p.toString().compareTo(headString) == 0) {
                        headFrag = p;
                    }
                }

                // body

                Qfset bodyQfset = new Qfset();

                for (String s : bodyItems) {
                    //Fragment bodyFrag;
                    for (ProjectionFragment p : ProjectionFragment.getAllProjections().values()) {
                        if (p.toString().compareTo(s) == 0) {
                            bodyQfset.addProjection(p);
                        }
                    }

                    for (SelectionFragment p : SelectionFragment.getAllSelections().values()) {
                        if (p.toString().compareTo(s) == 0) {
                            bodyQfset.addSelection(p);
                        }
                    }

                    for (MeasureFragment p : MeasureFragment.getAllMeasures().values()) {
                        if (p.toString().compareTo(s) == 0) {
                            bodyQfset.addMeasure(p);
                        }
                    }


                }
                extracted.add(new Rule(bodyQfset, headFrag, support, confidence));
            }
        }

        return extracted;
    }

    /**
     * Return the item names of an itemset, found in the dataset.
     * @param is the itemset.
     * @param dataset the dataset.
     * @return a list of item names.
     */
    // incredible that weka does not have this!
    public static ArrayList<String> getItems(ItemSet is, Instances dataset) {
        ArrayList<String> theItems = new ArrayList<String>();
        int[] itemsInt = is.items();
        for (int i = 0; i < itemsInt.length; i++) {
            if (itemsInt[i] == 0) {
                theItems.add(dataset.attribute(i).name());
                //System.out.println(dataset.attribute(i).name());
            }
        }

        return theItems;
    }

    /**
     * Display the dataset.
     * @param attributes tha attributes of the dataset.
     * @param dataset the dataset.
     */
    public static void print(FastVector attributes, Instances dataset) {
        for (int i = 0; i < attributes.size(); i++) {
            //System.out.println(attributes.elementAt(i).toString());
        }

        //System.out.println();
        //System.out.println(dataset.toString());
    }
}
