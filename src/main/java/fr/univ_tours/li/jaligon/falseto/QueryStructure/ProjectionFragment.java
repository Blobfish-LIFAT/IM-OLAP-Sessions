package fr.univ_tours.li.jaligon.falseto.QueryStructure;

import java.util.HashMap;
import mondrian.olap.Hierarchy;
import mondrian.olap.Level;

public class ProjectionFragment extends Fragment implements java.io.Serializable {

    private Level attribute;
    /**
     * lists all the projection fragment objects created
     */
    private static HashMap<Level, ProjectionFragment> allProjections = new HashMap<Level, ProjectionFragment>();

    /**
     * Return a projection fragment if the level is found or create a new projection fragment otherwise.
     * @param l the level.
     * @return the new projection fragment.
     */
    public static ProjectionFragment newInstance(Level l) {
        if (allProjections.containsKey(l)) {
            return (allProjections.get(l));
        } else {
            return (new ProjectionFragment(l));
        }
    }

    /**
     * Remove a projection fragment, functions of the level.
     * @param l the level.
     */
    public static void remove(Level l) {
        allProjections.remove(l);
    }

    /**
     * Create a new projection fragment, added in the list of projections {@link #allProjections}.
     * @param l the level allowing to build the new projection fragment.
     */
    public ProjectionFragment(Level l) {
        super(l.toString());
        attribute = l;
        type = 0;
        allProjections.put(l, this);
    }

    /**
     * Return the level of the projection fragment.
     * @return the level.
     */
    public Level getLevel() {
        return attribute;
    }

    /**
     * Return the hierarchy of the level.
     * @return the hierachy.
     */
    public Hierarchy getHierarchy() {
        return attribute.getHierarchy();
    }

    /**
     * Test if two projection fragments are similar.
     * Two projection fragments are similar if their types and their hierarchies are equal.
     * @param other another fragment.
     * @return true if the two projection fragments are similar, false otherwise.
     */
    @Override
    public boolean isSimilar(Fragment other) {
        if (other.getType() == this.type) {
            return (attribute.getHierarchy() == ((ProjectionFragment) other).getHierarchy());
        } else {
            return false;
        }
    }

    /**
     * Test if two projection fragments are equal.
     * Two projection fragments are equal if their types and their levels are equal.
     * @param other another fragment.
     * @return true if the two projection fragments are equal, false otherwise.
     */
    @Override
    public boolean isEqual(Fragment other) {
        if (other.getType() == this.type) {
            return (this.attribute == ((ProjectionFragment) other).getLevel());
        } else {
            return false;
        }
    }

    /**
     *
     * @return the projection fragments.
     */
    public static HashMap<Level, ProjectionFragment> getAllProjections() {
        return allProjections;
    }


    public int getHierarchyLength(){
        return getHierarchy().getLevels().length - 1;
    }

    public double computeSimilarity(ProjectionFragment p){

       return 1-(double)Math.abs(this.attribute.getDepth()-p.attribute.getDepth())/getHierarchyLength();
    }

    public double computeDistLev(ProjectionFragment p){

       return Math.abs(this.attribute.getDepth()-p.attribute.getDepth());
    }
}
