package fr.univ_tours.info.im_olap.model;


import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import fr.univ_tours.info.im_olap.graph.OGraph;
import fr.univ_tours.info.im_olap.mondrian.CubeUtils;
import mondrian.olap.*;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SessionGraph {
    static Pattern dimPattern = Pattern.compile("\\[([^\\[\\]]*)\\]\\.\\[([^\\[\\]]*)\\]\\.\\[([^\\[\\]]*)\\]");

    @Deprecated
    private static OGraph<Double, QueryPart> buildBaseGraph(List<Session> sessions){
        OGraph<Double, QueryPart> result = new OGraph<>();
        //int  t = 0;
        for (Session session : sessions){
            //System.out.printf("Session %d has %d queries%n", t++, session.length());
            for (int i = 0; i < session.length(); i++) {
                Query q1 = session.queries.get(i);
                QueryPart[] q1parts = q1.flat();

                for (int j = 0; j < q1parts.length; j++) {
                    for (int k = j ; k < q1parts.length; k++) {
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


        return result;
    }


    /**
     * This will inject edges in the graph based on a XML mondrian (v3) Schema, this is probably not compatible with complex schemas
     * will have to check for that
     * @param base
     * @param mondrianFile
     * @return
     */
    @Deprecated
    private static OGraph<Double, QueryPart> injectSchema(OGraph<Double, QueryPart> base, String mondrianFile){
        SAXReader reader = new SAXReader();
        try {
            Document schema = reader.read(Paths.get(mondrianFile).toFile());
            List<Node> nodes = schema.selectNodes("//Dimension"); //We select all dimensions with XPath

            //For each dimensions we must add father/child links in the graph
            for (Node dimension : nodes){
                String dimName = ((Element) dimension).attributeValue("name");
                List<Node> hierarchies = dimension.selectNodes("Hierarchy");
                //System.out.printf("[DEBUG] Found %d hierarchies in %s%n",hierarchies.size(), dimName);
                for (Node h : hierarchies){
                    Element hierarchy = (Element) h;
                    String prefix = "[" + dimName + "].[" + hierarchy.attributeValue("name") + "]";

                    List<String> levels = new ArrayList<>();
                    levels.add(hierarchy.attributeValue("allLevelName"));
                    h.selectNodes("./Level").stream().map(o -> (Element) o).forEach(level -> levels.add(((Element) level).attributeValue("name")));

                    for (int i = 0; i < levels.size() - 1; i++) {
                        QueryPart p1 = QueryPart.newDimension(prefix + ".[" + levels.get(i) + "]");
                        QueryPart p2 = QueryPart.newDimension(prefix + ".[" + levels.get(i+1) + "]");
                        base.safeComputeEdge(p1, p2, o -> Optional.of(1.0));
                        base.safeComputeEdge(p2, p1, o -> Optional.of(1.0));
                        //System.out.printf("Linking %s | %s%n", p1, p2);
                    }
                }


            }

            return base;

        } catch (Exception e) {
            System.err.printf("Could not parse schema in '%s', verify file permission/format.", mondrianFile);
            return base;
        }
    }

    @Deprecated
    public static OGraph<Double, QueryPart> buildTopologyGraph(List<Session> sessions, String schemaPath){
        OGraph<Double, QueryPart> base = buildBaseGraph(sessions);
        injectSchema(base, schemaPath);
        base.getNodes().forEach(n -> base.setEdge(n, n, 1.0));
        return base;
    }

    public static MutableValueGraph<QueryPart, Double> buildUsageGraph(List<Session> sessions){
        MutableValueGraph<QueryPart, Double> result = ValueGraphBuilder.directed().allowsSelfLoops(true).build();

        for (Session session : sessions){
            for (int i = 0; i < session.length(); i++) {

                Query q1 = session.queries.get(i);
                QueryPart[] q1parts = q1.flat();

                for (int j = 0; j < q1parts.length; j++) {
                    for (int k = j + 1; k < q1parts.length; k++) {
                        result.putEdgeValue(q1parts[j], q1parts[k], result.edgeValue(q1parts[j], q1parts[k]).map(n -> n + 1).orElse(1d));
                        result.putEdgeValue(q1parts[k], q1parts[j], result.edgeValue(q1parts[k], q1parts[j]).map(n -> n + 1).orElse(1d));
                    }
                }

                if (i < session.length() - 1){
                    Query q2 = session.queries.get(i + 1);
                    QueryPart[] q2parts = q2.flat();
                    for (int j = 0; j < q1parts.length; j++) {
                        for (QueryPart q2part : q2parts) {
                            result.putEdgeValue(q1parts[j], q2part, result.edgeValue(q1parts[j], q2part).map(n -> n + 1).orElse(1d));
                        }
                    }
                }
            }
        }

        return result;
    }

    public static MutableValueGraph<QueryPart, Double> buildFromLog(List<Session> sessions){
        MutableValueGraph<QueryPart, Double> graph = ValueGraphBuilder.directed().allowsSelfLoops(true).build();

        for (Session session : sessions){
            for (int i = 0; i < session.length(); i++) {
                Query q1 = session.queries.get(i);
                QueryPart[] q1parts = q1.flat();

                for (int j = 0; j < q1parts.length; j++) {
                    for (int k = j ; k < q1parts.length; k++) {
                        graph.putEdgeValue(q1parts[j], q1parts[k], graph.edgeValue(q1parts[j], q1parts[k]).map(n -> n + 1).orElse(1d));
                        graph.putEdgeValue(q1parts[k], q1parts[j], graph.edgeValue(q1parts[k], q1parts[j]).map(n -> n + 1).orElse(1d));
                    }
                }

                if (i < session.length() - 1){
                    Query q2 = session.queries.get(i + 1);
                    QueryPart[] q2parts = q2.flat();
                    for (int j = 0; j < q1parts.length; j++) {
                        for (QueryPart q2part : q2parts) {
                            graph.putEdgeValue(q1parts[j], q2part, graph.edgeValue(q1parts[j], q2part).map(n -> n + 1).orElse(1d));
                        }
                    }
                }
            }
        }

        return graph;
    }

    public static Session resolveDimUsage(Session in, String schema){
        try {
            SAXReader reader = new SAXReader();
            Document doc = reader.read(Paths.get(schema).toFile());
            List<Node> nodes = doc.selectNodes("//Cube[@name='"+in.cubeName+"']/DimensionUsage");
            HashMap<String, String> aliases = new HashMap<>();
            nodes.stream().map(n -> (Element) n).forEach(e -> aliases.put(e.attributeValue("name"), e.attributeValue("source")));
            for (Query q : in.queries){
                HashSet<QueryPart> fixed = new HashSet<>();
                for (QueryPart qp : q.dimensions){
                    Matcher m = dimPattern.matcher(qp.value);
                    if (m.matches()){
                        String dim = m.group(1);
                        if (aliases.containsKey(dim)){
                            QueryPart correct = QueryPart.newDimension(qp.value.replace(dim, aliases.get(dim)));
                            fixed.add(correct);
                        }else
                            fixed.add(qp);
                    }else
                        fixed.add(qp);
                }
                q.setDimensions(fixed);
            }

        } catch (DocumentException e){
            System.err.printf("Failed to resolve DimensionUsage for session %s and schema %s !%n", in, schema);
            e.printStackTrace();
        }

        return in;
    }


    public static List<Session> fixSessions(List<Session> sessions, String schema){
        return sessions.stream().map(s -> resolveDimUsage(s, schema)).collect(Collectors.toList());
    }
}
