package edu.nju.seg.isd.sisd.constraint;

import edu.nju.seg.isd.sisd.ast.expression.*;
import edu.nju.seg.isd.sisd.exception.UnexpectedExpression;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;

import java.util.ArrayList;
import java.util.List;

public class ConstraintConstructor {
    List<Constraint>constraints;
    boolean isProperty;

    public ConstraintConstructor() {
        this.constraints = new ArrayList<>();
    }

    public List<Constraint> construct(List<BoolExpression> consExp,boolean isProperty) {
        this.isProperty=isProperty;
        for (BoolExpression exp : consExp) {
            handlerExpression(exp);
        }
        return constraints;
    }

    public void handlerExpression(BoolExpression expression) {
        if (expression instanceof And) {
            handlerExpression(((And) expression).left());
            handlerExpression(((And) expression).right());
        } else if (expression instanceof Or) {
            throw new UnexpectedExpression();
        } else {
            constraints.add(handlerBinaryBoolExpression((BinaryBoolExpression) expression));
        }
    }

    public Constraint handlerBinaryBoolExpression(BinaryBoolExpression expression) {
        var left=handlerDuration(expression.left());
        var right=handlerDuration(expression.right());
        boolean isTaskTime=left._2()||right._2();
        BinRel binRel=expression.relation();

        Tuple3<Boolean,Tuple2<String,String>,Positive> leftTuple3=left._1();
        Tuple3<Boolean,Tuple2<String,String>,Positive> rightTuple3=right._1();
        Positive positive;
        Tuple2<String,String> eventEventTuple2;
        if (leftTuple3._1()) {
            positive = rightTuple3._3();
            eventEventTuple2=leftTuple3._2();
        }
        else {
            positive = leftTuple3._3();
            eventEventTuple2=rightTuple3._2();
            if (binRel==BinRel.Lt) {
                binRel=BinRel.Gt;
            }
            else if (binRel==BinRel.Gt) {
                binRel=BinRel.Lt;
            }
            else if (binRel==BinRel.Le) {
                binRel=BinRel.Ge;
            }
            else if (binRel==BinRel.Ge) {
                binRel=BinRel.Le;
            }
        }
        return new Constraint(eventEventTuple2._1(), eventEventTuple2._2(), positive,binRel, isTaskTime, this.isProperty);
    }

    public Tuple2<Tuple3, Boolean> handlerDuration(Duration duration) {
        if (duration instanceof ArithExpression) {
            return Tuple.of(handlerArithExpression((ArithExpression) duration), false);
        }
        else {
            return Tuple.of(handlerArithExpression(((TaskTime) duration).expression()), true);
        }
    }

    public Tuple3<Boolean,Tuple2<String,String>,Positive> handlerArithExpression(ArithExpression expression) {
        if (expression instanceof Arithmetic) {
            Variable left = (Variable) ((Arithmetic) expression).left();
            Variable right = (Variable) ((Arithmetic) expression).right();
            return Tuple.of(true, Tuple.of(left.symbol(),right.symbol()), null);
        }
        else {
            return Tuple.of(false, null, (Positive) expression);
        }
    }
}