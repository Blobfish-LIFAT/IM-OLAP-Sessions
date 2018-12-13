/*
*   This program is free software: you can redistribute it and/or modify
*   it under the terms of the GNU General Public License as published by
*   the Free Software Foundation, either version 3 of the License, or
*   (at your option) any later version.
*
*   This program is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with this program.  If not, see .
*/

/*
* HierarchicalClusterer.java
* Copyright (C) 2009-2012 University of Waikato, Hamilton, New Zealand
*/
package fr.univ_tours.li.jaligon.falseto.Summary.Intention.Clustering;

/**
 * Shows cluster trees represented in Newick format as dendrograms.
 *
 * @author Remco Bouckaert (rrb@xm.co.nz, remco@cs.waikato.ac.nz)
 * @version $Revision: 8034 $
 */
import java.io.IOException;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Log;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.QuerySession;
import fr.univ_tours.li.jaligon.falseto.Summary.Intention.Coverage.SummarizeTiltedWindow;
//import fr.univ_tours.li.jaligon.falseto.logParsing.gpsj.LogParsing;

public class SummarizationHierarchicalTree {
    
    private static final long serialVersionUID = 1L;
    String m_sNewick;
    Cluster m_tree;
    Log log;
    int m_nLeafs;
    double m_fHeight;
    double m_fScaleX = 10;
    double m_fScaleY = 10;
    
