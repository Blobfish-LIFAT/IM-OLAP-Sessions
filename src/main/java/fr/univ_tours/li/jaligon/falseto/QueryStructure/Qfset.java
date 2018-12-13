package fr.univ_tours.li.jaligon.falseto.QueryStructure;

import mondrian.calc.Calc;
import mondrian.calc.ExpCompiler;
import mondrian.mdx.ResolvedFunCall;
import mondrian.olap.Category;
import mondrian.olap.Dimension;
import mondrian.olap.Evaluator;
import mondrian.olap.Exp;
import mondrian.olap.Hierarchy;
import mondrian.olap.Member;
import mondrian.olap.Query;
import mondrian.olap.Level;
import mondrian.olap.QueryAxis;
import mondrian.rolap.RolapEvaluator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import mondrian.olap.Position;
import mondrian.rolap.RolapResult;
import fr.univ_tours.li.jaligon.falseto.Generics.Generics;

public class Qfset implements java.io.Serializable {

    Query mondrianQuery;
    /**
     * Set of measure with the corresponding aggregate function
     */
    protected HashSet<MeasureFragment> measures;
    /**
     * A group by set: Set of attribute, at most one for each dimension
     */
    protected HashSet<ProjectionFragment> attributes;
    /**
     * A set of selection criteria: at most one for each dimension
     */
    protected HashSet<SelectionFragment> selectionPredicates;
    /**
     * Score assigned to the query for future recommendation
     */
    protected double score = 0;
    /**
     * Describes the alignment of the query with other queries. For debug
     * purpose only).
     */
    protected String alignment = "";

    /**
     * This constructor creates new empty projection, selection and measure
     * fragments.
     */
    public Qfset() {
        attributes = new HashSet<ProjectionFragment>();
        selectionPredicates = new HashSet<SelectionFragment>();
        measures = new HashSet<MeasureFragment>();
    }

    /**
     * This constructor creates a new Qfset according to a query.
     *
     * @param mq the query.
     */
    public Qfset(Query mq) {
        this();
        mondrianQuery = mq;

        processSlicer();
        processAxis();

        //TODO completion is not used because it demands too much resource
        complete();

        // maybe selections should be of the form: level in {set of members}
        // well, maybe not: if we have l1=m1, l1=m2, etc. then we have more rules?
    }

    public Qfset(HashSet<ProjectionFragment> projections, HashSet<SelectionFragment> selections, HashSet<MeasureFragment> measures) {
        this.attributes = projections;
        this.selectionPredicates = selections;
        this.measures = measures;
    }

    /**
     *
     * @return the total size of the lists of projection, selection and measure
     * fragments.
     */
    public int size() {
        return (attributes.size() + selectionPredicates.size() + measures.size());
    }

    /**
     *
     * @return the list of projection fragments.
     */
    public HashSet<ProjectionFragment> getAttributes() {
        return (HashSet<ProjectionFragment>) attributes;
    }

    /**
     *
     * @return the list of selection fragments.
     */
    public HashSet<SelectionFragment> getSelectionPredicates() {
        return (HashSet<SelectionFragment>) selectionPredicates;
    }

    /**
     *
     * @return the list of measure fragments.
     */
    public HashSet<MeasureFragment> getMeasures() {
        return (HashSet<MeasureFragment>) measures;
    }

    /**
     * Return the names of all projection, selection and measure fragments.
     *
     * @return a list of names.
     */
    public ArrayList<String> listStrings() {
        ArrayList<String> result = new ArrayList<String>();
        for (Fragment f : attributes) {
            result.add(f.toString());
        }

        for (Fragment f : measures) {
            result.add(f.toString());
        }

        for (Fragment f : selectionPredicates) {
            result.add(f.toString());
        }
        return result;
    }

    /**
     * Explore the slicer of the query {@link #mondrianQuery} The Slicer
     * contains only members but they can be computed with MDX functions. Note
     * and question : 1997.Q2.Parent should be a selection of 1997 and
     * projection over years or selection of Q2 and projection over years? In
     * this method, the latter is implemented for Parent and Ancestor only
     */
    private void processSlicer() {
        QueryAxis theSlicer = mondrianQuery.getSlicerAxis(); // its slicer
        if (theSlicer != null) {
            Exp e = theSlicer.getSet();
            //it can be either a single member or a tuple

            // single member
            if (e.getCategory() == Category.Member) {
                processMember(e);
            }

            //tuple
            if (e.getCategory() == Category.Tuple) {
                Exp[] theArgs = ((ResolvedFunCall) e).getArgs();
                for (Exp ex : theArgs) {
                    processMember(ex);
                }
            }

        }
    }

    /**
     * Explore each member of an expression according to some functions of a
     * member (Ancestor or Parent). It is a complement of the exploration of
     * {@link #exploreExp(Exp e)} For each function, we extract the selection
     * and projection fragments which we seem correct.
     *
     * @param e an expression.
     */
    private void processMember(Exp e) {
        Evaluator re = RolapEvaluator.create(mondrianQuery.getStatement());
        ExpCompiler c = mondrianQuery.createCompiler();
        Calc ca;
        //check!!! done recursive call to explore if {arg} and arg not member
        if (e.getClass().toString().compareTo("class mondrian.mdx.ResolvedFunCall") == 0) {
            String fn = ((ResolvedFunCall) e).getFunName();
            if (fn.equals("Ancestor")) { // process Ancestor
                Exp em = ((ResolvedFunCall) e).getArg(0);
                ca = c.compile(em);
                Member memb = (Member) ca.evaluate(re);
                selectionPredicates.add(SelectionFragment.newInstance(memb));
                ca = c.compile(e);
                memb = (Member) ca.evaluate(re);
                attributes.add(ProjectionFragment.newInstance(memb.getLevel()));
            } else if (fn.equals("Parent")) { // process Parent
                Exp em = ((ResolvedFunCall) e).getArg(0);
                ca = c.compile(em);
                Member memb = (Member) ca.evaluate(re);
                selectionPredicates.add(SelectionFragment.newInstance(memb));
                attributes.add(ProjectionFragment.newInstance(memb.getLevel().getParentLevel()));
            } else {
                ca = c.compile(e);
                Member memb = (Member) ca.evaluate(re);
                if (memb.isMeasure()) {
                    measures.add(MeasureFragment.newInstance(memb));
                } else {
                    selectionPredicates.add(SelectionFragment.newInstance(memb));
                    attributes.add(ProjectionFragment.newInstance(memb.getLevel()));
                }
            }
        } else {
            ca = c.compile(e);
            Member memb = (Member) ca.evaluate(re);
            if (memb.isMeasure()) {
                measures.add(MeasureFragment.newInstance(memb));
            } else {
                selectionPredicates.add(SelectionFragment.newInstance(memb));
                attributes.add(ProjectionFragment.newInstance(memb.getLevel()));
            }
        }
    }

