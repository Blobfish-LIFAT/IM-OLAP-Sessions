package fr.univ_tours.info.im_olap;

import com.alexsxode.utilities.collection.Pair;
import fr.univ_tours.info.im_olap.data.DopanLoader;
import fr.univ_tours.info.im_olap.graph.Graph;
import fr.univ_tours.info.im_olap.graph.OGraph;
import fr.univ_tours.info.im_olap.model.GraphUpdate;
import fr.univ_tours.info.im_olap.model.QueryPart;
import fr.univ_tours.info.im_olap.model.Session;
import fr.univ_tours.info.im_olap.model.SessionGraph;

import java.util.List;
import java.util.stream.Collectors;

public class Proto1 {
    // I know you don't like static variables but it's easier for now since type are not set yet
    // just use function arguments
    static String dataDir = "data/logs/dopan_converted";


    public static void main(String[] args) {

        //TODO this is only skeleton for the tests

        String path = "data/";
        loadSessions(path);

        //Session test = DopanLoader.loadFile("/home/alex/IdeaProjects/IM-OLAP-Sessions/data/logs/dopan_converted/dibstudent03--2016-09-25--15-56.log.json");

        List<Session> sessions = DopanLoader.loadDir(dataDir);

        Session s1 = sessions.get(0);
        List<Session> thisUser = sessions.stream().filter(s -> s.getUserName().equals(s1.getUserName())).collect(Collectors.toList());
        thisUser.remove(s1);
        System.out.println(s1.getFilename());

        Graph<Double, QueryPart> base = SessionGraph.buildTopologyGraph(thisUser, "data/cubeSchemas/DOPAN_DW3.xml");

        GraphUpdate graphUpdate = new GraphUpdate(GraphUpdate::simpleInterconnections,
                GraphUpdate::replaceEdges,
                GraphUpdate.KLForGraphs());


        List<Pair<Graph<Double, QueryPart>, Double>> liste =  graphUpdate.evaluateSession(base, s1);
        liste.stream().forEach(p -> {
            System.out.println("value : " + p.right);
        });

    }

    /**
     * Loads sessions into memory
     */
    private static void loadSessions(String path) {

    }
}
