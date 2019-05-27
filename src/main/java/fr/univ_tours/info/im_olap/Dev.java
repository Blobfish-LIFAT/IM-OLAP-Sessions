package fr.univ_tours.info.im_olap;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import fr.univ_tours.info.im_olap.model.QueryPart;

import java.util.HashSet;
import java.util.Set;

public class Dev {
    public static void main(String[] args) {


    /*
        List<Session> dopan = Proto1.loadDopanSessions();

        Map<String, Integer> test = dopan.stream()
                .flatMap(s -> s.queries.stream())
                .flatMap(q -> q.getAllParts().stream())
                .filter(QueryPart::isMeasure)
                .map(QueryPart::getValue)
                .collect(Collectors.groupingBy(String::toLowerCase, Collectors.summingInt(x -> 1)));

        System.out.println(test.toString().replace(',', '\n'));

     */

        MutableGraph<QueryPart> graph1 = GraphBuilder.undirected().build();
        MutableGraph<QueryPart> graph2 = GraphBuilder.undirected().build();
        QueryPart partA = QueryPart.newDimension("coucou");
        QueryPart partB = QueryPart.newDimension("hello");

        graph1.putEdge(partA, partB);
        graph2.putEdge(partA, partB);

        EndpointPair<QueryPart> pair1 = graph1.edges().stream().findFirst().get();
        EndpointPair<QueryPart> pair2 = graph2.edges().stream().findFirst().get();

        Set<EndpointPair<QueryPart>> set = new HashSet<>(graph1.edges());
        set.addAll(graph2.edges());
        System.out.println(set);

        System.out.println(pair1 == pair2);
        System.out.println(pair1.equals(pair2));

    }
}
