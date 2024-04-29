package edu.nju.seg.isd.sisd.topo;

import edu.nju.seg.isd.sisd.graph.Edge;

public record Order(
        PartialNode source,
        PartialNode target)
        implements Edge<PartialNode> {

}
