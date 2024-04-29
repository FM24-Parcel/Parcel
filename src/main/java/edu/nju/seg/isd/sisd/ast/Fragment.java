package edu.nju.seg.isd.sisd.ast;

import edu.nju.seg.isd.sisd.topo.BasicFragment;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NotNull
public sealed abstract class Fragment
        implements Item
        permits AltFragment, BoundFragment, Container {

    protected int priority = 0;

    protected List<Item> items;

    public Fragment(@NotNull List<Item> items) {
        this.items = items;
    }

    public Fragment(int priority,
                    @NotNull List<Item> items) {
        this.priority = priority;
        this.items = items;
    }

    @NotNull
    public List<Item> getItems() {
        return items;
    }

    public int getPriority() {
        return priority;
    }

    public abstract String uniqueName();

    public abstract String name();

    public Set<Event> normalEventsInsideFragment() {
        return items
                .stream()
                .filter(item -> item instanceof Message)
                .map(item -> (Message) item)
                .flatMap(m -> Stream.of(m.sending(), m.receiving()))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean notNested() {
        return items.stream().allMatch(Item::notNested);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fragment fragment = (Fragment) o;
        return priority == fragment.priority && Objects.equals(items, fragment.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(priority, items);
    }

}
