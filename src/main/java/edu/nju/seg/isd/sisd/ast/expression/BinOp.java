package edu.nju.seg.isd.sisd.ast.expression;

import org.jetbrains.annotations.NotNull;

public enum BinOp {

    Add,

    Minus,

    Product,

    Divide;

    @Override
    @NotNull
    public String toString() {
        return switch (this) {
            case Add -> "+";
            case Minus -> "-";
            case Product -> "*";
            case Divide -> "/";
        };
    }

}
