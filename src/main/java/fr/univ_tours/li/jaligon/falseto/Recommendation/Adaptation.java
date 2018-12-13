package fr.univ_tours.li.jaligon.falseto.Recommendation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import fr.univ_tours.li.jaligon.falseto.Preference.ExtractRulesFromLogWithApriori;
import fr.univ_tours.li.jaligon.falseto.Preference.Rule;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Fragment;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.MeasureFragment;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.ProjectionFragment;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Qfset;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.QuerySession;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.SelectionFragment;
import fr.univ_tours.li.jaligon.falseto.Similarity.Session.BaseRecommendation;
import fr.univ_tours.li.jaligon.falseto.Similarity.Session.SmithWaterman;
import fr.univ_tours.li.jaligon.falseto.Tailor.Alpes.AlpesFormatToLog;
import fr.univ_tours.li.jaligon.falseto.Tailor.Alpes.LogToAlpesFormat;
import weka.associations.Apriori;
import weka.core.Instances;

/**
 *
 * @author julien
 */
public class Adaptation {

    private QuerySession currentSession;
    private TreeSet<BaseRecommendation> baseRecommendations;
    private List<QuerySession> scoredSessionList;
    private List<ArrayList<ArrayList<Qfset>>> alignementList;
    private List<QuerySession> sessionList;
    private List<Qfset> futureRecommended;

    private BaseRecommendation selectedBaseRecommendation;
    private int iterations;

    private HashMap<Fragment, Integer> fragmentSupport;
    private QuerySession adaptedSession;
    private SmithWaterman swRecommendation;
    private HashSet<Rule> types1 = new HashSet<>();
    private HashSet<Rule> types2 = new HashSet<>();

    private List<QuerySession> allAdaptedSessions = new ArrayList();

    public Adaptation(TreeSet baseRecommendations, List<QuerySession> scoredSessionList,
            QuerySession currentSession, List<ArrayList<ArrayList<Qfset>>> alignementList,
            List<QuerySession> sessionList) {
        this.baseRecommendations = baseRecommendations;
        this.scoredSessionList = scoredSessionList;
        this.currentSession = currentSession;
        this.alignementList = alignementList;
        this.sessionList = sessionList;
    }

    /**
     * Computes the Adaptation step. If the adapted recommendation intersects
     * with the current session, the result is discarded and the process is
     * repeated on the next base recommendation. The base recommendations are
     * ordered with decreasing relevance.
     *
     * @return
     */
    public boolean computeAdaptation() throws Exception {
        iterations = 0;
        for (BaseRecommendation baseRecommendation : baseRecommendations) {
            //nbFitting++;
            selectedBaseRecommendation = baseRecommendation;
            futureRecommended = scoredSessionList.get(baseRecommendation.getIndexOfMostRelevantSession())
                    .extractSubsequence(baseRecommendation.getStartOfMostRelevantAlignment(), baseRecommendation.getEndOfMostRelevantAlignment())
                    .getQueries();
            adaptRecommendation();
            if (!adaptedSession.intersects(currentSession)) {
                return true;
            }
            iterations++;//are you sure that the iteration has to be here ?

        }

        return false;
    }

    public void adaptRecommendation() throws Exception {

        List<Qfset> pastRecommended = new ArrayList<>();

        for (QuerySession qs : sessionList) {
            for (int i = 0; i < qs.getQueries().size(); i++) {
                //System.out.println("FUTURE : "+futureRecommended.size());
                if (qs.getQueries().get(i) == futureRecommended.get(0)) {
                    for (int j = 0; j < i; j++) {
                        pastRecommended.add(qs.get(j));
                    }
                    break;
                }
            }
        }

        HashSet<QuerySession> alignedSequences = formatAlignement(alignementList, pastRecommended, futureRecommended);

        List<Rule> rulesType1 = extractType1(currentSession);

        List<Rule> rulesType2 = new ArrayList<>();
        if (alignedSequences != null) {
            HashSet<Fragment> frs = new HashSet<>();

            for (QuerySession s : alignedSequences) {
                for (Qfset q : s.getQueries()) {
                    frs.addAll(q.getAttributes());
                    frs.addAll(q.getMeasures());
                    frs.addAll(q.getSelectionPredicates());
                }
            }

            supportOfFragments(alignedSequences);

            rulesType2 = extractType2(alignedSequences);
        }

        List<Rule> rulesType1Ranked = rankType1(rulesType1, currentSession);
        List<Rule> rulesType2Ranked = rankType2(rulesType2, currentSession);

        adaptedSession = substitute(futureRecommended, rulesType1Ranked, rulesType2Ranked, currentSession);
    }

