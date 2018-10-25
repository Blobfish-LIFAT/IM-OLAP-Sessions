package fr.univ_tours.info.im_olap;

import fr.univ_tours.info.im_olap.graph.OGraph;
import fr.univ_tours.info.im_olap.model.LoadSessions;
import fr.univ_tours.info.im_olap.model.QueryPart;
import fr.univ_tours.info.im_olap.model.Session;
import fr.univ_tours.info.im_olap.model.SessionGraph;
import org.dom4j.*;
import org.dom4j.io.SAXReader;


import java.nio.file.Paths;
import java.util.List;

public class Dev {
    public static void main(String[] args) throws DocumentException {
        List<Session> sessions = LoadSessions.loadFromDir("data/session_set_1");
        System.out.println(sessions.size());

        OGraph<Integer, QueryPart> base = SessionGraph.buildBaseGraph(sessions);
        SessionGraph.injectSchema(base, "data/schema.xml");


    }
}
