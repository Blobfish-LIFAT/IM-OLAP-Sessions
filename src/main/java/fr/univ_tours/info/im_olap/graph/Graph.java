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

    Graph<E,N> clone();

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
     * @return the set of nodes of the graph, even nodes that are not connected with other nodes
     */
    Set<N> getNodes();

    /**
     *
     * @param node
     * @return true if node was added, false if it was already present
     */
    boolean addNode(N node);

    /**
     *
     * @return the set of edges with the 2 connected nodes and the edge value
     */
    Set<Edge<N,E>> getEdges();

    /**
     *
     * @param from source node
     * @param to target node
     * @param value new value of the edge
     */
    void setEdge(N from, N to, E value);

    default void removeEdge(N from, N to){
        this.safeComputeEdge(from, to, ignored -> Optional.empty());
    }

    /**
     * Tell if a node is present in the graph
     * override this function for better performances
     * @param node
     * @return true if the node exists else false
     */
    default boolean nodeExists(N node){
        return getNodes().stream().anyMatch(n -> n.equals(node));
    }

    void deleteNodeAndItsEdges(N node);

    default void computeEdge(N from, N to, Function<E, E> f){
        safeComputeEdge(from, to, val -> val.map(f));
    }

    void safeComputeEdge(N from, N to, Function<Optional<E>, Optional<E>> f);

    default void safePutEdge(N from, N to, Function<Optional<E>, E> f) {
        this.safeComputeEdge(from, to, x -> Optional.ofNullable(f.apply(x)));
    }

    /**
     *
     * @param from
     * @param to
     * @return value on edge or Null if the edge is not present
     */
    E getEdge(N from, N to);

    List<CPair<N, E>> fromNode(N node);

    List<CPair<N, E>> toNode(N node);

    default Optional<E> safeGetEdge(N from, N to) {
        return Optional.ofNullable(this.getEdge(from, to));
    }

    <F extends Comparable<F>> Graph<F,N> mapEdges(Function<Edge<N, E>, F> edgeFunction);

    /**
     * Get the string representation of the graph
     * @param name
     * @return
     */
    default String toPrettyString(String name) {
        StringBuilder sb = new StringBuilder();

        sb.append(name+":");
        sb.append("Nodes:");

        for (N node: this.getNodes()) {
            sb.append(", ");
            sb.append(node.toString());
        }

        sb.append("\nEdges:");

        for (Edge<N,E> edge : this.getEdges()) {
            sb.append("\n\t");
            sb.append(edge.toString());
        }

        return sb.toString();
    }



    default boolean equal(Graph<E,N> other) {
        return this.getEdges().equals(other.getEdges())
                && this.getNodes().equals(other.getNodes());
    }
}
