/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.logParsing.gpsj;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import mondrian.olap.Hierarchy;
import mondrian.olap.Level;
import mondrian.olap.Member;
import fr.univ_tours.li.jaligon.falseto.Generics.MondrianObject;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Log;

import fr.univ_tours.li.jaligon.falseto.QueryStructure.Qfset;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.QuerySession;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.SelectionFragment;

/**
 *
 * @author Elisa
 */
public class StatisticalStudentSessionLogParsing {

    /*Log file path**/
    private String filePath;
    private static int counter = 0;
    //private String bug;
    /*Mondrian schema**/
    //private Connection connection;
    //MondrianObject mObject;

    public StatisticalStudentSessionLogParsing(String filePath) throws IOException {
        this.filePath = filePath;
        //this.connection = new Connection();
        //this.connection.open();

    }

    /* This method parses the log file and return the set of session stored in the log. The log contains queries related to the default ipums schema
     * @return the list of ssessions in the log
     **/
    public Log ReadSessionLog() {
        Log result = new Log();
        File file = new File(filePath);
        BufferedReader reader = null;

        List<String> relevantQueriesSt = new ArrayList<>();
        List<Qfset> relevantQueries = new ArrayList<>();

        try {
            reader = new BufferedReader(new FileReader(file));
            String text = null;

            while ((text = reader.readLine()) != null) {
                String idQuestion = "";
                //read the stats info
                while (!text.contains("#0")) {
                    text = reader.readLine();
                    if (text.contains("Relevant queries:")) {
                        //System.out.println(text+" "+text.substring(18, text.length()));
                        String idQueries = text.substring(18, text.length());
                        StringTokenizer st = new StringTokenizer(idQueries, ",");

                        while (st.hasMoreTokens()) {
                            String s = st.nextToken();
                            relevantQueriesSt.add("#" + s);
                        }
                    } else if (text.contains("ID question:")) {
                        idQuestion = text.substring(13, text.length());

                    }
                }

                //create a new session
                counter++;
                QuerySession session = new QuerySession(Integer.toString(counter));
                session.setIdQuestion(Integer.parseInt(idQuestion));

                while (text.contains("#") && !text.contains("#endSession")) {
                    //create a new query
                    String idQuery = text;
                    Qfset query = new Qfset();
                    //Read measures
                    text = reader.readLine();

                    String[] measures = text.split(",");

                    for (String element : measures) //Extract measure 
                    {
                        query.addMeasure(MondrianObject.getMeasure(element));
                    }
                    //Read group by

                    text = reader.readLine();

                    //bug = text;
                    String[] attributes = text.split(",");

                    for (String element : attributes) {
                        // ! split with REGEX
                        //String[] dhl_string = element.split("\\."); // dimension hierarchy level
                        String hierarchy = element.substring(0, element.lastIndexOf("."));
                        String attribute = element.substring(element.lastIndexOf(".") + 1, element.length());
                        Level l = MondrianObject.getLevel(attribute, hierarchy);
                        query.addProjection(l);
                    }
                    //Read selection criteria
                    text = reader.readLine();

                    String[] selCriteria = text.split("\",");
                    if (!selCriteria[0].equals("NONE")) {
                        for (int k = 0; k < selCriteria.length; k++) {
                            String selection = selCriteria[k];
                            String[] attHierachy_val = selection.split("\\=");

                            String hierarchy = attHierachy_val[0].substring(0, attHierachy_val[0].lastIndexOf("."));
                            String attr = attHierachy_val[0].substring(attHierachy_val[0].lastIndexOf(".") + 1, attHierachy_val[0].length());
                            String value = attHierachy_val[1].replace("\"", "");
                            //System.out.println(attr+" "+hierarchy+" "+value);
                            Member sl = MondrianObject.getSelection(attr, hierarchy, value);
                            query.addSelection(sl);
                        }
                    }

                    //useful to remove selections predicates that correspond to a projection...
                    for (Hierarchy h : fr.univ_tours.li.jaligon.falseto.Generics.Generics.getHierarchies()) {
                        HashSet<SelectionFragment> selections = query.getSelectionsFromHierarchy(h);

                        if (!selections.isEmpty()) {
                            List<Member> members = fr.univ_tours.li.jaligon.falseto.Generics.Connection.getCube().getSchema().getSchemaReader().withLocus().getLevelMembers(selections.iterator().next().getLevel(), true);

                            if (members.size() == selections.size()) {
                                for (SelectionFragment sf : selections) {
                                    query.removeSelection(sf);
                                }
                            }
                        }
                    }

                    if (relevantQueriesSt.contains(idQuery)) {
                        relevantQueries.add(query);
                    }
                    session.add(query);

                    //read the last to create a new query or session
                    text = reader.readLine();
                }

                session.setRelevantQueries(relevantQueries);
                result.add(session);

            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(StatisticalStudentSessionLogParsing.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(StatisticalStudentSessionLogParsing.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        return result;
    }

}
