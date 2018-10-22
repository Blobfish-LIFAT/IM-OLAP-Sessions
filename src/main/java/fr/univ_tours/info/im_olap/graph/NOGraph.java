package fr.univ_tours.info.im_olap.graph;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Non oriented Graph class. When querying the graph with two nodes N1 and N2,
 * the smallest of the two according to Comparable is taken first to really
 * perform the query ( (N1,N2) if N1 < N2 else (N2,N1) ).
 * 
 * @param <E>
 *            type of edge values
 * @param <N>
 *            type of node labels, must implement comparable
 */
public class NOGraph<E extends Comparable<E>, N extends Comparable<N>> implements Graph<E, N> {

	private HashMap<CPair<N, N>, E> hashMap;

	public NOGraph() {
		this.hashMap = new HashMap<>();
	}

	/**
	 * Return the number of edges of the non oriented graph (double the result
	 * to get the number of edges that would be counted in an oriented graph)
	 * 
	 * @return number of edges
	 */
	@Override
	public int edgeCount() {
		return hashMap.size();
	}

	@Override
	public Set<N> getNodes() {
		Set<N> nodes = new TreeSet<>();

		for (CPair<N, N> pair : hashMap.keySet()) {
			nodes.add(pair.getA());
			nodes.add(pair.getB());
		}

		return nodes;
	}

	@Override
	public Set<Edge<N, E>> getEdges() {
		return hashMap.entrySet().stream()
				.map(entry -> new Edge<>(entry.getKey().getA(), entry.getKey().getB(), entry.getValue()))
				.collect(Collectors.toCollection(TreeSet::new));
	}

	@Override
	public void setEdge(N node1, N node2, E value) {
		if (node1.compareTo(node2) > 0) {
			N temp = node1;
			node1 = node2;
			node2 = temp;
		}

		this.hashMap.put(new CPair<>(node1, node2), value);
	}

	@Override
	public void deleteNodeAndItsEdges(N node) {
		Set<CPair<N,N>> set = new HashSet<>(hashMap.keySet());
		set.stream().filter(p -> p.getA().equals(node) || p.getB().equals(node))
				.forEach(p -> hashMap.remove(p));
	}

	@Override
	public void safeComputeEdge(N node1, N node2, Function<Optional<E>, Optional<E>> f) {
		if (node1.compareTo(node2) > 0) {
			N temp = node1;
			node1 = node2;
			node2 = temp;
		}
		hashMap.compute(new CPair<>(node1, node2), (k, v) -> f.apply(Optional.ofNullable(v)).orElse(null));
	}

	@Override
	public E getEdge(N node1, N node2) {
		if (node1.compareTo(node2) > 0) {
			N temp = node1;
			node1 = node2;
			node2 = temp;
		}

		return hashMap.get(new CPair<>(node1, node2));
	}

	@Override
	public List<CPair<N, E>> fromNode(N node) {
		return hashMap.entrySet().stream().filter(p -> p.getKey().getA().equals(node) | p.getKey().getB().equals(node))
				.map(p -> p.getKey().getA().equals(node) ? new CPair<>(p.getKey().getB(), p.getValue())
						: new CPair<>(p.getKey().getA(), p.getValue()))
				.collect(Collectors.toCollection(ArrayList::new));
	}

	@Override
	public List<CPair<N, E>> toNode(N node) {
		return fromNode(node);
	}

	@Override
	public <F extends Comparable<F>> Graph<F, N> mapEdges(Function<Edge<N, E>, F> edgeFunction) {
		Graph<F, N> newGraph = new NOGraph<>();
		for (Edge<N, E> edge : this.getEdges()) {
			newGraph.setEdge(edge.from, edge.to, edgeFunction.apply(edge));
		}
		return newGraph;
	}

	public HashMap<CPair<N, N>, E> getHashmap() {
		return this.hashMap;
	}
}
