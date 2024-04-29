package edu.nju.seg.isd.sisd.block;

import edu.nju.seg.isd.sisd.ast.expression.BinRel;
import edu.nju.seg.isd.sisd.ast.expression.Positive;
import edu.nju.seg.isd.sisd.ast.expression.Variable;
import edu.nju.seg.isd.sisd.constraint.Constraint;
import edu.nju.seg.isd.sisd.graph.Node;
import edu.nju.seg.isd.sisd.topo.*;
import io.vavr.Tuple;
import io.vavr.Tuple2;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class IntBlock implements Block {
    private static int globalIndex = 1;
    private final int index;
    private int originalIndex;
    private final Set<PartialNode> nodes;
    private final Set<Order> edges;
    private List<Constraint> localConstraints;
    private List<Constraint> nonTaskConstraints;
    private List<Constraint> taskConstraints;
    private List<Constraint> intConstraints;
    private Start start;
    private End end;
    private int maxPriority;
    private List<PureBlock> internalBlocks;
    private List<BlockTrace> internalTraces;
    private Map<Variable, List<PartialNode>> maskZeroNodes;
    private Map<Variable, List<PartialNode>> maskOneNodes;

    public IntBlock(Block block) {
        this.index = globalIndex;
        globalIndex++;
        this.nodes = block.nodes();
        this.edges = block.edges();
        this.localConstraints = new ArrayList<>();
        this.localConstraints.addAll(block.localConstraints());
        this.nonTaskConstraints=new ArrayList<>();
        this.nonTaskConstraints.addAll(block.nonTaskConstraints());
        this.taskConstraints=new ArrayList<>();
        this.taskConstraints.addAll(block.taskConstraints());
        this.maxPriority = block.priority();
        this.maskZeroNodes = new HashMap<>();
        block.maskZeroNodes().keySet().forEach(i -> this.maskZeroNodes.put(i, new ArrayList<>()));
        this.maskOneNodes = new HashMap<>();
        block.maskOneNodes().keySet().forEach(i -> this.maskOneNodes.put(i, new ArrayList<>()));
        this.maskOneNodes = block.maskOneNodes();
        this.start=new Start(this);
        this.end=new End(this);
        if (block instanceof PureBlock){
            this.originalIndex=block.index();
            this.intConstraints=new ArrayList<>();
            this.internalTraces=new ArrayList<>();
            this.internalBlocks = new ArrayList<>();
        }
        else {
            this.originalIndex=((IntBlock)block).originalIndex;
            this.intConstraints=new ArrayList<>();
            this.intConstraints.addAll(((IntBlock) block).intConstraints);
            this.internalTraces=new ArrayList<>();
            this.internalTraces.addAll(((IntBlock) block).internalTraces);
            this.internalBlocks = new ArrayList<>();
            this.internalBlocks.addAll(((IntBlock) block).internalBlocks);
        }
    }

    public void interrupt(BlockTrace intTrace) {
        for (Node lowNode : nodes) {
                addLocalConstraint(new Constraint(lowNode.name(), intTrace.getSort().get(0).start().name()
                        , new Positive(0), BinRel.Ge, false, true));
                addLocalConstraint(new Constraint(intTrace.getSort().get(intTrace.size()-1).end().name(), lowNode.name()
                        , new Positive(0), BinRel.Ge, false, true));
        }
        for (Variable mask : intTrace.masks()) {
            checkMask(mask,intTrace).ifPresent(i->this.intConstraints.addAll(i));
        }
        for (BlockTrace one:this.getInternalTraces()){
            if (one.maxPriority>intTrace.maxPriority){
                for (Node lowNode : intTrace.getSort().stream().flatMap(i->i.nodes().stream()).toList()) {
                    addLocalConstraint(new Constraint(lowNode.name(), one.getSort().get(0).start().name()
                            , new Positive(0), BinRel.Ge, false, true));
                    addLocalConstraint(new Constraint(one.getSort().get(intTrace.size()-1).end().name(), lowNode.name()
                            , new Positive(0), BinRel.Ge, false, true));
                }
            }
            else if (one.maxPriority== intTrace.maxPriority){
                for (Node lowNode : one.getSort().stream().flatMap(i->i.nodes().stream()).toList()) {
                    addLocalConstraint(new Constraint(lowNode.name(), intTrace.getSort().get(0).start().name()
                            , new Positive(0), BinRel.Ge, false, true));
                    addLocalConstraint(new Constraint(intTrace.getSort().get(intTrace.size()-1).end().name(), lowNode.name()
                            , new Positive(0), BinRel.Ge, false, true));
                }
                for (Node lowNode : intTrace.getSort().stream().flatMap(i->i.nodes().stream()).toList()) {
                    addLocalConstraint(new Constraint(lowNode.name(), one.getSort().get(0).start().name()
                            , new Positive(0), BinRel.Ge, false, true));
                    addLocalConstraint(new Constraint(one.getSort().get(intTrace.size()-1).end().name(), lowNode.name()
                            , new Positive(0), BinRel.Ge, false, true));
                }
            }
            else {
                for (Node lowNode : one.getSort().stream().flatMap(i->i.nodes().stream()).toList()) {
                    addLocalConstraint(new Constraint(lowNode.name(), intTrace.getSort().get(0).start().name()
                            , new Positive(0), BinRel.Ge, false, true));
                    addLocalConstraint(new Constraint(intTrace.getSort().get(intTrace.size()-1).end().name(), lowNode.name()
                            , new Positive(0), BinRel.Ge, false, true));
                }
            }
        }
        this.internalBlocks.addAll(intTrace.getSort().stream().map(i -> (PureBlock) i).toList());
        this.maxPriority=this.internalBlocks.stream().mapToInt(Block::priority).max().orElseThrow();
        this.localConstraints.addAll(intTrace.getSort().stream().flatMap(i->i.localConstraints().stream()).toList());
        this.taskConstraints.addAll(intTrace.getSort().stream().flatMap(i->i.taskConstraints().stream()).toList());
        this.nonTaskConstraints.addAll(intTrace.getSort().stream().flatMap(i->i.nonTaskConstraints().stream()).toList());
        this.internalTraces.add(intTrace);
        intTrace.getSort().forEach(block->{
            for (Variable variable : block.maskOneNodes().keySet()) {
                if(this.maskOneNodes.containsKey(variable))
                    this.maskOneNodes.get(variable).addAll(block.maskOneNodes().get(variable));
                else
                    this.maskOneNodes.put(variable,block.maskOneNodes().get(variable));
            }
            for (Variable variable : block.maskZeroNodes().keySet()) {
                if(this.maskZeroNodes.containsKey(variable))
                    this.maskZeroNodes.get(variable).addAll(block.maskZeroNodes().get(variable));
                else
                    this.maskZeroNodes.put(variable,block.maskZeroNodes().get(variable));
            }
        });
    }
    public Optional<List<Constraint>> checkMask(Variable mask,BlockTrace intTrace){
        if(this.maskOneNodes.containsKey(mask)&&this.maskZeroNodes.containsKey(mask)){
            //不可发生在屏蔽开关对间
            List<Constraint>res=new ArrayList<>();
            var oneNodes=this.maskOneNodes.get(mask);
            var zeroNodes=this.maskZeroNodes.get(mask);
            for (PartialNode oneNode : oneNodes) {
                for (PartialNode zeroNode : zeroNodes) {
                    intTrace.getSort()
                            .stream()
                            .flatMap(i->i.nodes().stream())
                            .forEach(i->{
                                res.add(new Constraint(i.name(),oneNode.name()
                                        , new Positive(0), BinRel.Ge, false, true));
                                res.add(new Constraint(zeroNode.name(),i.name()
                                        , new Positive(0), BinRel.Ge, false, true));
                            }

                    );
                }
            }
            return Optional.of(res);
        }
        else
            return Optional.empty();
    }
    @Override
    public String name() {
        return Integer.toString(index);
    }

    @Override
    public String toString() {
        return name();
    }

    @Override
    public Set<PartialNode> nodes() {
        var allNodes = new HashSet<>(this.nodes);
        this.internalBlocks.forEach(block -> allNodes.addAll(block.nodes()));
        return allNodes;
    }

    @Override
    public Set<Order> edges() {
        var allEdges = new HashSet<>(this.edges);
        this.internalBlocks.forEach(block -> allEdges.addAll(block.edges()));
        return allEdges;
    }

    public List<Tuple2<Start,End>> getIntHeadsAndTails(){
        return this.internalBlocks.stream().map(i-> Tuple.of(i.start(), i.end())).toList();
    }
    @Override
    public List<Constraint> localConstraints() {
        return this.localConstraints;
    }

    @Override
    public void addLocalConstraint(Constraint constraint) {
        this.localConstraints.add(constraint);
    }

    @Override
    public void addLocalConstraints(List<Constraint> constraints) {
        this.localConstraints.addAll(constraints);
    }

    public void addNode(PartialNode node) {
        this.nodes.add(node);
    }

    public void addNodes(Set<PartialNode> nodes) {
        this.nodes.addAll(nodes);
    }

    public void addEdges(Set<Order> edges) {
        this.edges.addAll(edges);
    }

    public void addEdge(Order edge) {
        this.edges.add(edge);
    }

    @Override
    public Block copy() {
        return null;
    }

    @Override
    public int priority() {
        return this.maxPriority;
    }

    public Start start() {
        return start;
    }

    public End end() {
        return end;
    }

    public int index() {
        return index;
    }

    @Override
    public boolean containEvent(String eventName) {
        return this.nodes.stream()
                .filter(i->i instanceof TopoEvent)
                .map(Node::name)
                .anyMatch(i->i.equals(eventName));
    }

    @Override
    public Map<Variable, List<PartialNode>> maskZeroNodes() {
        return this.maskZeroNodes;
    }

    @Override
    public Map<Variable, List<PartialNode>> maskOneNodes() {
        return this.maskOneNodes;
    }

    @Override
    public Optional<List<Tuple2<PartialNode, PartialNode>>> ZOPairs(Variable variable) {
        List<PartialNode>ONodes=this.maskOneNodes.get(variable);
        List<PartialNode>ZNodes=this.maskZeroNodes.get(variable);
        if(ONodes==null||ZNodes==null)
            return Optional.empty();
        List <Tuple2<PartialNode, PartialNode>> result=new ArrayList<>();
        ONodes.forEach(i->ZNodes.forEach(j->result.add(Tuple.of(i,j))));
        return Optional.of(result);
    }

    @Override
    public List<Constraint> taskConstraints(){
        return this.taskConstraints;
    }
    @Override
    public List<Constraint> nonTaskConstraints(){
        return this.nonTaskConstraints;
    }
    public List<PureBlock> getInternalBlocks() {
        return internalBlocks;
    }

    public int getOriginalIndex() {
        return originalIndex;
    }

    public List<BlockTrace> getInternalTraces() {
        return internalTraces;
    }

    public List<Constraint> getIntConstraints() {
        return intConstraints;
    }

}
