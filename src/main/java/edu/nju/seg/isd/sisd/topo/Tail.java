package edu.nju.seg.isd.sisd.topo;

import edu.nju.seg.isd.sisd.ast.Fragment;
import org.jetbrains.annotations.NotNull;

@NotNull
public record Tail(
        Fragment fragment
) implements PartialNode {

    public Tail(@NotNull Fragment fragment) {
        this.fragment = fragment;
    }

    @Override
    @NotNull
    public Fragment fragment() {
        return fragment;
    }

    @Override
    @NotNull
    public String name() {
        return "$virtual_tail_" + fragment.name();
    }

}
