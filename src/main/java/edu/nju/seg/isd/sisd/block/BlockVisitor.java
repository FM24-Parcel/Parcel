package edu.nju.seg.isd.sisd.block;

import edu.nju.seg.isd.sisd.ast.expression.Variable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockVisitor {

    int nodeNum;
    List<Block>nodes;
    List<Side>edges;
    Map<Block, Boolean> booleanMap;
    Map<Block, Integer> indegreeMap;
    List<Block>curSort;
    List<List<Block>>allSorts;
    List<Variable> masks;
    int priority;

    public BlockVisitor(BlockChart chart) {
        this.nodeNum = chart.nodes().size();
        this.nodes = chart.nodes().stream().toList();
        this.edges = chart.edges().stream().toList();
        this.curSort=new ArrayList<>();
        this.allSorts=new ArrayList<>();
        this.booleanMap = new HashMap<>();
        for (Block node : nodes) {
            booleanMap.put(node,true);
        }
        this.indegreeMap = new HashMap<>();
        this.masks = new ArrayList<>();
        this.priority= chart.nodes().stream().map(Block::priority).max(Integer::compareTo).orElse(0);
        nodes.forEach(node -> indegreeMap.put(node, 0));
        edges.forEach(edge -> indegreeMap.put(edge.target(), indegreeMap.get(edge.target()) + 1));
    }
    public List<BlockTrace> visit(){
        return dfsAndPrune(0)
                .stream()
                .map(i->new BlockTrace(i,this.priority,masks))
                .toList();
    }
    public List<List<Block>> dfsAndPrune(int num) {
        if (num == this.nodeNum) {
            allSorts.add(new ArrayList<>(curSort));
        } else {
            for (int i = 0; i < nodes.size(); i++) {
                Block node = nodes.get(i);
                if(indegreeMap.get(node)==0&&booleanMap.get(node)){
                    curSort.add(node);
                    booleanMap.put(node,false);
                    for (Side side : edges) {
                        if (node.equals(side.source())) {
                            indegreeMap.put(side.target(), indegreeMap.get(side.target()) - 1);
                        }
                    }
                    dfsAndPrune(num + 1);
                    booleanMap.put(node,true);
                    curSort.remove(node);
                    for (Side side: edges) {
                        if (node.equals(side.source())) {
                            indegreeMap.put(side.target(), indegreeMap.get(side.target()) + 1);
                        }
                    }
                }
            }
        }
        return allSorts;
    }
}
