package edu.nju.seg.isd.sisd.parser.isd;

import edu.nju.seg.isd.sisd.exception.MissingElement;
import edu.nju.seg.isd.sisd.exception.MissingType;
import edu.nju.seg.isd.sisd.exception.WrongElementNumber;
import edu.nju.seg.isd.sisd.ast.*;
import edu.nju.seg.isd.sisd.ast.expression.BoolExpression;
import edu.nju.seg.isd.sisd.ast.expression.Positive;
import edu.nju.seg.isd.sisd.ast.expression.Variable;
import edu.nju.seg.isd.sisd.parser.xml.Element;
import edu.nju.seg.isd.sisd.parser.xml.ElementType;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import org.jetbrains.annotations.NotNull;
import org.petitparser.context.Result;
import org.petitparser.parser.Parser;
import org.petitparser.tools.GrammarDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static edu.nju.seg.isd.sisd.parser.isd.ExpressionParser.*;
import static org.petitparser.parser.primitive.CharacterParser.anyOf;
import static org.petitparser.parser.primitive.CharacterParser.digit;
import static org.petitparser.parser.primitive.CharacterParser.letter;
import static org.petitparser.parser.primitive.CharacterParser.of;
import static org.petitparser.parser.primitive.StringParser.of;

public class ISDParser {

    @NotNull
    public static ISD parseInterruptSequenceDiagram(@NotNull List<Element> es) {
        Tuple3<Element, Element, Element> p = partition(es);
        Tuple3<String, Container, List<Instance>> diagram = parseDiagram(p._1());
        List<BoolExpression> constraints = parseConstraints(p._2());
        List<BoolExpression> properties = parseProperties(p._3());
        return new ISD(diagram._1(), diagram._2(), diagram._3(), constraints, properties);
    }

    // (diagram, constraints, properties)
    @NotNull
    private static Tuple3<Element, Element, Element> partition(@NotNull List<Element> es) {
        if (es.size() == 3) {
            Element diagram = es.stream()
                    .filter(e -> e.type() == ElementType.UMLSequenceAllInOne)
                    .findAny().orElseThrow(() -> new MissingElement(MissingType.Diagram));
            Element constraints = es.stream()
                    .filter(e -> e.type() == ElementType.UMLNote && e.attributes().contains("Constraints"))
                    .findAny().orElseThrow(() -> new MissingElement(MissingType.Constraints));
            Element properties = es.stream()
                    .filter(e -> e.type() == ElementType.UMLNote && e.attributes().contains("Properties"))
                    .findAny().orElseThrow(() -> new MissingElement(MissingType.Properties));
            return Tuple.of(diagram, constraints, properties);
        } else {
            throw new WrongElementNumber(es.size());
        }
    }

    @NotNull
    private static Tuple3<String, Container, List<Instance>> parseDiagram(@NotNull Element e) {
        String attributes = e.attributes();
        Result r1 = fullTitle().parse(attributes);
        int p1 = r1.getPosition();
        Result r2 = instances().parse(attributes.substring(p1));
        int p2 = r2.getPosition();
        List<Instance> instances = r2.get();
        Map<String, Instance> context = instances.stream()
                .collect(Collectors.toMap(Instance::variable, Function.identity()));
        return Tuple.of(r1.get(), parseContainer(context, attributes.substring(p1 + p2)), instances);
    }

    @NotNull
    private static Container parseContainer(Map<String, Instance> context,
                                            String syntax) {
        ItemDefinition definition = new ItemDefinition(context);
        Parser p = definition.build();
        List<Item> items = p.parse(syntax).get();
        return new Container(items);
    }

    @NotNull
    private static Parser masks() {
        return of('[').trim()
                .seq(variable())
                .seq(of(',').trim().seq(variable())
                        .map((List<Variable> vs) -> vs.get(1))
                        .star())
                .seq(of(']').trim())
                .map((List<Object> os) -> {
                    Variable v = (Variable) os.get(1);
                    List<Variable> vs = (List<Variable>) os.get(2);
                    List<Variable> r = new ArrayList<>();
                    r.add(v);
                    r.addAll(vs);
                    return r;
                });
    }

    @NotNull
    private static Parser priority() {
        return of('(').trim()
                .seq(of('p').trim())
                .seq(of('=').trim())
                .seq(integer())
                .seq(of(')').trim())
                .map((List<Integer> is) -> is.get(3));
    }

    @NotNull
    private static Parser loopBound() {
        return of('(').trim()
                .seq(integer())
                .seq(of(',').trim())
                .seq(integer())
                .seq(of(')').trim())
                .map((List<Integer> is) -> Tuple.of(is.get(1), is.get(3)));
    }

    @NotNull
    private static Parser intBound() {
        return of('(').trim()
                .seq(integer())
                .seq(of(',').trim())
                .seq(integer())
                .seq(of(',').trim().seq(number()).map((List<Positive> fs) -> fs.get(1)).optional())
                .seq(of(')').trim())
                .map((List<Object> is) -> Tuple.of(is.get(1), is.get(3), is.get(4)));
    }

    @NotNull
    private static List<BoolExpression> parseConstraints(@NotNull Element e) {
        Parser p = keyword("Constraints").seq(constraintsOrProperties())
                .map((List<List<BoolExpression>> es) -> es.get(1));
        return p.parse(e.attributes()).get();
    }

    @NotNull
    private static List<BoolExpression> parseProperties(@NotNull Element e) {
        Parser p = keyword("Properties").seq(constraintsOrProperties())
                .map((List<List<BoolExpression>> es) -> es.get(1));
        return p.parse(e.attributes()).get();
    }

