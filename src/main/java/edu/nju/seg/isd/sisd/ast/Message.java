package edu.nju.seg.isd.sisd.ast;

import edu.nju.seg.isd.sisd.topo.TopoEvent;

import java.util.stream.Stream;

import static edu.nju.seg.isd.sisd.topo.Direction.Receive;
import static edu.nju.seg.isd.sisd.topo.Direction.Send;

public record Message(
        String info,
        Event sending,
        Event receiving,
        Instance source,
        Instance target
) implements Item {

    @Override
    public boolean notNested() {
        return true;
    }

    public Stream<TopoEvent> topoEvents() {
        var s = new TopoEvent(Send, sending, source);
        var r = new TopoEvent(Receive, receiving, target);
        return Stream.of(s, r);
    }

}
