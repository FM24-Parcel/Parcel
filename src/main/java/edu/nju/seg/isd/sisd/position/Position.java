package edu.nju.seg.isd.sisd.position;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import org.jetbrains.annotations.NotNull;

@NotNull
public class Position {

    private final Seq<Offset> offsets;

    public Position() {
        this.offsets = List.empty();
    }

    public Position(@NotNull Seq<Offset> offsets) {
        this.offsets = offsets;
    }

    @NotNull
    public Seq<Offset> getPositions() {
        return offsets;
    }

    @NotNull
    public Position addOffset(@NotNull Offset offset) {
        return new Position(offsets.append(offset));
    }

    @NotNull
    public String encode() {
        return offsets.map(Offset::encode)
                .mkString("-");
    }

    @NotNull
    public static Position decode(@NotNull String position) {
        String[] offsets = position.split("-");
        return new Position(List.of(offsets).map(Offset::decode));
    }

}