    private HashSet<Fragment> getConstantFragments(List<Qfset> futureRecommended) {
        HashSet<Fragment> constantFragments = new HashSet<>();
        HashSet<Fragment> allFragments = new HashSet<>();

        for (Qfset q : futureRecommended) {
            allFragments.addAll(q.getAttributes());
            allFragments.addAll(q.getMeasures());
            allFragments.addAll(q.getSelectionPredicates());
        }

        for (Fragment f : allFragments) {
            boolean add = true;
            for (Qfset q : futureRecommended) {
                HashSet<Fragment> fragments = new HashSet<>();
                fragments.addAll(q.getAttributes());
                fragments.addAll(q.getMeasures());
                fragments.addAll(q.getSelectionPredicates());
                if (!fragments.contains(f)) {
                    add = false;
                    break;
                }
            }
            if (add) {
                constantFragments.add(f);
            }
        }
        return constantFragments;
    }

    public QuerySession getAdaptedSession() {
        return adaptedSession;
    }

    private HashSet<QuerySession> formatAlignement(List<ArrayList<ArrayList<Qfset>>> alignementList, List<Qfset> pastRecommended, List<Qfset> futureRecommended) {
        ArrayList<ArrayList<Qfset>> alignment = null;
        boolean found = false;
        for (ArrayList<ArrayList<Qfset>> align : alignementList) {
            for (Qfset q : align.get(1)) {
                if (q != null) {
                    if (pastRecommended.contains(q)) {
                        alignment = align;
                        found = true;
                        break;
                    }
                }
            }
            if (found) {
                break;
            }
        }

        if (alignment == null) {
            return null;
        } else {

            HashSet<QuerySession> alignedSequences = new HashSet<>();

            for (int i = 0; i < alignment.get(1).size(); i++) {//we loop the alignment of the past recommended session
                if (alignment.get(1).get(i) != null && alignment.get(0).get(i) != null) {
                    if (futureRecommended.contains(alignment.get(1).get(i))) {
                        break;
                    }
                } else {
                    continue;
                }

                QuerySession qs = new QuerySession("aligned");

                List<Fragment> currentFragments = new ArrayList<>();

                for (Qfset q : currentSession.getQueries()) {
                    List<Fragment> queryFragments = new ArrayList<>();
                    queryFragments.addAll(q.getAttributes());
                    queryFragments.addAll(q.getMeasures());
                    queryFragments.addAll(q.getSelectionPredicates());

                    for (Fragment f : queryFragments) {
                        if (!currentFragments.contains(f)) {
                            currentFragments.add(f);
                        }
                    }
                }

                HashSet<Fragment> constantFragments = getConstantFragments(futureRecommended);

                Qfset qr = new Qfset();
                int nbFragment = 0;
                for (ProjectionFragment pf : alignment.get(1).get(i).getAttributes()) {
                    if (constantFragments.contains(pf) && !currentFragments.contains(pf)) {
                        qr.addProjection(pf);
                        nbFragment++;
                    }
                }
                for (SelectionFragment sf : alignment.get(1).get(i).getSelectionPredicates()) {
                    if (constantFragments.contains(sf) && !currentFragments.contains(sf)) {
                        qr.addSelection(sf);
                        nbFragment++;
                    }
                }
                for (MeasureFragment mf : alignment.get(1).get(i).getMeasures()) {
                    if (constantFragments.contains(mf) && !currentFragments.contains(mf)) {
                        qr.addMeasure(mf);
                        nbFragment++;
                    }
                }

                if (nbFragment != 0) {
                    qs.add(qr);
                    //qs.add(alignment.get(1).get(i));
                    qs.add(alignment.get(0).get(i));
                    alignedSequences.add(qs);
                }
            }

            return alignedSequences;
        }
    }