    public SummarizationHierarchicalTree(String sNewick, Log l) {
        try {
            log = l;
            parseNewick(sNewick);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    } // c'tor
    
    public void defineCluster() {
        
        Log l = new Log();
        
        sessionPerCluster(m_tree, l);
        
        /*for (QuerySession s : m_tree.getLog().getSessions()) {
        System.out.print(s.id + " ");
        }
        
        System.out.println();
        
        for (QuerySession s : m_tree.getChildren().get(0).getLog().getSessions()) {
        System.out.print(s.id + " ");
        }
        
        System.out.println();
        
        for (QuerySession s : m_tree.getChildren().get(0).getChildren().get(0).getLog().getSessions()) {
        System.out.print(s.id + " ");
        }
        
        System.out.println();
        
        for (QuerySession s : m_tree.getChildren().get(0).getChildren().get(0).getChildren().get(0).getLog().getSessions()) {
        System.out.print(s.id + " ");
        }
        
        System.out.println();*/
    }
    
    public void defineSummaries(int nbWindow) throws IOException {
        summaryPerCluster(this.m_tree, nbWindow);
    }
    
    public void defineSummaries() throws IOException {
        summaryPerClusterWithDifferentSize(this.m_tree);
    }
    /**
     * Méthode permettant d'avoir le résumé d'un cluster
     * @param c
     * @param nbWindow
     * @throws IOException 
     */
    private void summaryPerCluster(Cluster c, int nbWindow) throws IOException {
        if (c.getChildren().isEmpty()) {
            Log l = c.getLog();
            SummarizeTiltedWindow stw = new SummarizeTiltedWindow(l);
            QuerySession summary = stw.summarizeLog(nbWindow);
            
            c.getSummaries().add(summary);
        } else {
            Log summaries = new Log();
            for (Cluster cl : c.getChildren()) {
                summaryPerCluster(cl, nbWindow);
                summaries.add(cl.getSummary(nbWindow));
            }
            
            SummarizeTiltedWindow stw = new SummarizeTiltedWindow(summaries);
            QuerySession summary = stw.summarizeLog(nbWindow);
            
            c.getSummaries().add(summary);
        }
    }
    /**
     * Cette méthode permet de faire un résumé en fonction de la taille
     * des requetes contenues dans un cluster
     * @param c
     * @throws IOException
     */
    private void summaryPerClusterWithDifferentSize(Cluster c) throws IOException {
        Log l = c.getLog();
//on initialise min a la plus grande valeur d'un int
int min = Integer.MAX_VALUE;
//on donne à min la plus petite taille des QuerySessions contenues dans le log de c
for (QuerySession qs : l.getSessions()) {
    if (qs.getQueries().size() < min) {
        min = qs.getQueries().size();
    }
}
//on résume en fonction de cette taille
SummarizeTiltedWindow stw = new SummarizeTiltedWindow(l);
QuerySession summary = stw.summarizeLog(min);
c.getSummaries().add(summary);
//on fait pareil si le cluster a des fils
if (!c.getChildren().isEmpty()) {
    for (Cluster cl : c.getChildren()) {
        summaryPerClusterWithDifferentSize(cl);
    }
}

    }
    /**
     * Cette méthode permet de mettre toutes les sessions du log
     * des clusters fils de c dans le log de c
     * @param c le cluster
     * @param l
     */
    private void sessionPerCluster(Cluster c, Log l) {
        if (!c.getChildren().isEmpty()) {
            Log sessionLog = new Log();
            for (Cluster cl : c.getChildren()) {
                sessionPerCluster(cl, l);//on refait la même chose pour chaque clusters fils
                sessionLog.addAll(cl.getLog().getSessions());
            }
            c.setLog(sessionLog);
        }
    }
    
    public Cluster getRoot() {
        return m_tree;
    }
    
    /**
     * helper method for parsing Newick tree *
     */
    private int nextNode(String sStr, int i) {
        int nBraces = 0;
        char c = sStr.charAt(i);
        do {
            i++;
            if (i < sStr.length()) {
                c = sStr.charAt(i);
                // skip meta data block
                if (c == '[') {
                    while (i < sStr.length() && sStr.charAt(i) != ']') {
                        i++;
                    }
                    i++;
                    if (i < sStr.length()) {
                        c = sStr.charAt(i);
                    }
                }
                
                switch (c) {
                    case '(':
                        nBraces++;
                        break;
                    case ')':
                        nBraces--;
                        break;
                    default:
                        break;
                }
            }
        } while (i < sStr.length()
                && (nBraces > 0 || (c != ',' && c != ')' && c != '(')));
        if (i >= sStr.length() || nBraces < 0) {
            return -1;
        } else if (sStr.charAt(i) == ')') {
            i++;
            if (sStr.charAt(i) == '[') {
                while (i < sStr.length() && sStr.charAt(i) != ']') {
                    i++;
                }
                i++;
                if (i >= sStr.length()) {
                    return -1;
                }
            }
            if (sStr.charAt(i) == ':') {
                i++;
                c = sStr.charAt(i);
                while (i < sStr.length() && (c == '.' || Character.isDigit(c))) {
                    i++;
                    if (i < sStr.length()) {
                        c = sStr.charAt(i);
                    }
                }
            }
        }
        return i;
    }
    
    /**
     * convert string containing Newick tree into tree datastructure but only in
     * the limited format as contained in m_sTrees
     *
     * @param sStr
     * @return tree consisting of a Node
     */
    private void parseNewick(String sNewick) throws Exception {
        m_sNewick = sNewick;
        int i = m_sNewick.indexOf('(');
        if (i > 0) {
            m_sNewick = m_sNewick.substring(i);
        }
        //System.err.println(m_sNewick);
        m_tree = parseNewick2(m_sNewick);
        //System.err.println(m_tree.toString());
        //m_nLeafs = positionLeafs(m_tree, 0);
        //positionRest(m_tree);
        //m_fHeight = positionHeight(m_tree, 0);
    }
    
    private Cluster parseNewick2(String sStr) throws Exception {
        // System.out.println(sStr);
        if (sStr == null || sStr.length() == 0) {
            return null;
        }
        Cluster node = new Cluster();
        if (sStr.startsWith("(")) {
            int i1 = nextNode(sStr, 0);
            int i2 = nextNode(sStr, i1);
            //node.m_children = new Node[2];
            node.getChildren().add(parseNewick2(sStr.substring(1, i1)));
            //node.m_children[0].m_Parent = node;
            String sStr2 = sStr.substring(i1 + 1, (i2 > 0 ? i2 : sStr.length()));
            node.getChildren().add(parseNewick2(sStr2));
            //node.m_children[1].m_Parent = node;
            
            if (sStr.lastIndexOf(':') > sStr.lastIndexOf(')')) {
                //System.out.println(sStr);
                sStr = sStr.substring(sStr.lastIndexOf(':'));
                sStr = sStr.replaceAll("[,\\):]", "");
                node.setDistance(new Double(sStr));
            } else {
                node.setDistance(1);
            }
        } else {
            // it is a leaf
            if (sStr.indexOf(')') >= 0) {
                sStr = sStr.substring(0, sStr.indexOf(')'));
            }
            sStr = sStr.replaceFirst("[,\\)]", "");
            //System.out.println("parsing <<"+sStr+">>");
            if (sStr.length() > 0) {
                if (sStr.indexOf(':') >= 0) {
                    int iColon = sStr.indexOf(':');
                    node.label = sStr.substring(0, iColon);
                    
                    for (QuerySession s : log.getSessions()) {
                        int pos = node.label.indexOf('.');
                        String labelTemp = node.label.substring(0, pos);
                        if (s.getId().equals(labelTemp)) {
                            Log l = new Log();
                            l.add(s);
                            node.setLog(l);
                            break;
                        }
                    }
                    //System.out.println(node.label);
                    if (sStr.indexOf(':', iColon + 1) >= 0) {
                        int iColon2 = sStr.indexOf(':', iColon + 1);
                        node.setDistance(new Double(sStr.substring(iColon + 1, iColon2)));
                        //m_fTmpLength = new Double(sStr.substring(iColon2+1));
                    } else {
                        node.setDistance(new Double(sStr.substring(iColon + 1)));
                    }
                } else {
                    node.label = sStr;
                    for (QuerySession s : log.getSessions()) {
                        if (s.getId().equals(node.label)) {
                            Log l = new Log();
                            l.add(s);
                            node.setLog(l);
                            break;
                        }
                    }
                    node.setDistance(1);
                }
            } else {
                return null;
            }
        }
        return node;
    }
    double m_fTmpLength;
    
    /**
     * Main method for testing this class.
     */
    /*public static void main(String[] args) throws IOException {
    Locale.setDefault(Locale.US);
    //HierarchyVisualizer a = new HierarchyVisualizer("((((human:2.0,(chimp:1.0,bonobo:1.0):1.0):1.0,gorilla:3.0):1.0,siamang:4.0):1.0,orangutan:5.0)");
    //HierarchyVisualizer a = new HierarchyVisualizer("(((human:2.0,(chimp:1.0,bonobo:1.0):1.0):1.0,gorilla:3.0):1.0,siamang:4.0)");
    //HierarchyVisualizer a = new HierarchyVisualizer(" (((5[theta=0.121335,lxg=0.122437]:0.00742795,3[theta=0.0972485,lxg=0.152762]:0.00742795)[theta=0.490359,lxg=0.0746703]:0.0183076,((2[theta=0.0866056,lxg=0.2295]:0.00993801,4[theta=0.135512,lxg=0.146674]:0.00993801)[theta=0.897783,lxg=0.0200762]:0.00901206,1[theta=0.200265,lxg=0.18925]:0.0189501)[theta=0.0946195,lxg=0.143427]:0.00678551)[theta=0.185562,lxg=0.139681]:0.0129598,(7[theta=0.176022,lxg=0.364039]:0.0320395,((0[theta=0.224286,lxg=0.156485]:0.0175487,8[theta=0.223313,lxg=0.157166]:0.0175487)[theta=0.631287,lxg=0.024042]:0.00758871,6[theta=0.337871,lxg=0.148799]:0.0251374)[theta=0.33847,lxg=0.040784]:0.00690208)[theta=0.209238,lxg=0.0636202]:0.00665587)[theta=0.560453,lxg=-0.138086]:0.01");
    //HierarchyVisualizer a = new HierarchyVisualizer(" ((5[theta=0.121335,lxg=0.122437]:0.00742795,3[theta=0.0972485,lxg=0.152762]:0.00742795)[theta=0.490359,lxg=0.0746703]:0.0183076,2[theta=0.0866056,lxg=0.2295]:0.00993801)[theta=0.897783,lxg=0.0200762]:0.00901206");
    Connection c = new Connection();
    c.open();
    
    LogParsing lp = new LogParsing(Generics.webPath + "Logs/22-10-2012-06-12-06log_intra3_inter10_session30PerCluster.txt");
    Log log = lp.ReadSessionLog();
    
    SummarizationHierarchicalTree a = new SummarizationHierarchicalTree("((((((((((1.0:0.32883,2.0:0.32883):0.60429,(23.0:0.5537,24.0:0.5537):0.37942):0.0436,(11.0:0.5509,12.0:0.5509):0.42582):0.01787,(9.0:0.32619,10.0:0.32619):0.6684):0.00541,((21.0:0.3298,22.0:0.3298):0.66993,(25.0:0.57859,26.0:0.57859):0.42113):0.00028):0,((19.0:0.51517,20.0:0.51517):0.48039,(29.0:0.68002,30.0:0.68002):0.31553):0.00444):0,(15.0:0.54961,16.0:0.54961):0.45039):0,(((7.0:0.37778,8.0:0.37778):0.5516,(17.0:0.68095,18.0:0.68095):0.24843):0.06363,(13.0:0.83374,14.0:0.83374):0.15928):0.00698):0,(5.0:0.35429,6.0:0.35429):0.64571):0,((3.0:0.56509,4.0:0.56509):0.39888,(27.0:0.55014,28.0:0.55014):0.41383):0.03603)", log);
    a.defineCluster();
    a.defineSummaries(6);
    
    }*/
} // class HierarchyVisualizer
