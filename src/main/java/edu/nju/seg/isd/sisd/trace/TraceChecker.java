package edu.nju.seg.isd.sisd.trace;

import edu.nju.seg.isd.sisd.constraint.Constraint;
import edu.nju.seg.isd.sisd.exception.ExceptionHandler;
import edu.nju.seg.isd.sisd.smt.SolverFactory;
import edu.nju.seg.isd.sisd.trace.visitor.ISDFormulaVisitor;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.sosy_lab.java_smt.api.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TraceChecker {
    List<Constraint> constraints;
    List<Constraint> properties;
    List<String> curTrace;
    Map<String, EventInfo> eventInfoMap;
    Map<String, Integer> priorMap;
    Map<String, Boolean> isMatched;
    SolverFactory smtContext;
    private final ProverEnvironment prover;
    private final IntegerFormulaManager integerManager;

    private final BooleanFormulaManager booleanManager;

    public TraceChecker(List<Constraint> constraints, List<Constraint> properties, List<String> curTrace, Map<String, Integer> priorMap) {
        this.constraints = constraints;
        this.properties = properties;
        this.curTrace = curTrace;
        this.priorMap = priorMap;
        this.isMatched = new HashMap<>();
        this.eventInfoMap = checkCurTrace();
        this.smtContext = new SolverFactory();
        this.prover = smtContext.getUnSatEnvironment();
        this.integerManager = smtContext.getIntManager();
        this.booleanManager = smtContext.getBoolManager();
    }

    public TraceChecker(List<Constraint> constraints, List<Constraint> properties, Map<String, Integer> priorMap) {
        this.constraints = constraints;
        this.properties = properties;
        this.priorMap = priorMap;
        this.isMatched = new HashMap<>();
        this.smtContext = new SolverFactory();
        this.prover = smtContext.getUnSatEnvironment();
        this.integerManager = smtContext.getIntManager();
        this.booleanManager = smtContext.getBoolManager();
    }


    public void check() throws InterruptedException, SolverException {
        var formulas = transformConsAndProp();
        prover.addConstraint(formulas);
        if (prover.isUnsat()) {
            System.out.println("Unsat");
            var formula = prover.getUnsatCore();
            System.out.println(formula);
        } else {
            System.out.println("Sat. this model is not a right example");
        }
    }

    public void checkWithExclude() throws InterruptedException, SolverException {
        var formulas = transformConsAndProp();
        prover.addConstraint(formulas);
        if (prover.isUnsat()) {
//            System.out.println("Unsat");
            var formula = prover.getUnsatCore();
//            System.out.println(formula);
            visitUnSATCore(formula);
        } else {
            System.out.println("Sat. this model is not a right example");
        }
    }

    public List<BooleanFormula> checkAllTrace(List<List<String>> traces) {
        List<BooleanFormula> formulas = new ArrayList<>();
        for (List<String> trace : traces) {
            this.curTrace = trace;
            this.eventInfoMap = checkCurTrace();
            try {
                BooleanFormula formula = transformConsAndProp();
                prover.push();
                prover.addConstraint(formula);
                if (prover.isUnsat()) {
                    var unsatCore = prover.getUnsatCore();
                } else {
                    System.out.println("Sat. this model is not a right example");
                }
            } catch (InterruptedException | SolverException e) {
                ExceptionHandler.handle(e);
            }
        }
        return formulas;
    }

    public List<BooleanFormula> visitUnSATCore(List<BooleanFormula> unsatcore) {
        List<BooleanFormula> unsatlist = new ArrayList<>();
        for (BooleanFormula booleanFormula : unsatcore) {
            ISDFormulaVisitor visitor = new ISDFormulaVisitor(booleanManager);
            var result = this.booleanManager.visit(booleanFormula, visitor);
            unsatlist.addAll(visitor.getAtoms());
        }
        //对每个元素都进行extractEventNames
        unsatlist.stream()
                .map(Object::toString).map(this::extractEventNames).toList();
        return unsatlist;
    }

    public Tuple2<String, String> extractEventNames(String input) {
        Pattern pattern = Pattern.compile("e\\d+@\\d+");
        Matcher matcher = pattern.matcher(input);
        List<String> eventNames = new ArrayList<>();
        while (matcher.find()) {
            String match = matcher.group(); // 匹配到的字符串
            eventNames.add(match);
        }
        if (eventNames.size() != 2) {
            throw new IllegalStateException("Unexpected value: " + eventNames.size());
        } else {
            return Tuple.of(eventNames.get(0), eventNames.get(1));
        }
    }

    public BooleanFormula transformConsAndProp() {
        List<BooleanFormula> partialFormula = generatePartialFormula();
        List<BooleanFormula> constraintFormula = new ArrayList<>();
        List<BooleanFormula> propertyFormula = new ArrayList<>();

        for (Constraint constraint : constraints) {
            if (constraint.isTaskTime())
                constraintFormula.add(handleTaskTime(constraint));
            else
                constraintFormula.add(handleNonTaskTime(constraint));
        }
        for (Constraint property : properties) {
            //properties最后的表达式应该是¬(p1∧p2∧...∧pn)
            if (property.isTaskTime()) {
                //tasktime处理
                propertyFormula.add(handleTaskTime(property));
            } else {
                //非tasktime处理
                propertyFormula.add(handleNonTaskTime(property));
            }
        }
        BooleanFormula partial = booleanManager.and(partialFormula);
        BooleanFormula constraint = booleanManager.and(constraintFormula);
        BooleanFormula property = booleanManager.and(propertyFormula);
        return this.booleanManager.and(partial, constraint, booleanManager.not(booleanManager.and(constraint, property)));
    }

    public BooleanFormula handleNonTaskTime(Constraint constraint) {
        List<Tuple2<String, String>> eventPair = calculateEventPair(constraint.front(), constraint.back());
        List<BooleanFormula> formulas = new ArrayList<>();
        for (Tuple2<String, String> pair : eventPair) {
            //生成对应formula
            NumeralFormula.IntegerFormula a = this.integerManager.makeVariable(pair._1());
            NumeralFormula.IntegerFormula b = this.integerManager.makeVariable(pair._2());
            NumeralFormula.IntegerFormula temp = this.integerManager.add(a, this.integerManager.negate(b));
            NumeralFormula.IntegerFormula num = this.integerManager.makeNumber(constraint.positive().value());
            switch (constraint.binRel()) {
                case Eq -> formulas.add(this.integerManager.equal(temp, num));
                case Ge -> formulas.add(this.integerManager.greaterOrEquals(temp, num));
                case Gt -> formulas.add(this.integerManager.greaterThan(temp, num));
                case Le -> formulas.add(this.integerManager.lessOrEquals(temp, num));
                case Lt -> formulas.add(this.integerManager.lessThan(temp, num));
                default -> {
                }
            }
        }
        return booleanManager.and(formulas);
    }

    public BooleanFormula handleTaskTime(Constraint constraint) {
        //分割后然后调用handleNonTaskTime
        //- 先确定约束内事件的优先级
        //- 统计两个事件序列内高优先级的事件序列的index
        //- 中间是否有不连续的过程
        //- 形成新的约束
        List<BooleanFormula> formulas = new ArrayList<>();
        int curPriority = Math.max(this.priorMap.get(constraint.front()),
                this.priorMap.get(constraint.back()));
        List<Tuple2<Integer, Integer>> indexRanges = calculateIndexRange(constraint.front(), constraint.back());
        HashMap<Tuple2<Integer, Integer>, List<Tuple2<Integer, Integer>>> map = calculateHighPriorIndexRange(indexRanges, curPriority);

        for (Tuple2<Integer, Integer> indexRange : indexRanges) {
            List<Tuple2<Integer, Integer>> contiguousSegments = map.get(indexRange);
            BooleanFormula formula = generateFormula(contiguousSegments, indexRange, constraint);
            formulas.add(formula);
        }
        return booleanManager.and(formulas);
    }

    public BooleanFormula generateFormula(List<Tuple2<Integer, Integer>> contiguousSegments,
                                          Tuple2<Integer, Integer> indexRange, Constraint constraint) {
        NumeralFormula.IntegerFormula a = this.integerManager.makeVariable(
                getEventInfoName(this.curTrace.get(indexRange._1()), indexRange._1()));
        NumeralFormula.IntegerFormula b = this.integerManager.makeVariable(
                getEventInfoName(this.curTrace.get(indexRange._2()), indexRange._2()));
        //若continuousSegments为空，直接返回
        NumeralFormula.IntegerFormula temp = this.integerManager.add(a, this.integerManager.negate(b));
        NumeralFormula.IntegerFormula num = this.integerManager.makeNumber(constraint.positive().value());
        if (contiguousSegments.isEmpty()) {
            for (Tuple2<Integer, Integer> segment : contiguousSegments) {
                int start = segment._1();
                int end = segment._2();
                NumeralFormula.IntegerFormula c = this.integerManager.makeVariable(
                        getEventInfoName(this.curTrace.get(start), start));
                NumeralFormula.IntegerFormula d = this.integerManager.makeVariable(
                        getEventInfoName(this.curTrace.get(end), end));
                NumeralFormula.IntegerFormula highPriorFormula = this.integerManager.add(c, this.integerManager.negate(d));
                temp = this.integerManager.add(temp, this.integerManager.negate(highPriorFormula));
            }
        }

        switch (constraint.binRel()) {
            case Eq:
                return this.integerManager.equal(temp, num);
            case Ge:
                return this.integerManager.greaterOrEquals(temp, num);
            case Gt:
                return this.integerManager.greaterThan(temp, num);
            case Le:
                return this.integerManager.lessOrEquals(temp, num);
            case Lt:
                return this.integerManager.lessThan(temp, num);
            default:
                throw new IllegalStateException("Unexpected value: " + constraint.binRel());
        }
    }

    public List<Tuple2<Integer, Integer>> calculateIndexRange(String name1, String name2) {
        List<Integer> frontIndexList = this.eventInfoMap.get(name1).getIndexList();
        List<Integer> backIndexList = this.eventInfoMap.get(name2).getIndexList();
        List<Tuple2<Integer, Integer>> indexRangeList = new ArrayList<>();
        for (int backIndex : backIndexList) {
            for (int index = backIndex; index >= 0; index--) {
                if (frontIndexList.contains(index)) {
                    // index,backIndex
                    indexRangeList.add(Tuple.of(index, backIndex));
                    break;
                }
            }
        }
        return indexRangeList;
    }

    public HashMap<Tuple2<Integer, Integer>, List<Tuple2<Integer, Integer>>> calculateHighPriorIndexRange(
            List<Tuple2<Integer, Integer>> indexRangeList, int curPriority) {
        List<EventInfo> highPriorEvents = new ArrayList<>();
        HashMap<Tuple2<Integer, Integer>, List<Tuple2<Integer, Integer>>> map = new HashMap<>();
        for (Tuple2<Integer, Integer> indexRange : indexRangeList) {
            List<Integer> highPriorIndex = new ArrayList<>();
            for (int index = indexRange._1(); index < indexRange._2(); index++) {
                String name = this.curTrace.get(index);
                EventInfo eventInfo = this.eventInfoMap.get(name);
                if (eventInfo.priority() > curPriority) {
                    highPriorIndex.add(index);
                    highPriorEvents.add(eventInfo);
                }
            }
            List<Tuple2<Integer, Integer>> contiguousSegments = findContiguousSegments(highPriorIndex);
            map.put(indexRange, contiguousSegments);
        }
        return map;
    }

    public List<Tuple2<Integer, Integer>> findContiguousSegments(List<Integer> list) {
        List<Tuple2<Integer, Integer>> firstAndLast = new ArrayList<>();
        int start = 0;
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i) != list.get(i - 1) + 1) {
                List<Integer> segment = new ArrayList<>();
                segment.add(list.get(start));
                segment.add(list.get(i - 1));
                firstAndLast.add(Tuple.of(list.get(start), list.get(i - 1)));
                start = i;
            }
        }
        return firstAndLast;
    }

    public List<BooleanFormula> generatePartialFormula() {
        List<BooleanFormula> formulas = new ArrayList<>();
        for (int i = 0; i < this.curTrace.size() - 1; i++) {
            //this.curTrace.get(i)是对应的抽象事件名，tostring是对应的事件名@版本号的事件
            NumeralFormula.IntegerFormula a = this.integerManager.
                    makeVariable(this.eventInfoMap.get(this.curTrace.get(i)).toString(i));
            NumeralFormula.IntegerFormula b = this.integerManager.
                    makeVariable(this.eventInfoMap.get(this.curTrace.get(i + 1)).toString(i + 1));
            formulas.add(this.integerManager.lessThan(a, b));
        }
        return formulas;
    }

    public List<Tuple2<String, String>> calculateEventPair(String front, String back) {
        List<Tuple2<String, String>> eventPairs = new ArrayList<>();
        //先要获得前后两事件的indexList,这会对其匹配
        List<Integer> frontIndexList = this.eventInfoMap.get(front).getIndexList().stream()
                .sorted(Collections.reverseOrder())
                .collect(Collectors.toList());
        List<Integer> backIndexList = this.eventInfoMap.get(back).getIndexList();
        //这里的front是发生在较后的事件
        for (int frontIndex : frontIndexList) {
            for (int index = frontIndex; index >= 0; index--) {
                if (backIndexList.contains(index)) {
                    String frontName = this.eventInfoMap.get(back).toString(index);
                    String backName = this.eventInfoMap.get(front).toString(frontIndex);
                    if (isMatched.getOrDefault(frontName, false) && isMatched.getOrDefault(backName, false)) {
                        continue;
                    }
                    eventPairs.add(Tuple.of(
                            this.eventInfoMap.get(back).toString(index),
                            this.eventInfoMap.get(front).toString(frontIndex)
                    ));
                    break;
                }
            }
        }
        return eventPairs;
    }

    public Map<String, EventInfo> checkCurTrace() {
        Map<String, EventInfo> eventInfoMap = new HashMap<>();
        Set<String> allEventInTrace = new HashSet<>(this.curTrace);

        for (String name : allEventInTrace) {
            eventInfoMap.put(name, seekEvent(name));
        }
        return eventInfoMap;
    }

    public EventInfo seekEvent(String eventName) {
        //返回一个list，包含所有符合条件的event的index
        //这个队列也应该解决事件名重复的问题
        Map<Integer, Integer> indexToVersion = new HashMap<>();
        Map<Integer, Integer> versionToIndex = new HashMap<>();
        int count = 0;
        for (int i = 0; i < this.curTrace.size(); i++) {
            if (this.curTrace.get(i).equals(eventName)) {
                indexToVersion.put(i, count);
                versionToIndex.put(count, i);
                count++;
            }
        }
        return new EventInfo(eventName, indexToVersion, versionToIndex, this.priorMap.get(eventName));
    }

    public String getEventInfoName(String name, int index) {
        return this.eventInfoMap.get(name).toString(index);
    }

    public int getIndex(String nameWithVersion) {
        List<String> temp = List.of(nameWithVersion.split("@"));
        String name = temp.get(0);
        int version = Integer.parseInt(temp.get(1));
        return this.eventInfoMap.get(name).versionToIndex().get(version);
    }
}
