package fr.univ_tours;

import fr.univ_tours.li.jaligon.falseto.Generics.Connection;
import fr.univ_tours.li.jaligon.falseto.Generics.falseto_params;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.QuerySession;
import fr.univ_tours.li.jaligon.falseto.Recommendation.ASRA;
import fr.univ_tours.li.jaligon.falseto.test.XmlLogParsing;

import java.util.List;
import java.util.TreeMap;

public class TestsAlex {
    /**
     * Protocol stuff:
     * session_set_3 is for our model
     *
     */

    public static void main(String[] args) throws Exception{
        // Needs to have a database withe the star schema, configured to use mine
        Connection c = new Connection(); //Can't figure out why the config file doesn't work
        c.open();
        XmlLogParsing log = new XmlLogParsing("cubeload/sessions_belief.xml");
        List<QuerySession> sessions = log.readSessionListLog();
        List<QuerySession> learn = sessions.subList(0,30);
        QuerySession test = sessions.get(32).extractSubsequence(1,10);
        ASRA recommender = new ASRA(learn, test);
        System.out.println(recommender.computeASRA());
    }
}
