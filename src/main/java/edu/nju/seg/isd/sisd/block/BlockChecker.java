package edu.nju.seg.isd.sisd.block;

import edu.nju.seg.isd.sisd.ast.ISD;
import edu.nju.seg.isd.sisd.constraint.Constraint;
import edu.nju.seg.isd.sisd.parser.ParserFacade;
import edu.nju.seg.isd.sisd.smt.SolverFactory;
import edu.nju.seg.isd.sisd.topo.End;
import edu.nju.seg.isd.sisd.topo.Start;
import edu.nju.seg.isd.sisd.trace.visitor.ISDFormulaVisitor;
import edu.nju.seg.isd.sisd.util.Timer;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.jetbrains.annotations.NotNull;
import org.sosy_lab.java_smt.api.*;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BlockChecker {
    private final ISD isd;
    private final ProverEnvironment prover;
    private final IntegerFormulaManager integerManager;
    private final BooleanFormulaManager booleanManager;
    Boolean subducted;
    Boolean pruned;
    long stime;

    public BlockChecker(File c, Boolean subducted, Boolean pruned) {
        this.isd = ParserFacade.parse(c);
        var smtContext = new SolverFactory();
        this.prover = smtContext.getUnSatEnvironment();
        this.integerManager = smtContext.getIntManager();
        this.booleanManager = smtContext.getBoolManager();
        this.subducted = subducted;
        this.pruned = pruned;
    }

    public void check() throws InterruptedException, SolverException {
        this.stime = System.currentTimeMillis();
        var constructor = new ChartConstructor(isd,subducted);
        var result = constructor.construct();
        var traces = new Interrupter(result._1, result._2).interrupts();
        encodeAndCheck(traces, result._3);
        long etime = System.currentTimeMillis();
        System.out.println("All sequences are checked for unsat");
        System.out.printf("check time：%d ms.\n", (etime - this.stime));
    }


    public void encodeAndCheck(List<BlockTrace> traces, List<Constraint> globalConstraints)
            throws InterruptedException, SolverException{
        for (BlockTrace trace : traces) {
            BooleanFormula formula = booleanManager.makeTrue();
            if (!globalConstraints.isEmpty()){
//                System.out.println("before: "+trace.size());
                var res =pruned?encodeGlobalConstraints(trace, globalConstraints)
                        :encodeGlobalConstraintsWithNotPrune(trace, globalConstraints);
                //System.out.println("global formula");
                formula = booleanManager.and(formula, res._1);
                trace = res._2;
//                System.out.println("after: "+trace.size());
            }
//            trace.print();
//            System.out.println("local formula");
//            formula = booleanManager.and(formula, encodeLocalConstraints(trace));
//            System.out.println(formula);
//            System.out.println("interrupt formula");
//            formula = booleanManager.and(formula, encodeIntBlock(trace));
//            System.out.println(formula);
//            System.out.println("partial formula");
//            formula = booleanManager.and(formula, encodeTracePartial(trace));
//            System.out.println(formula);
//            prover.push(formula);

            formula = booleanManager.and(formula, encodeLocalConstraints(trace));

            formula = booleanManager.and(formula, encodeIntBlock(trace));

            formula = booleanManager.and(formula, encodeTracePartial(trace));
            prover.push(formula);
            //System.out.println(prover.getStatistics());
            if (prover.isUnsat()) {
//                System.out.println("Unsat");
//                var temp=prover.getUnsatCore();
//                checkUnSATCore(temp);
            } else {
                System.out.println("Sat");
                long etime = System.currentTimeMillis();
                trace.print();
                System.out.printf("check time：%d ms.", (etime - this.stime));
                  System.exit(0);
            }
            prover.pop();
        }
    }

    @NotNull
    private void checkUnSATCore(List<BooleanFormula>formulas){
        ISDFormulaVisitor visitor=new ISDFormulaVisitor(booleanManager);
        List<BooleanFormula> unsaList = new ArrayList<>();
        for (BooleanFormula booleanFormula:formulas){
            var result = this.booleanManager.visit(booleanFormula, visitor);
            unsaList.addAll(visitor.getAtoms());
        }
        unsaList.stream()
                .map(Object::toString).map(this::extractStrings).forEach(System.out::println);
    }
    public Tuple2<String, String> extractEventNames(String input) {
        System.out.println(input);
        Pattern pattern = Pattern.compile("e\\d+@\\d+");
        Matcher matcher = pattern.matcher(input);
        List<String> eventNames = new ArrayList<>();
        while (matcher.find()) {
            String match = matcher.group();
            eventNames.add(match);
        }
        if (eventNames.size() != 2) {
            throw new IllegalStateException("Unexpected value: " + eventNames.size());
        } else {
            return Tuple.of(eventNames.get(0), eventNames.get(1));
        }
    }
    public List<String> extractStrings(String input) {
        // Regular expression to find e followed by digits or standalone digits, and relational operators
        String regex = "e\\d+|\\d+|[<>]=?";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        List<String> matches = new ArrayList<>();
        while (matcher.find()) {
            matches.add(matcher.group());
        }

        // Prioritizing the order of matches directly in the list
        // Numbers (e\d+ or \d+) are mixed with relational operators as they appear in the expression
        return matches;
    }
    @NotNull
    private Tuple2<BooleanFormula, BlockTrace> encodeGlobalConstraints(BlockTrace trace, List<Constraint> constraints) {
        var prop = booleanManager.makeTrue();
        var cons = booleanManager.makeTrue();
        int min = trace.size(), max = 0;
        for (Constraint constraint : constraints) {
            var frontIndex = trace.getSort()
                    .stream()
                    .filter(i -> i.containEvent(constraint.front()))
                    .map(i -> trace.getSort().indexOf(i))
                    .toList();
            var backIndex = trace.getSort()
                    .stream()
                    .filter(i -> i.containEvent(constraint.back()))
                    .map(i -> trace.getSort().indexOf(i))
                    .toList();
            if (!frontIndex.isEmpty() && !backIndex.isEmpty()) {
                var pairs = pairFrontAndBack(frontIndex, backIndex);
                min = Math.min(min, Math.min(Collections.min(frontIndex), Collections.min(backIndex)));
                max = Math.max(max, Math.max(Collections.max(frontIndex), Collections.max(backIndex)));
                for (Tuple2<Integer, Integer> pair : pairs) {
                    var res = encodeConstraint(constraint, Tuple.of(trace.getSort().get(pair._1), trace.getSort().get(pair._2)));
                    cons = booleanManager.and(cons, res._1);
                    prop = booleanManager.and(prop, res._2);
                }
            }
        }
        if (min>max)
            return Tuple.of(booleanManager.makeTrue(), new BlockTrace(trace.getSort(), trace.maxPriority, trace.masks()));
        return Tuple.of(booleanManager.and(cons, this.booleanManager.not(prop)),
                new BlockTrace(trace.getSort().subList(min,max+1), trace.maxPriority, trace.masks()));
    }

    @NotNull
    private Tuple2<BooleanFormula, BlockTrace> encodeGlobalConstraintsWithNotPrune(BlockTrace trace, List<Constraint> constraints) {
        var prop = booleanManager.makeTrue();
        var cons = booleanManager.makeTrue();
        int min = trace.size(), max = 0;
        for (Constraint constraint : constraints) {
            var frontIndex = trace.getSort()
                    .stream()
                    .filter(i -> i.containEvent(constraint.front()))
                    .map(i -> trace.getSort().indexOf(i))
                    .toList();
            var backIndex = trace.getSort()
                    .stream()
                    .filter(i -> i.containEvent(constraint.back()))
                    .map(i -> trace.getSort().indexOf(i))
                    .toList();
            if (!frontIndex.isEmpty() && !backIndex.isEmpty()) {
                var pairs = pairFrontAndBack(frontIndex, backIndex);
                min = Math.min(min, Math.min(Collections.min(frontIndex), Collections.min(backIndex)));
                max = Math.max(max, Math.max(Collections.max(frontIndex), Collections.max(backIndex)));
                for (Tuple2<Integer, Integer> pair : pairs) {
                    var res = encodeConstraint(constraint, Tuple.of(trace.getSort().get(pair._1), trace.getSort().get(pair._2)));
                    cons = booleanManager.and(cons, res._1);
                    prop = booleanManager.and(prop, res._2);
                }
            }
        }
        return Tuple.of(booleanManager.and(cons, this.booleanManager.not(prop)),
                new BlockTrace(trace.getSort(), trace.maxPriority, trace.masks()));
    }

    @NotNull
    private List<Tuple2<Integer, Integer>> pairFrontAndBack(List<Integer> frontIndex, List<Integer> backIndex) {
        List<Tuple2<Integer, Integer>> pairs = new ArrayList<>();
        Map<Integer, Boolean> frontIndexUsed = new HashMap<>();
        Map<Integer, Boolean> backIndexUsed = new HashMap<>();
        for (Integer front : frontIndex) {
            for (Integer back : backIndex) {
                if (front >= back && !frontIndexUsed.getOrDefault(front, false)
                        && !backIndexUsed.getOrDefault(back, false)) {
                    pairs.add(Tuple.of(front, back));
                    frontIndexUsed.put(front, true);
                    backIndexUsed.put(back, true);
                    break;
                }
            }
        }
        return pairs;
    }


    private Tuple2<BooleanFormula, BooleanFormula> encodeConstraint(Constraint constraint, Block block) {
        var prop = booleanManager.makeTrue();
        var cons = booleanManager.makeTrue();
        var t1=constraint.front() + "-" + block.hashCode();
        var t2=constraint.back() + "-" + block.hashCode();
        NumeralFormula.IntegerFormula a = this.integerManager.makeVariable(t1);
        NumeralFormula.IntegerFormula b = this.integerManager.makeVariable(t2);
        NumeralFormula.IntegerFormula temp = this.integerManager.add(a, this.integerManager.negate(b));
        NumeralFormula.IntegerFormula num = this.integerManager.makeNumber(constraint.positive().value());
        if (constraint.isTaskTime() && block instanceof IntBlock) {
            for (Tuple2<Start, End> tuple2 : ((IntBlock) block).getIntHeadsAndTails()) {
                NumeralFormula.IntegerFormula c = this.integerManager.makeVariable(tuple2._1.name()+"-" + block.hashCode());
                NumeralFormula.IntegerFormula d = this.integerManager.makeVariable(tuple2._2.name() +"-"+ block.hashCode());
                NumeralFormula.IntegerFormula temp2 = this.integerManager.add(d, this.integerManager.negate(c));
                temp = this.integerManager.add(temp, temp2);
            }
        }
        BooleanFormula formula=this.booleanManager.makeTrue();
        switch (constraint.binRel()) {
            case Eq -> formula = this.integerManager.equal(temp, num);
            case Ge -> formula = this.integerManager.greaterOrEquals(temp, num);
            case Gt -> formula = this.integerManager.greaterThan(temp, num);
            case Le -> formula = this.integerManager.lessOrEquals(temp, num);
            case Lt -> formula = this.integerManager.lessThan(temp, num);
            default -> throw new IllegalStateException("Unexpected value: " + constraint.binRel());
        }
        if (constraint.isProperty())
            prop = booleanManager.and(prop, formula);
        else
            cons = booleanManager.and(cons, formula);
        return Tuple.of(cons, prop);
    }
    private Tuple2<BooleanFormula, BooleanFormula> encodeConstraint(Constraint constraint, Tuple2<Block, Block> blocks) {
        var prop = booleanManager.makeTrue();
        var cons = booleanManager.makeTrue();
        var t1=constraint.front() + "-" + blocks._1.hashCode();
        var t2=constraint.back() + "-" + blocks._2.hashCode();
        NumeralFormula.IntegerFormula a = this.integerManager.makeVariable(t1);
        NumeralFormula.IntegerFormula b = this.integerManager.makeVariable(t2);
        NumeralFormula.IntegerFormula temp = this.integerManager.add(a, this.integerManager.negate(b));
        NumeralFormula.IntegerFormula num = this.integerManager.makeNumber(constraint.positive().value());
        if (constraint.isTaskTime() && blocks._1 instanceof IntBlock) {
            for (Tuple2<Start, End> tuple2 : ((IntBlock) blocks._1).getIntHeadsAndTails()) {
                NumeralFormula.IntegerFormula c = this.integerManager.makeVariable(tuple2._1.name()+"-" + blocks._1.hashCode());
                NumeralFormula.IntegerFormula d = this.integerManager.makeVariable(tuple2._2.name() +"-"+ blocks._1.hashCode());
                NumeralFormula.IntegerFormula temp2 = this.integerManager.add(d, this.integerManager.negate(c));
                temp = this.integerManager.add(temp, temp2);
            }}
        if (constraint.isTaskTime() && blocks._2 instanceof IntBlock) {
            for (Tuple2<Start, End> tuple2 : ((IntBlock) blocks._2).getIntHeadsAndTails()) {
                NumeralFormula.IntegerFormula c = this.integerManager.makeVariable(tuple2._1.name()+"-" + blocks._2.hashCode());
                NumeralFormula.IntegerFormula d = this.integerManager.makeVariable(tuple2._2.name() +"-"+ blocks._2.hashCode());
                NumeralFormula.IntegerFormula temp2 = this.integerManager.add(d, this.integerManager.negate(c));
                temp = this.integerManager.add(temp, temp2);
            }
        }
        BooleanFormula formula=this.booleanManager.makeTrue();
        switch (constraint.binRel()) {
            case Eq -> formula = this.integerManager.equal(temp, num);
            case Ge -> formula = this.integerManager.greaterOrEquals(temp, num);
            case Gt -> formula = this.integerManager.greaterThan(temp, num);
            case Le -> formula = this.integerManager.lessOrEquals(temp, num);
            case Lt -> formula = this.integerManager.lessThan(temp, num);
            default -> throw new IllegalStateException("Unexpected value: " + constraint.binRel());
        }
        if (constraint.isProperty())
            prop = booleanManager.and(prop, formula);
        else
            cons = booleanManager.and(cons, formula);
        return Tuple.of(cons, prop);
    }

    @NotNull
    private BooleanFormula encodeTracePartial(BlockTrace trace) {
        var formula = booleanManager.makeTrue();
        for (int i = 1; i < trace.size(); i++) {
            var back = trace.getSort().get(i);
            var front = trace.getSort().get(i - 1);
            var backStart = back.start().name()+"-" + back.hashCode();
            var frontEnd = front.end().name()+"-" + front.hashCode();
            NumeralFormula.IntegerFormula a = this.integerManager.makeVariable(backStart);
            NumeralFormula.IntegerFormula b = this.integerManager.makeVariable(frontEnd);
            NumeralFormula.IntegerFormula temp = this.integerManager.add(a, this.integerManager.negate(b));
            NumeralFormula.IntegerFormula num = this.integerManager.makeNumber(0);
            formula = booleanManager.and(formula, this.integerManager.greaterOrEquals(temp, num));
        }
        return formula;
    }

    @NotNull
    private BooleanFormula encodeIntBlock(BlockTrace trace){
        List<BooleanFormula> formulas = new ArrayList<>();
        var intBlocks =trace.getSort()
                .stream()
                .filter(i->i instanceof IntBlock)
                .map(i->(IntBlock)i)
                .toList();
        for (IntBlock intBlock:intBlocks){
            //intconstraint
            intBlock.getIntConstraints().stream()
                    .map(i->encodeConstraint(i,intBlock))
                    .forEach(i-> {
                        formulas.add(i._1);
                        formulas.add(i._2);
                    });

            //tracepartial
            intBlock.getInternalTraces().forEach(i->formulas.add(encodeTracePartial(i)));
        }
        return booleanManager.and(formulas);
    }

    @NotNull
    private BooleanFormula encodeLocalConstraints(BlockTrace trace){
        var res=trace.getSort()
                .stream()
                .map(this::encodeBlockTask)
                .reduce(Tuple.of(booleanManager.makeTrue(), booleanManager.makeTrue()),
                        (a, b) -> Tuple.of(booleanManager.and(a._1, b._1), booleanManager.and(a._2, b._2)));
        return booleanManager.and(res._1, this.booleanManager.not(res._2));

    }
    @NotNull
    private Tuple2<BooleanFormula, BooleanFormula> encodeBlockTask(Block block) {
        var res=block.localConstraints()
                .stream()
                .map(i->encodeConstraint(i,block))
                .reduce(Tuple.of(booleanManager.makeTrue(), booleanManager.makeTrue()),
                        (a, b) -> Tuple.of(booleanManager.and(a._1, b._1), booleanManager.and(a._2, b._2)));
        return res;
    }
}
