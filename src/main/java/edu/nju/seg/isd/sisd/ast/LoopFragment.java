package edu.nju.seg.isd.sisd.ast;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@NotNull
public final class LoopFragment
        extends BoundFragment {

    private final int min;

    private final int max;

    public LoopFragment(@NotNull List<Item> items,
                        int min,
                        int max) {
        super(items);
        this.min = min;
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    @Override
    public String uniqueName() {
        return name() + "_" + this.hashCode();
    }

    @Override
    public String name() {
        return "loop";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LoopFragment that = (LoopFragment) o;
        return min == that.min && max == that.max;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), min, max);
    }

}
