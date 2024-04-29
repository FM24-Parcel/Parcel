package edu.nju.seg.isd.sisd.topo.next;

import edu.nju.seg.isd.sisd.graph.Edge;
import edu.nju.seg.isd.sisd.topo.PartialNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record Next(
        PartialNode event,
        BlankNode source,
        BlankNode target,
        @Nullable Integer marker,
        @Nullable Integer guard
) implements Edge<BlankNode> {

    public static Next normal(@NotNull PartialNode e,
                              @NotNull BlankNode s,
                              @NotNull BlankNode t) {
        return new Next(e, s, t, null, null);
    }

    @Override
    public String toString() {
        return String.format("%s -> %s", source, target);
    }

}
