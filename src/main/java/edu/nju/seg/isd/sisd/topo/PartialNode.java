package edu.nju.seg.isd.sisd.topo;

import edu.nju.seg.isd.sisd.graph.Node;

public sealed interface PartialNode
        extends Node
        permits End, Start, Head, Tail, TopoEvent {

}
