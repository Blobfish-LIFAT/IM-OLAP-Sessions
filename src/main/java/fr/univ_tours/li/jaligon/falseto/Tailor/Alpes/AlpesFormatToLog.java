/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.Tailor.Alpes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;
import mondrian.olap.Level;
import fr.univ_tours.li.jaligon.falseto.Generics.MondrianObject;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Qfset;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.QuerySession;

/**
 *
 * @author julien
 */
public class AlpesFormatToLog {

    private HashSet<QuerySession> querySessions;
    private HashMap<QuerySession, Integer> querySessionAndSupport;

    public AlpesFormatToLog(ArrayList<String> sequences, boolean rawFormat) {
        querySessions = new HashSet<QuerySession>();
        querySessionAndSupport = new HashMap<QuerySession, Integer>();

        int idSession = 0;
        for (String line : sequences) {
            QuerySession session = new QuerySession("" + idSession);
            idSession++;
            //StringTokenizer itemsetization = new StringTokenizer(line, ")(");
//System.out.println(line);
            if (rawFormat) {
                line = line.replace(" @ @", ")>").replace(" @ ", ")(");
            }

            int indexSupport = line.indexOf(")> = ");

            if (indexSupport != -1) {
                int support = Integer.parseInt(line.substring(indexSupport + 5, line.length()));
                querySessionAndSupport.put(session, support);
                line = line.replace(line.substring(indexSupport, line.length()), "");
            }
            line = line.replace("<(", "");
            line = line.replace(")>", "");


            String[] itemsetization = line.split("\\)\\(");
            String itemset;

            for (int i = 0; i < itemsetization.length; i++) {
                itemset = itemsetization[i];

                StringTokenizer itemization = new StringTokenizer(itemset, " ");

                String item;

                Qfset query = new Qfset();

                while (itemization.hasMoreTokens()) {
                    item = itemization.nextToken();
                    //System.out.println("Item Before = " + item);
                    int nb = 0;

                    for (int id = 0; id < item.length(); id++) {
                        if (item.charAt(id) == '"') {
                            nb++;
                        }
                    }

                    if (nb == 1) {
                        String s = itemsetization[i];
                        while (!s.contains("\"")) {
                            item += s;
                            s = itemsetization[i];
                        }
                        item += s;
                    }

                    //System.out.println("Item After = " + item);
                    StringTokenizer members = new StringTokenizer(item, "_");

                    String type = members.nextToken();

                    if(type.equals("NoSelection") || type.equals("NoMeasure"))
                    {
                    
                    }
                    else
                    if (type.equals("Measure")) {
                        while (members.hasMoreTokens()) {
                            String element = members.nextToken();
                            query.addMeasure(MondrianObject.getMeasure(element));
                        }
                    } else if (type.equals("Selection")) {
                        StringTokenizer selectionToken = new StringTokenizer(item, "=");

                        String info = selectionToken.nextToken();
                        StringTokenizer infoToken = new StringTokenizer(info, "_");
                        infoToken.nextToken();//useless here: will return "Selection"
                        String info2 = infoToken.nextToken();
                        StringTokenizer info2Token = new StringTokenizer(info2, ".");
                        String hierarchy = info2Token.nextToken();
                        String level = info2Token.nextToken();

                        String value = selectionToken.nextToken().replace("_", " ").replace("\"", "");

                        //System.out.println("Value = " + value);

                        query.addSelection(MondrianObject.getSelection(level, hierarchy, value));

                    } else if (type.equals("Projection")) {
                        while (members.hasMoreTokens()) {
                            String info = members.nextToken();
                            StringTokenizer infoToken = new StringTokenizer(info, ".");
                            String hierarchy = infoToken.nextToken();
                            String level = infoToken.nextToken();

                            Level l = MondrianObject.getLevel(level, hierarchy);
                            query.addProjection(l);
                        }
                    } else {
                        System.out.println("AlpesFormatToLog PROBLEM !!!!");
                        System.out.println("Line : "+line);
                        System.out.println("Item : "+item);
                    }

                }
                session.add(query);
                //System.out.println("Query : " + query.toString());
            }
            querySessions.add(session);
        }
    }

    public HashSet<QuerySession> getSequences() {
        return querySessions;
    }

    public HashMap<QuerySession, Integer> getSequencesWithSupport() {
        return querySessionAndSupport;
    }

    /*public static void getStatistics(String fileName) {
     HashMap<Integer, Integer> supportFrequences = new HashMap<Integer, Integer>();
     String sequence = "";
     try {
     InputStream ips = new FileInputStream(fileName);
     InputStreamReader ipsr = new InputStreamReader(ips);
     BufferedReader br = new BufferedReader(ipsr);

     String line;

     for (int i = 1; i <= 182; i++) {
     supportFrequences.put(i, 0);
     }

     while ((line = br.readLine()) != null) {
     sequence = line;
     StringTokenizer st = new StringTokenizer(line, ">");
     st.nextToken();
     StringTokenizer st2 = new StringTokenizer(st.nextToken(), "=");
     st2.nextToken();
     int support = Integer.parseInt(st2.nextToken().replace(" ", ""));


     supportFrequences.put(support, supportFrequences.get(support) + 1);
     }

     //System.out.println(supportFrequences);

     for (Integer i : supportFrequences.keySet()) {
     System.out.println(i + ";" + supportFrequences.get(i));
     }

     br.close();
     } catch (Exception e) {
     System.out.println(sequence);
     e.printStackTrace();
     System.out.println(e.toString());
     }
     }*/
}
