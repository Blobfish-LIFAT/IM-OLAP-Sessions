package fr.univ_tours.info.im_olap;

import com.alexsxode.utilities.collection.Pair;
import fr.univ_tours.info.im_olap.data.DopanLoader;
import fr.univ_tours.info.im_olap.graph.OGraph;
import fr.univ_tours.info.im_olap.mondrian.MondrianConfig;
import fr.univ_tours.info.im_olap.mondrian.CubeUtils;
import fr.univ_tours.info.im_olap.graph.Graph;
import fr.univ_tours.info.im_olap.model.Query;
import fr.univ_tours.info.im_olap.model.QueryPart;
import mondrian.olap.*;
import fr.univ_tours.info.im_olap.model.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Proto1 {
    // I know you don't like static variables but it's easier for now since type are not set yet
    // just use function arguments
    static String dataDir = "data/logs/dopan_converted";
    private static String cubeSchema = "data/cubeSchemas/DOPAN_DW3.xml";


    public static void main(String[] args) {

        Connection olap = MondrianConfig.getMondrianConnection();

        List<Session> sessions = SessionGraph.fixSessions(DopanLoader.loadDir(dataDir), cubeSchema);
        Session s1 = sessions.get(0);

        CubeUtils mdUtils = new CubeUtils(olap, s1.getCubeName());
        List<Hierarchy> allHierarchies = Arrays
                .stream(mdUtils.getCube().getDimensions())
                .flatMap(d -> Arrays.stream(d.getHierarchies()))
                .collect(Collectors.toList());


        List<Session> thisUser = sessions.stream().filter(s -> s.getUserName().equals(s1.getUserName())).collect(Collectors.toList());
        thisUser.remove(s1);



        OGraph<Double, QueryPart> base = SessionGraph.buildTopologyGraph(thisUser, "data/cubeSchemas/DOPAN_DW3.xml");
        SessionGraph.injectFilters(base, mdUtils);

        System.exit(0);

        /**
         * Ben's stuff
         */

        ((OGraph<Double, QueryPart>)base).checkSync();

        for (Query query : s1.queries) {
            for (QueryPart qp : query.getAllParts()) {
                base.addNode(qp);
            }
        }

        for (QueryPart queryPart : base.getNodes()) {
            base.setEdge(queryPart, queryPart, 1.0);
        }

        Set<QueryPart> queryParts = base.getNodes();

        queryParts.removeAll(s1.queries.stream().flatMap(x -> x.getAllParts().stream()).collect(Collectors.toSet()));

        if (queryParts.isEmpty()) {
            System.err.println("Error: some query parts are in session but not in the base graph!");
            System.out.println(queryParts);
        }

        ((OGraph<Double, QueryPart>)base).checkSync();

        GraphUpdate graphUpdate = new GraphUpdate(GraphUpdate::simpleInterconnections,
                GraphUpdate::replaceEdges,
                GraphUpdate.KLForGraphs());

        System.out.println("Evaluating session...");
        List<Pair<Query, Double>> liste =  graphUpdate.evaluateSession(base, s1);
        liste.stream().forEach(p -> {
            System.out.println();
            System.out.println(p.left);
            System.out.println("value : " + p.right);
        });
        System.out.println("End of evaluation.");
    }

}
