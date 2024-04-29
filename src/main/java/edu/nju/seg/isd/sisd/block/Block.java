package edu.nju.seg.isd.sisd.block;

import edu.nju.seg.isd.sisd.ast.expression.Variable;
import edu.nju.seg.isd.sisd.constraint.Constraint;
import edu.nju.seg.isd.sisd.graph.Node;
import edu.nju.seg.isd.sisd.topo.*;
import io.vavr.Tuple2;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public sealed interface Block
        extends Node permits IntBlock, PureBlock {
     Start start();
     End end();
    Set<PartialNode> nodes();
    Set<Order> edges() ;
    List<Constraint> localConstraints();
    List<Constraint> taskConstraints();
    List<Constraint> nonTaskConstraints();
    void addLocalConstraint(Constraint constraint);
    void addLocalConstraints(List<Constraint> constraints);
    Block copy();
    int priority();
    int index();
    boolean containEvent(String eventName);
    Map<Variable, List<PartialNode>> maskZeroNodes();
    Map<Variable, List<PartialNode>> maskOneNodes();
    Optional<List<Tuple2<PartialNode, PartialNode>>> ZOPairs(Variable variable);
}
