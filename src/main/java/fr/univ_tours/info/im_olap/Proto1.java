package fr.univ_tours.info.im_olap;

import com.alexsxode.utilities.collection.Pair;
import fr.univ_tours.info.im_olap.data.DopanLoader;
import fr.univ_tours.info.im_olap.data.MondrianConfig;
import fr.univ_tours.info.im_olap.data.MondrianUtils;
import fr.univ_tours.info.im_olap.graph.Graph;
import fr.univ_tours.info.im_olap.graph.OGraph;
import mondrian.olap.Connection;
import mondrian.olap.Cube;
import mondrian.olap.Schema;
import fr.univ_tours.info.im_olap.model.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Proto1 {
    // I know you don't like static variables but it's easier for now since type are not set yet
    // just use function arguments
    static String dataDir = "data/logs/dopan_converted";


    public static void main(String[] args) {

        //TODO this is only skeleton for the tests

        Connection olap = MondrianConfig.getMondrianConnection();
        MondrianUtils mdutils = new MondrianUtils(olap);
        Cube cube = mdutils.getCubeByName("Cube1MobProInd");
        System.out.println(Arrays.toString(cube.getDimensions()));


        //Session test = DopanLoader.loadFile("/home/alex/IdeaProjects/IM-OLAP-Sessions/data/logs/dopan_converted/dibstudent03--2016-09-25--15-56.log.json");

        List<Session> sessions = SessionGraph.fixSessions(DopanLoader.loadDir(dataDir), "data/cubeSchemas/DOPAN_DW3.xml");

        Session s1 = sessions.get(0);
        List<Session> thisUser = sessions.stream().filter(s -> s.getUserName().equals(s1.getUserName())).collect(Collectors.toList());
        thisUser.remove(s1);

        System.exit(0);

        /**
         * Ben's stuff
         */

        Graph<Double, QueryPart> base = SessionGraph.buildTopologyGraph(thisUser, "data/cubeSchemas/DOPAN_DW3.xml");


        for (Query query : s1.queries) {
            for (QueryPart qp : query.getAllParts()) {
                base.addNode(qp);
            }
        }

        GraphUpdate graphUpdate = new GraphUpdate(GraphUpdate::simpleInterconnections,
                GraphUpdate::replaceEdges,
                GraphUpdate.KLForGraphs());


        List<Pair<Query, Double>> liste =  graphUpdate.evaluateSession(base, s1);
        liste.stream().forEach(p -> {
            System.out.println();
            System.out.println(p.left);
            System.out.println("value : " + p.right);
        });

    }

}
