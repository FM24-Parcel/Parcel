package edu.nju.seg.isd.sisd.topo;

import edu.nju.seg.isd.sisd.ast.Fragment;
import org.jetbrains.annotations.NotNull;

@NotNull
public record Head(
        Fragment fragment
) implements PartialNode {

    public Head(@NotNull Fragment fragment) {
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
        return "$virtual_head_" + fragment.name();
    }

}
