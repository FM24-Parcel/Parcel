package edu.nju.seg.isd.sisd.ast.expression;

public record Arithmetic(
        ArithExpression left,
        BinOp op,
        ArithExpression right
) implements ArithExpression {

}
