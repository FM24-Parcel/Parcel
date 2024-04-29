package edu.nju.seg.isd.sisd.ast.expression;

public record And(
        BoolExpression left,
        BoolExpression right
) implements BoolExpression {

}
