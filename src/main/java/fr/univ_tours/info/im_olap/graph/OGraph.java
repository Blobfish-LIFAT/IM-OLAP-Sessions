package fr.univ_tours.info.im_olap.graph;



import com.alexsxode.utilities.collection.Pair;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OGraph<E extends Comparable<E>,N extends Comparable<N>> implements Graph<E,N> {

    private TreeMap<N,Pair<TreeSet<N>,TreeSet<N>>> nodes; // A Pair<X,Y> = Pair<From X to A,From A to Y>

    private HashMap<Pair<N,N>,E> edges;

    public OGraph(){
        nodes = new TreeMap<>();
        edges = new HashMap<>();
    }

    public void checkSync() throws IllegalStateException {

        // check if all of the nodes edges are present with a value in edges

        for (Map.Entry<N, Pair<TreeSet<N>, TreeSet<N>>> entry : nodes.entrySet()) {

            for (N from : entry.getValue().left ) {
                Pair<N,N> pair1 = new Pair<>(from, entry.getKey());

                if (!edges.containsKey(pair1)) {
                    throw new IllegalStateException();
                }

                if (edges.get(pair1) == null) {
                    throw new IllegalStateException();
                }

            }

            for (N to : entry.getValue().right) {
                Pair<N,N> pair2 = new Pair<>(entry.getKey(), to);

                if (!edges.containsKey(pair2)) {
                    throw new IllegalStateException();
                }

                if (edges.get(pair2) == null) {
                    throw new IllegalStateException();
                }
            }

        }

        // check if all of the edges have values and their nodes are correctly set in nodes

        for (Map.Entry<Pair<N,N>, E> entry : edges.entrySet()) {

            if (entry.getValue() == null) {
                System.err.println("An edge value is null");
                throw new IllegalStateException();
            }

            if (!nodes.containsKey(entry.getKey().left)) {
                throw new IllegalStateException();
            }

            if (!nodes.containsKey(entry.getKey().right)) {
                throw new IllegalStateException();
            }

            if (!nodes.get(entry.getKey().left).right.contains(entry.getKey().right)) {
                throw new IllegalStateException();
            }

            if (!nodes.get(entry.getKey().right).left.contains(entry.getKey().left)) {
                throw new IllegalStateException();
            }

        }

    }

    @Override
    public Graph<E, N> clone() {

        OGraph<E,N> graph = new OGraph<>();

        // deepcopy

        this.nodes.forEach((key, value) -> {
            graph.nodes.put(key, new Pair<>(new TreeSet<>(value.left), new TreeSet<>(value.right)));
        });

        graph.edges.putAll(this.edges);

        return graph;
    }

    @Override
    public int edgeCount() {
        return edges.size();
    }

    @Override
    public int nodeCount() {
        return nodes.size();
    }

    @Override
    public Set<N> getNodes() {
        return nodes.keySet();
    }

    @Override
    public void addNode(N node) {
        this.nodes.computeIfAbsent(node, x -> new Pair<>(new TreeSet<>(), new TreeSet<>()));
    }

    @Override
    public Set<Edge<N, E>> getEdges() {
        return edges.entrySet()
                .stream()
                .map(e -> new Edge<N,E>(e.getKey().getA(), e.getKey().getB(), e.getValue()))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private void unsafeAddEdgeInNodes(N from, N to){
        this.addNode(from);
        this.addNode(to);
        unsafeAddOnlyEdgesInNodes(from, to);
    }

    private void unsafeAddOnlyEdgesInNodes(N from, N to) {
        nodes.get(from).getB().add(to);
        nodes.get(to).getA().add(from);
    }

    public void unsafeSetEdge(N from, N to, E value) {
        unsafeAddEdgeInNodes(from, to);
        edges.put(new Pair<>(from, to), value);
    }

    @Override
    public void setEdge(N from, N to, E value) {
        if (value == null){
            removeEdge(from, to);
        }
        else {
            unsafeAddEdgeInNodes(from, to);
            edges.put(new Pair<>(from, to), value);
        }
    }

    private void unsafeRemoveEdgeInNodes(N from, N to){
        nodes.get(from).getB().remove(to);
        nodes.get(to).getA().remove(from);
    }

    @Override
    public void removeEdge(N from, N to) {
        edges.remove(new Pair<>(from,to));
        unsafeRemoveEdgeInNodes(from, to);
    }

    @Override
    public boolean nodeExists(N node) {
        return nodes.containsKey(node);
    }

    @Override
    public void removeNodeEdgesKeepNode(N node){

        Pair<TreeSet<N>,TreeSet<N>> p = nodes.get(node);
        p.getA().forEach(from -> {
            removeEdge(from, node);
        });
        p.getB().forEach(to -> {
            removeEdge(node, to);
        });

    }

    @Override
    public void deleteNodeAndItsEdges(N node) {
        removeNodeEdgesKeepNode(node);
        nodes.remove(node);
    }

    @Override
    public void safeComputeEdge(N from, N to, Function<Optional<E>, Optional<E>> f) {
        addNode(from);
        addNode(to);
        edges.compute(new Pair<>(from, to), (k,v) -> {
            E res = f.apply(Optional.ofNullable(v)).orElse(null);

            this.setEdge(from, to, res);

            return res;
        });
    }

    @Override
    public E getEdge(N from, N to) {
        return edges.get(new Pair<>(from, to));
    }

    @Override
    public List<CPair<N, E>> fromNode(N node) {

        return nodes.get(node)
                .getB()
                .stream()
                .map(to -> {
                    E v = edges.get(new Pair<>(node, to));
                    if (v == null) {
                        throw new IllegalStateException("Edge value should not be null");
                    }
                    return new CPair<>(to, v);
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<CPair<N, E>> toNode(N node) {
        return nodes.get(node)
                .getA()
                .stream()
                .map(from -> {
                    E v = edges.get(new Pair<>(from, node));
                    if (v == null) {
                        throw new IllegalStateException("Edge value should not be null");
                    }
                    return new CPair<>(from, v);
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public <F extends Comparable<F>> Graph<F, N> mapEdges(Function<Edge<N, E>, F> edgeFunction) {
        Graph<F,N> newGraph = new OGraph<>();
        newGraph.getNodes().addAll(this.getNodes());
        this.getEdges().forEach(e -> {
            F newVal = edgeFunction.apply(e);
            newGraph.setEdge(e.from, e.to, newVal);
        });

        return newGraph;
    }

    @Override
    public String toString() {
        return this.toPrettyString("Directed Graph");
    }

    public static void main(String[] args){

        OGraph<Double, String> g = new OGraph<>();

        g.safePutEdge("A", "B", o -> o.map(x -> x+1.0).orElse(1.0));
        g.safePutEdge("A", "B", o -> o.map(x -> x+1.0).orElse(1.0));
        g.safePutEdge("B", "B", o -> o.map(x -> x+1.0).orElse(1.0));
        g.safePutEdge("B", "A", o -> o.map(x -> x+1.0).orElse(1.0));
        g.safePutEdge("A", "C", o -> o.map(x -> x+1.0).orElse(1.0));

        g.fromNode("A").forEach(System.out::println);

        System.out.println(Graphs.sortedINDMatrix(g));

        System.out.println(g.fromNode("A"));

    }

}