    private void supportOfFragments(HashSet<QuerySession> sessionList) {
        fragmentSupport = new HashMap<>();

        ArrayList<Fragment> fragmentList = new ArrayList<>();
        fragmentList.addAll(ProjectionFragment.getAllProjections().values());
        fragmentList.addAll(SelectionFragment.getAllSelections().values());
        fragmentList.addAll(MeasureFragment.getAllMeasures().values());

        for (Fragment f : fragmentList) {
            for (QuerySession qs : sessionList) {
                for (Qfset q : qs.getQueries()) {
                    ArrayList<Fragment> fragmentListQuery = new ArrayList<>();

                    fragmentListQuery.addAll(q.getAttributes());
                    fragmentListQuery.addAll(q.getSelectionPredicates());
                    fragmentListQuery.addAll(q.getMeasures());

                    if (fragmentListQuery.contains(f)) {
                        if (fragmentSupport.containsKey(f)) {
                            fragmentSupport.put(f, fragmentSupport.get(f) + 1);
                        } else {
                            fragmentSupport.put(f, 1);
                        }
                        break;
                    }
                }
            }
        }
    }

    private QuerySession substitute(List<Qfset> future, List<Rule> rulesType1, List<Rule> rulesType2, QuerySession currentSession) {
        ArrayList<Qfset> futureCopy = new ArrayList<>();

        List<Fragment> currentFragments = new ArrayList<>();

        for (Qfset q : currentSession.getQueries()) {
            List<Fragment> queryFragments = new ArrayList<>();
            queryFragments.addAll(q.getAttributes());
            queryFragments.addAll(q.getMeasures());
            queryFragments.addAll(q.getSelectionPredicates());

            for (Fragment f : queryFragments) {
                if (!currentFragments.contains(f)) {
                    currentFragments.add(f);
                }
            }
        }

        HashSet<Fragment> constantFragments = getConstantFragments(future);

        for (Qfset q : future) {
            List<Fragment> queryFragments = new ArrayList<>();
            queryFragments.addAll(q.getAttributes());
            queryFragments.addAll(q.getMeasures());
            queryFragments.addAll(q.getSelectionPredicates());

            List<Fragment> toSubstitute = getDifferentFragments(q, currentSession);
            queryFragments.removeAll(toSubstitute);

            List<Fragment> alreadySubstituted = new ArrayList<>();

            Qfset queryAdapted = new Qfset();

            for (Fragment f : queryFragments) {
                queryAdapted.add(f);
            }
            for (Fragment f : toSubstitute) {
                queryAdapted.add(f);
            }

            double begin = System.currentTimeMillis();
            //nbRulesType1IncludingCommonFragmentinBody++;
            for (Rule ruleType2 : rulesType2) {

                List<Fragment> bodyFragment = new ArrayList<>();
                bodyFragment.addAll(ruleType2.getBody().getAttributes());
                bodyFragment.addAll(ruleType2.getBody().getMeasures());
                bodyFragment.addAll(ruleType2.getBody().getSelectionPredicates());

                Fragment body = bodyFragment.get(0);
                Fragment head = ruleType2.getHead();

                if (!queryFragments.contains(head) && !toSubstitute.contains(head) && !alreadySubstituted.contains(body) && toSubstitute.contains(body) && constantFragments.contains(body)) {

                    if (head.getType() == 0)//projection
                    {
                        ProjectionFragment pf = (ProjectionFragment) head;
                        ProjectionFragment pfBody = (ProjectionFragment) body;

                        if (pf.getHierarchy() != pfBody.getHierarchy()) {
                            continue;
                        }

                        //HashSet<SelectionFragment> selections = queryAdapted.getSelectionsFromHierarchy(pf.getHierarchy());
                        toSubstitute.remove(body);
                        toSubstitute.add(head);
                        alreadySubstituted.add(head);
                        types2.add(ruleType2);

                    } else if (head.getType() == 1) {
                        SelectionFragment sf = (SelectionFragment) head;
                        SelectionFragment sfBody = (SelectionFragment) body;

                        if (sf.getLevel().getHierarchy() != sfBody.getLevel().getHierarchy()) {
                            continue;
                        }

                        toSubstitute.remove(body);
                        toSubstitute.add(head);
                        alreadySubstituted.add(head);
                        types2.add(ruleType2);
                    } else {//a measure
                        //System.out.println("apply type 2");
                        toSubstitute.remove(body);
                        toSubstitute.add(head);
                        alreadySubstituted.add(head);
                        types2.add(ruleType2);
                    }
                }

                queryAdapted = new Qfset();

                for (Fragment f : queryFragments) {
                    queryAdapted.add(f);
                }
                for (Fragment f : toSubstitute) {
                    queryAdapted.add(f);
                }
            }
            double end = System.currentTimeMillis();

            //timeAdaptType1 += (end - begin);
            //timeAdaptType1nb++;
            //queryFragments.removeAll(alreadySubstituted);
            //queryFragments.addAll(toSubstitute);
            //System.out.println("BEFORE TEMP "+q);
            //System.out.println("AFTER TEMP"+queryAdapted);
            alreadySubstituted = new ArrayList<>();

            queryFragments = new ArrayList<>();
            queryFragments.addAll(queryAdapted.getAttributes());
            queryFragments.addAll(queryAdapted.getMeasures());
            queryFragments.addAll(queryAdapted.getSelectionPredicates());

            begin = System.currentTimeMillis();
            for (Rule ruleType1 : rulesType1) {
                Qfset body = ruleType1.getBody();
                Fragment head = ruleType1.getHead();

                List<Fragment> bodyFragments = new ArrayList<>();
                bodyFragments.addAll(body.getAttributes());
                bodyFragments.addAll(body.getMeasures());
                bodyFragments.addAll(body.getSelectionPredicates());

                if (queryFragments.containsAll(bodyFragments) && !queryFragments.contains(head)) {

                    //System.out.println("apply type 1");
                    if (head.getType() == 0)//projection
                    {
                        ProjectionFragment pf = (ProjectionFragment) head;
                        ProjectionFragment pfToReplace = null;

                        for (ProjectionFragment pf2 : queryAdapted.getAttributes()) {
                            if (pf2.getHierarchy() == pf.getHierarchy()) {
                                pfToReplace = pf2;
                                break;
                            }
                        }

                        //avoid the transitivity
                        if (alreadySubstituted.contains(pfToReplace) || !constantFragments.contains(pfToReplace)) {
                            continue;
                        }

                        //HashSet<SelectionFragment> selections = queryAdapted.getSelectionsFromDimension(pf.getHierarchy());
                        queryFragments.remove(pfToReplace);
                        queryFragments.add(head);
                        alreadySubstituted.add(head);
                        types1.add(ruleType1);

                    } else if (head.getType() == 1) {
                        SelectionFragment sf = (SelectionFragment) head;
                        SelectionFragment sftoReplace = null;

                        for (SelectionFragment sf2 : queryAdapted.getSelectionPredicates()) {
                            if (sf.getLevel().getHierarchy() == sf2.getLevel().getHierarchy()) {
                                sftoReplace = sf2;
                                break;
                            }
                        }

                        //avoid the transitivity
                        if (alreadySubstituted.contains(sftoReplace) || !constantFragments.contains(sftoReplace)) {
                            continue;
                        }

                        if (sftoReplace != null) {
                            queryFragments.remove(sftoReplace);
                        }
                        queryFragments.add(head);
                        alreadySubstituted.add(head);
                        types1.add(ruleType1);

                    } else {//a measure
                        //System.out.println("apply type 1");
                        if (!currentFragments.contains(head)) {
                            continue;
                        }

                        queryFragments.add(head);
                        types1.add(ruleType1);
                    }
                    queryAdapted = new Qfset();

                    for (Fragment f : queryFragments) {
                        queryAdapted.add(f);
                    }

                    queryFragments = new ArrayList<>();
                    queryFragments.addAll(queryAdapted.getAttributes());
                    queryFragments.addAll(queryAdapted.getMeasures());
                    queryFragments.addAll(queryAdapted.getSelectionPredicates());
                }

            }
            end = System.currentTimeMillis();

            //timeAdaptType2 += (end - begin);
            //timeAdaptType2nb++;
            //System.out.println("BEFORE " + q);
            //System.out.println("AFTER " + queryAdapted);
            futureCopy.add(queryAdapted);
        }

        QuerySession qs = new QuerySession(futureCopy, "recommended");

        return qs;
    }

