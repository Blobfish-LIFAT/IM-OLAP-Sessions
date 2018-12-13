package fr.univ_tours;

import com.google.gson.Gson;
import fr.univ_tours.li.jaligon.falseto.Generics.Connection;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Qfset;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.QuerySession;
import fr.univ_tours.li.jaligon.falseto.Recommendation.ASRA;
import fr.univ_tours.li.jaligon.falseto.test.XmlLogParsing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class TestsAlex {
    static Gson gson = new Gson();
    static String tmpFolder = "tmp";

    public static void main(String[] args) throws Exception{
        Connection c = new Connection();//connection to a database (based on the olapConnection.properties file), works only with an Oracle database for now.
        c.open();
        XmlLogParsing log = new XmlLogParsing("cubeload/Workload-1544710131.xml");
        List<QuerySession> sessions = log.readSessionListLog();
        List<QuerySession> learn = sessions.subList(0,30);
        QuerySession test = sessions.get(32);
        ASRA recommender = new ASRA(learn, test);
        System.out.println(recommender.computeASRA());
    }
}
