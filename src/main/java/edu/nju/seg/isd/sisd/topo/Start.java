package edu.nju.seg.isd.sisd.topo;

import edu.nju.seg.isd.sisd.block.Block;

public record Start(Block block) implements PartialNode {
    @Override
    public String name() {
        return "start_" +block.name();
    }
}