    public double getPercentageRuleType1() {
        if (types1.isEmpty() && types2.isEmpty()) {
            return 0;
        }
        return types1.size() / (types1.size() + types2.size());
    }

    public double getPercentageRuleType2() {
        if (types1.isEmpty() && types2.isEmpty()) {
            return 0;
        }
        return types2.size() / (types1.size() + types2.size());
    }

    private List<Fragment> getDifferentFragments(Qfset query, QuerySession currentSession) {
        List<Fragment> result = new ArrayList<>();

        List<Fragment> queryFragments = new ArrayList<>();
        queryFragments.addAll(query.getAttributes());
        queryFragments.addAll(query.getMeasures());
        queryFragments.addAll(query.getSelectionPredicates());

        for (Fragment f : queryFragments) {
            boolean difference = true;
            for (Qfset qf : currentSession.getQueries()) {
                List<Fragment> currentFragments = new ArrayList<>();
                currentFragments.addAll(qf.getAttributes());
                currentFragments.addAll(qf.getMeasures());
                currentFragments.addAll(qf.getSelectionPredicates());

                if (currentFragments.contains(f)) {
                    difference = false;
                    break;
                }
            }
            if (difference) {
                result.add(f);
            }
        }

        return result;
    }

    private List<Rule> extractType1(QuerySession currentSession) throws Exception {

        Instances dataset = ExtractRulesFromLogWithApriori.preprocess((ArrayList) currentSession.getQueries());

        double confidence = 1;
        double minsup = 0.7;
        int nbRulesExp = 100;
        int minNbRules = 5;
        double minconf = 0.5;
        double toolow = 0.1;

        Apriori apriori = null;
        //try {
            apriori = ExtractRulesFromLogWithApriori.extract(dataset, confidence, minsup, nbRulesExp, minNbRules, minconf, toolow);

        /*} catch (Exception ex) {
            Logger.getLogger(Adaptation.class
                    .getName()).log(Level.SEVERE, null, ex);
        }*/

        ArrayList<Rule> rules = ExtractRulesFromLogWithApriori.rulesToFragments(apriori, dataset);

        //System.out.println("Nb of rules Type 1 : " + rules.size());
        //if(!rules.isEmpty())
        //System.out.println(rules.get(0).getBody() + " --> " + rules.get(0).getHead());
        return rules;
    }

