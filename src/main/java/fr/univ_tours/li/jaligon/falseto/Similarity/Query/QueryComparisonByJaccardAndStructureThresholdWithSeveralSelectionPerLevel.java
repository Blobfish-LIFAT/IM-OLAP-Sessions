/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.Similarity.Query;

import fr.univ_tours.li.jaligon.falseto.Generics.MondrianObject;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.MeasureFragment;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.ProjectionFragment;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Qfset;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.SelectionFragment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import mondrian.olap.Hierarchy;
import mondrian.olap.Member;



/**
 *
 * @author Elisa
 */
public class QueryComparisonByJaccardAndStructureThresholdWithSeveralSelectionPerLevel extends QueryComparison {

    private static HashMap<String,SelectionFragment> allLevelSelectionFragments = new HashMap();
	
	static {
		HashSet<Hierarchy> hierarchies = fr.univ_tours.li.jaligon.falseto.Generics.Generics.getHierarchies();
        for(Hierarchy h : hierarchies){
            if(h.getClass().equals(mondrian.rolap.RolapCubeHierarchy.class)){
                Member m = h.getAllMember();
                allLevelSelectionFragments.put(h.getName(), SelectionFragment.newInstance(MondrianObject.getSelection(m.getLevel().getName(), h.getName(), m.getName())));
            }
        }
	}
    
    public QueryComparisonByJaccardAndStructureThresholdWithSeveralSelectionPerLevel(Qfset q1, Qfset q2, double alpha, double beta, double gamma) {
        super(q1, q2, alpha, beta, gamma);
    }

    /**
     * Method to compute the similarity of the current query with the input
     * query. The similarity is computed by using these criteria: -Measure:
     * |Intersection(M1,M2)|/Max(|M1|,|M2|) (or Min(|M1|,|M2|)) -Group by set:
     * -Selection criteria:
     */
    @Override
    public Similarity computeSimilarity() {
        /*System.out.println("Measure : "+computeMeasureSimilarity());
        System.out.println("GB : "+computeGroupBySimilarity());
        System.out.println("Sel : "+computeSelPredSimilarity());*/
        
        return new StructureSimilarity(computeMeasureSimilarity(), computeGroupBySimilarity(), computeSelPredSimilarity(), alpha, beta, gamma);
    }

    private double computeSelPredSimilarity() {
        double numberDimension = fr.univ_tours.li.jaligon.falseto.Generics.Generics.getHierarchies().size()-1;

        double sumDistance = 0;

        for (Hierarchy h : fr.univ_tours.li.jaligon.falseto.Generics.Generics.getHierarchies()) {
            if (!h.getDimension().isMeasures()) {
                double distancePred = 0;
                HashSet<SelectionFragment> s1 = query1.getSelectionsFromHierarchy(h);
                HashSet<SelectionFragment> s2 = query2.getSelectionsFromHierarchy(h);

                //System.out.println("s1 : "+s1.size());
                //System.out.println("s2 : "+s2.size());

                //if s1 or s2 are null, we have a selection on the ALLlevel
                if (s1.isEmpty()) {
                    s1.add(allLevelSelectionFragments.get(h.getName()));
                }
                if (s2.isEmpty()) {
                    s2.add(allLevelSelectionFragments.get(h.getName()));
                }

                //System.out.println("after s1 : "+s1.size());
                //System.out.println("after s2 : "+s2.size());

                distancePred = 1 - jaccard(s1, s2);

                if (distancePred == 1 && s1.iterator().next().getLevel() != s2.iterator().next().getLevel()) //double homothety = (double)((double)s1.getLevel().getHierarchy().getLevels().length - (double) 1) / (double) (s1.getLevel().getHierarchy().getLevels().length);
                {
                    distancePred = s1.iterator().next().computeDistLev(s2.iterator().next()) + 1;
                }

                sumDistance += (double) distancePred / (double) h.getLevels().length;
            }
        }

        /*for (ProjectionFragment attribute : this.query1.getAttributes()) {
         double distancePred = 0;
         Hierarchy h = attribute.getHierarchy();
         String dimension = h.getName();
         HashSet<SelectionFragment> s1 = query1.getSelectionsFromDimension(dimension);
         HashSet<SelectionFragment> s2 = query2.getSelectionsFromDimension(dimension);

         //System.out.println("s1 : "+s1.size());
         //System.out.println("s2 : "+s2.size());

         //if s1 or s2 are null, we have a selection on the ALLlevel
         if (s1.isEmpty()) {
         s1 = new HashSet<SelectionFragment>();
         s1.add(buildAllLevelSelectionFragment(attribute, dimension));
         }
         if (s2.isEmpty()) {
         s2 = new HashSet<SelectionFragment>();
         s2.add(buildAllLevelSelectionFragment(attribute, dimension));
         }

         //System.out.println("after s1 : "+s1.size());
         //System.out.println("after s2 : "+s2.size());

         distancePred = 1 - jaccard(s1, s2);

         if (distancePred == 1 && s1.iterator().next().getLevel() != s2.iterator().next().getLevel()) //double homothety = (double)((double)s1.getLevel().getHierarchy().getLevels().length - (double) 1) / (double) (s1.getLevel().getHierarchy().getLevels().length);
         {
         distancePred = s1.iterator().next().computeDistLev(s2.iterator().next()) + 1;
         }

         sumDistance += (double) distancePred / (double) h.getLevels().length;
         }*/

//System.out.println("Selections : "+(1 - ((double) sumDistance / (double) numberDimension)));
        return 1 - ((double) sumDistance / (double) numberDimension);
    }