    @NotNull
    private static Parser message(Map<String, Instance> context) {
        return semiVariable()
                .seq(of("->>>"))
                .seq(semiVariable())
                .seq(of(':').trim())
                .seq(of('(').trim())
                .seq(event())
                .seq(of(',').trim())
                .seq(event())
                .seq(of(',').trim())
                .seq(info())
                .seq(of(')').trim())
                .seq(of(';').trim().optional())
                .map((List<Object> os) -> {
                    String source = (String) os.get(0);
                    String target = (String) os.get(2);
                    Event se = (Event) os.get(5);
                    Event te = (Event) os.get(7);
                    String info = (String) os.get(9);
                    return new Message(info, se, te, context.get(source), context.get(target));
                });
    }

    @NotNull
    private static Parser info() {
        return letter().or(digit()).or(anyOf("_-")).plus()
                .trim()
                .flatten()
                .map(String::trim);
    }

    @NotNull
    private static Parser event() {
        return semiVariable().seq(assignments().optional())
                .map((List<Object> os) -> {
                    String name = (String) os.get(0);
                    List<Assignment> as = (List<Assignment>) os.get(1);
                    return new Event(name, as == null ? new ArrayList<>(0) : as);
                });
    }

    @NotNull
    private static Parser assignments() {
        return of('{').trim()
                .seq(assignment())
                .seq(of(',').trim().seq(assignment()).map((List<Assignment> as) -> as.get(1)).star())
                .seq(of('}').trim())
                .map((List<Object> os) -> {
                    List<Assignment> r = new ArrayList<>();
                    r.add((Assignment) os.get(1));
                    r.addAll((List<Assignment>) os.get(2));
                    return r;
                });
    }

    @NotNull
    private static Parser assignment() {
        return variable()
                .seq(of(":=").trim())
                .seq(zeroOrOne())
                .map((List<Object> os) -> new Assignment((Variable) os.get(0), (ZeroOrOne) os.get(2)));
    }

    @NotNull
    private static Parser instances() {
        return fullInstance().star();
    }

    @NotNull
    private static Parser fullInstance() {
        return keyword("obj")
                .seq(of('=').trim())
                .seq(instance())
                .map((List<Instance> is) -> is.get(2));
    }

    @NotNull
    private static Parser instance() {
        return instanceName()
                .seq(of('~').trim())
                .seq(instanceVariable())
                .map((List<String> ss) -> new Instance(ss.get(0), ss.get(2)));
    }

    @NotNull
    private static Parser instanceName() {
        return semiVariable();
    }

    @NotNull
    private static Parser instanceVariable() {
        return letter().plus()
                .trim()
                .flatten()
                .map(String::trim);
    }

    @NotNull
    private static Parser keyword(String k) {
        return of(k).trim();
    }

    @NotNull
    private static Parser fullTitle() {
        return keyword("title")
                .seq(of('=').trim())
                .seq(title())
                .map((List<String> ss) -> ss.get(2));
    }

    @NotNull
    private static Parser title() {
        return letterOrDash().plus()
                .trim()
                .flatten()
                .map(String::trim);
    }

    @NotNull
    private static Parser letterOrDash() {
        return letter().or(of('-'));
    }

    public static class ItemDefinition extends GrammarDefinition {

        public ItemDefinition(Map<String, Instance> context) {
            def("start", ref("items"));

            def("items", ref("item").star());

            def("item", ref("fragment").or(message(context)));

            def("fragment", keyword("combinedFragment")
                    .seq(of('=').trim())
                    .seq(ref("loopFragment")
                            .or(ref("altFragment"))
                            .or(ref("intFragment")))
                    .map((List<Fragment> fs) -> fs.get(2)));

            def("loopFragment", keyword("loop")
                    .seq(loopBound())
                    .seq(of('~').trim())
                    .seq(ref("items"))
                    .seq(of("--").trim())
                    .map((List<Object> os) -> {
                        Tuple2<Integer, Integer> bound = (Tuple2<Integer, Integer>) os.get(1);
                        int min = bound._1();
                        int max = bound._2();
                        List<Item> items = (List<Item>) os.get(3);
                        return new LoopFragment(items, min, max);
                    }));

            def("altFragment", keyword("alt")
                    .seq(of('~').trim())
                    .seq(ref("items"))
                    .seq(of("..").trim().seq(ref("items"))
                            .map((List<List<Item>> ll) -> ll.get(1)).optional())
                    .seq(of("--").trim())
                    .map((List<Object> os) -> {
                        List<Item> items = (List<Item>) os.get(2);
                        List<Item> elseItems = (List<Item>) os.get(3);
                        return new AltFragment(items, elseItems == null ? new ArrayList<>(0) : elseItems);
                    }));

            def("intFragment", keyword("int")
                    .seq(priority())
                    .seq(intBound().optional())
                    .seq(masks().optional())
                    .seq(semiVariable().optional()) // task name
                    .seq(of('~').trim())
                    .seq(ref("items"))
                    .seq(of("--").trim())
                    .map((List<Object> os) -> {
                        int p = (int) os.get(1);
                        Tuple3<Integer, Integer, Positive> bound = (Tuple3<Integer, Integer, Positive>) os.get(2);
                        List<Variable> masks = (List<Variable>) os.get(3);
                        List<Item> items = (List<Item>) os.get(6);
                        return new IntFragment(p, items, bound, masks);
                    }));
        }

    }

}
