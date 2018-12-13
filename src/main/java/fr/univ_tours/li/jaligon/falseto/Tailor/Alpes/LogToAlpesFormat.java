/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.Tailor.Alpes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.MeasureFragment;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.ProjectionFragment;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Qfset;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.QuerySession;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.SelectionFragment;

/**
 *
 * @author julien
 */
public class LogToAlpesFormat {

    private ArrayList<String> stringSequence = new ArrayList<String>();
    private HashSet<QuerySession> sessions;

    public LogToAlpesFormat(HashSet sessions) {
        this.sessions = sessions;
        generateSequences();
    }

    private void generateSequences() {
        //System.out.println("Number of sessions : " + sessions.size());
        for (QuerySession qs : sessions) {
            String sequence = "<";
            for (Qfset q : qs.getQueries()) {

                sequence += "(";
                if (!q.getMeasures().isEmpty()) {
                    String measure = "";

                    ArrayList<String> measures = new ArrayList<String>();
                    for (MeasureFragment mf : q.getMeasures()) {
                        measures.add("Measure_" + mf.getAttribute().getName().toString().replace(" ", "_"));
                    }
                    Collections.sort(measures);

                    for (String m : measures) {
                        measure += m + " ";
                    }

                    sequence += measure;


                }

                if (!q.getAttributes().isEmpty()) {

                    String projection = "";

                    ArrayList<String> projections = new ArrayList<String>();
                    for (ProjectionFragment pf : q.getAttributes()) {
                        String proj = pf.getLevel().getHierarchy().getName() + "." + pf.getLevel().getName();
                        projections.add("Projection_" + proj.replace(" ", "_"));
                    }

                    Collections.sort(projections);

                    for (String p : projections) {
                        projection += p + " ";
                    }

                    sequence += projection;
                }

                if (!q.getSelectionPredicates().isEmpty()) {
                    String selection = "";

                    ArrayList<String> selections = new ArrayList<String>();

                    for (SelectionFragment sf : q.getSelectionPredicates()) {
                        String sel = sf.getLevel().getHierarchy().getName() + "." + sf.getLevel().getName() + "=\"" + sf.getValue().getName() + "\"";
                        selections.add("Selection_" + sel.replace(" ", "_"));
                    }

                    Collections.sort(selections);

                    for (String s : selections) {
                        selection += s + " ";
                    }

                    sequence += selection;
                }
                sequence = sequence.substring(0, sequence.length() - 1);
                sequence += ")";
            }
            sequence += ">";
            //System.out.println(sequence);
            stringSequence.add(sequence);
        }
    }

    /*private void generateSequencesWithAllGBSetAndMeasureSetAndSelectionSet() {
     System.out.println("Number of sessions : " + sessions.size());
     for (QuerySession qs : sessions) {
     String s = "";
     for (Qfset q : qs.getQueries()) {
     s += "Measure_" + q.toStringMeasures().replace(" ", "_").replace(".", "_") + " . ";

     s += "Projection_" + q.toStringAttributes().replace(" ", "_").replace(".", "_") + " . ";

     String selection = q.toStringSelections();

     if (!selection.equals("NO SELECTION")) {
     s += "Selection_" + selection.replace(" ", "_").replace(".", "_") + " . ";
     }
     }
     s += ".";
     stringSequence.add(s);
     }
     }*/

    /*private void generateSequencesWithAllGBSetAndMeasureSet() {
     System.out.println("Number of sessions : " + sessions.size());
     for (QuerySession qs : sessions) {
     String s = "";
     for (Qfset q : qs.getQueries()) {
     s += "Measure_" + q.toStringMeasures().replace(" ", "_").replace(".", "_") + " . ";

     s += "Projection_" + q.toStringAttributes().replace(" ", "_").replace(".", "_") + " . ";

     for (SelectionFragment sf : q.getSelectionPredicates()) {
     String selection = sf.getLevel().getHierarchy().getName() + "." + sf.getLevel().getName() + "=\"" + sf.getValue().getName() + "\"";

     s += "Selection_" + selection.replace(" ", "_").replace(".", "_") + " . ";
     }
     }
     s += ".";
     stringSequence.add(s);
     }
     }*/
    private void generateSequencesWithAllGBSetAndMeasureSetAndSelectionSet() {
        System.out.println("Number of sessions : " + sessions.size());
        for (QuerySession qs : sessions) {
            String s = "<";
            for (Qfset q : qs.getQueries()) {
                s += "(";
                if (!q.getMeasures().isEmpty()) {
                    String measure = "";
                    String mes = q.toStringMeasures();
                    if (!mes.equals("NO MEASURE")) {
                        measure += "Measure_" + mes.replace(" ", "_") + " ";
                    }
                    s += measure;
                }

                if (!q.getAttributes().isEmpty()) {
                    String projection = "";
                    String proj = q.toStringAttributes();
                    if (proj.equals("NO ATTRIBUTE")) {
                        projection += "Projection_" + proj.replace(" ", "_") + " ";
                    }
                    s += projection;
                }

                if (!q.getSelectionPredicates().isEmpty()) {
                    String selection = "";
                    String sel = q.toStringSelections();
                    if (sel.equals("NO SELECTION")) {
                        selection += "Selection_" + sel.replace(" ", "_") + " ";
                    }
                    s += selection;
                }
                s = s.substring(0, s.length() - 1);
                s += ")";
            }
            s += ">";
            stringSequence.add(s);
        }
    }

