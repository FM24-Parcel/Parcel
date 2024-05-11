package edu.nju.seg.isd.sisd.block;

import edu.nju.seg.isd.sisd.ast.*;
import edu.nju.seg.isd.sisd.ast.expression.Variable;
import edu.nju.seg.isd.sisd.constraint.Constraint;
import edu.nju.seg.isd.sisd.constraint.ConstraintConstructor;
import edu.nju.seg.isd.sisd.exception.Impossible;
import edu.nju.seg.isd.sisd.graph.Node;
import edu.nju.seg.isd.sisd.position.*;
import edu.nju.seg.isd.sisd.topo.BasicFragment;
import edu.nju.seg.isd.sisd.topo.TopoCalculus;
import io.vavr.Tuple;
import io.vavr.Tuple3;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static edu.nju.seg.isd.sisd.position.Alt.Else;
import static edu.nju.seg.isd.sisd.position.Alt.If;

public class ChartConstructor {
    private final List<Instance> instances;
    ISD isd;
    int priority;
    int maxPrior;
    List<Constraint> constraints;
    Map<Constraint, Boolean> constraintMap;
    Boolean subducted;

    public ChartConstructor(ISD isd, Boolean subducted) {
        this.isd = isd;
        this.instances = isd.instances();
        this.priority = 0;
        this.subducted = subducted;
        this.constraintMap = new HashMap<>();
        this.constraints = new ArrayList<>();
        this.constraints.addAll(new ConstraintConstructor().construct(isd.constraints(), false));
        this.constraints.addAll(new ConstraintConstructor().construct(isd.properties(), true));
    }

    public Tuple3<BlockChart, SortedMap<Integer, List<BlockChart>>, List<Constraint>> construct() {
        var parts = isd.partition();
        maxPrior=parts._2.keySet().stream().max(Integer::compareTo).orElse(0);
        var pure = constructBlockChartForContainer(parts._1(),new Position()).get();
        var ints = constructBlockChartForInt(parts._2(),new Position());
        var globalCons = findGlobalConstraints();
        return Tuple.of(pure, ints, globalCons);
    }

    @NotNull
    private List<Constraint> findGlobalConstraints() {
        return this.constraints
                .stream()
                .filter(x -> !this.constraintMap.getOrDefault(x, false))
                .toList();
    }

    public Optional<BlockChart> constructBlockChartForContainer(@NotNull Container container,@NotNull Position position) {
        return constructBlockForPureFragment(container, position.addOffset(new InContainer()));
    }

