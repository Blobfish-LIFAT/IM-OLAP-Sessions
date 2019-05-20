package fr.univ_tours.info.im_olap.graph;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GGraph<E extends Comparable<E>,N extends Comparable<N>> implements Graph<E,N> {
    MutableValueGraph<N, E> internal;

    public GGraph() {
        internal = ValueGraphBuilder.directed().allowsSelfLoops(true).build();
    }

    @Override
    public Graph<E, N> clone() {
        return null;
    }

    @Override
    public Set<N> getNodes() {
        return new HashSet<>(internal.nodes());
    }

    @Override
    public void addNode(N node) {
        internal.addNode(node);
    }

    @Override
    public Set<Edge<N, E>> getEdges() {
        return internal.edges().stream().map(e -> new Edge<>(e.nodeU(), e.nodeV(), internal.edgeValueOrDefault(e.nodeU(), e.nodeV(), null))).collect(Collectors.toSet());
    }

    @Override
    public void setEdge(N from, N to, E value) {
        internal.putEdgeValue(from, to, value);
    }

    @Override
    public void removeNodeEdgesKeepNode(N node) {

    }

    @Override
    public void deleteNodeAndItsEdges(N node) {

    }

    @Override
    public void safeComputeEdge(N from, N to, Function<Optional<E>, Optional<E>> f) {
        internal.putEdgeValue(from, to, f.apply(internal.edgeValue(from, to)).orElse(null));
    }

    @Override
    public E getEdge(N from, N to) {
        return internal.edgeValue(from, to).orElse(null);
    }

    @Override
    public List<CPair<N, E>> fromNode(N node) {
        return null;
    }

    @Override
    public List<CPair<N, E>> toNode(N node) {
        return internal.incidentEdges(node).stream().map(e -> new CPair<>(e.nodeU(), internal.edgeValueOrDefault(e.nodeU(), e.nodeV(), null))).collect(Collectors.toList());
    }

    @Override
    public <F extends Comparable<F>> Graph<F, N> mapEdges(Function<Edge<N, E>, F> edgeFunction) {
        return null;
    }
}