    /**
     * Explore each axis of the query {@link #mondrianQuery}
     */
    private void processAxis() {
        QueryAxis[] theAxes = mondrianQuery.getAxes(); // the axes of the query

        for (int j = 0; j < theAxes.length; j++) { //for all axes
            Exp e;
            e = theAxes[j].getSet();
            exploreExp(e);

        }
    }

    /**
     * Explore an expression according to the functions used in this expression.
     * For the moment, the functions explored are: - Children - Siblings -
     * Ascendants - Members - Descendants - the {} which can be a single member
     * or another expression For each function, we extract the selection and
     * projection fragments which we seem correct.
     *
     * @param e the expression
     */
    private void exploreExp(Exp e) {
        Evaluator re = RolapEvaluator.create(mondrianQuery.getStatement());
        ExpCompiler c = mondrianQuery.createCompiler();

        if (e.getCategory() == Category.Member) {	// single member, needed if {member1, member2, ...}
            processMember(e);
        } else {
            if (((ResolvedFunCall) e).getArgCount() == 1) {
                String fn = ((ResolvedFunCall) e).getFunName();
                if (fn.equals("Children")) { // process Children
                    Exp arg = ((ResolvedFunCall) e).getArg(0);
                    Calc ca = c.compile(arg);
                    Member m = (Member) ca.evaluate(re);
                    selectionPredicates.add(SelectionFragment.newInstance(m));
                    attributes.add(ProjectionFragment.newInstance(m.getLevel().getChildLevel()));
                }
                if (fn.equals("Siblings")) { // process Siblings
                    Exp arg = ((ResolvedFunCall) e).getArg(0);
                    Calc ca = c.compile(arg);
                    Member m = (Member) ca.evaluate(re);
                    //selections.add(SelectionFragment.newInstance(m.getParentMember()));
                    attributes.add(ProjectionFragment.newInstance(m.getLevel()));
                }
                if (fn.equals("Ascendants")) {
                    Exp arg = ((ResolvedFunCall) e).getArg(0);
                    Calc ca = c.compile(arg);
                    Member m = (Member) ca.evaluate(re);
                    selectionPredicates.add(SelectionFragment.newInstance(m));
                    attributes.add(ProjectionFragment.newInstance(m.getLevel()));
                    while (!m.isAll()) {
                        m = m.getParentMember();
                        attributes.add(ProjectionFragment.newInstance(m.getLevel()));
                    }
                }

                if (fn.equals("Members")) { // process Members, the same for Members-like functions, eg, AllMembers
                    Exp arg = ((ResolvedFunCall) e).getArg(0);

                    if (arg.getCategory() == Category.Level) {
                        Calc ca = c.compile(arg);
                        Level l = (Level) ca.evaluate(re);
                        attributes.add(ProjectionFragment.newInstance(l));
                    }
                    if (arg.getCategory() == Category.Hierarchy) {
                        Calc ca = c.compile(arg);
                        Hierarchy h = (Hierarchy) ca.evaluate(re);
                        Level[] levels = h.getLevels();
                        for (Level l : levels) {
                            attributes.add(ProjectionFragment.newInstance(l));
                        }
                    }
                    if (arg.getCategory() == Category.Dimension) {
                        Calc ca = c.compile(arg);
                        Dimension d = (Dimension) ca.evaluate(re);
                        Hierarchy[] hierarchies = d.getHierarchies();
                        for (Hierarchy h : hierarchies) {
                            Level[] levels = h.getLevels();
                            for (Level l : levels) {
                                attributes.add(ProjectionFragment.newInstance(l));
                            }
                        }
                    }
                }// end members
                if (fn.equals("{}")) { // a singleton !not necessarily a member!!!
                    Exp arg = ((ResolvedFunCall) e).getArg(0);
                    if (e.getCategory() == Category.Member) {
                        processMember(arg);
                    } else {
                        exploreExp(arg);
                    }
                }
                if (fn.equals("Descendants")) {//can we have this case in the condition (ResolvedFunCall) e).getArgCount() == 1) ?
                    Exp arg = ((ResolvedFunCall) e).getArg(0);
                    Calc ca = c.compile(arg);
                    Member m = (Member) ca.evaluate(re);
                    selectionPredicates.add(SelectionFragment.newInstance(m));
                    ca = c.compile(e);
                    List<Member> descendants = (List<Member>) ca.evaluate(re);
                    for (Member memb : descendants) {
                        attributes.add(ProjectionFragment.newInstance(memb.getLevel()));
                    }
                }
            } // end 1 argument
            else {// more than 1 argument
                if (((ResolvedFunCall) e).getFunName().equals("Descendants")) {
                    Exp arg = ((ResolvedFunCall) e).getArg(0);
                    Calc ca = c.compile(arg);
                    Member m = (Member) ca.evaluate(re);
                    selectionPredicates.add(SelectionFragment.newInstance(m));
                    ca = c.compile(e);
                    List<Member> descendants = (List<Member>) ca.evaluate(re);
                    for (Member memb : descendants) {
                        attributes.add(ProjectionFragment.newInstance(memb.getLevel()));
                    }
                } else if (((ResolvedFunCall) e).getFunName().equals("Filter")) {
                    Exp arg = ((ResolvedFunCall) e).getArg(0);//it is the projection (.Members type)
                    exploreExp(arg);
                    arg = ((ResolvedFunCall) e).getArg(1);

                    Exp expBoolean = ((ResolvedFunCall) arg).getArg(0);//it is the boolean expression                 
                    Exp expMeasure = ((ResolvedFunCall) expBoolean).getArg(0);//it is the measure

                    Calc ca = c.compile(expMeasure);
                    Member m = (Member) ca.evaluate(re);

                    measures.add(MeasureFragment.newInstance(m));
                } else {
                    Exp[] theArgs = ((ResolvedFunCall) e).getArgs();
                    for (Exp ex : theArgs) {
                        exploreExp(ex);
                    }
                }
            }
        }
    }

