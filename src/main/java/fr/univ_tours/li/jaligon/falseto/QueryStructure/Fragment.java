package fr.univ_tours.li.jaligon.falseto.QueryStructure;

/**
 * The class for fragments (so far only a string)
 */
public abstract class Fragment implements java.io.Serializable {

    private String the_fragment;
    protected int type;

    /**
     * Create a new fragment
     * @param s the name of the fragment (the name of the member if selection or measure or the name of the level if projection)
     */
    public Fragment(String s) {
        the_fragment = s;
    }

    /**
     * Test if two fragments are equal.
     * @param other another fragment.
     * @return true if the two fragments are equal, false otherwise.
     */
    public abstract boolean isEqual(Fragment other);

    /**
     * Test if two fragments are similar.
     * @param other another fragment.
     * @return true if the two fragments are similar, false otherwise.
     */
    public abstract boolean isSimilar(Fragment other);

    /**
     * Compare two fragments.
     * @param f another fragment.
     * @return true if the name of the two fragments are equals, false otherwise.
     */
    public int compareTo(Fragment f) {
        return (the_fragment.compareTo(f.toString()));
    }

    /**
     *
     * @return the name of the fragment
     */
    @Override
    public String toString() {
        return the_fragment;
    }

    /**
     *
     * @return the type of fragment : 0 is Projection, 1 is Selection, 2 is Measure
     */
    public int getType() {
        return type;
    }
}
