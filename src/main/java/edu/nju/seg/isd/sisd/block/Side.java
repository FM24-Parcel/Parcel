package edu.nju.seg.isd.sisd.block;

import edu.nju.seg.isd.sisd.graph.Edge;

public record Side(
        Block source,
        Block target
)implements Edge<Block> {
}