    /**
     * Add the default members when a dimension is not specify in the list of
     * projections {@link #projections}
     */
    public void complete() {
        HashSet<Hierarchy> thisHiers = new HashSet<Hierarchy>();
        for (ProjectionFragment p : attributes) {
            if (!thisHiers.contains(p.getHierarchy())) {
                thisHiers.add(p.getHierarchy());
            }
        }

        for (Hierarchy h : Generics.getHierarchies()) {
            if ((!thisHiers.contains(h)) && h.hasAll()) {
                //System.out.println(h);
                attributes.add(ProjectionFragment.newInstance(h.getAllMember().getLevel()));
                //do we have to add a selection as well? no makes no sense for all
            }
        }

    }

    /**
     * Add a projection fragment according to the level, so a projection
     * fragment is created if not found.
     *
     * @param l the level.
     */
    public void addProjection(Level l) {
        ProjectionFragment ni = ProjectionFragment.newInstance(l);
        if (!attributes.contains(ni)) {
            attributes.add(ni);
        }
    }

    /**
     * Add a selection fragment according to the member, so a selection fragment
     * is created if not found.
     *
     * @param m the member.
     */
    public void addSelection(Member m) {
        SelectionFragment ni = SelectionFragment.newInstance(m);
        if (!selectionPredicates.contains(ni)) {
            selectionPredicates.add(ni);
        }
    }

    /**
     * Add a measure fragment according to the member, so a measure fragment is
     * created if not found.
     *
     * @param m the member.
     */
    public void addMeasure(Member m) {
        if (m != null) {
            MeasureFragment ni = MeasureFragment.newInstance(m);
            if (!measures.contains(ni)) {
                measures.add(ni);
            }
        }
    }

    /**
     * Add a projection fragment according to a projection fragment already
     * created.
     *
     * @param p the projection fragment.
     */
    public void addProjection(ProjectionFragment p) {
        if (!attributes.contains(p)) {
            attributes.add(p);
        }
    }

    /**
     * Add a selection fragment according to a selection fragment already
     * created.
     *
     * @param s the selection fragment.
     */
    public void addSelection(SelectionFragment s) {
        if (!selectionPredicates.contains(s)) {
            selectionPredicates.add(s);
        }
    }

    /**
     * Add a measure fragment according to a measure fragment already created.
     *
     * @param m the measure fragment.
     */
    public void addMeasure(MeasureFragment m) {
        if (!measures.contains(m)) {
            measures.add(m);
        }
    }

    /**
     * Add a fragment according to a fragment already created. We add a
     * projection, selection or measure fragment according to the type of the
     * fragment.
     *
     * @param f the fragment.
     */
    public void add(Fragment f) {
        switch (f.getType()) {
            case 0:
                attributes.add((ProjectionFragment) f);
                break;
            case 1:
                selectionPredicates.add((SelectionFragment) f);
                break;
            case 2:
                measures.add((MeasureFragment) f);
                break;
        }
    }

    /**
     * Replace a projection fragment by another projection fragment.
     *
     * @param l the new level where the new projection fragment will be created.
     * @param pf the old projection fragment.
     */
    public void replaceProjection(Level l, ProjectionFragment pf) {
        ProjectionFragment ni = ProjectionFragment.newInstance(l);//creation of the fragment matching the level l
        attributes.remove(pf);//remove the fragment to replace of the list of all projections
        attributes.add(ni);
    }

    /**
     * Replace a selection fragment by another selection fragment.
     *
     * @param m the new member where the new selection fragment will be created.
     * @param sf the old selection fragment.
     */
    public void replaceSelection(Member m, SelectionFragment sf) {
        SelectionFragment ni = SelectionFragment.newInstance(m);
        selectionPredicates.remove(sf);
        selectionPredicates.add(ni);
    }

    /**
     * Remove the selections fragments according to the dimension.
     *
     * @param d the dimension.
     */
    public void removeSelectionByDim(Hierarchy d) {
        HashSet<SelectionFragment> cloneS = (HashSet<SelectionFragment>) ((HashSet<SelectionFragment>) selectionPredicates).clone();
        for (SelectionFragment s : selectionPredicates) {
            if (s.getLevel().getHierarchy().getName().compareTo(d.getName()) == 0) {
                cloneS.remove(s);
            }
        }
        selectionPredicates = cloneS;
    }

    /**
     * Remove a selection fragment of the set of selection fragments.
     *
     * @param sf the selection fragment to remove.
     */
    public void removeSelection(SelectionFragment sf) {
        selectionPredicates.remove(sf);
    }

    public void removeMeasure(MeasureFragment mf) {
        measures.remove(mf);
    }

    /**
     * Test if their is a similar fragment according to another fragment in
     * projection, selection or measure fragments.
     *
     * @param cons the fragment to compare.
     * @return true if a similar fragment is found, false otherwise.
     */
    public boolean existSimilars(Fragment cons) {
        switch (cons.getType()) {
            case 0: //projection
                for (ProjectionFragment p : attributes) {
                    if (p.getHierarchy() == ((ProjectionFragment) cons).getHierarchy()) {
                        return true;
                    }
                }
                break;
            case 1: //selection
                for (SelectionFragment p : selectionPredicates) {
                    if (p.getLevel() == ((SelectionFragment) cons).getLevel()) {
                        return true;
                    }
                }
                break;
            case 2: //measures
                if (!measures.isEmpty()) {
                    return true;
                }
                break;
        }

        return false;
    }

    /**
     * Remove similar fragment according to another fragment in projection,
     * selection or measure fragments.
     *
     * @param cons the fragment to compare.
     */
    public void removeSimilars(Fragment cons) {
        switch (cons.getType()) {
            case 0: //projection
                HashSet<ProjectionFragment> cloneP = (HashSet<ProjectionFragment>) ((HashSet<ProjectionFragment>) attributes).clone();
                for (ProjectionFragment p : attributes) {
                    if (p.getHierarchy().getName().equals(((ProjectionFragment) cons).getHierarchy().getName())) {
                        cloneP.remove(p);
                    }
                }
                attributes = cloneP;
                break;
            case 1: //selection
                HashSet<SelectionFragment> cloneS = (HashSet<SelectionFragment>) ((HashSet<SelectionFragment>) selectionPredicates).clone();
                for (SelectionFragment p : selectionPredicates) {
                    if (p.getLevel().getName().equals(((SelectionFragment) cons).getLevel().getName())) {
                        cloneS.remove(p);
                    }
                }
                selectionPredicates = cloneS;
                break;
            case 2: //measures
                if (!measures.isEmpty()) {
                    measures.clear();
                }
                break;
        }
    }

