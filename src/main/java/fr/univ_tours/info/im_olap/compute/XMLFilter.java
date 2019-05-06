package fr.univ_tours.info.im_olap.compute;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.io.SAXWriter;
import org.dom4j.io.XMLWriter;

import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.util.List;

public class XMLFilter {
    static String target = "Goal Oriented";
    static String inputFile = "cubeload/Workload-1545151556.xml";
    static String outfile = "data/falseto/goal_oriented.xml";


    public static void main(String[] args) throws Exception{
        SAXReader reader = new SAXReader();
        FileOutputStream fos = new FileOutputStream(outfile);
        XMLWriter writer = new XMLWriter(fos);

        Document log = reader.read(Paths.get(inputFile).toFile());

        List<Node> nodes = log.selectNodes("//Session");

        int d = 0, r = 0;

        for (Node node : nodes){
            //System.out.println(((Element)node).attributeValue("template"));
            if (!((Element)node).attributeValue("template").equals(target)){
                log.getRootElement().remove(node);
                d++;
            }
            else {
                r++;
            }
            //System.out.println("----------------");
        }

        Element nb = (Element) log.selectObject("/Benchmark/ProfileParameters/Profile/NumberOfSessions");
        nb.attribute("value").setData(r);

        writer.write(log);
        System.out.printf("Done, %d deleted, %d remaining of type '%s'%n", d,r,target);
    }
}
