package edu.nju.seg.isd.sisd.graph;

import io.vavr.collection.List;
import io.vavr.collection.HashSet;

import java.util.Set;
import java.util.stream.Collectors;

public interface Graph<N extends Node, E extends Edge<N>> {

    Set<N> nodes();

    Set<E> edges();

    default Set<N> successors(N node) {
        return edges()
                .stream()
                .filter(edge -> edge.source().equals(node))
                .map(Edge::target)
                .collect(Collectors.toSet());
    }

    default Set<E> successiveEdges(N node) {
        return edges()
                .stream()
                .filter(edge -> edge.source().equals(node))
                .collect(Collectors.toSet());
    }

    default Set<E> previousEdges(N node) {
        return edges()
                .stream()
                .filter(edge -> edge.target().equals(node))
                .collect(Collectors.toSet());
    }

    default Set<N> zeroInDegreeNodes() {
        var inNodes = edges().stream()
                .map(Edge::target)
                .collect(Collectors.toSet());
        return nodes()
                .stream()
                .filter(n -> !inNodes.contains(n))
                .collect(Collectors.toSet());
    }

    default Set<N> zeroOutDegreeNodes() {
        var outNodes = edges().stream()
                .map(Edge::source)
                .collect(Collectors.toSet());
        return nodes()
                .stream()
                .filter(n -> !outNodes.contains(n))
                .collect(Collectors.toSet());
    }

    default List<List<E>> paths(N start, N n) {
        return visit(start, n, List.empty(), HashSet.empty(), List.empty());
    }

    private List<List<E>> visit(N current,
                                N target,
                                List<E> path,
                                HashSet<N> visited,
                                List<List<E>> paths) {
        var newVisited = visited.add(current);
        if (current.equals(target)) {
            return paths.append(path);
        } else {
            return edges()
                    .stream()
                    .filter(edge -> edge.source().equals(current))
                    .filter(edge -> !newVisited.contains(edge.target()))
                    .map(edge -> visit(edge.target(), target, path.append(edge), newVisited, paths))
                    .reduce(List.empty(), List::appendAll);
        }
    }

}
