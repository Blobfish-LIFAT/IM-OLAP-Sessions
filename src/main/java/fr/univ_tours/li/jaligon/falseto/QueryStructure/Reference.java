/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.QueryStructure;

import java.util.ArrayList;
import java.util.HashMap;
import mondrian.olap.Member;

/**
 *
 * @author Julien
 */
public class Reference {

    public static ArrayList<Reference> referenceList = new ArrayList<Reference>();
    private ArrayList<Member> reference;
    private HashMap<Member, String> measures;

    public Reference() {
        reference = new ArrayList<Member>();
        measures = new HashMap<Member, String>();
    }

    public Reference(ArrayList<Member> reference) {
        this.reference = reference;
        measures = new HashMap<Member, String>();
    }

    public void addMeasureValue(Member measure, String value) {
        measures.put(measure, value);
    }

    public HashMap<Member, String> getMeasures() {
        return measures;
    }

    public static Reference getReference(ArrayList<Member> reference) {
        Reference r = null;
        for (Reference ref : referenceList) {
            int identicalMembers = 0;
            for (int i = 0; i < ref.sizeMember(); i++) {
                if (ref.getReference().get(i) == reference.get(i)) {
                    identicalMembers++;
                }
            }

            if (identicalMembers == ref.sizeMember()) {
                r = ref;

                break;
            }
        }

        if (r == null) {
            r = new Reference(reference);
            referenceList.add(r);
        }

        return r;
    }

    public static ArrayList<Reference> getReferenceSet(ArrayList<ArrayList<Member>> references) {

        ArrayList<Reference> result = new ArrayList<Reference>();

        for (ArrayList<Member> ref : references) {
            result.add(Reference.getReference(ref));
        }

        return result;
    }

    public void addMember(Member m) {
        reference.add(m);
    }

    public int sizeMember() {
        return reference.size();
    }

    public void removeMember(int index) {
        reference.remove(index);
    }

    public ArrayList<Member> getReference() {
        return reference;
    }

    public void addAllMembers(Reference members) {
        reference.addAll(members.getReference());
    }

    public boolean isEmpty() {
        return reference.isEmpty();
    }
    /*public String toString()
    {
    
    }*/
}
