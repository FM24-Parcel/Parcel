package edu.nju.seg.isd.sisd.topo.next;

import edu.nju.seg.isd.sisd.ast.IntFragment;
import edu.nju.seg.isd.sisd.ast.LoopFragment;
import edu.nju.seg.isd.sisd.graph.Graph;
import edu.nju.seg.isd.sisd.topo.Head;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class NextGraph implements Graph<BlankNode, Next> {

    private final Set<BlankNode> nodes = new HashSet<>();

    private final Set<Next> edges = new HashSet<>();

    private final BlankNode initial;

    private final Set<BlankNode> finals = new HashSet<>();

    private Map<BlankNode, Set<Next>> cache;

    public NextGraph() {
        this.initial = new BlankNode();
        this.nodes.add(initial);
    }

    public NextGraph(@NotNull BlankNode initial) {
        this.initial = initial;
        this.nodes.add(initial);
    }

    @NotNull
    public BlankNode initial() {
        return this.initial;
    }

    @NotNull
    public Set<Next> initialEdges() {
        return this.successiveEdges(initial());
    }

    @NotNull
    public Set<Next> finalEdges() {
        return finals().stream()
                .flatMap(f -> previousEdges(f).stream())
                .collect(Collectors.toSet());
    }

    public void addFinal(@NotNull BlankNode node) {
        this.finals.add(node);
    }

    @NotNull
    public Set<BlankNode> finals() {
        return this.finals;
    }


    @Override
    public Set<Next> successiveEdges(BlankNode node) {
        if (cache == null) {
            this.cache = new HashMap<>();
        }
        if (!cache.containsKey(node)) {
            this.cache.put(node, Graph.super.successiveEdges(node));
        }
        return this.cache.get(node);
    }

    @NotNull
    public Set<IntFragment> ints() {
        return this.edges().stream()
                .filter(e -> e.event() instanceof Head)
                .map(e -> (Head) e.event())
                .map(Head::fragment)
                .filter(f -> f instanceof IntFragment)
                .map(f -> (IntFragment) f)
                .collect(Collectors.toSet());
    }

    @NotNull
    public Set<LoopFragment> loops() {
        return this.edges().stream()
                .filter(e -> e.event() instanceof Head)
                .map(e -> (Head) e.event())
                .map(Head::fragment)
                .filter(f -> f instanceof LoopFragment)
                .map(f -> (LoopFragment) f)
                .collect(Collectors.toSet());
    }

    public void addNode(@NotNull BlankNode node) {
        this.cache = null;
        this.nodes.add(node);
    }

    public void addEdge(@NotNull Next edge) {
        this.cache = null;
        addNode(edge.source());
        addNode(edge.target());
        this.edges.add(edge);
    }

    @NotNull
    public NextGraph concat(@NotNull NextGraph that) {
        /* remove final nodes of this */
        var preFinalEdges = finals.stream()
                .flatMap(f -> previousEdges(f).stream())
                .collect(Collectors.toSet());
        var succFinalEdges = finals.stream()
                .flatMap(f -> successiveEdges(f).stream())
                .collect(Collectors.toSet());
        preFinalEdges.forEach(this.edges::remove);
        succFinalEdges.forEach(this.edges::remove);
        preFinalEdges.forEach(e -> addEdge(new Next(e.event(), e.source(), that.initial(), e.marker(), e.guard())));
        succFinalEdges.forEach(e -> addEdge(new Next(e.event(), that.initial(), e.target(), e.marker(), e.guard())));
        finals.clear();
        that.edges().forEach(this::addEdge);
        finals.addAll(that.finals());
        return this;
    }

    @Override
    public Set<BlankNode> nodes() {
        return this.nodes;
    }

    @Override
    public Set<Next> edges() {
        return this.edges;
    }

}
