package fr.univ_tours.info.im_olap.graph;



import com.alexsxode.utilities.collection.Pair;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OGraph<E extends Comparable<E>,N extends Comparable<N>> implements Graph<E,N> {
    HashMap<Pair<N,N>, E> hashMap;

    HashMap<N,Set<N>> edgeMap;

    public OGraph() {
        this.hashMap = new HashMap<>();
        this.edgeMap = new HashMap<>();
    }

    public OGraph(HashMap<Pair<N,N>, E> edges) {
        this.hashMap = new HashMap<>(edges);
        this.edgeMap = new HashMap<>(hashMap.size());
        for (Pair<N,N> p : hashMap.keySet()){
            edgeMap.compute(p.getA(), (k,v) -> {
                if (k == null || v == null){
                    v = new HashSet<>();
                }
                v.add(p.getB());
                return v;
            });
        }
    }

    @Override
    public int nodeCount() {
        return getNodes().size();
    }

    @Override
    public int edgeCount() {
        return hashMap.size();
    }

    @Override
    public Set<N> getNodes() {
        Set<N> set = new HashSet<>();

        for (Pair<N,N> p : hashMap.keySet()){
            set.add(p.getA());
            set.add(p.getB());
        }

        return set;
    }

    @Override
    public Set<Edge<N, E>> getEdges() {
        return hashMap.entrySet()
                .stream()
                .map(x -> new Edge<>(x.getKey().getA(), x.getKey().getB(), x.getValue()))
                .collect(Collectors.toSet());
    }

    @Override
    public void setEdge(N node1, N node2, E value) {
        hashMap.put(new Pair<>(node1, node2), value);
        edgeMap.compute(node1, (k,v) -> {
            if (k == null || v == null){
                Set<N> set = new HashSet<>();
                set.add(node2);
                return set;
            }
            else {
                v.add(node2);
                return v;
            }
        });
    }

    @Override
    public void removeEdge(N node1, N node2) {
        hashMap.remove(new Pair<>(node1, node2));
        edgeMap.compute(node1, (k,v) -> {
            if (k == null || v == null){
                return null;
            }
            else {
                v.remove(v);
                return v;
            }
        });
    }

    @Override
    public boolean nodeExists(N node) {
        return getNodes().contains(node);
    }

    @Override
    public void deleteNodeAndItsEdges(N node) {
        hashMap.replaceAll((x,v) -> x.getA().equals(node) || x.getB().equals(node) ? null : v);
        edgeMap.replaceAll((k,v) -> {
            if (k == null || v == null){
                return null;
            }
            else if (k.equals(node)){
                return null;
            }
            else {
                v.remove(node);
                if (v.isEmpty()){
                    return null;
                }

                return v;
            }
        });
    }

    private void checkRemoveNode(N node, Set<N> nodes){
        if (!nodes.contains(node)){
            edgeMap.remove(node);
            edgeMap.replaceAll((k,v) -> {
                if (k.equals(node)){
                    return null;
                }
                else {
                    v.remove(node);
                    if (v.isEmpty()){
                        return null;
                    }
                    else return v;
                }
            });
        }

    }

    @Override
    public void safeComputeEdge(N node1, N node2, Function<Optional<E>, Optional<E>> f) {
        Pair<N,N> p = new Pair<>(node1, node2);
        E v = hashMap.get(p);
        E newV = f.apply(Optional.ofNullable(v)).orElse(null);
        if (newV == null){
            hashMap.remove(p);
            Set<N> nodes = getNodes();
            checkRemoveNode(node1, nodes);
            checkRemoveNode(node2, nodes);
        }
        else {
            hashMap.put(p, newV);
            edgeMap.compute(node1, (k,old_v) -> {
                if (node1 == null | old_v == null){
                    old_v = new HashSet<>();
                }
                old_v.add(node2);
                return old_v;
            });
        }
    }

    @Override
    public E getEdge(N node1, N node2) {
        return hashMap.get(new Pair<>(node1, node2));
    }

    @Override
    public List<CPair<N, E>> fromNode(N node) {
        return edgeMap.get(node)
                .stream()
                .map(to -> new CPair<>(to, this.hashMap.get(new Pair<>(node, to)) ) )
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<CPair<N, E>> toNode(N node) {
        return hashMap.entrySet()
                .stream()
                .filter(e -> e.getKey().getB().equals(node))
                .map(e -> new CPair<>(e.getKey().getA(), e.getValue()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public <F extends Comparable<F>> Graph<F, N> mapEdges(Function<Edge<N, E>, F> edgeFunction) {
        OGraph<F,N> newGraph = new OGraph();

        for (Map.Entry<Pair<N,N>, E> entry : hashMap.entrySet()){
            N from = entry.getKey().getA();
            N to = entry.getKey().getB();
            newGraph.setEdge(from, to, edgeFunction.apply(new Edge<>(from, to, entry.getValue())));
        }

        return newGraph;
    }

    public static void main(String[] args){

        OGraph<Double, String> graph = new OGraph<>();

        graph.setEdge("A", "B", 2.0);
        graph.safeComputeEdge("A", "B", x -> x.map(y -> y-1));
        graph.safeComputeEdge("B", "C", x -> Optional.of(2.0));
        graph.setEdge("D", "D", 0.0);
        graph.safeComputeEdge("D","D", x -> Optional.empty());

        System.out.println(graph.hashMap);
        System.out.println(graph.getEdges());
        System.out.println(graph.getNodes());
        System.out.println(graph.edgeMap);

    }

}
