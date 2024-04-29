package edu.nju.seg.isd.sisd.topo;

import edu.nju.seg.isd.sisd.graph.Graph;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@NotNull
public class FlatTopo implements Graph<PartialNode, Order> {

    private final Set<PartialNode> nodes;

    private final Set<Order> edges;

    public FlatTopo() {
        this.nodes = new HashSet<>();
        this.edges = new HashSet<>();
    }

    public void addPartialOrder(@NotNull Order order) {
        this.edges.add(order);
        this.nodes.add(order.source());
        this.nodes.add(order.target());
    }

    @NotNull
    public FlatTopo merge(@NotNull FlatTopo that) {
        this.nodes.addAll(that.nodes());
        this.edges.addAll(that.edges());
        return this;
    }

    @Override
    @NotNull
    public Set<PartialNode> nodes() {
        return nodes;
    }

    @Override
    @NotNull
    public Set<Order> edges() {
        return edges;
    }

}
