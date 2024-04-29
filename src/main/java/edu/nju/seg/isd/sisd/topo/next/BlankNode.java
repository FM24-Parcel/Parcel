package edu.nju.seg.isd.sisd.topo.next;

import edu.nju.seg.isd.sisd.graph.Node;

public class BlankNode implements Node {

    private static int globalIndex = 1;

    private final int index;

    public BlankNode() {
        this.index = globalIndex;
        globalIndex++;
    }

    @Override
    public String name() {
        return Integer.toString(index);
    }

    @Override
    public String toString() {
        return name();
    }

}
