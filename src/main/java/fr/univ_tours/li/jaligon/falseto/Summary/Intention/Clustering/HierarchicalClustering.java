/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.Summary.Intention.Clustering;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.QuerySession;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Session;
import weka.clusterers.HierarchicalClusterer;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;

/**
 *
 * @author aligon_j
 */
public class HierarchicalClustering {

    private String graph;

    public HierarchicalClustering(HashSet<QuerySession> sessions) throws Exception {
        HierarchicalClusterer hc = new HierarchicalClusterer();

        SmithWatermanDistance sw = new SmithWatermanDistance();
        hc.setDistanceFunction(sw);

        SelectedTag tag = new SelectedTag("COMPLETE", HierarchicalClusterer.TAGS_LINK_TYPE);
        hc.setLinkType(tag);

        hc.setNumClusters(1);
        hc.setDistanceIsBranchLength(true);

        List<Instance> sessionList = new ArrayList<Instance>();
        
        FastVector fv = new FastVector();
        
        for (Session s : sessions) {
            sessionList.add((Instance) s);
            fv.addElement(new Attribute(s.id));
            //System.out.println(s.id);
        }


        Instances dataset = new Instances("CLUSTER", fv, fv.size());

        for (Instance ins : sessionList) {
            dataset.add(ins);
        }

        hc.buildClusterer(dataset);

        
        
        graph = hc.graph();

        graph = graph.replace("Newick:", "");
        //graph = graph.replace(".0:", ":");
        //System.out.println(graph);
    }

    public String getGraph() {
        return graph;
    }

    /*public static void main(String[] args) throws IOException, Exception {
        Locale.setDefault(Locale.US);

        Connection c = new Connection();
        c.open();

        LogParsing lp = new LogParsing(Generics.webPath + "Logs/22-10-2012-06-12-06log_intra3_inter10_session30PerCluster.txt");
        Log log = lp.ReadSessionLog();

        HierarchicalClustering summarize = new HierarchicalClustering(log.getSessions());

        //File file = new File("test.txt");
        //BufferedReader reader = new BufferedReader(new FileReader(file));

    }*/
}
