package edu.nju.seg.isd.sisd.topo;

import edu.nju.seg.isd.sisd.ast.*;
import edu.nju.seg.isd.sisd.exception.Impossible;
import edu.nju.seg.isd.sisd.topo.next.BlankNode;
import edu.nju.seg.isd.sisd.topo.next.Next;
import edu.nju.seg.isd.sisd.topo.next.NextGraph;
import io.vavr.Tuple;
import io.vavr.Tuple3;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@NotNull
public class Transformer {

    private final ISD isd;

    private final List<Instance> instances;

    public Transformer(@NotNull ISD isd) {
        this.isd = isd;
        this.instances = isd.instances();
    }

    @NotNull
    public Tuple3<Head, Tail, NextGraph> transform() {
        var parts = isd.partition();
        var pure = parts._1();
        var ints = parts._2();
        var tuple = constructGraphForContainer(pure);
        var completeGraph = interrupt(tuple._3(), ints);
        return Tuple.of(tuple._1(), tuple._2(), completeGraph);
    }

    @NotNull
    private NextGraph interrupt(@NotNull NextGraph pure,
                                @NotNull SortedMap<Integer, List<IntFragment>> ints) {
        var keys = ints.keySet().stream().sorted().toList();
        var result = pure;
        for (var key : keys) {
            var list = ints.get(key);
            result = interruptSpecificInts(result, list);
        }
        return result;
    }

    @NotNull
    private NextGraph interruptSpecificInts(@NotNull NextGraph interrupted,
                                            @NotNull List<IntFragment> list) {
        var result = new NextGraph(interrupted.initial());
        interrupted.edges().forEach(result::addEdge);
        interrupted.finals().forEach(result::addFinal);
        for (var intFragment : list) {
            var intGraph = constructGraphForIntFragment(intFragment);
            var initialEdge = intGraph.successiveEdges(intGraph.initial()).iterator().next();
            var finalEdge = intGraph.finals()
                    .stream()
                    .flatMap(f -> intGraph.previousEdges(f).stream())
                    .findFirst()
                    .orElseThrow();
            interrupted.nodes().forEach(n -> {
                result.addEdge(new Next(initialEdge.event(), n, initialEdge.target(), n.hashCode(), null));
                result.addEdge(new Next(finalEdge.event(), finalEdge.source(), n, null, n.hashCode()));
            });
            intGraph.edges()
                    .stream()
                    .filter(e -> !e.equals(initialEdge) && !e.equals(finalEdge))
                    .forEach(result::addEdge);
        }
        return result;
    }

    @NotNull
    private Tuple3<Head, Tail, NextGraph> constructGraphForContainer(@NotNull Container container) {
        var graph = constructGraphForPureFragment(container);
        var head = graph.edges()
                .stream()
                .filter(e -> e.event() instanceof Head)
                .map(e -> (Head) e.event() )
                .findFirst()
                .orElseThrow();
        var tail = graph.edges()
                .stream()
                .filter(e -> e.event() instanceof Tail)
                .map(e -> (Tail) e.event())
                .findFirst()
                .orElseThrow();
        return Tuple.of(head, tail, graph);
    }

    @NotNull
    private NextGraph constructGraphForAltFragment(@NotNull AltFragment altFragment) {
        var ifGraph = constructGraphForPureFragment(new Container(altFragment.getItems()));
        var elseGraph = constructGraphForPureFragment(new Container(altFragment.getElseItems()));
        var elseInitialEdge = elseGraph.successiveEdges(elseGraph.initial());
        elseInitialEdge.forEach(e -> ifGraph.addEdge(Next.normal(e.event(), ifGraph.initial(), e.target())));
        elseGraph.edges()
                .stream()
                .filter(e -> !e.source().equals(elseGraph.initial()))
                .forEach(ifGraph::addEdge);
        ifGraph.finals().addAll(elseGraph.finals());
        return ifGraph;
    }

    @NotNull
    private NextGraph constructGraphForLoopFragment(@NotNull LoopFragment loopFragment) {
        var graph = constructGraphForPureFragment(loopFragment);
        var finalEdges = graph.finals().stream()
                .flatMap(f -> graph.previousEdges(f).stream())
                .collect(Collectors.toSet());
        finalEdges.forEach(graph.edges()::remove);
        finalEdges.forEach(e -> graph.addEdge(Next.normal(e.event(), e.source(), graph.initial())));
        graph.finals().clear();
        graph.addFinal(graph.initial());
        return graph;
    }

    @NotNull
    private NextGraph constructGraphForIntFragment(@NotNull IntFragment intFragment) {
        return constructGraphForPureFragment(intFragment);
    }

    @NotNull
    private NextGraph constructGraphForPureFragment(@NotNull Fragment fragment) {
        var result = segment(fragment)
                .stream()
                .map(this::constructGraphForSegment)
                .reduce(headGraph(fragment), NextGraph::concat);
        return result.concat(tailGraph(fragment));
    }