    /**
     * Test if the projection fragments contain all projection fragments of
     * another Qfset.
     *
     * @param other another Qfset.
     * @return true if the projection fragments include the others, false
     * otherwise.
     */
    public boolean containProjections(Qfset other) {
        if (other.getAttributes().isEmpty()) {
            return true;
        } else {
            return attributes.containsAll(other.getAttributes());
        }
    }

    /**
     * Test if the selection fragments contain all selection fragments of
     * another Qfset.
     *
     * @param other another Qfset.
     * @return true if the selection fragments include the others, false
     * otherwise.
     */
    public boolean containSelections(Qfset other) {
        if (other.getSelectionPredicates().isEmpty()) {
            return true;
        } else {
            //System.out.println("SELECTION : "+selectionPredicates.containsAll(other.getSelectionPredicates()));
            return selectionPredicates.containsAll(other.getSelectionPredicates());
        }
    }

    /**
     * Test if the measure fragments contain all measure fragments of another
     * Qfset.
     *
     * @param other another Qfset.
     * @return true if the measure fragments include the others, false
     * otherwise.
     */
    public boolean containMeasures(Qfset other) {
        if (other.getMeasures().isEmpty()) {
            return true;
        } else {
            return measures.containsAll(other.getMeasures());
        }
    }

    /**
     * Test if a fragment is contained in the list of projections
     * {@link #projections}
     *
     * @param f the fragment
     * @return true if the fragment is contained, false otherwise.
     */
    public boolean containProjections(Fragment f) {
        return attributes.contains((ProjectionFragment) f);
    }

    /**
     * Test if a fragment is contained in the list of selections
     * {@link #selections}
     *
     * @param f the fragment
     * @return true if the fragment is contained, false otherwise.
     */
    public boolean containSelections(Fragment f) {
        return selectionPredicates.contains((SelectionFragment) f);
    }

    /**
     * Test if a fragment is contained in the list of measures {@link #measures}
     *
     * @param f the fragment
     * @return true if the fragment is contained, false otherwise.
     */
    public boolean containMeasures(Fragment f) {
        return measures.contains((MeasureFragment) f);
    }

