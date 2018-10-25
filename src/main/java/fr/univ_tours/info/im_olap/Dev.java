package fr.univ_tours.info.im_olap;

import fr.univ_tours.info.im_olap.model.LoadSessions;
import fr.univ_tours.info.im_olap.model.Session;
import org.dom4j.*;
import org.dom4j.io.SAXReader;


import java.nio.file.Paths;
import java.util.List;

public class Dev {
    public static void main(String[] args) throws DocumentException {
        List<Session> sessions = LoadSessions.loadFromDir("data/session_set_1");
        System.out.println(sessions.size());

        SAXReader reader = new SAXReader();
        Document doc = reader.read(Paths.get("data/schema.xml").toFile());
        System.out.println(doc.getRootElement().getName());

        List<Node> nodes = doc.selectNodes("//Dimension");
        System.out.println(nodes);



        for (Node node : nodes){
            Element e = (Element) node;
            System.out.printf("Working on node: %s%n", e.attributeValue("name"));
            List<Node> levels = node.selectNodes("//Hierarchy/Level"); // Hierarchy is itself a level the global aggregate
            System.out.println(levels);
        }
    }
}
