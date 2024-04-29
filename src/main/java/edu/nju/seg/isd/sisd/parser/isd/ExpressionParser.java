package edu.nju.seg.isd.sisd.parser.isd;

import edu.nju.seg.isd.sisd.ast.ZeroOrOne;
import edu.nju.seg.isd.sisd.ast.expression.*;
import org.jetbrains.annotations.NotNull;
import org.petitparser.parser.Parser;
import org.petitparser.tools.ExpressionBuilder;

import java.util.List;

import static org.petitparser.parser.primitive.CharacterParser.digit;
import static org.petitparser.parser.primitive.CharacterParser.letter;
import static org.petitparser.parser.primitive.CharacterParser.of;
import static org.petitparser.parser.primitive.StringParser.of;

@NotNull
public class ExpressionParser {

    @NotNull
    public static Parser constraintsOrProperties() {
        return constraintOrProperty().star();
    }

    @NotNull
    public static Parser constraintOrProperty() {
        return isdStyle().and().seq(isdStyle()).map((List<Expression> es) -> es.get(0))
                .or(boolExpression());
    }

    @NotNull
    private static Parser isdStyle() {
        return number().seq(relation()).seq(duration()).seq(relation()).seq(number())
                .map((List<Object> es) -> new And(
                        new BinaryBoolExpression((Positive) es.get(0), (BinRel) es.get(1), (Duration) es.get(2)),
                        new BinaryBoolExpression((Duration) es.get(2), (BinRel) es.get(3), (Positive) es.get(4))
                ));
    }

    @NotNull
    private static Parser boolExpression() {
        ExpressionBuilder builder = new ExpressionBuilder();
        builder.group()
                .primitive(binaryBoolExpression())
                .wrapper(of('(').trim(),
                        of(')').trim(),
                        (List<Duration> ps) -> ps.get(1));
        builder.group().left(of("&&").trim(), (List<BoolExpression> bes) -> new And(bes.get(0), bes.get(2)));
        builder.group().left(of("||").trim(), (List<BoolExpression> bes) -> new Or(bes.get(0), bes.get(2)));
        return builder.build();
    }

    @NotNull
    private static Parser binaryBoolExpression() {
        return duration().seq(relation()).seq(duration())
                .map((List<Object> os) -> new BinaryBoolExpression((Duration) os.get(0), (BinRel) os.get(1), (Duration) os.get(2)));
    }

    @NotNull
    public static Parser duration() {
        return taskTime().or(arithmetic());
    }

    @NotNull
    public static Parser taskTime() {
        return of('^').trim()
                .seq(of('(').trim())
                .seq(arithmetic())
                .seq(of(')').trim())
                .map((List<Object> os) -> new TaskTime((Arithmetic) os.get(2)));
    }

    @NotNull
    public static Parser arithmetic() {
        ExpressionBuilder builder = new ExpressionBuilder();
        builder.group()
                .primitive(variable().or(number()))
                .wrapper(of('(').trim(),
                        of(')').trim(),
                        (List<ArithExpression> ps) -> ps.get(1));
        builder.group()
                .left(of('*').trim(), (List<ArithExpression> ps) -> arithmetic(ps, BinOp.Product))
                .left(of('/').trim(), (List<ArithExpression> ps) -> arithmetic(ps, BinOp.Divide));
        builder.group()
                .left(of('+').trim(), (List<ArithExpression> ps) -> arithmetic(ps, BinOp.Add))
                .left(of('-').trim(), (List<ArithExpression> ps) -> arithmetic(ps, BinOp.Minus));
        return builder.build();
    }

    @NotNull
    private static Arithmetic arithmetic(List<ArithExpression> ps,
                                         BinOp op) {
        return new Arithmetic(ps.get(0), op, ps.get(2));
    }

    @NotNull
    public static Parser variable() {
        return semiVariable().map(Variable::new);
    }

    @NotNull
    public static Parser semiVariable() {
        return letter().plus().seq(digit().star())
                .trim()
                .flatten()
                .map(String::trim);
    }

    @NotNull
    public static Parser number() {
        return digit().plus()
                .seq(of('.').seq(digit().plus()).optional())
                .trim()
                .flatten()
                .map(String::trim)
                .map(Positive::parse);
    }

    @NotNull
    public static Parser integer() {
        return digit().plus()
                .trim()
                .flatten()
                .map(String::trim)
                .map((String s) -> Integer.parseInt(s));
    }

    @NotNull
    public static Parser zeroOrOne() {
        return of('0').or(of('1'))
                .trim()
                .flatten()
                .map(String::trim)
                .map(ZeroOrOne::parse);
    }

    // the order matters
    @NotNull
    private static Parser relation() {
        return of("==").trim().map(s -> BinRel.Eq)
                .or(of("!=").trim().map(s -> BinRel.NotEq))
                .or(of(">=").trim().map(s -> BinRel.Ge))
                .or(of('>').trim().map(s -> BinRel.Gt))
                .or(of("<=").trim().map(s -> BinRel.Le))
                .or(of('<').trim().map(s -> BinRel.Lt));
    }

}
