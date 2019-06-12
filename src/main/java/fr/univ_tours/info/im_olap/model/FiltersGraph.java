package fr.univ_tours.info.im_olap.model;

import com.google.common.graph.MutableValueGraph;
import fr.univ_tours.info.im_olap.mondrian.CubeUtils;
import mondrian.olap.Dimension;
import mondrian.olap.Hierarchy;
import mondrian.olap.Member;
import mondrian.olap.SchemaReader;

import java.util.List;

public class FiltersGraph {

    public static MutableValueGraph<QueryPart, Double> injectFilters(MutableValueGraph<QueryPart, Double> in, CubeUtils util){
        SchemaReader schemaReader = util.getCube().getSchemaReader(null).withLocus();

        for (Dimension dimension : util.getCube().getDimensions()){
            //Skip measures
            if (dimension.isMeasures())
                continue;

            //System.out.println("Dimension: " + dimension);
            for (Hierarchy hierarchy : dimension.getHierarchies()){
                List<Member> topLevel = util.fetchMembers(hierarchy.getLevels()[0]);

                for (Member member : topLevel) {
                    injectFiltersNode(in, schemaReader, member);
                }

            }
        }

        return in;
    }

    private static MutableValueGraph<QueryPart, Double> injectFiltersNode(MutableValueGraph<QueryPart, Double> in, SchemaReader schemaReader, Member m) {
        List<Member> children = schemaReader.getMemberChildren(m);
        //Stop condition: reached finest granularity
        if (children == null || children.size() == 0) {
            return in;
        }
        QueryPart us = QueryPart.newFilter(m.getName(), m.getLevel().toString());
        //in.addNode(us);

        for (int i = 0; i < children.size(); i++) {
            Member child = children.get(i);
            QueryPart c = QueryPart.newFilter(child.getName(), child.getLevel().toString());

            //in.addNode(c);
            in.putEdgeValue(us, c, 1.0);
            in.putEdgeValue(c, us, 1.0);
/*
            for (int j = i + 1; j < children.size(); j++) {
                QueryPart other = fromMember(children.get(j));
                in.setEdge(c, other, 1.0);
                in.setEdge(other, c, 1.0);
            }
*/
            injectFiltersNode(in, schemaReader, child);
        }

        return in;
    }

    public static MutableValueGraph<QueryPart, Double> injectCompressedFilters(MutableValueGraph<QueryPart, Double> in, CubeUtils util){
        SchemaReader schemaReader = util.getCube().getSchemaReader(null).withLocus();

        for (Dimension dimension : util.getCube().getDimensions()){
            //Skip measures
            if (dimension.isMeasures())
                continue;

            //System.out.println("Dimension: " + dimension);
            for (Hierarchy hierarchy : dimension.getHierarchies()){
                List<Member> topLevel = util.fetchMembers(hierarchy.getLevels()[0]);

                for (Member member : topLevel) {
                    recCall2(in, schemaReader, member);
                }

            }
        }

        return in;
    }

    private static MutableValueGraph<QueryPart, Double> recCall2(MutableValueGraph<QueryPart, Double> in, SchemaReader schemaReader, Member m) {
        List<Member> children = schemaReader.getMemberChildren(m);

        QueryPart us = QueryPart.newFilter(m.getName(), m.getLevel().toString());
        QueryPart dim = QueryPart.newDimension(m.getLevel().toString());
        in.addNode(us);
        in.addNode(dim);
        in.putEdgeValue(us, dim, 1.0);
        in.putEdgeValue(dim, us, 1.0);

        //Stop condition: reached finest granularity
        if (children == null || children.size() == 0) {
            return in;
        }

        for (int i = 0; i < children.size(); i++) {
            Member child = children.get(i);
            QueryPart c = QueryPart.newFilter(child.getName(), child.getLevel().toString());

            in.addNode(c);
            in.putEdgeValue(us, c, 1.0);
            in.putEdgeValue(c, us, 1.0);

            injectFiltersNode(in, schemaReader, child);
        }

        return in;
    }
}