    private List<Rule> extractType2(HashSet<QuerySession> alignedSequences) {

        //System.out.println("Type 2 size aligned sequences : "+alignedSequences.size());
        double begin = System.currentTimeMillis();
        LogToAlpesFormat alpes = new LogToAlpesFormat(alignedSequences);
        String fileName = "sequences.txt";
        try {
            alpes.saveToFileRawFormat(fileName);
        } catch (IOException ex) {
            Logger.getLogger(ASRA.class.getName()).log(Level.SEVERE, null, ex);
        }
        double end = System.currentTimeMillis();

        String osName = System.getProperty("os.name");

        //timetype1Conv1 += end - begin;
        //timetype1NbConv1++;
        String path = "";
        //String winCommandProjection = path + "alpes.exe";
        String command = null;
        if (osName.indexOf("Win") >= 0) {
            command = path + "alpes.exe -i " + fileName + "" + " -s 1" + " --max-order=2" + " --min-order=2" + " -R --delim=@ -d";
        } else if((osName.indexOf("Mac") >= 0)){
            command = path + "./alpes" + " -i " + path + fileName + "" + " -s 1" + " --max-order=2" + " --min-order=2" + " -R --delim=@ -d";
        }
        else if((osName.indexOf("nix") >= 0 || osName.indexOf("nux") >= 0 || osName.indexOf("aix") > 0 ))
        {
                command = path + "./alpes-unix" + " -i " + path + fileName + "" + " -s 1" + " --max-order=2" + " --min-order=2" + " -R --delim=@ -d";
        }

        //String unixCommandProjection = path + "./alpes" + " -i " + path + fileName + "" + " -s 1" + " --max-order=2" + " --min-order=2" + " -R --delim=@ -d";
        //System.out.println(winCommandProjection);
        //System.out.println(unixCommandProjection);
        begin = System.currentTimeMillis();
        ArrayList<String> frequentSequences = alpesProgramm(command);
        end = System.currentTimeMillis();
        //System.out.println("Type 1 " + (end - begin));
        //timetype1Sum += (end - begin);
        //timetype1Nb++;
        //System.out.pr

        //System.out.println("FREQUENT SEQ "+frequentSequences.size());
        begin = System.currentTimeMillis();
        AlpesFormatToLog afl = new AlpesFormatToLog(frequentSequences, true);

        HashSet<QuerySession> sequences = afl.getSequences();

        HashSet<QuerySession> sequencesWithOneItem = new HashSet<>();

        for (QuerySession qs : sequences) {

            //for (Qfset q : qs.getQueries()) {
            boolean nextStep = false;
            boolean isProjection = false;
            boolean isMeasure = false;
            boolean isSelection = false;

            Qfset q = qs.getQueries().get(0);
            if (q.getAttributes().size() == 1 && q.getMeasures().isEmpty() && q.getSelectionPredicates().isEmpty()) {
                nextStep = true;
                isProjection = true;
            } else if (q.getAttributes().isEmpty() && q.getMeasures().size() == 1 && q.getSelectionPredicates().isEmpty()) {
                nextStep = true;
                isMeasure = true;
            } else if (q.getAttributes().isEmpty() && q.getMeasures().isEmpty() && q.getSelectionPredicates().size() == 1) {
                nextStep = true;
                isSelection = true;
            }

            if (nextStep) {
                q = qs.getQueries().get(1);
                if (isProjection && q.getAttributes().size() == 1 && q.getMeasures().isEmpty() && q.getSelectionPredicates().isEmpty()) {
                    sequencesWithOneItem.add(qs);
                } else if (isMeasure && q.getAttributes().isEmpty() && q.getMeasures().size() == 1 && q.getSelectionPredicates().isEmpty()) {
                    sequencesWithOneItem.add(qs);
                } else if (isSelection && q.getAttributes().isEmpty() && q.getMeasures().isEmpty() && q.getSelectionPredicates().size() == 1) {
                    sequencesWithOneItem.add(qs);
                }
            }
            //}
        }

        //System.out.println("Before One Item per itemset " + sequences.size());
        //System.out.println("After One Item per itemset " + sequencesWithOneItem.size());
        ArrayList<Rule> rules = new ArrayList<>();

        for (QuerySession qs : sequencesWithOneItem) {
            Fragment ant = null;

            if (!qs.get(0).getAttributes().isEmpty()) {
                ant = qs.get(0).getAttributes().iterator().next();
            } else if (!qs.get(0).getMeasures().isEmpty()) {
                ant = qs.get(0).getMeasures().iterator().next();
            } else if (!qs.get(0).getSelectionPredicates().isEmpty()) {
                ant = qs.get(0).getSelectionPredicates().iterator().next();
            }

            Fragment cons = null;

            if (!qs.get(1).getAttributes().isEmpty()) {
                cons = qs.get(1).getAttributes().iterator().next();
            } else if (!qs.get(1).getMeasures().isEmpty()) {
                cons = qs.get(1).getMeasures().iterator().next();
            } else if (!qs.get(1).getSelectionPredicates().isEmpty()) {
                cons = qs.get(1).getSelectionPredicates().iterator().next();
            }

            Qfset q = new Qfset();
            q.add(ant);

            float supp = afl.getSequencesWithSupport().get(qs);
            supp = supp / (float) alignedSequences.size();

            double conf = supp / ((double) fragmentSupport.get(ant) / (double) alignedSequences.size());

            Rule r = new Rule(q, cons, supp, conf);

            rules.add(r);
        }

        //System.out.println("Nb of rules Type 2 : " + rules.size());
        end = System.currentTimeMillis();

        //timetype1Conv2 += end - begin;
        //timetype1NbConv2++;
        return rules;
    }

