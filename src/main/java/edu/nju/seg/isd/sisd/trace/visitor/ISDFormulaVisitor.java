package edu.nju.seg.isd.sisd.trace.visitor;

import com.google.common.collect.Lists;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.BooleanFormulaManager;
import org.sosy_lab.java_smt.api.FunctionDeclaration;
import org.sosy_lab.java_smt.api.visitors.DefaultBooleanFormulaVisitor;
import org.sosy_lab.java_smt.api.visitors.TraversalProcess;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

public class ISDFormulaVisitor extends DefaultBooleanFormulaVisitor<TraversalProcess> {
    private final BooleanFormulaManager bfmgr;
    private final Collection<BooleanFormula> atoms = new LinkedHashSet<>();


    public ISDFormulaVisitor(BooleanFormulaManager bfmgr) {
        this.bfmgr = bfmgr;
    }

    public Collection<BooleanFormula> getAtoms() {
        return atoms;
    }

    @Override
    protected TraversalProcess visitDefault() {
        return TraversalProcess.CONTINUE;
    }

    @Override
    public TraversalProcess visitAtom(
            BooleanFormula atom, FunctionDeclaration<BooleanFormula> funcDecl) {
        atoms.add(atom);
        return TraversalProcess.CONTINUE;
    }

    @Override
    public TraversalProcess visitAnd(List<BooleanFormula> pOperands) {
        pOperands.forEach(f -> bfmgr.visit(f, this));
        return visitDefault();
    }
}