    @NotNull
    private NextGraph headGraph(@NotNull Fragment fragment) {
        var graph = new NextGraph();
        var finalNode = new BlankNode();
        graph.addEdge(Next.normal(new Head(fragment), graph.initial(), finalNode));
        graph.addFinal(finalNode);
        return graph;
    }

    @NotNull
    private NextGraph tailGraph(@NotNull Fragment fragment) {
        var graph = new NextGraph();
        var finalNode = new BlankNode();
        graph.addEdge(Next.normal(new Tail(fragment), graph.initial(), finalNode));
        graph.addFinal(finalNode);
        return graph;
    }

    /* unsafe, do not call this method */
    @NotNull
    private NextGraph constructGraphForSegment(Object o) {
        return switch (o) {
            case BasicFragment bf -> constructGraphForBasicFragment(bf);
            case AltFragment af -> constructGraphForAltFragment(af);
            case LoopFragment lf -> constructGraphForLoopFragment(lf);
            default -> throw new Impossible();
        };
    }

    @NotNull
    private NextGraph constructGraphForBasicFragment(@NotNull BasicFragment bf) {
        var calculus = new TopoCalculus(instances);
        bf.messages().forEach(calculus::recordMessage);
        var topo = calculus.toTopo();
        var graph = constructGraphViaTopo(topo, bf.events());
        graph.zeroOutDegreeNodes().forEach(graph::addFinal);
        return graph;
    }

    /**
     * This algorithm is based on the following paper:
     * Easy Modelling
     * @param topo the partial orders of the fragment
     * @param events the events of the fragment
     * @return the graph whose edges represent the events
     */
    @NotNull
    private NextGraph constructGraphViaTopo(@NotNull FlatTopo topo,
                                            @NotNull Set<PartialNode> events) {
        var graph = new NextGraph();
        Map<BlankNode, io.vavr.collection.Set<PartialNode>> pathEventMap = new HashMap<>();
        pathEventMap.put(graph.initial(), io.vavr.collection.HashSet.empty());
        Set<BlankNode> visited = new HashSet<>();
        var zeroOuts = graph.zeroOutDegreeNodes();
        while (!visited.containsAll(zeroOuts)) {
            zeroOuts.stream()
                    .filter(noOut -> !visited.contains(noOut))
                    .forEach(noOut -> {
                        visited.add(noOut);
                        var pathEvents = pathEventMap.get(noOut);
                        for (var remainingEv : setMinus(events, pathEvents)) {
                            var deps = topoDeps(topo, remainingEv);
                            if (pathEvents.containsAll(deps)) {
                                reverseLookup(pathEventMap, pathEvents.add(remainingEv))
                                        .ifPresentOrElse(
                                                eq -> graph.addEdge(Next.normal(remainingEv, noOut, eq)),
                                                () -> {
                                                    var newNoOut = new BlankNode();
                                                    graph.addEdge(Next.normal(remainingEv, noOut, newNoOut));
                                                    pathEventMap.put(newNoOut, pathEventMap.get(noOut).add(remainingEv));
                                                }
                                        );
                            }
                        }
                    });
            zeroOuts = graph.zeroOutDegreeNodes();
        }
        return graph;
    }

    @NotNull
    private Optional<BlankNode> reverseLookup(@NotNull Map<BlankNode, io.vavr.collection.Set<PartialNode>> map,
                                              @NotNull io.vavr.collection.Set<PartialNode> pathEvents) {
        for (var entry : map.entrySet()) {
            if (entry.getValue().eq(pathEvents)) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    @NotNull
    private Set<PartialNode> topoDeps(@NotNull FlatTopo topo,
                                      @NotNull PartialNode event) {
        return topo.edges()
                .stream()
                .filter(o -> o.target().equals(event))
                .map(Order::source)
                .collect(Collectors.toSet());
    }

    @NotNull
    private io.vavr.collection.HashSet<PartialNode> setMinus(Set<PartialNode> events,
                                                             io.vavr.collection.Set<PartialNode> subtrahend) {
        return io.vavr.collection.HashSet.ofAll(events).removeAll(subtrahend);
    }

    @NotNull
    private List<Object> segment(@NotNull Fragment fragment) {
        var items = fragment.getItems();
        List<Object> result = new ArrayList<>();
        List<Message> current = new ArrayList<>();
        for (var item : items) {
            switch (item) {
                case Message m -> current.add(m);
                case AltFragment af -> {
                    if (!current.isEmpty()) {
                        result.add(new BasicFragment(current));
                        current = new ArrayList<>();
                    }
                    result.add(af);
                }
                case LoopFragment lf -> {
                    if (!current.isEmpty()) {
                        result.add(new BasicFragment(current));
                        current = new ArrayList<>();
                    }
                    result.add(lf);
                }
                case Container ignored -> throw new Impossible();
                case IntFragment ignored -> throw new Impossible();
            }
        }
        if (!current.isEmpty()) {
            result.add(new BasicFragment(current));
        }
        return result;
    }

}
