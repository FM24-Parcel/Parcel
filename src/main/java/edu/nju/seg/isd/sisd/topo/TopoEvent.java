package edu.nju.seg.isd.sisd.topo;

import edu.nju.seg.isd.sisd.ast.Assignment;
import edu.nju.seg.isd.sisd.ast.Event;
import edu.nju.seg.isd.sisd.ast.Instance;
import edu.nju.seg.isd.sisd.ast.Message;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record TopoEvent(
        Direction direction,
        Event event,
        Instance instance) implements PartialNode {

    @NotNull
    public static TopoEvent sending(@NotNull Message m) {
        return new TopoEvent(
                Direction.Send,
                m.sending(),
                m.source()
        );
    }

    @NotNull
    public static TopoEvent receiving(@NotNull Message m) {
        return new TopoEvent(
                Direction.Receive,
                m.receiving(),
                m.target()
        );
    }

    public boolean isSending() {
        return direction == Direction.Send;
    }

    public boolean isReceiving() {
        return direction == Direction.Receive;
    }

    @Override
    @NotNull
    public String name() {
        return event.name();
    }

    public List<Assignment> getAssignments() {
        return event.assignments();
    }
}
