package fr.univ_tours;

import fr.univ_tours.li.jaligon.falseto.Generics.Connection;
import fr.univ_tours.li.jaligon.falseto.Generics.falseto_params;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.QuerySession;
import fr.univ_tours.li.jaligon.falseto.Recommendation.ASRA;
import fr.univ_tours.li.jaligon.falseto.test.XmlLogParsing;

import java.util.List;

public class TestsAlex {

    public static void main(String[] args) throws Exception{
        // Needs to have a database withe the star schema, configured to use mine
        Connection c = new Connection("olapConnection.properties");
        c.open();
        XmlLogParsing log = new XmlLogParsing("cubeload/Workload-1544710131.xml");
        List<QuerySession> sessions = log.readSessionListLog();
        List<QuerySession> learn = sessions.subList(0,30);
        QuerySession test = sessions.get(32);
        ASRA recommender = new ASRA(learn, test);
        System.out.println(recommender.computeASRA());
    }
}
