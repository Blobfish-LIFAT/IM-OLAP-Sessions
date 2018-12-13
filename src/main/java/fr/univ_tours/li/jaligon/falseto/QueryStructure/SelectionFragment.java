package fr.univ_tours.li.jaligon.falseto.QueryStructure;

import java.util.HashMap;
import mondrian.olap.Level;
import mondrian.olap.Member;

public class SelectionFragment extends Fragment {

    private Level attribute;
    private Member value; // update: should be a set of values?
    private int numericValue = Integer.MAX_VALUE;//used by the Filter clause. ex : [Measure].[SUM] > 1000 -> 1000 is the numeriv value. When MaxValue, no Filter clause.
    
    /**
     * lists all the selections fragment objects created
     */
    private static HashMap<Member, SelectionFragment> allSelections = new HashMap<Member, SelectionFragment>();

    /**
     * Return a selection fragment if the member is found or create a new selection fragment otherwise.
     * @param m the member.
     * @return a selection fragment.
     */
    public static SelectionFragment newInstance(Member m) {
        if (allSelections.containsKey(m)) {
            return (allSelections.get(m));
        } else {
            return (new SelectionFragment(m));
        }
    }

    /**
     * Return a selection fragment if the member is found or create a new selection fragment otherwise.
     * Moreover, the numeric value is set.
     * Then, this method should be called for a 'Filter' clause.
     * @param m the member.
     * @param numericValue the numeric value.
     * @return a selection fragment.
     */
    public static SelectionFragment newInstance(Member m, int numericValue) {
        if (allSelections.containsKey(m)) {
            SelectionFragment sf = allSelections.get(m);
            sf.setNumericValue(numericValue);
            return sf;
        } else {
            SelectionFragment sf = new SelectionFragment(m);
            sf.setNumericValue(numericValue);
            return sf;
        }
    }

    /**
     * Create a new selection fragment, added in the list of selections {@link #allSelections}.
     * @param m the member allowing to build the new selection fragment.
     */
    public SelectionFragment(Member m) {
        super(m.toString());
        value = m;
        attribute = m.getLevel();
        type = 1;
        allSelections.put(m, this);
    }

    /*public SelectionFragment(Level l) {
        super(null);
        attribute = l;
        type = 1;
    }*/

    /*public SelectionFragment(SelectionFragment sf,String fragmentName) {
       super(fragmentName);
        this.attribute = sf.attribute;
        this.value = sf.value;
        this.numericValue = sf.numericValue;
        this.type = sf.type;
    }*/

    /**
     * Return the level of the member of the selection fragment.
     * @return the level.
     */
    public Level getLevel() {
        return attribute;
    }

    /**
     * Return the member of the selection fragment.
     * @return the member.
     */
    public Member getValue() {
        return value;
    }

    /**
     * Return the numeric value of the selection fragment.
     * @return a numeric value.
     */
    public int getNumericValue() {
        return numericValue;
    }

    /**
     * Set the numeric value.
     * @param numericValue the numeric value.
     */
    public void setNumericValue(int numericValue) {
        this.numericValue = numericValue;
    }

    /**
     * Set value of the selection.
     * @param value the value of the selection.
     */
    public void setValue(Member value) {
        this.value = value;
    }

    /**
     * Test if two selection fragments are similar.
     * Two selection fragments are similar if their types and their levels are equal.
     * @param other another selection fragment.
     * @return true if the two selection fragments are similar, false otherwise.
     */
    @Override
    public boolean isSimilar(Fragment other) {
        if (other.getType() == this.type) {
            return (attribute == ((SelectionFragment) other).getLevel());
        } else {
            return false;
        }
    }

    /**
     * Test if two selection fragments are equal.
     * Two selection fragments are equal if their types, their levels and their members are equal.
     * @param other another selection fragment.
     * @return true if the two selection fragments are equal, false otherwise.
     */
    @Override
    public boolean isEqual(Fragment other) {
        if (other.getType() == this.type) {
            return (this.attribute == ((SelectionFragment) other).getLevel()
                    && this.value == ((SelectionFragment) other).getValue());
        } else {
            return false;
        }
    }

    /**
     *
     * @return the selection fragments.
     */
    public static HashMap<Member, SelectionFragment> getAllSelections() {
        return allSelections;
    }

    public double computeSimilarity(SelectionFragment p) {
        return 1-(double)Math.abs(this.attribute.getDepth()-p.attribute.getDepth())/(double)(this.attribute.getHierarchy().getLevels().length - 1);

    }
    
    public double computeDistLev(SelectionFragment p)
    {
        return Math.abs(this.attribute.getDepth()-p.attribute.getDepth());
    }

}