    // true if this includes q
    /**
     * Test if the list of projections, selection and measure fragments include
     * all lists of projection, selection ans measure fragments of another
     * Qfset.
     *
     * @param q the Qfset.
     * @return true if the list of projection, selection and measure fragments
     * include the others.
     */
    public boolean contains(Qfset q) {
        if (containProjections(q) && containSelections(q) && containMeasures(q)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Return the list of similar fragments according to another fragment.
     *
     * @param cons the another fragment.
     * @return a list of fragments.
     */
    public ArrayList<Fragment> getSimilars(Fragment cons) {
        ArrayList<Fragment> result = new ArrayList<Fragment>();
        switch (cons.getType()) {
            case 0: //projection
                for (ProjectionFragment p : attributes) {
                    if (p.isSimilar(cons)) {
                        result.add(p);
                    }
                }
                break;
            case 1: //selection
                for (SelectionFragment p : selectionPredicates) {
                    if (p.isSimilar(cons)) {
                        result.add(p);
                    }
                }
                break;
            case 2: //measures
                if (!measures.isEmpty()) {
                    for (MeasureFragment p : measures) {
                        result.add(p);
                    }
                }
                break;
        }
        return result;
    }

    public boolean isEmpty() {
        return (measures.isEmpty() && selectionPredicates.isEmpty() && attributes.isEmpty());
    }

    public String toString() {
        String s = "";
        int size = 0;
        for (MeasureFragment mf : this.measures) {
            s += mf.getAttribute().getName() + ",";
        }

        if (!"".equals(s)) {
            s = s.substring(0, s.length() - 1);
        }
        s += "\n";
        size = s.length();

        for (ProjectionFragment pf : this.attributes) {
            s += pf.getLevel().getHierarchy().getName() + "." + pf.getLevel().getName() + ",";
        }

        if (size != s.length()) {
            s = s.substring(0, s.length() - 1);
        }
        s += "\n";
        size = s.length();

        for (SelectionFragment sf : this.selectionPredicates) {
            s += sf.getLevel().getHierarchy().getName() + "." + sf.getLevel().getName() + "=\"" + sf.getValue().getName() + "\",";
        }

        if (size != s.length()) {
            s = s.substring(0, s.length() - 1);
        } else {
            s += "NONE";
        }

        return s;
    }

    public String toStringAttributes() {
        ArrayList<String> projectionSet = new ArrayList<String>();
        for (ProjectionFragment pf : this.attributes) {
            projectionSet.add(pf.getLevel().getHierarchy().getName() + "." + pf.getLevel().getName());
        }
        Collections.sort(projectionSet);

        String result = "";

        for (String s : projectionSet) {
            result += s + " ";
        }

        if (result.equals("")) {
            return "NO ATTRIBUTE";
        } else {
            return (String) result.subSequence(0, result.length() - 1);
        }
    }

    public String toStringSelections() {
        ArrayList<String> selectionSet = new ArrayList<String>();
        for (SelectionFragment sf : this.selectionPredicates) {
            selectionSet.add(sf.getLevel().getHierarchy().getName() + "." + sf.getLevel().getName() + "=\"" + sf.getValue().getName() + "\"");
        }
        Collections.sort(selectionSet);

        String result = "";

        for (String s : selectionSet) {
            result += s + " ";
        }

        if (result.equals("")) {
            return "NO SELECTION";
        } else {
            return (String) result.subSequence(0, result.length() - 1);
        }
    }

    public String toStringMeasures() {
        ArrayList<String> measureSet = new ArrayList<String>();
        for (MeasureFragment mf : this.measures) {
            measureSet.add(mf.getAttribute().getName());
        }
        Collections.sort(measureSet);

        String result = "";

        for (String s : measureSet) {
            result += s + " ";
        }

        if (result.equals("")) {
            return "NO MEASURE";
        } else {
            return (String) result.subSequence(0, result.length() - 1);
        }
    }

    public boolean addAllMeasures(HashSet<MeasureFragment> c) {
        return this.measures.addAll(c);
    }

    public boolean addAllAttributes(HashSet<ProjectionFragment> c) {
        return this.attributes.addAll(c);
    }

    public boolean addAllSelectionPredicates(HashSet<SelectionFragment> c) {
        return this.selectionPredicates.addAll(c);
    }

    public boolean isEqual(Qfset query) {
        if (this.attributes.containsAll(query.getAttributes()) && query.getAttributes().containsAll(this.attributes)) {
            if (this.selectionPredicates.containsAll(query.getSelectionPredicates()) && query.getSelectionPredicates().containsAll(this.selectionPredicates)) {
                if (this.measures.containsAll(query.getMeasures()) && query.getMeasures().containsAll(this.measures)) {
                    return true;
                }
            }
        }
        return false;
    }

    public HashSet<ProjectionFragment> getAttributesFromHierarchy(Hierarchy h) {
        HashSet<ProjectionFragment> result = new HashSet<ProjectionFragment>();

        for (ProjectionFragment f : this.attributes) {
            if (f.getLevel().getHierarchy() == h) {
                result.add(f);
            }
        }
        return result;
    }

    public ProjectionFragment getAttributeFromHierarchy(Hierarchy h) {

        for (ProjectionFragment f : this.attributes) {
            if (f.getLevel().getHierarchy() == h) {
                return f;
            }
        }
        return null;
    }

    public HashSet<SelectionFragment> getSelectionsFromHierarchy(Hierarchy h) {
        HashSet<SelectionFragment> result = new HashSet<SelectionFragment>();

        for (SelectionFragment f : this.selectionPredicates) {
            if (f.getLevel().getHierarchy() == h) {
                result.add(f);
            }
        }
        return result;
    }

    public SelectionFragment getSelectionFromHierarchy(Hierarchy h) {
        for (SelectionFragment f : this.selectionPredicates) {
            if (f.getLevel().getHierarchy() == h) {
                return f;
            }
        }
        return null;
    }

    public Qfset copy() {
        Qfset q = new Qfset();
        q.addAllAttributes(this.getAttributes());
        q.addAllMeasures(this.getMeasures());
        q.addAllSelectionPredicates(this.getSelectionPredicates());

        return q;
    }

    /**
     * Method to drilldown an attribute to another attribute
     */
    public boolean drillDownLevel(Level attribute, int level) {

        ProjectionFragment oldAttribute = ProjectionFragment.newInstance(attribute);
        Level l = oldAttribute.getLevel();

        for (int i = 0; i < level; i++) {
            l = l.getChildLevel();
        }

        ProjectionFragment newAttribute = ProjectionFragment.newInstance(l);

        if (this.attributes.remove(oldAttribute)) {
            return this.attributes.add(newAttribute);
        } else {
            return false;
        }
    }

    public boolean rollupLevel(Level attribute, int level) {

        ProjectionFragment oldAttribute = ProjectionFragment.newInstance(attribute);
        Level l = oldAttribute.getLevel();

        for (int i = 0; i < level; i++) {
            l = l.getParentLevel();
        }

        ProjectionFragment newAttribute = ProjectionFragment.newInstance(l);

        if (this.attributes.remove(oldAttribute)) {
            return this.attributes.add(newAttribute);
        } else {
            return false;
        }
    }

    public void changeValue(Level attribute, Member newValue) {
        SelectionFragment newSelection = SelectionFragment.newInstance(newValue);

        SelectionFragment selection = getSelection(attribute);

        //System.out.println("REMOVE "+selection.getValue().getName());
        //System.out.println("ADD "+newSelection.getValue().getName());
        this.selectionPredicates.remove(selection);
        this.selectionPredicates.add(newSelection);
    }

    public SelectionFragment getSelection(Level attribute) {
        for (SelectionFragment s : this.selectionPredicates) {
            if (s.getLevel() == attribute) {
                return s;
            }
        }

        return null;
    }

    public Level levelRandom(int ran) {
        int id = 0;
        for (ProjectionFragment pf : this.getAttributes()) {
            if (id == ran) {
                return pf.getLevel();
            }
            id++;
        }

        return null;
    }

    public SelectionFragment selectionRandom(int ran) {
        int id = 0;
        for (SelectionFragment sf : this.getSelectionPredicates()) {
            if (id == ran) {
                return sf;
            }
            id++;
        }

        return null;
    }

    public MeasureFragment measureRandom(int ran) {
        int id = 0;
        for (MeasureFragment mf : this.getMeasures()) {
            if (id == ran) {
                return mf;
            }
            id++;
        }

        return null;
    }

    public ArrayList<ArrayList<ProjectionFragment>> getProjectionByHierarchy() {
        ArrayList<ArrayList<ProjectionFragment>> result = new ArrayList<ArrayList<ProjectionFragment>>();

        for (Hierarchy d : Generics.getHierarchies()) {
            if (!d.getName().equals("Measures")) {
                ArrayList<ProjectionFragment> projections = new ArrayList<ProjectionFragment>();
                for (ProjectionFragment pf : this.attributes) {
                    //System.out.println(pf.getLevel().getDimension().getName());
                    if (pf.getLevel().getHierarchy().getName().equals(d.getName())) {
                        projections.add(pf);
                    }
                }
                result.add(projections);
            }
        }

        return result;
    }

    public boolean cover(Qfset q) {

        if (!q.getMeasures().containsAll(this.measures)) {
            return false;
        }

        if (!q.getSelectionPredicates().containsAll(this.selectionPredicates)) {
            return false;
        }

        for (ProjectionFragment attributeQ1 : this.getAttributes()) {
            Hierarchy h = attributeQ1.getHierarchy();

            ProjectionFragment attributeQ2 = q.getAttributeFromHierarchy(h);

            if (attributeQ1.getLevel().getDepth() > attributeQ2.getLevel().getDepth()) {
                return false;
            }
        }

        return true;
    }

    public boolean belongsQTop() {
        if (this.getMeasures().isEmpty() || this.getSelectionPredicates().isEmpty()) {
            return true;
        }

        for (ProjectionFragment p : this.getAttributes()) {
            if (!p.getLevel().isAll()) {
                return false;
            }
        }

        return true;
    }
    
    public boolean isqTop() {
       
        if (!(this.getMeasures().isEmpty() && this.getSelectionPredicates().isEmpty())) {
            return false;
        }

        for (ProjectionFragment p : this.getAttributes()) {
            if (!p.getLevel().isAll()) {
                return false;
            }
        }

        return true;
    }

    public ReferenceSet computeReferences() {
        ReferenceSet result = new ReferenceSet();

        for (Hierarchy d : Generics.getHierarchies()) {

            if (!d.toString().equals("[Measures]")) {
                ArrayList<Member> members = new ArrayList<Member>();
                for (SelectionFragment s : selectionPredicates) {
                    if (s.getLevel().getHierarchy()== d) {
                        members.add(s.getValue());
                    }
                }
                if (members.isEmpty()) {
                    members.add(d.getDefaultMember());
                }
                result.addReferencePerDimension(members);
            }
        }

        return result;
    }

    public Query toMDX() {
        if (mondrianQuery == null) {
            String select = "SELECT ";
            String where = "";
            String from = "FROM " + fr.univ_tours.li.jaligon.falseto.Generics.Connection.getCubeName();
            ArrayList<SelectionFragment> forbiddenSelections = new ArrayList<SelectionFragment>();

            select += "{";
            int nbMeasure = 0;

            int axis = 0;

            //for identifying the attributes where a selection has been done (because it is a different treatment)
            for (ProjectionFragment pf : attributes) {
                for (SelectionFragment sf : selectionPredicates) {
                    if (sf.getLevel().getHierarchy()== pf.getLevel().getHierarchy()) {
                        forbiddenSelections.add(sf);
                    }
                }
            }

            for (MeasureFragment mf : measures) {
                if (measures.size() - 1 == nbMeasure) {
                    select += mf.getAttribute().getUniqueName() + "} ON " + axis + ",\n";
                    axis++;
                } else {
                    select += mf.getAttribute().getUniqueName() + ", ";
                }
                nbMeasure++;
            }

            if (axis == 0) {
                select = "SELECT ";
            }
            select += "{" + crossjoinAttributesMDX(attributes, forbiddenSelections) + "} ON " + axis + " \n";

            //where clause
            ArrayList<SelectionFragment> sfs = new ArrayList<SelectionFragment>();

            for (SelectionFragment sf : selectionPredicates) {
                if (!forbiddenSelections.contains(sf)) {
                    sfs.add(sf);
                }
            }

            if (!sfs.isEmpty()) {
                where = "where(" + crossjoinSelectionsMDX(sfs) + ")";
            }

            //final query
            String queryString = select + " " + from + " " + where;

            mondrian.olap.Connection cnx = fr.univ_tours.li.jaligon.falseto.Generics.Connection.getCnx();
            Query query = cnx.parseQuery(queryString);

            //RolapResult result = (RolapResult) cnx.execute(query);
            //System.out.println(result);
            return query;
        } else {
            return mondrianQuery;
        }
    }

    public Query toMDX(HashSet<ProjectionFragment> projectionOnRows, HashSet<ProjectionFragment> projectionOnColumns) {
        if (mondrianQuery == null) {
            String select;
            String where = "";
            String from = "FROM " + fr.univ_tours.li.jaligon.falseto.Generics.Connection.getCubeName();
            ArrayList<SelectionFragment> forbiddenSelections = new ArrayList<SelectionFragment>();

            int nbMeasure = 0;

            int axis = 0;

            //for identifying the attributes where a selection has been done (because it is a different treatment)
            for (ProjectionFragment pf : attributes) {
                for (SelectionFragment sf : selectionPredicates) {
                    if (sf.getLevel().getHierarchy() == pf.getLevel().getHierarchy()) {
                        forbiddenSelections.add(sf);
                    }
                }
            }

            String measureString = "";

            if (measures.isEmpty() && projectionOnRows.isEmpty()) {
                select = "SELECT ";
            } else {
                if (projectionOnRows.size() == 1) {
                    Level l = projectionOnRows.iterator().next().getLevel();
                    ArrayList<SelectionFragment> selectionDim = new ArrayList<SelectionFragment>();

                    for (SelectionFragment sf : forbiddenSelections) {
                        if (sf.getLevel().getHierarchy() == l.getHierarchy()) {
                            selectionDim.add(sf);
                        }
                    }

                    if (selectionDim.isEmpty()) {
                        select = "{" + projectionOnRows.iterator().next().getLevel().getUniqueName() + ".members}";
                    } else {
                        select = "{";
                        for (SelectionFragment sf : selectionDim) {
                            select += sf.getValue().getUniqueName() + ",";
                        }
                        select = select.substring(0, select.length() - 1) + "}";
                    }
                } else {
                    select = crossjoinAttributesMDX(projectionOnRows, forbiddenSelections);
                }

                for (MeasureFragment mf : measures) {
                    if (measures.size() - 1 == nbMeasure) {
                        measureString += mf.getAttribute().getUniqueName() + "}";
                    } else {
                        measureString += mf.getAttribute().getUniqueName() + ", ";
                    }
                    nbMeasure++;
                }

                if (!measures.isEmpty()) {
                    select = "SELECT {Crossjoin({" + measureString + "," + select + ")} ON " + axis + " ";
                    axis++;
                } else {
                    select = "SELECT {" + select + "} ON " + axis + " ";
                    axis++;
                }
            }

            if (!projectionOnColumns.isEmpty()) {
                if (!projectionOnRows.isEmpty()) {
                    select += ", \n";
                }
                if (projectionOnColumns.size() == 1) {
                    Level l = projectionOnColumns.iterator().next().getLevel();
                    ArrayList<SelectionFragment> selectionDim = new ArrayList<SelectionFragment>();

                    for (SelectionFragment sf : forbiddenSelections) {
                        if (sf.getLevel().getHierarchy() == l.getHierarchy()) {
                            selectionDim.add(sf);
                        }
                    }

                    if (selectionDim.isEmpty()) {
                        select += "{" + projectionOnColumns.iterator().next().getLevel().getUniqueName() + ".members} ON " + axis + " \n";
                    } else {
                        select += "{";
                        for (SelectionFragment sf : selectionDim) {
                            select += sf.getValue().getUniqueName() + ",";
                        }
                        select = select.substring(0, select.length() - 1) + "} ON " + axis + " \n";
                    }

                    //select += "{" + projectionOnColumns.iterator().next().getLevel().getUniqueName() + ".members} ON " + axis + " \n";
                } else {
                    select += "{" + crossjoinAttributesMDX(projectionOnColumns, forbiddenSelections) + "} ON " + axis + " \n";

                }
            }

            //select += "{" + crossjoinAttributesMDX(projectionOnColumns, forbiddenSelections) + "} ON " + axis + " \n";
            //where clause
            ArrayList<SelectionFragment> sfs = new ArrayList<SelectionFragment>();

            for (SelectionFragment sf : selectionPredicates) {
                if (!forbiddenSelections.contains(sf)) {
                    sfs.add(sf);
                }
            }

            if (!sfs.isEmpty()) {
                where = "where(" + crossjoinSelectionsMDX(sfs) + ")";
            }

            //final query
            String queryString = select + " " + from + " " + where;

            //System.out.println(queryString);
            mondrian.olap.Connection cnx = fr.univ_tours.li.jaligon.falseto.Generics.Connection.getCnx();

            Query query = cnx.parseQuery(queryString);

            //RolapResult result = (RolapResult) cnx.execute(query);
            //System.out.println(queryString);

            return query;
        } else {
            return mondrianQuery;
        }
    }

    public Query toMDX(HashSet<ProjectionFragment> projectionOnRows, HashSet<ProjectionFragment> projectionOnColumns, ArrayList<Level> levelsToLimit) {
        if (mondrianQuery == null) {
            String select;
            String where = "";
            String from = "FROM " + fr.univ_tours.li.jaligon.falseto.Generics.Connection.getCubeName();
            ArrayList<SelectionFragment> forbiddenSelections = new ArrayList<SelectionFragment>();

            int nbMeasure = 0;

            int axis = 0;

            //for identifying the attributes where a selection has been done (because it is a different treatment)
            for (ProjectionFragment pf : attributes) {
                for (SelectionFragment sf : selectionPredicates) {
                    if (sf.getLevel().getHierarchy() == pf.getLevel().getHierarchy()) {
                        forbiddenSelections.add(sf);
                    }
                }
            }

            String measureString = "";

            if (measures.isEmpty() && projectionOnRows.isEmpty()) {
                select = "SELECT ";
            } else {

                if (projectionOnRows.size() == 1) {
                    Level l = projectionOnRows.iterator().next().getLevel();

                    if (levelsToLimit.contains(l)) {
                        select = "{TOPCOUNT(" + l.getUniqueName() + ".members,5)}";
                    } else {
                        select = "{" + l.getUniqueName() + ".members}";
                    }
                } else {
                    select = crossjoinAttributesMDX(projectionOnRows, forbiddenSelections, levelsToLimit);
                }

                for (MeasureFragment mf : measures) {
                    if (measures.size() - 1 == nbMeasure) {
                        measureString += mf.getAttribute().getUniqueName() + "}";
                    } else {
                        measureString += mf.getAttribute().getUniqueName() + ", ";
                    }
                    nbMeasure++;
                }

                if (!measures.isEmpty()) {
                    select = "SELECT {Crossjoin({" + measureString + "," + select + ")} ON " + axis + " ";
                    axis++;
                } else {
                    select = "SELECT {" + select + "} ON " + axis + " ";
                    axis++;
                }
            }

            if (!projectionOnColumns.isEmpty()) {
                if (!projectionOnRows.isEmpty()) {
                    select += ", \n";
                }
                if (projectionOnColumns.size() == 1) {
                    Level l = projectionOnColumns.iterator().next().getLevel();

                    if (levelsToLimit.contains(l)) {
                        select += "{TOPCOUNT(" + l.getUniqueName() + ".members,5)} ON " + axis + " \n";
                    } else {
                        select += "{" + l.getUniqueName() + ".members} ON " + axis + " \n";
                    }
                } else {
                    select += "{" + crossjoinAttributesMDX(projectionOnColumns, forbiddenSelections, levelsToLimit) + "} ON " + axis + " \n";

                }
            }

            //select += "{" + crossjoinAttributesMDX(projectionOnColumns, forbiddenSelections) + "} ON " + axis + " \n";
            //where clause
            ArrayList<SelectionFragment> sfs = new ArrayList<SelectionFragment>();

            for (SelectionFragment sf : selectionPredicates) {
                if (!forbiddenSelections.contains(sf)) {
                    sfs.add(sf);
                }
            }

            if (!sfs.isEmpty()) {
                where = "where(" + crossjoinSelectionsMDX(sfs) + ")";
            }

            //final query
            String queryString = select + " " + from + " " + where;

            //System.out.println(queryString);
            mondrian.olap.Connection cnx = fr.univ_tours.li.jaligon.falseto.Generics.Connection.getCnx();

            Query query = cnx.parseQuery(queryString);

            //RolapResult result = (RolapResult) cnx.execute(query);
            //System.out.println(result);
            return query;
        } else {
            return mondrianQuery;
        }
    }

    private String crossjoinSelectionsMDX(ArrayList<SelectionFragment> selections) {

        int pair = 0;
        String s = "";

        for (Hierarchy h : Generics.getHierarchies()) {

            if (pair != 0 && pair % 2 == 0) {
                //s = s.substring(0, s.length() - 1);
                //s+="),";
                s = "CrossJoin(" + s + "),";
                pair++;
            }

            String selectionDim = "{";

            int sel = 0;
            for (SelectionFragment sf : selections) {
                if (sf.getLevel().getHierarchy() == h) {
                    selectionDim += sf.getValue().getUniqueName() + ",";
                    sel++;
                }
            }

            selectionDim = selectionDim.substring(0, selectionDim.length() - 1);
            selectionDim += "}";

            if (sel != 0) {
                if (pair == 0) {
                    s += selectionDim + ",";
                } else {
                    s += selectionDim;
                }
                pair++;
            }

        }

        s = s.substring(0, s.length() - 1);

        return s;
    }

    private String crossjoinAttributesMDX(HashSet<ProjectionFragment> attributes, ArrayList<SelectionFragment> selections, ArrayList<Level> levelsToLimit) {
        int pair = 0;
        String s = "";

        for (ProjectionFragment pf : attributes) {
            if (pair != 0 && pair % 2 == 0) {
                s = "CrossJoin(" + s + "),";
                pair++;
            }

            ArrayList<SelectionFragment> selectionDim = new ArrayList<SelectionFragment>();

            for (SelectionFragment sf : selections) {
                if (sf.getLevel().getHierarchy() == pf.getHierarchy()) {
                    selectionDim.add(sf);
                }
            }

            if (selectionDim.isEmpty()) {
                Level l = pf.getLevel();
                if (levelsToLimit.contains(l)) {
                    s += "{TOPCOUNT(" + l.getUniqueName() + ".members,5)}";
                } else {
                    s += "{" + l.getUniqueName() + ".members}";
                }
            } else {
                String temp = "{";
                for (SelectionFragment sf : selectionDim) {
                    temp += sf.getValue().getUniqueName() + ",";
                }
                temp = temp.substring(0, temp.length() - 1) + "}";

                //{DESCENDANTS(" + selection.getValue().getUniqueName() + "," + pf.getLevel().getUniqueName() + ")}
                s += "{DESCENDANTS(" + temp + "," + pf.getLevel().getUniqueName() + ")}";
            }

            if (pair == 0) {
                s += ",";
            }

            pair++;
        }

        s = "CrossJoin(" + s + ")";
        return s;
    }

    private String crossjoinAttributesMDX(HashSet<ProjectionFragment> attributes, ArrayList<SelectionFragment> selections) {
        int pair = 0;
        String s = "";

        for (ProjectionFragment pf : attributes) {
            if (pair != 0 && pair % 2 == 0) {
                s = "CrossJoin(" + s + "),";
                pair++;
            }

            ArrayList<SelectionFragment> selectionDim = new ArrayList<SelectionFragment>();

            for (SelectionFragment sf : selections) {
                if (sf.getLevel().getHierarchy() == pf.getHierarchy()) {
                    selectionDim.add(sf);
                }
            }

            if (selectionDim.isEmpty()) {
                s += "{" + pf.getLevel().getUniqueName() + ".members}";
            } else {
                String temp = "{";
                for (SelectionFragment sf : selectionDim) {
                    temp += sf.getValue().getUniqueName() + ",";
                }
                temp = temp.substring(0, temp.length() - 1) + "}";

                //{DESCENDANTS(" + selection.getValue().getUniqueName() + "," + pf.getLevel().getUniqueName() + ")}
                s += "{DESCENDANTS(" + temp + "," + pf.getLevel().getUniqueName() + ")}";
            }

            if (pair == 0) {
                s += ",";
            }

            pair++;
        }

        s = "CrossJoin(" + s + ")";
        return s;
    }

    /*public Query toMDXwithIPUMS() {
     if (mondrianQuery == null) {
     String select;
     String where = "";
     String from = "FROM " + fr.univ_tours.li.jaligon.falseto.Generics.Connection.getCubeName();
     ArrayList<SelectionFragment> forbiddenSelections = new ArrayList<SelectionFragment>();

     int nbMeasure = 0;

     int axis = 0;

     //for identifying the attributes where a selection has been done (because it is a different treatment)
     for (ProjectionFragment pf : attributes) {
     for (SelectionFragment sf : selectionPredicates) {
     if (sf.getLevel().getDimension() == pf.getLevel().getDimension()) {
     forbiddenSelections.add(sf);
     }
     }
     }

     HashSet<ProjectionFragment> projectionOnRows = new HashSet<ProjectionFragment>();
     HashSet<ProjectionFragment> projectionOnColumns = new HashSet<ProjectionFragment>();

     for (ProjectionFragment pf : attributes) {
     if (pf.getLevel().getDimension().getName().equalsIgnoreCase("occupation") || pf.getLevel().getDimension().getName().equalsIgnoreCase("race")) {
     projectionOnRows.add(pf);
     } else {
     projectionOnColumns.add(pf);
     }
     }

     select = crossjoinAttributesMDX(projectionOnRows, forbiddenSelections);

     String measureString = "";
     for (MeasureFragment mf : measures) {
     if (measures.size() - 1 == nbMeasure) {
     measureString += mf.getAttribute().getUniqueName() + "}";
     } else {
     measureString += mf.getAttribute().getUniqueName() + ", ";
     }
     nbMeasure++;
     }

     if (!measures.isEmpty()) {
     select = "SELECT {Crossjoin({" + measureString + "," + select + ")} ON " + axis + ", \n";
     axis++;
     } else {
     select = "SELECT {" + select + "} ON " + axis + ", \n";
     axis++;
     }


     if (measures.isEmpty() && projectionOnRows.isEmpty()) {
     select = "SELECT ";
     }
     select += "{" + crossjoinAttributesMDX(projectionOnColumns, forbiddenSelections) + "} ON " + axis + " \n";

     //where clause
     ArrayList<SelectionFragment> sfs = new ArrayList<SelectionFragment>();

     for (SelectionFragment sf : selectionPredicates) {
     if (!forbiddenSelections.contains(sf)) {
     sfs.add(sf);
     }
     }

     if (!sfs.isEmpty()) {
     where = "where(" + crossjoinSelectionsMDX(sfs) + ")";
     }

     //final query
     String queryString = select + " " + from + " " + where;

     //System.out.println(queryString);

     mondrian.olap.Connection cnx = fr.univ_tours.li.jaligon.falseto.Generics.Connection.getCnx();
     Query query = cnx.parseQuery(queryString);

     //RolapResult result = (RolapResult) cnx.execute(query);
     //System.out.println(result);

     return query;
     } else {
     return mondrianQuery;
     }
     }*/
    public static ReferenceSet getReferences(RolapResult query) {
        ReferenceSet result = new ReferenceSet();

        ArrayList<ArrayList<Member>> referencesPerDimension = new ArrayList<ArrayList<Member>>();

        for (Hierarchy d : Generics.getHierarchies()) {

            if (!d.getDimension().isMeasures()) {
                boolean exploredDimension = false;
                for (int i = 0; i < query.getAxes().length; i++) {
                    ArrayList<Member> membersDimension = new ArrayList<Member>();
                    for (Position p : query.getAxes()[i].getPositions()) {

                        for (Member m : p) {
                            if (m.getLevel().getHierarchy() == d) {
                                membersDimension.add(m);
                            }
                        }
                    }

                    if (!membersDimension.isEmpty()) {
                        referencesPerDimension.add(membersDimension);
                        exploredDimension = true;
                        break;
                    }

                }

                if (!exploredDimension) {
                    for (Position p : query.getSlicerAxis().getPositions()) {//this axis returns all the set of members in the slicer in one position (so all members with any dimension)
                        for (Member m : p) {
                            ArrayList<Member> membersDimension = new ArrayList<Member>();
                            if (m.getLevel().getHierarchy()== d) {
                                membersDimension.add(m);
                                referencesPerDimension.add(membersDimension);
                            }
                        }
                    }
                }
            }
        }

        result.addAllReferencesPerDimension(referencesPerDimension);
        return result;
    }

    public void addScore(double score) {
        this.score += score;
    }

    public double getScore() {
        return score;
    }

    public void resetScore() {
        this.score = 0;
    }

    public void addAlignment(String alignment) {
        this.alignment += alignment;
    }

    public String getAlignment() {
        return alignment;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.measures);
        hash = 17 * hash + Objects.hashCode(this.attributes);
        hash = 17 * hash + Objects.hashCode(this.selectionPredicates);
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

        final Qfset query = (Qfset) obj;
        if (this.attributes.containsAll(query.getAttributes()) && query.getAttributes().containsAll(this.attributes)) {
            if (this.selectionPredicates.containsAll(query.getSelectionPredicates()) && query.getSelectionPredicates().containsAll(this.selectionPredicates)) {
                if (this.measures.containsAll(query.getMeasures()) && query.getMeasures().containsAll(this.measures)) {
                    return true;
                }
            }
        }
        return false;
    }

}
