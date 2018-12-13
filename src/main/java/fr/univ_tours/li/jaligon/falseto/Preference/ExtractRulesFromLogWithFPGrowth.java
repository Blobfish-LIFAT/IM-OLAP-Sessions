package fr.univ_tours.li.jaligon.falseto.Preference;

import fr.univ_tours.li.jaligon.falseto.QueryStructure.SelectionFragment;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Qfset;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.ProjectionFragment;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Fragment;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.MeasureFragment;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import weka.associations.Apriori;
import weka.associations.AprioriItemSet;
import weka.associations.FPGrowth;
import weka.associations.FPGrowth.AssociationRule;
import weka.associations.FPGrowth.BinaryItem;
import weka.associations.ItemSet;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;

public class ExtractRulesFromLogWithFPGrowth {

    /**
     * Create the attributes, which are the names of projection, selection and
     * measure fragments, and set their values to 'yes' according to these
     * fragments.
     *
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
     *
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
    public static FPGrowth extract(Instances dataset,
            Double confidence,
            Double minsup,
            Integer nbRulesExp,
            Integer minNbRules,
            Double minconf,
            Double toolow) throws Exception {

        FPGrowth fpgrowth = new FPGrowth();

        String supString = minsup.toString();
        String confString = confidence.toString();
        String nbString = nbRulesExp.toString();
        //-P 2 -I -1 -N 10 -T 0 -C 0.9 -D 0.05 -U 1.0 -M 0.1
        //String[] options = {"-N", nbString, "-T", "0", "-C", confString, "-D", "0.05", "-U", "1.0", "-M", supString};
String[] options = {"-P", "1", "-I", "-1", "-N", nbString, "-T", "0", "-C", confString, "-D", "0.1", "-U", "1.0", "-M", supString};
        
        fpgrowth.setOptions(options);
        //System.out.println("Building association rules");
        fpgrowth.buildAssociations(dataset);
        //System.out.println("END Building association rules");
        return fpgrowth;
    }

    /**
     * Launch the Apriori algorithm, by adjusting the confidence (by step of
     * 0.1). If the confidence is lower than the minimum confidence, then the
     * support is reduced to 0.1 and the confidence is adjusted to 1.
     *
     * @param dataset the dataset
     * @param minsup the minimum support
     * @param nbRulesExp the number of wanted rules.
     * @param minNbRules ??
     * @param minconf the minimal confidence.
     * @param toolow ??
     * @return the result of Apriori algorithm.
     * @throws Exception
     */
    public static FPGrowth adjustConfidence(Instances dataset,
            Double minsup,
            Integer nbRulesExp,
            Integer minNbRules,
            Double minconf,
            Double toolow) throws Exception {

        //System.out.print("Adjusting confidence");
        boolean allCovered = false;
        boolean changes = true;
        Double confidence = (double) 1;
        FPGrowth fpgrowth = null;
        int nbIterations = 0;

        while (!allCovered && changes) {
            Instances datasetCopy = new Instances(dataset);
            //System.out.println("confidence=" + confidence);
            //ArrayList<Rule> extracted=new ArrayList<Rule>();
            fpgrowth = new FPGrowth();

            String supString = minsup.toString();
            String confString = confidence.toString();
            String nbString = nbRulesExp.toString();
            String[] options = {"-N", nbString, "-T", "0", "-C", confString, "-D", "0.05", "-U", "1.0", "-M", supString};

            fpgrowth.setOptions(options);
            fpgrowth.buildAssociations(dataset);

            List<AssociationRule> result = fpgrowth.getAssociationRules();

            int nbRules = result.size();

            for (int i = 0; i < nbRules; i++) {

                Collection<BinaryItem> body = result.get(i).getPremise();
                Collection<BinaryItem> head = result.get(i).getConsequence();
                ArrayList<String> headItems = getItems(head);


                int[] toDelete = new int[datasetCopy.numInstances()];
                int k = 0;
                for (int j = 0; j < datasetCopy.numInstances(); j++) {
                    if (containedBy(body, datasetCopy.instance(j)) && containedBy(head, datasetCopy.instance(j))
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
        return fpgrowth;
    }

    /**
     * Launch the Apriori algorithm, by adjusting the confidence (by step of
     * 0.1). The Qfset of the query which will be personalize, is used to remove
     * the rules which are not covered by this Qfset.
     *
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
    public static FPGrowth adjustConfidenceWithQfset(
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
        FPGrowth fpgrowth = null;
        int nbIterations = 0;

        while (!allCovered && changes) {
            Instances datasetCopy = new Instances(dataset);
            //System.out.println("confidence=" + confidence);
            fpgrowth = new FPGrowth();

            String supString = minsup.toString();
            String confString = confidence.toString();
            String nbString = nbRulesExp.toString();
            String[] options = {"-N", nbString, "-T", "0", "-C", confString, "-D", "0.05", "-U", "1.0", "-M", supString};

            fpgrowth.setOptions(options);
            fpgrowth.buildAssociations(dataset);

            List<AssociationRule> result = fpgrowth.getAssociationRules();

            int nbRules = result.size();

            for (int i = 0; i < nbRules; i++) {

                Collection<BinaryItem> body = result.get(i).getPremise();
                Collection<BinaryItem> head = result.get(i).getConsequence();
                ArrayList<String> headItems = getItems(head);

                ArrayList<String> queryItems = qfs.listStrings();

                int[] toDelete = new int[datasetCopy.numInstances()];
                int k = 0;
                for (int j = 0; j < datasetCopy.numInstances(); j++) {
                    if (containedBy(body, datasetCopy.instance(j)) && containedBy(head, datasetCopy.instance(j))
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
        return fpgrowth;
    }

    /**
     * Launch the extraction of association rules with the Apriori Algorithm and
     * taking into account the Qfset of the query to personalize. This Qfset is
     * used to remove the rules which are not covered by this Qfset in
     * {@link #adjustConfidenceWithQfset(Qfset qfs, Instances dataset, Double minsup, Integer nbRulesExp, Integer minNbRules, Double minconf, Double toolow)}.
     *
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

        FPGrowth fpgrowth = adjustConfidenceWithQfset(qfs, dataset, minsup, nbRulesExp, minNbRules, minconf, toolow);

        // print results
        //System.out.println(fpgrowth);

        return rulesToFragments(fpgrowth, dataset);
    }

    /**
     * Launch the extraction of association rules using only the Apriori
     * algorithm.
     *
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
        FPGrowth fpgrowth = extract(dataset, confidence, minsup, nbRulesExp, minNbRules, minconf, toolow);
        //System.out.println(apriori);
        //System.out.println("Extraction done");
        return rulesToFragments(fpgrowth, dataset);
    }

    /**
     * Launch the extraction of association rules using the Apriori algorithm
     * and adjusting the confidence.
     *
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

        FPGrowth fpgrowth = adjustConfidence(dataset, minsup, nbRulesExp, minNbRules, minconf, toolow);

        // print results
        //System.out.println(fpgrowth);

        //System.out.println("Extraction done");
        return rulesToFragments(fpgrowth, dataset);
    }

    /**
     * Convert the assocation rules found with Apriori algorithm in the Rule
     * class format {@link Rule}.
     *
     * @param apriori the result of the Apriori algorithm.
     * @param dataset the dataset.
     * @return a list of rules.
     */
    public static ArrayList<Rule> rulesToFragments(FPGrowth fpgrowth, Instances dataset) {

        List<AssociationRule> result = fpgrowth.getAssociationRules();
        
        ArrayList<Rule> extracted = new ArrayList<Rule>();

        int nbRules = result.size();
        // rule creation
        for (int i = 0; i < nbRules; i++) {

            Collection<BinaryItem> body = result.get(i).getPremise();
            Collection<BinaryItem> head = result.get(i).getConsequence();

            float support = result.get(i).getConsequenceSupport() / dataset.numInstances();
            double confidence = result.get(i).getMetricValue();

            ArrayList<String> bodyItems = getItems(body);
            ArrayList<String> headItems = getItems(head);

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
     *
     * @param is the itemset.
     * @param dataset the dataset.
     * @return a list of item names.
     */
    // incredible that weka does not have this!
    public static ArrayList<String> getItems(Collection<BinaryItem> bic) {
        ArrayList<String> theItems = new ArrayList<String>();

        for (BinaryItem bi : bic) {
            theItems.add(bi.getAttribute().name());
        }

        return theItems;
    }

    /**
     * Display the dataset.
     *
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

    public static boolean containedBy(Collection<BinaryItem> items, Instance instance) {

        for (int i = 0; i < instance.numAttributes(); i++) {
            BinaryItem item = null;
            for (BinaryItem bi : items) {
                if (instance.attribute(i) == bi.getAttribute()) {
                    item = bi;
                    break;
                }
            }

            if (item.getValueIndex() > -1) {
                if (instance.isMissing(i)) {
                    return false;
                }
                if (item.getValueIndex() != (int) instance.value(i)) {
                    return false;
                }
            }
        }
        return true;
    }
}
