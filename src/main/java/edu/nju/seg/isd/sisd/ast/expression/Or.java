package edu.nju.seg.isd.sisd.ast.expression;

public record Or(
        BoolExpression left,
        BoolExpression right
) implements BoolExpression {

}