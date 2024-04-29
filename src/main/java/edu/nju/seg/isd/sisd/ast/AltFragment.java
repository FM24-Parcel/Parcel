package edu.nju.seg.isd.sisd.ast;

import io.vavr.collection.Stream;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@NotNull
public final class AltFragment
        extends Fragment {

    private final List<Item> elseItems;

    public AltFragment(List<Item> items,
                       List<Item> elseItems) {
        super(items);
        this.elseItems = elseItems;
    }

    @NotNull
    public List<Item> getElseItems() {
        return elseItems;
    }

    @Override
    public String uniqueName() {
        return name() + "_" + this.hashCode();
    }

    @Override
    public String name() {
        return "alt";
    }

    @Override
    public Set<Event> normalEventsInsideFragment() {
        return Stream.concat(items, elseItems)
                .filter(item -> item instanceof Message)
                .map(item -> (Message) item)
                .flatMap(m -> Stream.of(m.sending(), m.receiving()))
                .toJavaSet();
    }

    @NotNull
    public Set<Event> normalEventsInsideIfFragment() {
        return items.stream()
                .filter(item -> item instanceof Message)
                .map(item -> (Message) item)
                .flatMap(m -> java.util.stream.Stream.of(m.sending(), m.receiving()))
                .collect(Collectors.toSet());
    }

    @NotNull
    public Set<Event> normalEventsInsideElseFragment() {
        return elseItems.stream()
                .filter(item -> item instanceof Message)
                .map(item -> (Message) item)
                .flatMap(m -> java.util.stream.Stream.of(m.sending(), m.receiving()))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean notNested() {
        return super.notNested() && elseItems.stream().allMatch(Item::notNested);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AltFragment that = (AltFragment) o;
        return Objects.equals(elseItems, that.elseItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), elseItems);
    }

}
