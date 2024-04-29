package edu.nju.seg.isd.sisd.block;

import edu.nju.seg.isd.sisd.ast.expression.Variable;
import edu.nju.seg.isd.sisd.graph.Graph;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlockChart implements Graph<Block, Side> {
    private final Set<Block> nodes=new HashSet<>();
    private final Set<Side> edges=new HashSet<>();

    private List<Variable> masks = new ArrayList<>();

    @Override
    public Set<Block> nodes() {
        return this.nodes;
    }

    @Override
    public Set<Side> edges() {
        return this.edges;
    }

    public void addBlock(Block block){
        this.nodes.add(block);
    }

    public void addSide(Side side){
        this.edges.add(side);
    }

    public void addMasks(List<Variable> masks){
        this.masks.addAll(masks);
    }
    public BlockChart merge(@NotNull BlockChart that){
        this.nodes.addAll(that.nodes());
        this.edges.addAll(that.edges());
        return this;
    }

    public BlockChart copy(){
        BlockChart that=new BlockChart();
        that.nodes.addAll(this.nodes().stream().map(Block::copy).toList());
        int len=this.nodes().size();
        for (Side(Block source, Block target) : this.edges()) {
            that.addSide(new Side(
                    that.nodes().stream().filter(block -> block.index()-len==source.index()).findFirst().orElseThrow(),
                    that.nodes().stream().filter(block -> block.index()-len==target.index()).findFirst().orElseThrow()
            ));
        }
        return that;
    }

    public BlockChart concat(@NotNull BlockChart that){
        var endBlocks=this.zeroOutDegreeNodes();
        var startBlocks=that.zeroInDegreeNodes();
        for (Block endBlock : endBlocks) {
            for (Block startBlock : startBlocks) {
                this.addSide(new Side(endBlock,startBlock));
            }
        }
        this.nodes.addAll(that.nodes());
        this.edges.addAll(that.edges());
        return this;
    }
}
