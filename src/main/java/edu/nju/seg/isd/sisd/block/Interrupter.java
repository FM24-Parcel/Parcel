package edu.nju.seg.isd.sisd.block;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Interrupter{
    BlockChart pure;
    SortedMap<Integer, List<BlockChart>> ints;

    public Interrupter(BlockChart pure, SortedMap<Integer, List<BlockChart>> ints) {
        this.pure = pure;
        this.ints = ints;
    }

    @NotNull
    public List<BlockTrace> interrupts() {
        var traces = toTrace(pure, ints);
        var pureTrace = traces._1;
        var intTraces = traces._2;
        return interrupt(pureTrace, intTraces);
    }

    public List<BlockTrace> interrupt(List<BlockTrace> pureTraces, SortedMap<Integer, List<BlockTrace>> intTraces) {
        if (intTraces.isEmpty())
            return pureTraces;
        List<BlockTrace> traces = pureTraces;
        for (Integer integer : intTraces.keySet()) {
            for (BlockTrace intTrace : intTraces.get(integer)) {
                var tem1=interruptToBlock(traces,intTrace);
                var tem2=interruptTrace(traces,intTrace);
                traces=Stream.concat(tem1.stream(),tem2.stream()).toList();
            }
        }
        return traces;
    }
    private List<BlockTrace> interruptToBlock(List<BlockTrace> footraces,BlockTrace intTrace){
        List<BlockTrace> traces=new ArrayList<>(footraces);
        traces = traces.stream()
                .flatMap(i -> intoBlock(i, intTrace)
                        .stream())
                .toList();
        return traces;
    }

    private List<BlockTrace> intoBlock(BlockTrace lowTrace, BlockTrace intTrace) {
        List<BlockTrace> traces = new ArrayList<>();
        for (int i = 0; i < lowTrace.size(); i++) {
            var block = lowTrace.getSort().get(i);
            if (block.priority() >= intTrace.maxPriority)
                continue;
            var intBlock = new IntBlock(block);
            intBlock.interrupt(intTrace);
            var tempTrace = new BlockTrace(lowTrace.getSort(), lowTrace.maxPriority,lowTrace.masks());
            tempTrace.replace(i, intBlock);
            traces.add(tempTrace);
        }
        return traces;
    }

    @NotNull
    private List<BlockTrace> interruptTrace(List<BlockTrace> lowTraces, BlockTrace highTrace) {
        List<BlockTrace> traces = new ArrayList<>();
        for (BlockTrace lowTrace:lowTraces){
            for (int i = 0; i < lowTrace.size(); i++){
                int frontPrior = 0, backPrior = lowTrace.getSort().get(i).priority();
                if (i != 0)
                    frontPrior = lowTrace.getSort().get(i - 1).priority();
                if (frontPrior == backPrior&&frontPrior>=highTrace.maxPriority)
                    continue;
                else{
                    var temp = new BlockTrace(lowTrace.getSort(), highTrace.maxPriority,
                            Stream.concat(lowTrace.masks().stream(), highTrace.masks().stream()) . collect(Collectors.toList()));
                    temp.insert(i, highTrace.getSort());
                    traces.add(temp);
                }
            }
            var temp = new BlockTrace(lowTrace.getSort(), highTrace.maxPriority,
                    Stream.concat(lowTrace.masks().stream(), highTrace.masks().stream()) . collect(Collectors.toList()));
            temp.add(highTrace.getSort());
            traces.add(temp);
        }
        return traces;
    }

    @NotNull
    private Tuple2<List<BlockTrace>, SortedMap<Integer, List<BlockTrace>>> toTrace(BlockChart pure,
                                                                                   SortedMap<Integer, List<BlockChart>> ints) {
        var pureTrace = new BlockVisitor(pure).visit();
        var intTraces = new TreeMap<Integer, List<BlockTrace>>();
        ints.keySet()
                .stream()
                .sorted()
                .forEach(key -> ints.get(key)
                        .forEach(i -> {
                            List<BlockTrace>temp=intTraces.getOrDefault(key,new ArrayList<>());
                            temp.addAll(new BlockVisitor(i).visit());
                            intTraces.put(key,temp);
                        })
                );
        return Tuple.of(pureTrace, intTraces);
    }
}
