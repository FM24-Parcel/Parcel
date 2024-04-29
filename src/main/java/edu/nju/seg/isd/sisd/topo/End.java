package edu.nju.seg.isd.sisd.topo;

import edu.nju.seg.isd.sisd.block.Block;

public record End(Block block) implements PartialNode {
    @Override
    public String name() {
        return "end_" +block.name();
    }
}
