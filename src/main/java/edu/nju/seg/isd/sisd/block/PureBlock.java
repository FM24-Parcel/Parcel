package edu.nju.seg.isd.sisd.block;

import edu.nju.seg.isd.sisd.ast.expression.Variable;
import edu.nju.seg.isd.sisd.constraint.Constraint;
import edu.nju.seg.isd.sisd.graph.Node;
import edu.nju.seg.isd.sisd.position.Position;
import edu.nju.seg.isd.sisd.topo.*;
import io.vavr.Tuple;
import io.vavr.Tuple2;

import java.util.*;

import static edu.nju.seg.isd.sisd.ast.ZeroOrOne.Zero;

public final class PureBlock implements Block{
    private static int globalIndex = 1;
    private final int index;
    private final Set<PartialNode> nodes;
    private final Set<Order> edges;
    private List<Constraint>localConstraints;
    private List<Constraint> nonTaskConstraints;
    private List<Constraint> taskConstraints;
    private final int priority;
    private final Start start;
    private final End end;
    private final Position position;
    private Map<Variable, List<PartialNode>> maskZeroNodes;
    private Map<Variable, List<PartialNode>> maskOneNodes;
    public PureBlock(FlatTopo topo,int priority,Position position){
        this.nodes=topo.nodes();
        this.edges=topo.edges();
        this.index=globalIndex;
        this.priority=priority;
        this.localConstraints=new ArrayList<>();
        this.nonTaskConstraints=new ArrayList<>();
        this.taskConstraints=new ArrayList<>();
        this.start=new Start(this);
        this.end=new End(this);
        this.position=position;
        globalIndex++;
        this.maskOneNodes=new HashMap<>();
        this.maskZeroNodes=new HashMap<>();
        initial(topo.zeroInDegreeNodes(),topo.zeroOutDegreeNodes());
    }
    public PureBlock(PureBlock that){
        this.nodes=that.nodes;
        this.edges=that.edges;
        this.index=globalIndex;
        this.priority=that.priority;
        this.localConstraints=that.localConstraints;
        this.nonTaskConstraints=new ArrayList<>();
        this.taskConstraints=new ArrayList<>();
        this.start=that.start;
        this.end=that.end;
        this.position=that.position;
        globalIndex++;
        this.maskOneNodes=new HashMap<>();
        this.maskZeroNodes=new HashMap<>();
    }

    private void initial(Set<PartialNode>startNodes,Set<PartialNode>endNodes){
        nodes.stream().map(i->(TopoEvent)i)
                .forEach(i-> i.getAssignments()
                            .forEach(asg->{
                                if (asg.value().equals(Zero)) {
                                    var list=this.maskZeroNodes.getOrDefault(asg.variable(),new ArrayList<>());
                                    list.add(i);
                                    this.maskZeroNodes.put(asg.variable(), list);
                                }
                                else {
                                    var list=this.maskOneNodes.getOrDefault(asg.variable(),new ArrayList<>());
                                    list.add(i);
                                    this.maskOneNodes.put(asg.variable(), list);
                                }
                                    }
                            )
                );
        this.nodes.add(this.start);
        this.nodes.add(this.end);
        startNodes.forEach(i->this.edges.add(new Order(this.start,i)));
        endNodes.forEach(i->this.edges.add(new Order(i,this.end)));
    }
    @Override
    public String name() {
        return Integer.toString(index);
    }

    @Override
    public String toString() {
        return name();
    }

    public Set<PartialNode> nodes() {
        return this.nodes;
    }

    public Set<Order> edges() {
        return this.edges;
    }

    @Override
    public List<Constraint> localConstraints() {
        return this.localConstraints;
    }
    @Override
    public List<Constraint> taskConstraints(){
        return this.taskConstraints;
    }
    @Override
    public List<Constraint> nonTaskConstraints(){
        return this.nonTaskConstraints;
    }

    public void addNonTaskConstraint(Constraint constraint){
        this.nonTaskConstraints.add(constraint);
    }
    public void addTaskConstraint(Constraint constraint){
        this.taskConstraints.add(constraint);
    }
    @Override
    public void addLocalConstraint(Constraint constraint){
        this.localConstraints.add(constraint);
    }
    @Override
    public void addLocalConstraints(List<Constraint> constraints){
        this.localConstraints.addAll(constraints);
    }

    @Override
    public Block copy() {
        return new PureBlock(this);
    }

    @Override
    public int priority() {
        return this.priority;
    }

    public Start start(){
        return start;
    }
    public End end(){
    return end;
    }

    public Position position() {
        return position;
    }

    public int index(){
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
    public Map<Variable, List<PartialNode>> maskZeroNodes() {
        return this.maskZeroNodes;
    }

    @Override
    public Map<Variable, List<PartialNode>> maskOneNodes() {
        return this.maskOneNodes;
    }
}
