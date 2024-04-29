package edu.nju.seg.isd.sisd.ast;

import edu.nju.seg.isd.sisd.ast.expression.BoolExpression;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Stream;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public record ISD(
        String title,
        Container container,
        List<Instance> instances,
        List<BoolExpression> constraints,
        List<BoolExpression> properties) {

    private static Tuple2<Container, SortedMap<Integer, List<IntFragment>>> cache = null;

    private static ISD cached = null;

    @NotNull
    public Set<Event> events() {
        return fragments()
                .stream()
                .flatMap(f -> f.normalEventsInsideFragment().stream())
                .collect(Collectors.toSet());
    }

    @NotNull
    public Set<Fragment> fragments() {
        Set<Fragment> r = new HashSet<>();
        traverseFragment(r, container);
        return r;
    }

    private void traverseFragment(@NotNull Set<Fragment> accumulator,
                                  @NotNull Item toTraverse) {
        switch (toTraverse) {
            case Message ignored -> {}
            case AltFragment a -> {
                accumulator.add(a);
                Stream.concat(a.getItems(), a.getElseItems()).forEach(item -> traverseFragment(accumulator, item));
            }
            case Fragment f -> {
                accumulator.add(f);
                f.getItems().forEach(item -> traverseFragment(accumulator, item));
            }
        }
    }

    @NotNull
    public Tuple2<Container, SortedMap<Integer, List<IntFragment>>> partition() {
        if (cached == null || !cached.equals(this)) {
            SortedMap<Integer, List<IntFragment>> ints = new TreeMap<>();
            cache = Tuple.of(partitionInts(container, ints), ints);
            cached = this;
        }
        return cache;
    }

    @NotNull
    private static Container partitionInts(@NotNull Container container,
                                           @NotNull SortedMap<Integer, List<IntFragment>> ints) {
        return new Container(removeIntFragments(container.getItems(), ints));
    }

    @NotNull
    private static AltFragment partitionInts(@NotNull AltFragment alt,
                                             @NotNull SortedMap<Integer, List<IntFragment>> ints) {
        return new AltFragment(
                removeIntFragments(alt.getItems(), ints),
                removeIntFragments(alt.getElseItems(), ints)
        );
    }

    @NotNull
    private static LoopFragment partitionInts(@NotNull LoopFragment loop,
                                              @NotNull SortedMap<Integer, List<IntFragment>> ints) {
        return new LoopFragment(
                removeIntFragments(loop.getItems(), ints),
                loop.getMin(),
                loop.getMax()
        );
    }

    @NotNull
    private static List<Item> removeIntFragments(@NotNull List<Item> items,
                                                 @NotNull SortedMap<Integer, List<IntFragment>> ints) {
        List<Item> result = new ArrayList<>();
        for (Item item : items) {
            switch (item) {
                case IntFragment intFrag -> addIntFragment(ints, intFrag);
                case Message message -> result.add(message);
                case Container container -> result.add(partitionInts(container, ints));
                case AltFragment altFrag -> result.add(partitionInts(altFrag, ints));
                case LoopFragment loopFrag -> result.add(partitionInts(loopFrag, ints));
            }
        }
        return result;
    }

    private static void addIntFragment(@NotNull SortedMap<Integer, List<IntFragment>> ints,
                                       @NotNull IntFragment fragment) {
        ints.computeIfAbsent(fragment.getPriority(), k -> new ArrayList<>()).add(fragment);
    }

}