    private List<Rule> rankType1(List<Rule> rules, QuerySession currentSession) {
        List<Rule> result = new ArrayList<>();

        HashMap<Rule, Double> ruleScores = new HashMap<>();

        for (Rule rule : rules) {
            Fragment head = rule.getHead();

            float supportCurrentSession = getSupport(head, currentSession);
            float positionCurrentSession = getPosition(head, currentSession);

            float support = rule.getSupport();
            double confidence = rule.getConfidence();

            double geometricMean = Math.pow(confidence * support * positionCurrentSession * supportCurrentSession, (double) 1 / (double) 4);

            ruleScores.put(rule, geometricMean);
        }

        //System.out.println("Scores Type 2");
        List<Double> scoreList = new ArrayList<>();
        scoreList.addAll(ruleScores.values());
        Collections.sort(scoreList);

        for (int i = scoreList.size() - 1; i >= 0; i--) {
            for (Rule r : ruleScores.keySet()) {
                if (scoreList.get(i) == ruleScores.get(r)) {
                    if (!result.contains(r)) {
                        //System.out.println(scoreList.get(i));
                        result.add(r);
                        break;
                    }
                }
            }
        }

        return result;
    }

    private List<Rule> rankType2(List<Rule> rules, QuerySession currentSession) {
        List<Rule> result = new ArrayList<>();

        HashMap<Rule, Double> ruleScores = new HashMap<>();

        for (Rule rule : rules) {
            Fragment head = rule.getHead();

            float supportCurrentSession = getSupport(head, currentSession);
            float positionCurrentSession = getPosition(head, currentSession);

            float support = rule.getSupport();
            double confidence = rule.getConfidence();

            double geometricMean = Math.pow(confidence * support * supportCurrentSession * positionCurrentSession, (double) 1 / (double) 4);

            ruleScores.put(rule, geometricMean);
        }

        List<Double> scoreList = new ArrayList<>();
        scoreList.addAll(ruleScores.values());
        Collections.sort(scoreList);
        //System.out.println("Scores Type 1");

        for (int i = scoreList.size() - 1; i >= 0; i--) {
            for (Rule r : ruleScores.keySet()) {
                if (scoreList.get(i) == ruleScores.get(r)) {
                    if (!result.contains(r)) {
                        //System.out.println(scoreList.get(i));
                        result.add(r);
                        break;
                    }
                }
            }
        }

        return result;
    }

