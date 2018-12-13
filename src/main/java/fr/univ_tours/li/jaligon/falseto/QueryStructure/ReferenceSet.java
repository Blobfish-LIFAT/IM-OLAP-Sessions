/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.QueryStructure;

import java.util.ArrayList;
import java.util.List;
import mondrian.olap.Member;
import mondrian.olap.SchemaReader;

/**
 *
 * @author aligon_j
 */
public class ReferenceSet {

    private ArrayList<ArrayList<Member>> referencesPerDimension;

    public ReferenceSet() {
        referencesPerDimension = new ArrayList<ArrayList<Member>>();
    }

    public ArrayList<ArrayList<Member>> getReferencesPerDimension() {
        return referencesPerDimension;
    }

    public void addReferencePerDimension(ArrayList<Member> membersForDimension) {
        referencesPerDimension.add(membersForDimension);
    }

    public void addAllReferencesPerDimension(ArrayList<ArrayList<Member>> references) {
        this.referencesPerDimension.addAll(references);
    }

    public ReferenceSet copy() {
        ReferenceSet result = new ReferenceSet();

        for (ArrayList<Member> members : referencesPerDimension) {
            ArrayList<Member> memberResult = new ArrayList<Member>();
            for (Member m : members) {
                memberResult.add(m);
            }
            result.addReferencePerDimension(memberResult);
        }

        return result;
    }

    public void setReferencesPerDimension(ArrayList<ArrayList<Member>> referencesPerDimension) {
        this.referencesPerDimension = referencesPerDimension;
    }

    public static ArrayList<ArrayList<Member>> referenceSetToReferencesPerDimension(ArrayList<ArrayList<Member>> referenceSet) {
        ArrayList<ArrayList<Member>> result = new ArrayList<ArrayList<Member>>();

        if (!referenceSet.isEmpty()) {
            for (int i = 0; i < referenceSet.get(0).size(); i++) {
                ArrayList<Member> membersPerDimension = new ArrayList<Member>();
                for (ArrayList<Member> ref : referenceSet) {
                    if (!membersPerDimension.contains(ref.get(i)) && ref.get(i) != null) {
                        //System.out.println(ref.get(i));
                        membersPerDimension.add(ref.get(i));
                    }
                }
                result.add(membersPerDimension);
            }
        }

        return result;
    }

    public static ArrayList<ArrayList<Member>> referencesPerDimensionToReferenceSet(ArrayList<ArrayList<Member>> referencesByDimension) {
        ArrayList<ArrayList<Member>> result = new ArrayList<ArrayList<Member>>();
        ArrayList<Member> reference = new ArrayList<Member>();

        ArrayList<ArrayList<Member>> referencesByDimensionWithoutDuplicates = new ArrayList<ArrayList<Member>>();

        for (ArrayList<Member> members : referencesByDimension) {
            ArrayList<Member> uniqueMembers = new ArrayList<Member>();
            for (Member m : members) {
                if (!uniqueMembers.contains(m)) {
                    uniqueMembers.add(m);
                }
            }
            if(!uniqueMembers.isEmpty())
            referencesByDimensionWithoutDuplicates.add(uniqueMembers);
        }

        getReference(referencesByDimensionWithoutDuplicates, 0, reference, result);

        /*System.out.println(result.size());
        for (ArrayList<Member> refer : result) {
        System.out.println("\n reference:");
        for (Member m : refer) {
        System.out.print(m + ", ");
        }
        }*/

        return result;
    }

    private static void getReference(ArrayList<ArrayList<Member>> referencesByDimensionWithoutDuplicates, int dimension, ArrayList<Member> reference, ArrayList<ArrayList<Member>> result) {
        //System.out.println(result.size());
        int dim = dimension + 1;//for preparing the next dimension to explore
        if (dimension < referencesByDimensionWithoutDuplicates.size()) {
            //System.out.println("Dimension : "+dimension);
            ArrayList<Member> ref = referencesByDimensionWithoutDuplicates.get(dimension);
            if (ref.isEmpty()) {
                //System.out.println("add");
                reference.add(null);
                //dimension++;

                getReference(referencesByDimensionWithoutDuplicates, dim, reference, result);

            } else {
                for (int i = 0; i < ref.size(); i++) {
                    //System.out.println("add");
                    reference.add(ref.get(i));
                    getReference(referencesByDimensionWithoutDuplicates, dim, reference, result);
                    reference.remove(reference.size() - 1);
                }
            }
        } else {
            //System.out.println("addreference");
            ArrayList<Member> members = new ArrayList<Member>();
            members.addAll(reference);
            
            if(!members.isEmpty())
            result.add(members);
        }
    }

    public static ArrayList<ArrayList<Member>> coverageFromReferencesPerDimension(ArrayList<ArrayList<Member>> referencesPerDimension) {
        ArrayList<ArrayList<Member>> result = new ArrayList<ArrayList<Member>>();

        for (ArrayList<Member> members : referencesPerDimension) {
            ArrayList<Member> membersDimension = new ArrayList<Member>();
            for (Member m : members) {
                ArrayList<Member> coverage = coverage(m);
                for (Member mCoverage : coverage) {
                    if (!membersDimension.contains(mCoverage)) {
                        membersDimension.add(mCoverage);
                    }
                }
            }
            if(!membersDimension.isEmpty())
            result.add(membersDimension);
        }

        return result;
    }

    private static ArrayList<Member> coverage(Member m) {
        SchemaReader schema = fr.univ_tours.li.jaligon.falseto.Generics.Connection.getCube().getSchema().getSchemaReader();
        ArrayList<Member> result = new ArrayList<Member>();

        finestCoverage(m, schema, result);

        return result;
    }

    private static void finestCoverage(Member m, SchemaReader schema, ArrayList<Member> finestChildren) {
        List<Member> children = schema.getMemberChildren(m);

        if (children.isEmpty()) {
            finestChildren.add(m);
        } else {
            for (Member child : children) {
                finestCoverage(child, schema, finestChildren);
            }
        }
    }

    @Override
    public String toString() {
        String result = "";

        for (int i = 0; i < referencesPerDimension.size(); i++) {
            result += "{";
            for (int j = 0; j < referencesPerDimension.get(i).size() - 1; j++) {
                result += referencesPerDimension.get(i).get(j) + ", ";
            }
            result += referencesPerDimension.get(i).get(referencesPerDimension.get(i).size() - 1);

            if (i == referencesPerDimension.size() - 1) {
                result += "}";
            } else {
                result += "} X ";
            }
        }

        return result;
    }
}
