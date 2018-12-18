package fr.univ_tours;

import fr.univ_tours.info.im_olap.model.Query;
import fr.univ_tours.info.im_olap.model.QueryPart;
import fr.univ_tours.info.im_olap.model.Session;
import fr.univ_tours.li.jaligon.falseto.Generics.Connection;
import fr.univ_tours.li.jaligon.falseto.Generics.falseto_params;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.*;
import fr.univ_tours.li.jaligon.falseto.Recommendation.ASRA;
import fr.univ_tours.li.jaligon.falseto.test.XmlLogParsing;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

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

        Session ourtype = fromJulien(recommended);
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
