package fr.univ_tours.info.im_olap.graph;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Parametrized Graph interface for oriented Graphs
 *
 * @param <E> type of Edges values
 * @param <N> type of Node labels, must implement .equals() function
 */
public interface Graph<E extends Comparable<E>,N extends Comparable<N>> {

    /**
     * Represents an edge in the graph
     * @param <F> type of the node labels
     * @param <G> type of the edge value
     */
    class Edge<F extends Comparable<F>,G extends Comparable<G>> implements Comparable<Edge<F,G>> {
        public final F from;
        public final F to;
        public final G value;

        public Edge(F from, F to, G value) {
            if (from == null || to == null || value == null) throw new IllegalArgumentException("parameter is null");
            this.from = from;
            this.to = to;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Edge<?, ?> edge = (Edge<?, ?>) o;
            return Objects.equals(from, edge.from) &&
                    Objects.equals(to, edge.to) &&
                    Objects.equals(value, edge.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(from, to, value);
        }

        @Override
        public int compareTo(Edge<F, G> o) {
            int comp = from.compareTo(o.from);
            if ( comp != 0){
                return comp;
            }
            else if ((comp = to.compareTo(o.to)) != 0){
                return comp;
            }
            else {
                return value.compareTo(o.value);
            }
        }

        @Override
        public String toString() {
            return "Edge{" + from +
                    ", " + to +
                    ", " + value +
                    '}';
        }
    }

    /**
     * Count the number of edges in the graph
     * @return the edge count
     */
    default int edgeCount(){
        return getEdges().size();
    }

    /**
     * Count the number of nodes in the graph
     * @return the node count
     */
    default int nodeCount(){
        return getNodes().size();
    }

    /**
     *
     * @return
     */
    Set<N> getNodes();

    Set<Edge<N,E>> getEdges();

    void setEdge(N node1, N node2, E value);

    default void removeEdge(N node1, N node2){
        this.safeComputeEdge(node1, node2, ignored -> Optional.empty());
    }

    default boolean nodeExists(N node){
        return getNodes().stream().anyMatch(n -> n.equals(node));
    }

    void deleteNodeAndItsEdges(N node);

    default void computeEdge(N node1, N node2, Function<E, E> f){
        safeComputeEdge(node1, node2, val -> val.map(f));
    }

    void safeComputeEdge(N node1, N node2, Function<Optional<E>, Optional<E>> f);

    E getEdge(N node1, N node2);

    List<CPair<N, E>> fromNode(N node);

    List<CPair<N, E>> toNode(N node);

    default Optional<E> safeGetEdge(N node1, N node2) {
        return Optional.ofNullable(this.getEdge(node1, node2));
    }

    <F extends Comparable<F>> Graph<F,N> mapEdges(Function<Edge<N, E>, F> edgeFunction);
}
