package edu.nju.seg.isd.sisd.ast.expression;

import org.jetbrains.annotations.NotNull;

public record Positive(
        double value
) implements Primary, Separation {

    public Positive {
        assert value > 0;
    }

    @NotNull
    public static Positive parse(@NotNull String s) {
        return new Positive(Math.abs(Double.parseDouble(s)));
    }

}
