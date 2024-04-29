package edu.nju.seg.isd.sisd.ast;

import edu.nju.seg.isd.sisd.ast.expression.Variable;

public record Assignment(
        Variable variable,
        ZeroOrOne value
) {

}
