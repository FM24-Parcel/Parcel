package edu.nju.seg.isd.sisd.ast;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NotNull
public final class Container extends Fragment {

    public Container(List<Item> items) {
        super(items);
    }

    @Override
    public String uniqueName() {
        return name() + "_" + this.hashCode();
    }

    @Override
    public String name() {
        return "container";
    }

}