    private double jaccard(HashSet<SelectionFragment> s1, HashSet<SelectionFragment> s2) {
        int matching = 0;
        for (SelectionFragment sf1 : s1) {
            for (SelectionFragment sf2 : s2) {
                if (sf1 == sf2) {
                    matching++;
                }
            }
        }

        return matching / (s1.size() + s2.size() - matching);
    }

    private double computeMeasureSimilarity() {
        HashSet<MeasureFragment> M1 = query1.getMeasures();
        HashSet<MeasureFragment> M2 = query2.getMeasures();
        if (M1.isEmpty() && M2.isEmpty()) {
            return 1;
        } else if (M1.isEmpty() || M2.isEmpty()) {
            return 0;
        } else {
            //System.out.println("Measure : "+(double) computeCardinalityIntersection(M1, M2) / computeCardinalityUnion(M1, M2));
            return (double) computeCardinalityIntersection(M1, M2) / computeCardinalityUnion(M1, M2);
        }
    }

    public double computeGroupBySimilarity() {
        //compute the average of the contribute on each dimension

        if (this.query1.getAttributes().isEmpty() && this.query2.getAttributes().isEmpty()) {//should not appear...
            return 1;
        } else if (this.query1.getAttributes().isEmpty() || this.query2.getAttributes().isEmpty()) {//should not appear...
            return 1;//pay attention for the returned value: here is the case for similarity between current session and frequent sequences...
        } else {
            double sumDistance = 0;
            HashSet<ProjectionFragment> attributes = query1.getAttributes();
            for (ProjectionFragment p1 : attributes) //compute the contribute of each dimension
            {
                ProjectionFragment p2 = query2.getAttributeFromHierarchy(p1.getLevel().getHierarchy());
                sumDistance += (double) p1.computeDistLev(p2) / (double) (p1.getHierarchy().getLevels().length - 1);
            }
            //System.out.println("GB : "+(1 - ((double) sumDistance / (double) attributes.size())));
            return 1 - ((double) sumDistance / (double) attributes.size());
        }
    }

    private int computeCardinalityIntersection(HashSet<MeasureFragment> M1, HashSet<MeasureFragment> M2) {
        int count = 0;
        for (MeasureFragment m1 : M1) {
            for (MeasureFragment m2 : M2) {
                if (m1.isEqual(m2)) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    private boolean containsMeasure(List<MeasureFragment> result, MeasureFragment m) {

        for (MeasureFragment m2 : result) {
            if (m2.isEqual(m)) {
                return true;
            }
        }
        return false;
    }

    private int computeCardinalityUnion(HashSet<MeasureFragment> M1, HashSet<MeasureFragment> M2) {
        ArrayList<MeasureFragment> result = new ArrayList<MeasureFragment>();
        result.addAll(M1);

        for (MeasureFragment m : M2) {
            if (!containsMeasure(result, m)) {
                result.add(m);
            }
        }

        return result.size();
    }
}
