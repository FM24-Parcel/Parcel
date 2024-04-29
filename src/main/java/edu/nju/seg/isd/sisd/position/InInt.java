package edu.nju.seg.isd.sisd.position;

import org.jetbrains.annotations.NotNull;

@NotNull
public class InInt implements Offset {

    private final int total;

    private final int index;

    public InInt(int total,
                 int index) {
        this.total = total;
        this.index = index;
    }

    @Override
    @NotNull
    public String encode() {
        return String.format("int:%d(%d)", total, index);
    }

}
