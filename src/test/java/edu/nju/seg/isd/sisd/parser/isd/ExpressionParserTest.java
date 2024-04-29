package edu.nju.seg.isd.sisd.parser.isd;

import edu.nju.seg.isd.sisd.ast.expression.*;
import org.junit.jupiter.api.Test;
import org.petitparser.parser.Parser;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class ExpressionParserTest {

    private final Parser ap = ExpressionParser.arithmetic();

    private final Parser tp = ExpressionParser.taskTime();

    private final Parser aot = ExpressionParser.duration();

    private final Parser cp = ExpressionParser.constraintOrProperty();

    @Test
    public void variable_test_1() {
        Parser p = ExpressionParser.variable();
        Variable v = p.parse("ab12").get();
        assertEquals(v.symbol(), "ab12");
    }

    @Test
    public void number_test_1() {
        Parser p = ExpressionParser.number();
        Positive f = p.parse("9.12").get();
        assertEquals(9.12, f.value(), 0);
    }

    @Test
    public void expression_test_1() {
        Arithmetic a = ap.parse("e1 - e0").get();
        assertEquals(new Arithmetic(
                        new Variable("e1"),
                        BinOp.Minus,
                        new Variable("e0")),
                a);
    }

    @Test
    public void expression_test_2() {
        Arithmetic a = ap.parse("e3 - (e1 - e0)").get();
        assertEquals(new Arithmetic(
                new Variable("e3"),
                BinOp.Minus,
                new Arithmetic(
                        new Variable("e1"),
                        BinOp.Minus,
                        new Variable("e0")
                )
        ), a);
    }

    @Test
    public void taskTime_test_1() {
        TaskTime t = tp.parse("^(e2 - e1)").get();
        assertEquals(new TaskTime(new Arithmetic(
                new Variable("e2"),
                BinOp.Minus,
                new Variable("e1")
        )), t);
    }

    @Test
    public void arithmetic_or_taskTime_test_1() {
        TaskTime t = aot.parse("^(e2 - e1)").get();
        assertEquals(new TaskTime(new Arithmetic(
                new Variable("e2"),
                BinOp.Minus,
                new Variable("e1")
        )), t);
    }

    @Test
    public void arithmetic_or_taskTime_test_2() {
        Arithmetic a = aot.parse("e3 - (e1 - e0)").get();
        assertEquals(new Arithmetic(
                new Variable("e3"),
                BinOp.Minus,
                new Arithmetic(
                        new Variable("e1"),
                        BinOp.Minus,
                        new Variable("e0")
                )
        ), a);
    }

    @Test
    public void constraint_or_property_test_1() {
        And e = cp.parse("4.2 <= e2 - e1 <= 5.9").get();
        Duration d = new Arithmetic(new Variable("e2"), BinOp.Minus, new Variable("e1"));
        assertEquals(new And(
                new BinaryBoolExpression(new Positive(4.2), BinRel.Le, d),
                new BinaryBoolExpression(d, BinRel.Le, new Positive(5.9))
        ), e);
    }

    @Test
    public void constraint_or_property_test_2() {
        And e = cp.parse("4.2 <= ^(e2 - e1) <= 5.9").get();
        Duration d = new TaskTime(new Arithmetic(new Variable("e2"), BinOp.Minus, new Variable("e1")));
        assertEquals(new And(
                new BinaryBoolExpression(new Positive(4.2), BinRel.Le, d),
                new BinaryBoolExpression(d, BinRel.Le, new Positive(5.9))
        ), e);
    }

    @Test
    public void constraint_or_property_test_3() {
        And e = cp.parse("4.2 <= ^(e2 - e1) && ^(e2 - e1) <= 5.9").get();
        Duration d = new TaskTime(new Arithmetic(new Variable("e2"), BinOp.Minus, new Variable("e1")));
        assertEquals(new And(
                new BinaryBoolExpression(new Positive(4.2), BinRel.Le, d),
                new BinaryBoolExpression(d, BinRel.Le, new Positive(5.9))
        ), e);
    }

    @Test
    public void constraint_or_property_test_4() {
        Or e = cp.parse("4.2 <= ^(e2 - e1) || ^(e2 - e1) >= 5.9").get();
        Duration d = new TaskTime(new Arithmetic(new Variable("e2"), BinOp.Minus, new Variable("e1")));
        assertEquals(new Or(
                new BinaryBoolExpression(new Positive(4.2), BinRel.Le, d),
                new BinaryBoolExpression(d, BinRel.Ge, new Positive(5.9))
        ), e);
    }

    @Test
    public void constraint_or_property_test_5() {
        Or e = cp.parse("e2 - e1 != 5 || e2 - e1 > 3 && e2 - e1 <= 4.9").get();
        Duration d = new Arithmetic(new Variable("e2"), BinOp.Minus, new Variable("e1"));
        assertEquals(new Or(
                new BinaryBoolExpression(d, BinRel.NotEq, new Positive(5)),
                new And(
                        new BinaryBoolExpression(d, BinRel.Gt, new Positive(3)),
                        new BinaryBoolExpression(d, BinRel.Le, new Positive(4.9))
                )
        ), e);
    }

}
