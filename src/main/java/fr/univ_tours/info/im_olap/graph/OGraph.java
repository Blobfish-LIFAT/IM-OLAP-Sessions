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
    public boolean addNode(N node) {
        if (nodes.containsKey(node)){
            return false;
        }
        nodes.put(node, new Pair<>(new TreeSet<>(), new TreeSet<>()));
        return true;
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
        nodes.get(from).getB().add(to);
        nodes.get(to).getA().add(from);
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
    public void deleteNodeAndItsEdges(N node) {
        Pair<TreeSet<N>,TreeSet<N>> p = nodes.get(node);
        p.getA().forEach(from -> {
            edges.remove(new Pair<>(from, node));
        });
        p.getB().forEach(to -> {
            edges.remove(new Pair<>(node, to));
        });
        nodes.remove(node);

    }

    @Override
    public void safeComputeEdge(N from, N to, Function<Optional<E>, Optional<E>> f) {
        addNode(from);
        addNode(to);
        edges.compute(new Pair<>(from, to), (k,v) -> {
            E res = f.apply(Optional.ofNullable(v)).orElse(null);

            if (res == null){
                unsafeRemoveEdgeInNodes(from, to);
            }
            else {
                unsafeAddEdgeInNodes(from, to);
            }

            return res;
        });
    }

    @Override
    public E getEdge(N from, N to) {
        return edges.get(new Pair<>(from, to));
    }

    @Override
    public List<CPair<N, E>> fromNode(N node) {

        /*
        nodes.entrySet().forEach(e -> {
            System.out.println(e.getKey());
            System.out.println("B:");
            e.getValue().getB().forEach(System.out::println);
        });*/

        return nodes.get(node)
                .getB()
                .stream()
                .map(to -> {
                    E v = edges.get(new Pair<>(node, to));
                    return new CPair<>(node, v);
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
                    return new CPair<>(node, v);
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

}
