package fr.univ_tours.info.im_olap.model;


import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import fr.univ_tours.info.im_olap.graph.OGraph;
import fr.univ_tours.info.im_olap.mondrian.CubeUtils;
import mondrian.olap.*;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SessionGraph {
    static Pattern dimPattern = Pattern.compile("\\[([^\\[\\]]*)\\]\\.\\[([^\\[\\]]*)\\]\\.\\[([^\\[\\]]*)\\]");

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

    public static OGraph<Double, QueryPart> injectCousins(OGraph<Double, QueryPart> base, List<Session> sessions){
        Set<QueryPart> filters = new HashSet<>();

        for (Session session : sessions){
            for (Query q : session.queries){
                QueryPart[] q1parts = q.flat();
                for (QueryPart q1part : q1parts) {
                    if (q1part.isFilter())
                        filters.add(q1part);
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
                    base.safeComputeEdge(f1, f2, o -> Optional.of(1.0));
                    base.safeComputeEdge(f2, f1, o -> Optional.of(1.0));
                    //System.out.println(f1 + " | " + f2);
                    //System.out.println(f1 + " | " + f1.t.hashCode());
                }
            }
        }
        return base;
    }

    /**
     * This will inject edges in the graph based on a XML mondrian (v3) Schema, this is probably not compatible with complex schemas
     * will have to check for that
     * @param base
     * @param mondrianFile
     * @return
     */
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
                        QueryPart p1 = new QueryPart(QueryPart.Type.DIMENSION, prefix + ".[" + levels.get(i) + "]");
                        QueryPart p2 = new QueryPart(QueryPart.Type.DIMENSION, prefix + ".[" + levels.get(i+1) + "]");
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

    public static OGraph<Double, QueryPart> buildTopologyGraph(List<Session> sessions, String schemaPath){
        OGraph<Double, QueryPart> base = buildBaseGraph(sessions);
        injectSchema(base, schemaPath);
        base.getNodes().forEach(n -> base.setEdge(n, n, 1.0));
        return base;
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

        result.getNodes().forEach(n -> result.setEdge(n,n,1.0));

        return result;
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
                            QueryPart correct = new QueryPart(QueryPart.Type.DIMENSION, qp.value.replace(dim, aliases.get(dim)));
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

    public static OGraph<Double, QueryPart> injectFilters(OGraph<Double, QueryPart> in, CubeUtils util){
        SchemaReader schemaReader = util.getCube().getSchemaReader(null).withLocus();

        for (Dimension dimension : util.getCube().getDimensions()){
            //Skip measures
            if (dimension.isMeasures())
                continue;

            System.out.println("Dimension: " + dimension);
            for (Hierarchy hierarchy : dimension.getHierarchies()){
                List<Member> topLevel = util.fetchMembers(hierarchy.getLevels()[0]);

                for (Member member : topLevel) {
                    injectFiltersNode(in, schemaReader, member);
                }

            }
        }


        return in;
    }

    private static OGraph<Double, QueryPart> injectFiltersNode(OGraph<Double, QueryPart> in, SchemaReader schemaReader, Member m) {
        List<Member> children = schemaReader.getMemberChildren(m);
        //Stop condition: reached finest granularity
        if (children == null || children.size() == 0) {
            //System.out.println("Stopped at " + m);
            return in;
        }
        QueryPart us = fromMember(m);
        in.addNode(us);

        for (int i = 0; i < children.size(); i++) {
            Member child = children.get(i);
            QueryPart c = fromMember(child);
            in.setEdge(us, c, 1.0);
            in.setEdge(c, us, 1.0);

            for (int j = i + 1; j < children.size(); j++) {
                QueryPart other = fromMember(children.get(j));
                in.setEdge(c, other, 1.0);
                in.setEdge(other, c, 1.0);
            }

            injectFiltersNode(in, schemaReader, child);
        }

        return in;
    }

    public static MutableValueGraph<QueryPart, Double> injectFiltersGuava(MutableValueGraph<QueryPart, Double> in, CubeUtils util){
        SchemaReader schemaReader = util.getCube().getSchemaReader(null).withLocus();

        for (Dimension dimension : util.getCube().getDimensions()){
            //Skip measures
            if (dimension.isMeasures())
                continue;

            System.out.println("Dimension: " + dimension);
            for (Hierarchy hierarchy : dimension.getHierarchies()){
                List<Member> topLevel = util.fetchMembers(hierarchy.getLevels()[0]);
                topLevel.forEach(member -> injectFiltersNodeGuava(in, schemaReader, member));
            }
        }


        return in;
    }

    private static MutableValueGraph<QueryPart, Double> injectFiltersNodeGuava(MutableValueGraph<QueryPart, Double> in, SchemaReader schemaReader, Member m){
        List<Member> children = schemaReader.getMemberChildren(m);
        //Stop condition: reached finest granularity
        if (children == null || children.size() == 0) {
            //System.out.println("Stopped at " + m);
            return in;
        }
        QueryPart us = fromMember(m);
        in.addNode(us);
        for (int i = 0; i < children.size(); i++) {
            Member child = children.get(i);
            QueryPart c = fromMember(child);
            in.addNode(c);
            in.putEdgeValue(us, c, 1.0);
            in.putEdgeValue(c, us, 1.0);

            for (int j = i + 1; j < children.size(); j++) {
                QueryPart other = fromMember(children.get(j));
                in.addNode(other);
                in.putEdgeValue(c, other, 1.0);
                in.putEdgeValue(other, c, 1.0);
            }

            injectFiltersNodeGuava(in , schemaReader, child);
        }

        return in;
    }

    private static OGraph<Double, QueryPart> injectFiltersForest(OGraph<Double, QueryPart> in, SchemaReader schemaReader, List<Member> list){
        for (int i = 0; i < list.size() ; i++) {
            for (int j = i + 1; j < list.size(); j++) {
                QueryPart qp1 = fromMember(list.get(i));
                QueryPart qp2 = fromMember(list.get(j));
                in.setEdge(qp1, qp2, 1.0);
                in.setEdge(qp2, qp1, 1.0);
            }
        }

        for (Member member : list) {
            injectFiltersNode(in, schemaReader, member);
        }

        return in;
    }

    private static QueryPart fromMember(Member m){
        return new QueryPart(m.getLevel().toString(), m.getName());
    }

    public static List<Session> fixSessions(List<Session> sessions, String schema){
        return sessions.stream().map(s -> resolveDimUsage(s, schema)).collect(Collectors.toList());
    }
}
