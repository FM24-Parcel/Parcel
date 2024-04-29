package edu.nju.seg.isd.sisd.ast;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public sealed abstract class BoundFragment
        extends Fragment
        permits LoopFragment, IntFragment {

    public BoundFragment(@NotNull List<Item> items) {
        super(items);
    }

    public BoundFragment(int priority,
                         @NotNull List<Item> items) {
        super(priority, items);
    }

    public abstract int getMin();

    public abstract int getMax();

}
