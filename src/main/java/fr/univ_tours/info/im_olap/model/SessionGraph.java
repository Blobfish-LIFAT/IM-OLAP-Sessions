package fr.univ_tours.info.im_olap.model;


import fr.univ_tours.info.im_olap.graph.OGraph;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.nio.file.Paths;
import java.util.*;

public class SessionGraph {
    public static OGraph<Double, QueryPart> buildBaseGraph(List<Session> sessions){
        OGraph<Double, QueryPart> result = new OGraph<>();
        Set<QueryPart> filters = new HashSet<>(); //will need those later
        for (Session session : sessions){
            for (int i = 0; i < session.length(); i++) {

                Query q1 = session.queries.get(i);
                QueryPart[] q1parts = q1.flat();

                for (int j = 0; j < q1parts.length; j++) {
                    if (q1parts[j].isFilter())
                        filters.add(q1parts[j]);
                    for (int k = j + 1; k < q1parts.length; k++) {
                        result.safeComputeEdge(q1parts[j], q1parts[k], o -> Optional.of(o.orElse(1.0)));
                        result.safeComputeEdge(q1parts[k], q1parts[j], o -> Optional.of(o.orElse(1.0)));
                    }
                }

                if (i < session.length() - 1){
                    Query q2 = session.queries.get(i + 1);
                    QueryPart[] q2parts = q2.flat();
                    for (int j = 0; j < q1parts.length; j++) {
                        for (QueryPart q2part : q2parts) {
                            result.safeComputeEdge(q1parts[j], q2part, o -> Optional.of(o.orElse(1.0)));
                        }
                    }
                }
            }
        }

        //we will link filters with different filter value but on same attribute
        List<QueryPart> filtersList = new ArrayList<>(filters);
        for (int i = 0; i < filtersList.size(); i++) {
            for (int j = i + 1; j < filtersList.size(); j++) {
                QueryPart f1 = filtersList.get(i);
                QueryPart f2 = filtersList.get(j);
                if (f1.value.split(" = ")[0].equals(f2.value.split(" = ")[0])){
                    result.safeComputeEdge(f1, f2, o -> Optional.of(1.0));
                    result.safeComputeEdge(f2, f1, o -> Optional.of(1.0));
                    //System.out.println(f1 + " | " + f2);
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
    public static OGraph<Double, QueryPart> injectSchema(OGraph<Double, QueryPart> base, String mondrianFile){
        SAXReader reader = new SAXReader();
        try {
            Document schema = reader.read(Paths.get(mondrianFile).toFile());
            List<Node> nodes = schema.selectNodes("//Dimension"); //We select all dimensions with XPath

            //For each dimensions we must add father/child links in the graph
            for (Node dimension : nodes){
                Element hierarchy = (Element) dimension.selectSingleNode("Hierarchy");
                String prefix = ((Element)dimension).attributeValue("name");

                List<String> levels = new ArrayList<>();
                levels.add(hierarchy.attributeValue("allLevelName"));
                dimension.selectNodes("//Hierarchy/Level").stream().map(o -> (Element) o).forEach(level -> levels.add(((Element) level).attributeValue("name")));

                for (int i = 0; i < levels.size() - 1; i++) {
                    QueryPart p1 = new QueryPart(QueryPart.Type.DIMENSION, prefix + "." + levels.get(i));
                    QueryPart p2 = new QueryPart(QueryPart.Type.DIMENSION, prefix + "." + levels.get(i+1));
                    base.safeComputeEdge(p1, p2, o -> Optional.of(1.0));
                    base.safeComputeEdge(p2, p1, o -> Optional.of(1.0));
                }

            }

            return base;

        } catch (DocumentException e) {
            System.err.printf("Could not parse schema in '%s', verify file permission/format.");
            return base;
        }
    }

    public static OGraph<Double, QueryPart> buildUsageGraph(Set<QueryPart> previousQPs, List<Session> sessions){
        OGraph<Double, QueryPart> result = new OGraph<>();

        for (QueryPart qp : previousQPs){
            result.addNode(qp);
        }

        for (Session session : sessions){
            for (int i = 0; i < session.length(); i++) {

                Query q1 = session.queries.get(i);
                QueryPart[] q1parts = q1.flat();

                for (int j = 0; j < q1parts.length; j++) {
                    for (int k = j + 1; k < q1parts.length; k++) {
                        result.safeComputeEdge(q1parts[j], q1parts[k], o -> Optional.of(o.map(n -> n+1).orElse(1.0)));
                        result.safeComputeEdge(q1parts[k], q1parts[j], o -> Optional.of(o.map(n -> n+1).orElse(1.0)));
                    }
                }

                if (i < session.length() - 1){
                    Query q2 = session.queries.get(i + 1);
                    QueryPart[] q2parts = q2.flat();
                    for (int j = 0; j < q1parts.length; j++) {
                        for (QueryPart q2part : q2parts) {
                            result.safeComputeEdge(q1parts[j], q2part, o -> Optional.of(o.map(n -> n+1).orElse(1.0)));
                        }
                    }
                }
            }
        }

        return result;
    }
}