    private void generateSequencesWithAllGBSetAndMeasureSet() {
        System.out.println("Number of sessions : " + sessions.size());
        for (QuerySession qs : sessions) {
            String s = "<";
            for (Qfset q : qs.getQueries()) {
                s += "(";
                if (!q.getMeasures().isEmpty()) {
                    String measure = "";
                    String mes = q.toStringMeasures();
                    if (!mes.equals("NO MEASURE")) {
                        measure += "Measure_" + q.toStringMeasures().replace(" ", "_") + " ";
                    }
                    s += measure;
                }

                if (!q.getAttributes().isEmpty()) {
                    String projection = "";
                    String proj = q.toStringAttributes();
                    if (proj.equals("NO ATTRIBUTE")) {
                        projection += "Projection_" + proj.replace(" ", "_") + " ";
                    }
                    s += projection;
                }

                if (!q.getSelectionPredicates().isEmpty()) {
                    String selection = "";
                    for (SelectionFragment sf : q.getSelectionPredicates()) {
                        String sel = sf.getLevel().getHierarchy().getName() + "." + sf.getLevel().getName() + "=\"" + sf.getValue().getName() + "\"";
                        selection += "Selection_" + sel.replace(" ", "_") + " ";
                    }
                    s += selection;
                }
                s = s.substring(0, s.length() - 1);
                s += ")";
            }
            s += ">";
            stringSequence.add(s);
        }
    }

    private void generateSequencesWithAllGBSet() {
        System.out.println("Number of sessions : " + sessions.size());
        for (QuerySession qs : sessions) {
            String s = "<";
            for (Qfset q : qs.getQueries()) {
                s += "(";
                if (!q.getMeasures().isEmpty()) {
                    String measure = "";
                    for (MeasureFragment mf : q.getMeasures()) {
                        measure += "Measure_" + mf.getAttribute().getName().toString().replace(" ", "_") + " ";
                    }
                    s += measure;
                }

                if (!q.getAttributes().isEmpty()) {
                    String projection = "";
                    String proj = q.toStringAttributes();
                    if (proj.equals("NO ATTRIBUTE")) {
                        projection += "Projection_" + proj.replace(" ", "_") + " ";
                    }
                    s += projection;
                }

                if (!q.getSelectionPredicates().isEmpty()) {
                    String selection = "";
                    for (SelectionFragment sf : q.getSelectionPredicates()) {
                        String sel = sf.getLevel().getHierarchy().getName() + "." + sf.getLevel().getName() + "=\"" + sf.getValue().getName() + "\"";
                        selection += "Selection_" + sel.replace(" ", "_") + " ";
                    }
                    s += selection;
                }
                s = s.substring(0, s.length() - 1);
                s += ")";
            }
            s += ">";
            stringSequence.add(s);
        }
    }

    /*private void generateSequencesWithAllFragments() {
     System.out.println("Number of sessions : " + sessions.size());
     for (QuerySession qs : sessions) {
     String s = "";
     for (Qfset q : qs.getQueries()) {
     for (MeasureFragment mf : q.getMeasures()) {
     s += "Measure_" + mf.getAttribute().getName().toString().replace(" ", "_").replace(".", "_") + " . ";
     }

     for (ProjectionFragment pf : q.getAttributes()) {
     s += "Projection_" + pf.getLevel().toString().replace(" ", "_").replace(".", "_") + " . ";
     }

     for (SelectionFragment sf : q.getSelectionPredicates()) {
     String selection = sf.getLevel().getHierarchy().getName() + "." + sf.getLevel().getName() + "=\"" + sf.getValue().getName() + "\"";

     s += "Selection_" + selection.replace(" ", "_").replace(".", "_") + " . ";
     }
     }
     s += ".";
     stringSequence.add(s);
     }
     }*/
    public void saveToFile(String fileName) throws IOException {
        PrintWriter file;

        file = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));

        for (String s : stringSequence) {
            file.println(s);
        }

        file.close();
    }

    public void saveToFileRawFormat(String fileName) throws IOException {
        PrintWriter file;

        file = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));

        for (String s : stringSequence) {
            s = s.replace("<(", "").replace(")>", " @ @").replace(")(", " @ ");
            file.println(s);
        }

        file.close();
    }

    public ArrayList<String> getSequences() {
        return stringSequence;
    }
}
