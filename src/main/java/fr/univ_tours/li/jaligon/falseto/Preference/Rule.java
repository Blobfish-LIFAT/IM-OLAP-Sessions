package fr.univ_tours.li.jaligon.falseto.Preference;

// encapsulate an association rule
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Qfset;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Fragment;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.MeasureFragment;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.ProjectionFragment;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.SelectionFragment;

public class Rule {

    private Qfset body;
    private Fragment head;
    private Float support;
    private Double confidence;

    /**
     * This constructor encapsulates an empty assocation rule.
     */
    public Rule() {
    }

    /**
     * This constructor encapsulates an assocation rule.
     * The body of the rule is a set of fragment, the head is only a fragment.
     * @param ant the body of the rule.
     * @param cons the head of the rule.
     * @param supp the suppoort of the rule.
     * @param conf the confidence of the rule.
     */
    public Rule(Qfset ant, Fragment cons, float supp, double conf) {
        body = ant;
        head = cons;
        support = supp;
        confidence = conf;
    }

    /**
     * This constructor encapsulates an assocation rule from item names.
     * The body of the rule is a set of fragment, the head is only a fragment.
     * @param ant the items names of the body of the rule.
     * @param cons the items names of the head of the rule.
     * @param supp the support of the rule.
     * @param conf the confidence of the rule.
     */
    public Rule(String[] ant, String cons, float supp, double conf) {
        body = buildBody(ant);
        head = buildHead(cons);
        support = supp;
        confidence = conf;
    }

    /**
     * Build the body of the rule from a set of item names.
     * @param bodyItems the item names of the body of the rule.
     * @return a set of fragments.
     */
    private Qfset buildBody(String[] bodyItems) {
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
        return bodyQfset;
    }

    /**
     * Build the head of the rule from the item name.
     * @param headItem the item name of the head.
     * @return a fragment.
     */
    private Fragment buildHead(String headItem) {

        for (ProjectionFragment p : ProjectionFragment.getAllProjections().values()) {
            if (p.toString().compareTo(headItem) == 0) {
                return p;
            }
        }

        for (SelectionFragment p : SelectionFragment.getAllSelections().values()) {
            if (p.toString().compareTo(headItem) == 0) {
                return p;
            }
        }

        for (MeasureFragment p : MeasureFragment.getAllMeasures().values()) {
            if (p.toString().compareTo(headItem) == 0) {
                return p;
            }
        }
        return null;
    }

    /**
     *
     * @return the body of the rule.
     */
    public Qfset getBody() {
        return body;
    }

    /**
     * Replace the body of the rule.
     * @param body the body of the rule.
     */
    public void setBody(Qfset body) {
        this.body = body;
    }

    /**
     *
     * @return the head of the rule.
     */
    public Fragment getHead() {
        return head;
    }

    /**
     * Replace the head of the rule.
     * @param head the head of the rule.
     */
    public void setHead(Fragment head) {
        this.head = head;
    }

    /**
     *
     * @return the confidence of the rule.
     */
    public double getConfidence() {
        return confidence;
    }

    /**
     *
     * @return the support of the rule.
     */
    public float getSupport() {
        return support;
    }

    /**
     *
     * @return the string of the rule.
     */
    @Override
    public String toString() {
        return (body.toString() + " -> " + head.toString() + " (" + support + ", " + confidence + ")");
    }
}