    public SortedMap<Integer, List<BlockChart>> constructBlockChartForInt(@NotNull SortedMap<Integer, List<IntFragment>> ints,@NotNull Position position) {
        var keys = ints.keySet().stream().sorted().toList();
        var intBlockCharts = new TreeMap<Integer, List<BlockChart>>();
        for (var key : keys) {
            this.priority=key;
            var intBlockChart = ints.get(key)
                    .stream()
                    .map(i->constructBlockForIntFragment(i,position,i.getMasks()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
            if (intBlockChart.isEmpty())
                continue;
            intBlockCharts.put(key, intBlockChart);
        }
        return intBlockCharts;
    }

    public Optional<BlockChart> constructBlockForIntFragment(@NotNull Fragment fragment,@NotNull Position position,@NotNull List<Variable> masks) {
        var intChart=constructBlockForPureFragment(fragment, position);
        if (intChart.isEmpty())
            return Optional.empty();
        else {
            var blockChart = intChart.get();
            blockChart.addMasks(masks);
            return Optional.of(blockChart);
        }
    }


        public Optional<BlockChart> constructBlockForPureFragment(@NotNull Fragment fragment,@NotNull Position position) {
        var result = segment(fragment)
                .stream()
                .map(i->constructBlockForSegment(i,position))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce(BlockChart::concat);
        return result;
    }

    public Optional<BlockChart> constructBlockForSegment(@NotNull Object segment,@NotNull Position position) {
        return switch (segment) {
            case BasicFragment bf -> constructBlockForBasicFragment(bf,position);
            case AltFragment af -> constructBlockForAltFragment(af,position);
            case LoopFragment lf -> constructBlockForLoopFragment(lf,position);
            default -> throw new Impossible();
        };
    }

    @NotNull
    private Optional<BlockChart> constructBlockForBasicFragment(@NotNull BasicFragment basicFragment,@NotNull Position position) {
        var calculus = new TopoCalculus(instances);
        basicFragment.messages().forEach(calculus::recordMessage);
        var topo = calculus.toTopo();
        var block = new PureBlock(topo, priority, position);
        pairConstraintWithBlock((PureBlock) block);
        if (subducted&&block.localConstraints().isEmpty()) {
//            System.out.println("Subducted block: " + block);
            return Optional.empty();
        }
        var chart = new BlockChart();
        chart.addBlock(block);
        return Optional.of(chart);
    }

    @NotNull
    private Optional<BlockChart> constructBlockForAltFragment(@NotNull AltFragment altFragment,@NotNull Position position) {
        var ifBlockChart = constructBlockForPureFragment(new Container(altFragment.getItems()), position.addOffset(new InAlt(If)));
        var elseBlockChart = constructBlockForPureFragment(new Container(altFragment.getElseItems()), position.addOffset(new InAlt(Else)));
        if (ifBlockChart.isEmpty() && elseBlockChart.isEmpty())
            return Optional.empty();
        else if (ifBlockChart.isEmpty())
            return elseBlockChart;
        else if (elseBlockChart.isEmpty())
            return ifBlockChart;
        else
            return Optional.of(ifBlockChart.get().merge(elseBlockChart.get()));
    }

    @NotNull
    private Optional<BlockChart> constructBlockForLoopFragment(@NotNull LoopFragment loopFragment,@NotNull Position position) {
        var optional = constructBlockForPureFragment(loopFragment,position.addOffset(new InLoop()));
        if (optional.isEmpty())
            return Optional.empty();
        var basicChart = optional.get();
        if (loopFragment.getMin() == 1 && loopFragment.getMax() == 1)
            return Optional.of(basicChart);
        var tempChart = basicChart.copy();
        var loopBlockChart= loopFragment.getMin() > 1?basicChart.concat(tempChart):basicChart;
        List<BlockChart> loopCharts = new ArrayList<>();
        for (int loop = 2; loop < loopFragment.getMin(); loop++) {
                var temp = tempChart.copy();
                loopBlockChart.concat(temp);
        }
        boolean loopFlag = true;
        if (loopFlag){
            for (int i = loopFragment.getMin(); i <= loopFragment.getMax(); i++) {
                var temp = tempChart.copy();
                loopBlockChart.concat(temp);
            }
            return Optional.of(loopBlockChart);
        }

        for (int i = loopFragment.getMin(); i <= loopFragment.getMax(); i++) {
            loopCharts.add(loopBlockChart);
            if(i ==loopFragment.getMax())
                break;
            if (i==1)
            loopBlockChart=loopBlockChart.copy().concat(tempChart);
            else loopBlockChart=loopBlockChart.copy().concat(tempChart.copy());

        }
        return Optional.of(loopCharts.stream().reduce(BlockChart::merge).orElseThrow());
    }

    @NotNull
    private List<Object> segment(@NotNull Fragment fragment) {
        var items = fragment.getItems();
        List<Object> result = new ArrayList<>();
        List<Message> current = new ArrayList<>();
        for (var item : items) {
            switch (item) {
                case Message m -> current.add(m);
                case AltFragment af -> {
                    if (!current.isEmpty()) {
                        result.add(new BasicFragment(current));
                        current = new ArrayList<>();
                    }
                    result.add(af);
                }
                case LoopFragment lf -> {
                    if (!current.isEmpty()) {
                        result.add(new BasicFragment(current));
                        current = new ArrayList<>();
                    }
                    result.add(lf);
                }
                case Container ignored -> throw new Impossible();
                case IntFragment ignored -> throw new Impossible();
            }
        }
        if (!current.isEmpty()) {
            result.add(new BasicFragment(current));
        }
        return result;
    }

    private void pairConstraintWithBlock(PureBlock block) {
        var nodeNameList = block.nodes().stream().map(Node::name).toList();
        this.constraints.stream()
                .filter(x -> nodeNameList.contains(x.front()) && nodeNameList.contains(x.back()))
                .forEach(x -> {
                    block.addLocalConstraint(x);
                    this.constraintMap.put(x, true);
                    if (x.isTaskTime()&&block.priority()<this.maxPrior)
                        block.addTaskConstraint(x);
                    else
                        block.addNonTaskConstraint(x);
                });
    }
}
