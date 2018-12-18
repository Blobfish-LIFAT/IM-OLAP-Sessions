package fr.univ_tours;

import com.alexsxode.utilities.collection.MultiSet;
import com.alexsxode.utilities.math.Distribution;
import fr.univ_tours.info.im_olap.Nd4jUtils;
import fr.univ_tours.info.im_olap.graph.Graphs;
import fr.univ_tours.info.im_olap.graph.OGraph;
import fr.univ_tours.info.im_olap.graph.PageRank;
import fr.univ_tours.info.im_olap.model.*;
import fr.univ_tours.info.im_olap.model.Session;
import fr.univ_tours.li.jaligon.falseto.Generics.Connection;
import fr.univ_tours.li.jaligon.falseto.Generics.falseto_params;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.*;
import fr.univ_tours.li.jaligon.falseto.Recommendation.ASRA;
import fr.univ_tours.li.jaligon.falseto.test.XmlLogParsing;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import static fr.univ_tours.info.im_olap.graph.PageRank.normalizeRowsi;

public class TestsAlex {
    /**
     * Protocol stuff:
     * data/session_set_3 is for our model
     * data/falseto/ profiles de log pour train falseto
     */

    public static void main(String[] args) throws Exception{
        // Needs to have a database withe the star schema, configured to use mine
        Connection c = new Connection(); //Can't figure out why the config file doesn't work
        c.open();
        XmlLogParsing log = new XmlLogParsing("data/falseto/goal_oriented.xml");
        List<QuerySession> sessions = log.readSessionListLog();
        List<QuerySession> learn = sessions.subList(0,30);
        QuerySession test = sessions.get(32).extractSubsequence(1,10);
        ASRA recommender = new ASRA(learn, test);
        QuerySession recommended = recommender.computeASRA();

        //type conversion
        Session ourtype = fromJulien(recommended);

        //Compute empirical distribution
        MultiSet<QueryPart> parts = new MultiSet<>();
        parts.addAll(ourtype.allParts());
        Distribution<QueryPart> empirical = new Distribution<>(parts);


        //TODO run getBelief and code divergence

    }

    private static Distribution<QueryPart> getBeliefs(String sessionsDir, String schemaPath, String userProfile, int userSize, double alpha) {
        List<Session> sessions = LoadSessions.loadFromDir(sessionsDir);
        Collections.shuffle(sessions);

        List<Session> user = new ArrayList<>();
        List<Session> learning = new ArrayList<>();

        int qota = userSize;
        for (Session session : sessions){
            if (session.getType().equals(userProfile) && qota > 0) {
                user.add(session);
                qota--;
            }
            else
                learning.add(session);
        }

        //System.out.printf("sessions=%d, user=%d, learning=%d, eval=%d%n", sessions.size(), user.size(), learning.size(), eval.size());
        /**
         * WARNING THIS IS ONLY FOR DOLAP
         */

        OGraph<Double, QueryPart> base = SessionGraph.buildTopologyGraph(learning, schemaPath);
        SessionGraph.injectCousins(base, sessions);

        OGraph<Double, QueryPart> usage = SessionGraph.buildUsageGraph(base.getNodes(), user);

        usage.getNodes().forEach(base::addNode);
        base.getNodes().forEach(n -> base.setEdge(n,n,1.0));


        INDArray topology = Graphs.sortedINDMatrix(base);
        INDArray tp = Graphs.sortedINDMatrix(usage);


        INDArray uniform = Nd4j.ones(topology.shape());

        normalizeRowsi(topology);
        normalizeRowsi(tp);
        normalizeRowsi(uniform);

        INDArray pr = topology.mul(1-alpha).add(tp.mul(alpha));
        INDArray pinf = PageRank.pageRank(pr, 42);



        TreeMap<QueryPart, Integer> querryMap = new TreeMap<>();
        List<QueryPart> baseParts = new ArrayList<>(base.getNodes());

        Collections.sort(baseParts);

        //baseParts.forEach(System.out::println);

        for (int i = 0; i < baseParts.size(); i++) {
            querryMap.putIfAbsent(baseParts.get(i), i);
        }

        Distribution<QueryPart> partDistribution = new Distribution<>();
        querryMap.forEach((queryPart, integer) -> partDistribution.setProba(queryPart, pinf.getDouble(0, integer)));
        return partDistribution;
    }

    /**
     * Hey, nicolas, this should convert Julien's session object to our session object
     * @param in Session in Julien's format
     * @return The same session in our format
     */
    public static Session fromJulien(QuerySession in){
        List<Qfset> queriesJulien = in.getQueries();
        List<Query> ourQueries = new ArrayList<>();

        for (Qfset qfset : queriesJulien){
            List<QueryPart> parts = new ArrayList<>();
            for (MeasureFragment fragment : qfset.getMeasures()){
                parts.add(new QueryPart(QueryPart.Type.MEASURE, fragment.getAttribute().getName()));
            }
            for (ProjectionFragment pf : qfset.getAttributes()){
                parts.add(new QueryPart(QueryPart.Type.DIMENSION, pf.getLevel().getHierarchy().getName() + "." + pf.getLevel().getName()));
            }
            for (SelectionFragment sf : qfset.getSelectionPredicates()){
                parts.add(new QueryPart(QueryPart.Type.FILTER, sf.getLevel().getHierarchy().getName() + "." + sf.getLevel().getName() + "=\"" + sf.getValue().getName()));
            }
            ourQueries.add(new Query(parts));
        }

        return new Session(ourQueries,in.getTemplate(),in.getId());
    }
}
