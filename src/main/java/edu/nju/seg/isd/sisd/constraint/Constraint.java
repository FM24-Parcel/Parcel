package edu.nju.seg.isd.sisd.constraint;

import edu.nju.seg.isd.sisd.ast.Event;
import edu.nju.seg.isd.sisd.ast.expression.BinRel;
import edu.nju.seg.isd.sisd.ast.expression.Positive;
import org.sosy_lab.java_smt.api.BooleanFormula;

public record Constraint(
        String front,
        String back,
        Positive positive,
        BinRel binRel,
        boolean isTaskTime,
        boolean isProperty
) {
    public boolean isTaskTime() {
        return isTaskTime;
    }
}
