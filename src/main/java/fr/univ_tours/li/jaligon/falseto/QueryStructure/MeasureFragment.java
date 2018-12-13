package fr.univ_tours.li.jaligon.falseto.QueryStructure;

import java.util.HashMap;
import mondrian.olap.Member;

public class MeasureFragment extends Fragment implements java.io.Serializable {

    private Member attribute;
    /**
     * lists all the measures fragment objects created
     */
    private static HashMap<Member, MeasureFragment> allMeasures = new HashMap<Member, MeasureFragment>();

    /**
     * Return a measure fragment if the member is found or create a new measure
     * fragment otherwise.
     *
     * @param m the member.
     * @return a measure fragment.
     */
    public static MeasureFragment newInstance(Member m) {
        if (allMeasures.containsKey(m)) {
            return (allMeasures.get(m));
        } else {
            return (new MeasureFragment(m));
        }
    }

    /**
     * Create a new measure fragment, added in the list of measures
     * {@link #allMeasures}.
     *
     * @param m the member allowing to build the new measure fragment.
     */
    public MeasureFragment(Member m) {
        super(m.toString());
        attribute = m;
        type = 2;
        allMeasures.put(m, this);
    }

    /**
     * Return the member of the measure fragment.
     *
     * @return a member.
     */
    public Member getAttribute() {
        return attribute;
    }

    /**
     * Test if two measure fragments are similar. Two measure fragments are
     * similar if their types are equal.
     *
     * @param other another measure fragment.
     * @return true if the two measure fragments are similar, false otherwise.
     */
    @Override
    public boolean isSimilar(Fragment other) {
        if (other.getType() == this.type) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Test if two measure fragments are equal. Two measure fragments are equal
     * if their types and their members are equal.
     *
     * @param other another measure fragment.
     * @return true if the two measure fragments are equal, false otherwise.
     */
    @Override
    public boolean isEqual(Fragment other) {
        if (other.getType() == this.type && this.type == 2) {
            return (this.attribute == ((MeasureFragment) other).getAttribute());
        } else {
            return false;
        }
    }

    /**
     *
     * @return the measure fragments.
     */
    public static HashMap<Member, MeasureFragment> getAllMeasures() {
        return allMeasures;
    }

}