    private float getPosition(Fragment fragment, QuerySession currentSession) {
        float sum = 0;
        float nbocc = 0;

        for (int i = 0; i < currentSession.getQueries().size(); i++) {
            Set<Fragment> fragments = new HashSet<>();
            fragments.addAll(currentSession.getQueries().get(i).getAttributes());
            fragments.addAll(currentSession.getQueries().get(i).getMeasures());
            fragments.addAll(currentSession.getQueries().get(i).getSelectionPredicates());

            if (fragments.contains(fragment)) {
                sum += (float) ((float) i + (float) 1) / (float) currentSession.size();
                nbocc++;
            }
        }

        return sum / nbocc;
    }

    private float getSupport(Fragment fragment, QuerySession currentSession) {
        float sum = 0;

        for (Qfset q : currentSession.getQueries()) {
            Set<Fragment> fragments = new HashSet<>();
            fragments.addAll(q.getAttributes());
            fragments.addAll(q.getMeasures());
            fragments.addAll(q.getSelectionPredicates());

            if (fragments.contains(fragment)) {
                sum++;
            }
        }

        return sum / (float) currentSession.size();
    }

    private void saveToFile(String fileName, List<String> stringSequence) throws IOException {
        PrintWriter file;

        file = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));

        for (String s : stringSequence) {
            file.println(s);
        }

        file.close();
    }

    private ArrayList<String> alpesProgramm(String command) {
        ArrayList<String> frequentSequences = new ArrayList<>();

        try {
            ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", command);
            //ProcessBuilder pb = new ProcessBuilder("cmd","/c",command);
            //ProcessBuilder pb = new ProcessBuilder("cmd","/c",command,"-i sequences.txt","-s 1","--max-order=2","--min-order=2","-R","--delim=@","-d");
//System.out.println("Alpes starting...");
//System.out.println("Command : "+command);
            Process p = pb.start();
//System.out.println("Started");
            //p.waitFor();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
//System.out.println(bufferedReader.ready());
            String line;
            //int i = 0;
            while ((line = bufferedReader.readLine()) != null) {
                frequentSequences.add(line);
                //System.out.println(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
            //System.out.println("\n" + command + ": unknown command");
        }

        return frequentSequences;
    }

    public int getMatchedQueriesSize() {
        return swRecommendation.getBestSubsequenceLength();
    }

    public int getPositionOfRecommendationInFutureOfCurrentSession() {
        return swRecommendation.getFirstMatchingQueryInBestSubsequence().getRow_index_prev();
    }

    public BaseRecommendation getSelectedBaseRecommendation() {
        return selectedBaseRecommendation;
    }

    public int getIterations() {
        return iterations;
    }

    public List<QuerySession> getAllAdaptedSessions() {
        return allAdaptedSessions;
    }

}
