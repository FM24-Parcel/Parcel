package edu.nju.seg.isd.sisd.block;

import edu.nju.seg.isd.sisd.ast.expression.Variable;
import edu.nju.seg.isd.sisd.constraint.Constraint;

import java.util.ArrayList;
import java.util.List;

public class BlockTrace {
    List<Block>sort;
    List<Side> blockOrders;
    int maxPriority;
    private List<Variable> masks;

    public BlockTrace(List<Block> sort,int maxPriority,List<Variable> masks) {
        this.sort = new ArrayList<>();
        this.sort.addAll(sort);
        this.maxPriority=maxPriority;
        this.masks=masks;
        this.blockOrders=initialBlockOrders();
    }
    public BlockTrace(List<Block> sort,List<Variable> masks) {
        this.sort = sort;
        this.maxPriority= sort.isEmpty()?0:sort.get(0).priority();
        this.masks=masks;
        this.blockOrders=initialBlockOrders();
    }

    private List<Side> initialBlockOrders(){
        List<Side> blockOrders=new ArrayList<>();
        for(int i=0;i<sort.size()-1;i++){
            blockOrders.add(new Side(sort.get(i),sort.get(i+1)));
        }
        return blockOrders;
    }
    public int size(){
        return this.sort.size();
    }

    public List<Block>getSort(){
        return this.sort;
    }

    public void insert(int index,List<Block> block){
        this.sort.addAll(index,block);
    }

    public void add(List<Block> block){
        this.sort.addAll(block);
    }
//
//    public void insert(int index,BlockTrace intTrace){
//        this.blockOrders.remove(index);
//        this.sort.addAll(index,intTrace.getSort());
//    }

    public void replace(int index,Block block){
        this.sort.set(index,block);
    }

    public List<Constraint> getAllConstraints(){
        return this.sort.stream().map(Block::localConstraints).reduce((a, b)->{a.addAll(b);return a;}).get();
    }

    public List<Variable> masks() {
        return masks;
    }

    public void print(){
        for (Block block : sort) {
            if (block instanceof IntBlock) {
                System.out.print("IntBlock_" + block.index());
                System.out.print(" OriginalIndex: "+((IntBlock) block).getOriginalIndex());
                System.out.print(" Contains:");
                ((IntBlock) block).getInternalBlocks().forEach(i->System.out.print("—"+i.index()));
            }
            else if (block instanceof PureBlock)
                System.out.print(block.index());
            System.out.print(", ");
        }
        System.out.println("\n");
    }

}
