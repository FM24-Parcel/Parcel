package edu.nju.seg.isd.sisd.ast.expression;

import org.jetbrains.annotations.NotNull;

public enum BinRel {

    Eq,
    NotEq,
    Gt,
    Ge,
    Lt,
    Le;

    @Override
    @NotNull
    public String toString() {
        return switch (this) {
            case Eq -> "==";
            case NotEq -> "!=";
            case Gt -> ">";
            case Ge -> ">=";
            case Lt -> "<";
            case Le -> "<=";
        };
    }

    public BinRel reverse() {
        return switch (this) {
            case Eq -> NotEq;
            case NotEq -> Eq;
            case Gt -> Le;
            case Ge -> Lt;
            case Lt -> Ge;
            case Le -> Gt;
        };
    }

}
