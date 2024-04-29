package edu.nju.seg.isd.sisd.ast.expression;

public record BinaryBoolExpression(
        Duration left,
        BinRel relation,
        Duration right
) implements BoolExpression {

}
