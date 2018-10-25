package fr.univ_tours.info.im_olap.model;


import fr.univ_tours.info.im_olap.graph.OGraph;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SessionGraph {
    public static OGraph<Integer, QueryPart> buildBaseGraph(List<Session> sessions){
        OGraph<Integer, QueryPart> result = new OGraph<>();
        for (Session session : sessions){
            for (int i = 0; i < session.length(); i++) {

                Query q1 = session.queries.get(i);
                QueryPart[] q1parts = q1.flat();

                for (int j = 0; j < q1parts.length; j++) {
                    for (int k = j + 1; k < q1parts.length; k++) {
                        result.safeComputeEdge(q1parts[j], q1parts[k], o -> Optional.of(o.orElse(1)));
                        result.safeComputeEdge(q1parts[k], q1parts[j], o -> Optional.of(o.orElse(1)));
                    }
                }

                if (i < session.length() - 1){
                    Query q2 = session.queries.get(i + 1);
                    QueryPart[] q2parts = q2.flat();
                    for (int j = 0; j < q1parts.length; j++) {
                        for (QueryPart q2part : q2parts) {
                            result.safeComputeEdge(q1parts[j], q2part, o -> Optional.of(o.orElse(1)));
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * This will inject edges in the graph based on a XML mondrian Schema, this is probably not compatible with complex schemas
     * will have to check for that
     * @param base
     * @param mondrianFile
     * @return
     */
    public static OGraph<Integer, QueryPart> injectSchema(OGraph<Integer, QueryPart> base, String mondrianFile){
        SAXReader reader = new SAXReader();
        try {
            Document schema = reader.read(Paths.get(mondrianFile).toFile());
            List<Node> nodes = schema.selectNodes("//Dimension"); //We select all dimensions with XPath

            //For each dimensions we must add father/child links in the graph
            for (Node dimension : nodes){
                Element hierarchy = (Element) dimension.selectSingleNode("/Hierarchy");
                String prefix = ((Element)dimension).attributeValue("name");

                List<String> levels = new ArrayList<>();
                levels.add(hierarchy.attributeValue("allLevelName"));
                dimension.selectNodes("//Hierarchy/Level").stream().map(o -> (Element) o).forEach(level -> levels.add(((Element) level).attributeValue("name")));

                for (int i = 0; i < levels.size() - 1; i++) {
                    QueryPart p1 = new QueryPart(QueryPart.Type.DIMENSION, prefix + "." + levels.get(i));
                    QueryPart p2 = new QueryPart(QueryPart.Type.DIMENSION, prefix + "." + levels.get(i+1));
                    base.safeComputeEdge(p1, p2, o -> Optional.of(1));
                    base.safeComputeEdge(p2, p1, o -> Optional.of(1));
                }

            }






            return base;

        } catch (DocumentException e) {
            System.err.printf("Could not parse schema in '%s', verify file permission/format.");
            return base;
        }
    }

    public static OGraph<Integer, QueryPart> buildUsageGraph(List<Session> sessions){
        OGraph<Integer, QueryPart> result = new OGraph<>();


        //TODO Ben you need to build this

        return result;
    }
}
